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
import java.util.*;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

// TODO: this should be finished

/**
 * See:
 * https://cloud.google.com/appengine/docs/standard/java/googlecloudstorageclient/app-engine-cloud-storage-sample
 * https://github.com/GoogleCloudPlatform/appengine-gcs-client/blob/master/java/example/src/main/java/com/google/appengine/demos/GcsExampleServlet.java
 * this is new servlet to try, see also net.cryptonomica.servlets.CSUploadHandlerServlet
 * <p>
 * usage: to upload documents for online verification
 */
public class CloudStorageServletDocuments extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(CloudStorageServletDocuments.class.getName());
    /* --- Gson*/
    private final static Gson GSON = new Gson();

    /**
     * Retrieves a file from GCS and returns it in the http response.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to read the GCS file named Bar in the bucket Foo.
     */
    @Override
    public void doGet(HttpServletRequest req,
                      HttpServletResponse resp
    ) throws IOException {

        String verificationDocumentId = req.getParameter("verificationDocumentId");

        if (verificationDocumentId == null || verificationDocumentId.length() < 33) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"verificationDocumentId is invalid\"}");
        }

        VerificationDocument verificationDocument = ofy()
                .load()
                .key(Key.create(VerificationDocument.class, verificationDocumentId))
                .now();

        String bucketName = verificationDocument.getBucketName();
        String objectName = verificationDocument.getObjectName();
        String extension = "";

        int i = objectName.lastIndexOf('.');
        if (i > 0) {
            extension = objectName.substring(i + 1);
        }
        CloudStorageService.serveFileFromCloudStorage(
                bucketName,
                objectName,
                "image/" + extension,
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
        String verificationDocumentsUploadKey = req.getHeader("verificationDocumentsUploadKey"); // from RandomStringUtils.random(33);
        String nationality = req.getHeader("nationality").toUpperCase(); // TODO: store to database

        // Returns the length, in bytes, of the request body
        // and made available by the input stream, or -1 if the  length is not known
        int reqContentLength = req.getContentLength();
        // use Integer.toString(i) or String.valueOf(i) to convert int to String
        LOG.warning("reqContentLength: " + String.valueOf(reqContentLength));

        // check headers provided:
        if (userId == null || userId.equalsIgnoreCase("")) { // see:
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"userID is empty\"}");
        } else if (userEmail == null || userEmail.equalsIgnoreCase("")) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"user email is empty\"}");
        } else if (verificationDocumentsUploadKey == null
                || verificationDocumentsUploadKey.equalsIgnoreCase("")) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"verification Document Upload Key received is empty\"}");
        } else if (verificationDocumentsUploadKey.length() != 33) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"verification Document Upload Key length is invalid\"}");
        } else if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"key fingerprint is invalid\"}");
        } else if (nationality == null || nationality.isEmpty() || nationality.length() != 2) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"nationality: " + nationality + " is invalid\"}");
        }

        ArrayList<String> isoCountries = new ArrayList<String>(Arrays.asList(Locale.getISOCountries()));
        if (!isoCountries.contains(nationality.toUpperCase())) {
            ServletUtils.sendJsonResponse(resp,
                    "{\"Error\":\"nationality: "
                            + nationality
                            + " is not country code defined in ISO 3166\"}");
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
        }
        if (cryptonomicaUser == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"User is not registered on Cryptonomica server\"}");
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
        }

        VerificationDocumentsUploadKey verificationDocumentsUploadKeyFromDB = null;

        try {
            verificationDocumentsUploadKeyFromDB = ofy()
                    .load()
                    .key(Key.create(VerificationDocumentsUploadKey.class, verificationDocumentsUploadKey))
                    .now();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Error loading DocumentsUploadKey from DB\"}");
        }

        if (verificationDocumentsUploadKeyFromDB == null) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Documents Upload Key provided not exists in DB\"}");
        }

        if (!verificationDocumentsUploadKeyFromDB.getGoogleUser().getUserId().equalsIgnoreCase(userId)) {
            ServletUtils.sendJsonResponse(resp, "\"Error\":\"Documents Upload Key provided is not valid for this user\"");
        }

        List<VerificationDocument> verificationDocumentsList = ofy()
                .load()
                .type(VerificationDocument.class)
                .filter("documentsUploadKey", verificationDocumentsUploadKey)
                .list();
        if (verificationDocumentsList.size() >= 2) {
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"Documents Upload Key provided is already used\"}");
        }

        // if everything good, upload files <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
        Date currentDate = new Date();
        String currentDateStr = df.format(currentDate);
        // String fileName = currentDateStr + ".webm";
        String fileName = currentDateStr; // we'll get file ext from uploaded file

        ArrayList<GcsFilename> gcsFilenames = null; // but it will be only one, see below
        try {
            gcsFilenames =
                    CloudStorageService.uploadFilesToCloudStorage(
                            req,
                            "onlineVerificationDocs", // folder
                            cryptonomicaUser.getEmail().getEmail() + "/" + fingerprint, // sub.folder
                            fileName
                    );
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            // see: http://stackoverflow.com/questions/22271099/convert-exception-to-json
            ServletUtils.sendJsonResponse(resp, GSON.toJson(e));
        }

        if (gcsFilenames == null) {
            LOG.severe("gcsFilenames == null");
            ServletUtils.sendJsonResponse(resp, "{\"Error\":\"gcsFilenames == null\"}");
        } else {
            LOG.warning(GSON.toJson(gcsFilenames));
        }

        GcsFilename gcsFilename = gcsFilenames.get(0);
        // here we'll have only one, because of https://github.com/angular-ui/ui-uploader on frontend, that
        // uploads files separately
        String uploadedDocumentId = RandomStringUtils.randomAlphanumeric(33);
        // save info about document:
        ofy()
                .save()
                .entity(
                        new VerificationDocument(
                                uploadedDocumentId,
                                cryptonomicaUser.getGoogleUser(),
                                gcsFilename.getBucketName(),
                                gcsFilename.getObjectName(),
                                verificationDocumentsUploadKey,
                                fingerprint
                        )
                )
                .now();

        // to test, but see:
        // https://github.com/objectify/objectify/wiki/Concepts#optimistic-concurrency
        OnlineVerification onlineVerification = ofy()
                .load()
                .key(
                        Key.create(OnlineVerification.class, fingerprint)
                )
                .now();
        if (onlineVerification == null) {
            throw new ServletException("OnlineVerification record not found in data base");
        }
        onlineVerification.addVerificationDocument(uploadedDocumentId);
        onlineVerification.setNationality(nationality.toUpperCase()); // TODO: this can be added twice (for every doc)
        // save onlineVerification
        ofy()
                .save()
                .entity(onlineVerification); // async
        // .now();

        String jsonResponseStr = "{\"uploadedDocumentId\":\""
                + uploadedDocumentId
                + "\"}";
        LOG.warning(jsonResponseStr);

        ServletUtils.sendJsonResponse(resp, jsonResponseStr);

    } // end doPost

}
