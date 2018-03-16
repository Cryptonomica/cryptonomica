package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.returns.GetCSuploadURLreturn;
import net.cryptonomica.service.UserTools;

import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * API for uploading files and images
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 *  * in this API:
 */
@Api(
        name = "uploadAPI",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {
                Constants.WEB_CLIENT_ID,
                Constants.API_EXPLORER_CLIENT_ID
        },
        description = "uploads API")
@SuppressWarnings("unused")
public class UploadAPI {

    private static final Logger LOG = Logger.getLogger(UploadAPI.class.getName());

    @ApiMethod(
            name = "getCsUploadURL",
            path = "getCsUploadURL",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public GetCSuploadURLreturn getCsUploadURL(User googleUser) throws Exception {

        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //
        String imageUploadUrl = BlobstoreServiceFactory.getBlobstoreService().createUploadUrl(
                "/cs-user-image-upload",            // upload handler servlet address
                UploadOptions.Builder.withGoogleStorageBucketName(
                        Constants.GAE_PROJECT_DOMAIN // Cloud Storage bucket name (f.e. "cryptonomica-test.appspot.com")
                )
        );
        String imageUploadKey = randomAlphanumeric(9);
        //
        cryptonomicaUser.setImageUploadLink(imageUploadUrl);
        cryptonomicaUser.setImageUploadKey(imageUploadKey);
        //
        Key<CryptonomicaUser> cryptonomicaUserKey = ofy().save().entity(cryptonomicaUser).now();
        LOG.warning("Upload URL: " + imageUploadUrl);
        LOG.warning("Saved for user: " + cryptonomicaUserKey.getName());
        LOG.warning("With imageUploadKey: " + imageUploadKey);

        return new GetCSuploadURLreturn(imageUploadUrl, imageUploadKey, null);
    }

}
