package net.cryptonomica.servlets;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.entities.VerificationVideo;
import net.cryptonomica.entities.VideoUploadKey;
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
        String userId = req.getHeader("userId"); // google user Id: $rootScope.currentUser.userId
        String userEmail = req.getHeader("userEmail"); // $rootScope.currentUser.email
        String fingerprint = req.getHeader("fingerprint"); // $scope.fingerprint = $stateParams.fingerprint;
        String videoUploadKeyReceived = req.getHeader("videoUploadKey"); // from RandomStringUtils.random(33);

        // Returns the length, in bytes, of the request body
        // and made available by the input stream, or -1 if the  length is not known
        int reqContentLength = req.getContentLength();
        // use Integer.toString(i) or String.valueOf(i) to convert int to String
        LOG.warning("reqContentLength: " + String.valueOf(reqContentLength));

        // check if headers provided:
        if (userId == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"userID is null\"}");
            // throw new ServletException();
        } else if (userEmail == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"user email is null\"}");
            // throw new ServletException("user email is null");
        } else if (videoUploadKeyReceived == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"videoUploadKey received is null\"}");
            // throw new ServletException("videoUploadKey received is null");
        } else if (videoUploadKeyReceived.length() != 33) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"videoUploadKey is invalid\"}");
            // throw new ServletException("videoUploadKey is invalid");
        } else if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"key fingerprint is invalid\"}");
        }

        CryptonomicaUser cryptonomicaUser = null;
        try {
            cryptonomicaUser = ofy()
                    .load()
                    .key(Key.create(CryptonomicaUser.class, userId))
                    .now();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"error reading user data from DB\"}");
            // throw new ServletException("Error");
        }
        if (cryptonomicaUser == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"User is not registered on Cryptonomica server\"}");
            // throw new ServletException("User is not registered on Cryptonomica server");
        }

        PGPPublicKeyData pgpPublicKeyData = null;
        try {
            pgpPublicKeyData = ofy()
                    .load()
                    .type(PGPPublicKeyData.class)
                    .filter("fingerprintStr", fingerprint)
                    .first()
                    .now();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"error reading key data from DB\"}");
        }
        if (pgpPublicKeyData == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Key with fingerprint "
                    + fingerprint
                    + " is not registered on Cryptonomica server\"}"
            );
        }

        if (!cryptonomicaUser.getUserId().equalsIgnoreCase(pgpPublicKeyData.getCryptonomicaUserId())) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Key with fingerprint "
                    + fingerprint
                    + " not owned by user "
                    + cryptonomicaUser.getEmail().getEmail()
                    + "\"}"
            );
        }

        // additional check for userID and user email match:
        if (!cryptonomicaUser.getEmail().getEmail().equalsIgnoreCase(userEmail)) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"User ID and user email did not match\"}");
            // throw new ServletException("User ID and user email did not match");
        }

        VideoUploadKey videoUploadKey = null;
        try {
            videoUploadKey = ofy()
                    .load()
                    .key(Key.create(VideoUploadKey.class, videoUploadKeyReceived))
                    .now();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Error loading videoUploadKey from DB\"}");
            // throw new ServletException("Error");
        }
        if (videoUploadKey == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Video Upload Key provided not exists in DB\"}");
            // throw new ServletException("Video Upload Key provided not exists in DB");
        }

        if (!videoUploadKey.getGoogleUser().getUserId().equalsIgnoreCase(userId)) {
            ServletUtils.sendJsonResponse(resp, "\"Error\":\"Video Upload Key provided is not valid for this user\"");
            // throw new ServletException("Video Upload Key provided is not valid for this user");
        }

        if (videoUploadKey.getUploadedVideoId() != null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Video Upload Key provided is already used\"}");
            // throw new ServletException("Video Upload Key provided is already used");
        }

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
                    CloudStorageService.uploadFilesToCloudStorage(
                            req,
                            "onlineVerificationVideos",
                            cryptonomicaUser.getEmail().getEmail() + "/" + fingerprint, // sub.folder
                            fileName
                    ).get(0); // get first from array of uploaded files (for video will be only one)
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            // see: http://stackoverflow.com/questions/22271099/convert-exception-to-json
            ServletUtils.sendJsonResponse(resp, GSON.toJson(e));
        }

        if (gcsFilename == null) {
            LOG.severe("gcsFilename==null");
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"gcsFilename==null\"}");
        }

        String verificationVideoId = RandomStringUtils.randomAlphanumeric(33);
        VerificationVideo verificationVideo = new VerificationVideo(
                verificationVideoId,
                cryptonomicaUser.getGoogleUser(),
                gcsFilename.getBucketName(),
                gcsFilename.getObjectName(),
                videoUploadKeyReceived
        );
        ofy().save().entity(verificationVideo); // <<< async without .now()

        videoUploadKey.setUploadedVideoId(verificationVideoId);
        ofy().save().entity(videoUploadKey); // <<< async without .now()

        String jsonResponseStr = "{\"verificationVideoId\":\"" + verificationVideoId + "\"}";
        LOG.warning(jsonResponseStr);
        ServletUtils.sendJsonResponse(resp, jsonResponseStr);

    } // end doPost

}
