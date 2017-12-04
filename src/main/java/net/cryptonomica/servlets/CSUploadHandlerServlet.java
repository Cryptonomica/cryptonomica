package net.cryptonomica.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.ImageData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;

/*
* see: https://habrahabr.ru/post/275211/
* shuld be mapped to
*/
public class CSUploadHandlerServlet extends HttpServlet {

    private final static Logger LOG = Logger.getLogger(CSUploadHandlerServlet.class.getName());
    private final static String HOST = Constants.GAE_PROJECT_URL;

    @Override
    public void doPost(
            // User googleUser, // <<< -- not legal here
            HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException {

        // ensure registered user // --- NOT WORK with CS Upload Handler Servlet
        // UserService userService = UserServiceFactory.getUserService();
        // User googleUser = userService.getCurrentUser();
        //-----------------

        LOG.warning("[Headers values]: ");
        LOG.warning("origin: " + req.getHeader("origin"));
        LOG.warning("Referer: " + req.getHeader("Referer"));
        LOG.warning("Cookie: " + req.getHeader("Cookie"));
        LOG.warning("X-AppEngine-BlobUpload: " + req.getHeader("X-AppEngine-BlobUpload"));
        LOG.warning("imageUploadKey: " + req.getHeader("imageUploadKey"));
        LOG.warning("[Additional: ]");
        LOG.warning("req.getQueryString(): " + req.getQueryString());
        LOG.warning("req.getRemoteUser()" + req.getRemoteUser());
        // -----------------------------------------------------------------
        // -----------------------------------------------------------------

        String imageUploadKey = req.getHeader("imageUploadKey");
        if (imageUploadKey == null) {
            throw new ServletException("imageUploadKey == null");
        }
        List<CryptonomicaUser> cryptonomicaUserList = ofy()
                .load()
                .type(CryptonomicaUser.class)
                .filter("imageUploadKey", imageUploadKey)
                .list();
        if (cryptonomicaUserList.size() > 1) {
            LOG.warning("Get a list with more than one Cryptonomica user: ");
            for (CryptonomicaUser cryptonomicaUser : cryptonomicaUserList) {
                LOG.warning(cryptonomicaUser.getEmail().getEmail());
            }
            throw new ServletException("Get a list with more than one Cryptonomica user");
        }

        if (cryptonomicaUserList.isEmpty()) {
            LOG.warning("cryptonomicaUserList is empty");
            throw new ServletException("cryptonomicaUserList is empty");
        }

        CryptonomicaUser cryptonomicaUser = cryptonomicaUserList.get(0);

        // Returns the FileInfo for any files that were uploaded, keyed by the upload form "name" field.
        // This method should only be called from within a request served by the destination of a createUploadUrl call.
        // https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/blobstore/BlobstoreService#getFileInfos-HttpServletRequest-
        //
        java.util.Map<java.lang.String, java.util.List<FileInfo>> fileInfoListsMap = BlobstoreServiceFactory
                .getBlobstoreService()
                .getFileInfos(req);

        LOG.warning("[LOGGER]: " + new Gson().toJson(fileInfoListsMap));

        ArrayList<UploadedFileData> uploadedFilesDataList = new ArrayList<>();

        for (java.util.List<FileInfo> fileInfoList : fileInfoListsMap.values()) {

            for (FileInfo fileInfo : fileInfoList) {

                UploadedFileData uploadedFileData = new UploadedFileData();
                uploadedFileData.fileInfo = fileInfo;

                LOG.warning("uploadedFileData created:" + new Gson().toJson(uploadedFileData));
                BlobKey blobKey = BlobstoreServiceFactory.getBlobstoreService()
                        .createGsBlobKey(
                                fileInfo.getGsObjectName()
                        );
                uploadedFileData.BlobKey = blobKey.getKeyString();
                uploadedFileData.fileServeServletLink = HOST + "/serve?blob-key=" + blobKey.getKeyString();

                // Use Images Java API to create serving URL
                // works only for images (PNG, JPEG, GIF, TIFF, BMP, ICO, WEBP)
                for (com.google.appengine.api.images.Image.Format type : com.google.appengine.api.images.Image.Format.values()) {

                    LOG.warning("com.google.appengine.api.images.Image.Format type: " + type.toString());
                    LOG.warning("fileInfo.getContentType(): " + fileInfo.getContentType());

                    if (fileInfo.getContentType().toLowerCase().contains(type.toString().toLowerCase())) {
                        uploadedFileData.servingUrlFromgsObjectName = ImagesServiceFactory.getImagesService()
                                .getServingUrl(ServingUrlOptions.Builder.withGoogleStorageFileName(fileInfo.getGsObjectName()));
                        // should be the same as servingUrlFromGsBlobKey
                        uploadedFileData.servingUrlFromGsBlobKey = ImagesServiceFactory.getImagesService()
                                .getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey));
                        // should be the same as servingUrlFromgsObjectName
                    }
                } // end for ( type : .values())
                uploadedFilesDataList.add(uploadedFileData);
            } // end for (fileInfo : fileInfoList)
        } // end for (fileInfoList : fileInfoListsMap.values())

        Link oldImageLink = cryptonomicaUser.getUserCurrentImageLink();
        if (oldImageLink != null) {
            if (cryptonomicaUser.getOldUserImageLinks() == null) {
                cryptonomicaUser.setOldUserImageLinks(new ArrayList<Link>());
            }
            cryptonomicaUser.getOldUserImageLinks().add(oldImageLink);
        }
        cryptonomicaUser.setUserCurrentImageLink(
                new Link(
                        uploadedFilesDataList.get(0).servingUrlFromgsObjectName.replace("http:", "https:")
                )
        );
        cryptonomicaUser.setImageUploadKeyToNull();
        ofy().save().entity(cryptonomicaUser).now();
        //
        ImageData imageData = new ImageData(
                Key.create(CryptonomicaUser.class, cryptonomicaUser.getUserId()),
                uploadedFilesDataList.get(0).BlobKey,
                uploadedFilesDataList.get(0).fileInfo,
                uploadedFilesDataList.get(0).fileServeServletLink,
                uploadedFilesDataList.get(0).servingUrlFromgsObjectName,
                uploadedFilesDataList.get(0).fileInfo.getGsObjectName()
        );
        ofy().save().entity(imageData).now();
        //
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter pw = res.getWriter(); //get the stream to write the data
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        pw.println(
                gson.toJson(uploadedFilesDataList)
        );
        LOG.warning("uploadedFilesDataMap" + new Gson().toJson(uploadedFilesDataList));
        pw.close(); //closing the stream

    } // doPost End

    /* Object to be returned as JSON in HTTP-response (and can be stored in data base) */
    class UploadedFileData {
        FileInfo fileInfo;
        String BlobKey;
        String fileServeServletLink;
        String servingUrlFromgsObjectName;
        String servingUrlFromGsBlobKey;
    } // end of uploadedFileData

}
