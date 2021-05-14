package net.cryptonomica.servlets;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.entities.*;
import net.cryptonomica.service.CloudStorageService;
import org.apache.commons.lang3.RandomStringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * See:
 * https://cloud.google.com/appengine/docs/standard/java/googlecloudstorageclient/app-engine-cloud-storage-sample
 * https://github.com/GoogleCloudPlatform/appengine-gcs-client/blob/master/java/example/src/main/java/com/google/appengine/demos/GcsExampleServlet.java
 * this is new servlet to try, see also net.cryptonomica.servlets.CSUploadHandlerServlet
 * <p>
 * usage: to upload videos for online verification
 */
//  <url-pattern>/gcs/*</url-pattern>
public class CloudStorageServletVideo extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(CloudStorageServletVideo.class.getName());
    /* --- Gson*/
    private final static Gson GSON = new Gson();

    // /**
    //  * TEST doGet
    //  */
    // @Override
    // public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    //     ServletUtils.sendJsonResponse(
    //             resp,
    //             ServletUtils.getAllRequestData(req)
    //     );
    // }

    /**
     * Retrieves a file from GCS and returns it in the http response.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to read the GCS file named Bar in the bucket Foo.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String verificationVideoId = req.getParameter("verificationVideoId");

        if (verificationVideoId == null || verificationVideoId.length() < 33) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"verificationVideoId is invalid\"}");
        }

        VerificationVideo verificationVideo = ofy()
                .load()
                .key(Key.create(VerificationVideo.class, verificationVideoId))
                .now();

        String bucketName = verificationVideo.getBucketName();
        String objectName = verificationVideo.getObjectName();
        // resp.addHeader("Access-Control-Allow-Origin", "*");
        CloudStorageService.serveFileFromCloudStorage(
                bucketName,
                objectName,
                "video/webm",
                resp
        );

    } // end of doGet


    /**
     * Writes the payload of the incoming post as the contents of a file to GCS.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to create a GCS file named Bar in bucket Foo.
     */
    @Override
    public void doPost(HttpServletRequest req,
                       HttpServletResponse resp) throws IOException, ServletException {

        // this should be POST request with "multipart/form-data"
        // thus it's easier to use headers, then to parse request parameters (as we do in service.CloudStorageServiceOld)

        // ! >  .getHeader returns null if the request does not have a header of that name

        String userId = req.getHeader("userId"); // google user Id: $rootScope.currentUser.userId
        String userEmail = req.getHeader("userEmail"); // $rootScope.currentUser.email
        String fingerprint = req.getHeader("fingerprint"); // $scope.fingerprint = $stateParams.fingerprint;
        String videoUploadKeyReceived = req.getHeader("videoUploadKey"); // from RandomStringUtils.random(33);


        // --- debug:
        // Returns the length, in bytes, of the request body
        // and made available by the input stream, or -1 if the  length is not known
        int reqContentLength = req.getContentLength();
        // use Integer.toString(i) or String.valueOf(i) to convert int to String
        LOG.warning("reqContentLength: " + String.valueOf(reqContentLength));

        String errorMessage = null;
        String successMessage = null;

        CryptonomicaUser cryptonomicaUser = null;
        PGPPublicKeyData pgpPublicKeyData = null;
        OnlineVerification onlineVerification = null;
        VideoUploadKey videoUploadKey = null;

        // TODO >>> rewrite code to not send requests to BD if http request data is invalid;

        final Key<CryptonomicaUser> cryptonomicaUserKey = Key.create(CryptonomicaUser.class, userId);

        cryptonomicaUser = ofy()
                .load()
                .key(cryptonomicaUserKey)
                .now();

        // --- DEBUG:
        if (cryptonomicaUser == null) {
            LOG.warning("cryptonomicaUser is null");
        } else {
            LOG.warning(
                    "cryptonomicaUser.getEmail().getEmail() : "
                            + cryptonomicaUser.getEmail().getEmail()
            );
        }

        final Key<PGPPublicKeyData> pgpPublicKeyDataKey = Key.create(cryptonomicaUserKey, PGPPublicKeyData.class, fingerprint);
        // --- DEBUG:
        LOG.warning("pgpPublicKeyDataKey:");
        LOG.warning(pgpPublicKeyDataKey.toString());

        PGPPublicKeyData pgpPublicKeyDataObject = ofy()
                .load()
                .key(pgpPublicKeyDataKey)
                .now();
        // --- DEBUG:
        if (pgpPublicKeyDataObject == null) {
            LOG.warning("pgpPublicKeyDataObject is null");
        } else {
            LOG.warning("pgpPublicKeyDataObject.getFingerprint() : " + pgpPublicKeyDataObject.getFingerprint());
        }

        pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint)
                .first() // < should be only one
                .now();

        onlineVerification = ofy()
                .load()
                .key(Key.create(OnlineVerification.class, fingerprint)).now();

        videoUploadKey = ofy()
                .load()
                .key(Key.create(VideoUploadKey.class, videoUploadKeyReceived))
                .now();

        if (userId == null || userId.isEmpty()) {

            errorMessage = "{\"Error\":\"userID is null or empty\"}";

        } else if (userEmail == null || userEmail.isEmpty()) {

            errorMessage = "{\"Error\":\"user email is null or empty\"}";

        } else if (videoUploadKeyReceived == null || videoUploadKeyReceived.isEmpty()) {

            errorMessage = "{\"Error\":\"videoUploadKey received is null or empty\"}";

        } else if (videoUploadKeyReceived.length() != 33) {

            errorMessage = "{\"Error\":\"videoUploadKey is invalid\"}";

        } else if (fingerprint == null || fingerprint.isEmpty() || fingerprint.length() != 40) {

            errorMessage = "{\"Error\":\"key fingerprint is invalid\"}";

        } else if (cryptonomicaUser == null) {

            errorMessage = "{\"Error\":\"User is not registered on Cryptonomica server\"}";

        } else if (pgpPublicKeyData == null) {

            errorMessage = "{\"Error\":\"Key with fingerprint " + fingerprint + " is not registered on Cryptonomica server\"}";

        } else if (onlineVerification == null) {

            errorMessage = "{\"Error\":\"OnlineVerification for the key with fingerprint " + fingerprint + " is not registered on Cryptonomica server\"}";

        } else if (!cryptonomicaUser.getUserId().equalsIgnoreCase(pgpPublicKeyData.getCryptonomicaUserId())) {

            errorMessage = "{\"Error\":\"Key with fingerprint " + fingerprint + " not owned by user " + cryptonomicaUser.getEmail().getEmail() + "\"}";

        } else if (!cryptonomicaUser.getEmail().getEmail().equalsIgnoreCase(userEmail)) {

            errorMessage = "{\"Error\":\"User ID and user email did not match\"}";

        } else if (videoUploadKey == null) {

            errorMessage = "{\"Error\":\"Video Upload Key provided not exists in DB\"}";

        } else if (!videoUploadKey.getGoogleUser().getUserId().equalsIgnoreCase(userId)) {

            errorMessage = "\"Error\":\"Video Upload Key provided is not valid for this user\"";

        } else if (videoUploadKey.getUploadedVideoId() != null) {

            errorMessage = "{\"Error\":\"Video Upload Key provided is already used\"}";

        }

        if (errorMessage != null) {

            LOG.warning(errorMessage);
            ServletUtils.sendJsonResponse(resp, errorMessage);

        } else {

            // if everything good, upload file <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            // DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
            Date currentDate = new Date();
            String currentDateStr = df.format(currentDate);
            // String fileName = currentDateStr + ".webm";
            String fileName = currentDateStr; // we'll get file ext from uploaded file

            GcsFilename gcsFilename = null;

            try {
                gcsFilename =
                        CloudStorageService.uploadFilesToCloudStorage( // <<<< actual file upload <<<<
                                req,
                                "onlineVerificationVideos",
                                cryptonomicaUser.getEmail().getEmail() + "/" + fingerprint, // sub.folder
                                fileName
                        ).get(0); // get first from array of uploaded files (for video will be only one)
            } catch (Exception e) {
                LOG.severe(e.getMessage());
                // see: http://stackoverflow.com/questions/22271099/convert-exception-to-json
                errorMessage = GSON.toJson(e);
            }

            if (gcsFilename == null) {

                LOG.severe("gcsFilename == null");
                if (errorMessage == null) {
                    errorMessage = "{\"Error\":\"gcsFilename is null\"}";
                }
                ServletUtils.sendJsonResponse(resp, errorMessage);

            } else { // file upload successful

                String verificationVideoId = RandomStringUtils.randomAlphanumeric(33);

                VerificationVideo verificationVideo = new VerificationVideo(
                        verificationVideoId,
                        cryptonomicaUser.getGoogleUser(),
                        gcsFilename.getBucketName(),
                        gcsFilename.getObjectName(),
                        videoUploadKeyReceived
                );

                // save video information to DB
                ofy().save().entity(verificationVideo).now(); //

                // save 'onlineVerification' entity to DB:
                onlineVerification.setVerificationVideoId(verificationVideoId);
                ofy().save().entity(onlineVerification).now();

                // save 'videoUploadKey' entity to DB:
                videoUploadKey.setUploadedVideoId(verificationVideoId);
                ofy().save().entity(videoUploadKey); // <<< async without .now()

                //
                successMessage = "{\"verificationVideoId\":\"" + verificationVideoId + "\"}";
                LOG.warning(successMessage);

                // see:
                // https://stackoverflow.com/questions/20881532/no-access-control-allow-origin-header-is-present-on-the-requested-resource
                //
                // resp.addHeader("Access-Control-Allow-Origin", "*");
                // resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
                // >> see 'service' method below
                ServletUtils.sendJsonResponse(resp, successMessage); // << success result
            }

        }

    } // end doPost

    @Override
    // see: https://stackoverflow.com/a/35756384 in
    // https://stackoverflow.com/questions/20881532/no-access-control-allow-origin-header-is-present-on-the-requested-resource
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        // setHeader : Adds a response header with the given name and value.
        // This method allows response headers to have multiple values.

        // addHeader : Sets a response header with the given name and
        // integer value.  If the header had already been set, the new value
        // overwrites the previous one.  The <code>containsHeader</code>
        // method can be used to test for the presence of a header before
        // setting its value.

        LOG.warning("req.getHeader(\"Origin\"): ");
        LOG.warning(req.getHeader("Origin"));

        // res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        // res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        // <-- or in ServletUtils.sendJsonResponse

        // see: https://stackoverflow.com/a/42061962
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        res.setHeader("Access-Control-Max-Age", "3600");
        // res.setHeader("Access-Control-Allow-Headers", "x-requested-with");
        // res.setHeader("Access-Control-Allow-Headers", "Authorization");
        //
        res.setHeader("Access-Control-Allow-Headers", "*");
        //
        // res.setHeader("Access-Control-Allow-Credentials", "true");
        // res.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        super.service(req, res);
    }

}
