package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.ApiKey;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.forms.GetUsersKeysByUserIdForm;
import net.cryptonomica.forms.PGPPublicKeyUploadForm;
import net.cryptonomica.forms.SearchPGPPublicKeysForm;
import net.cryptonomica.pgp.PGPTools;
import net.cryptonomica.returns.PGPPublicKeyGeneralView;
import net.cryptonomica.returns.PGPPublicKeyUploadReturn;
import net.cryptonomica.returns.SearchPGPPublicKeysReturn;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.service.ApiKeysService;
import net.cryptonomica.service.UserTools;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 * * in this API:
 */

@Api(name = "pgpPublicKeyAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "API to work with public PGP keys")
@SuppressWarnings("unused")
public class PGPPublicKeyAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(PGPPublicKeyAPI.class.getName());

    /* --- Gson: */
    private static final Gson GSON = new Gson();

    @ApiMethod(
            name = "getPGPPublicKeyByWebSafeString",
            path = "getPGPPublicKeyByWebSafeString",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    // get key by by WebSafeString
    public PGPPublicKeyGeneralView getPGPPublicKeyByWebSafeString(
            final User googleUser,
            final @Named("websafestring") String webSafeString
    ) throws Exception {
        /* Check authorization */
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* Validate form: */
        LOG.warning("@Named(\"websafestring\") : " + webSafeString);

        if (webSafeString == null || webSafeString.length() < 1) {
            throw new Exception("Invalid public key in request");
        }

        /* Load PGPPublicKeyData from DB*/
        Key<PGPPublicKeyData> pgpPublicKeyDataKey = Key.create(webSafeString);
        PGPPublicKeyData pgpPublicKeyData = null;
        try {
            pgpPublicKeyData = ofy()
                    .load()
                    .key(pgpPublicKeyDataKey)
                    .now();
        } catch (Exception e) {
            LOG.warning("Load PGPPublicKeyData from DB failed: " + e.getMessage());
        }
        if (pgpPublicKeyData == null) {
            LOG.warning("pgpPublicKeyData == null");
            throw new Exception("Public PGP key not found");
        }
        return new PGPPublicKeyGeneralView(pgpPublicKeyData);
    }

    @ApiMethod(
            name = "getPGPPublicKeyByFingerprint",
            path = "getPGPPublicKeyByFingerprint",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    // get key by by fingerprint
    public PGPPublicKeyGeneralView getPGPPublicKeyByFingerprint(
            final User googleUser,
            final @Named("fingerprint") String fingerprint
    ) throws Exception {

        /* Log request: */
        LOG.warning("Request: @Named(\"fingerprint\") : " + fingerprint);

        /* Check authorization: */
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // GET Key from DataBase by fingerprint:
        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(fingerprint);

        // make key representation, return result
        PGPPublicKeyGeneralView pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(pgpPublicKeyData);

        LOG.warning("Result: " + GSON.toJson(pgpPublicKeyGeneralView));

        return pgpPublicKeyGeneralView;

    } // end of getPGPPublicKeyByFingerprint()

    @ApiMethod(
            name = "getPGPPublicKeyByFingerprintWithApiKey",
            path = "getPGPPublicKeyByFingerprintWithApiKey",
            httpMethod = ApiMethod.HttpMethod.GET,
            description = "This API method can be called with API key provided to partner services"
    )
    @SuppressWarnings("unused")
    // get key by by fingerprint
    public PGPPublicKeyGeneralView getPGPPublicKeyByFingerprintWithApiKey(
            // final User googleUser,
            final HttpServletRequest httpServletRequest,
            // final @Named("serviceName") String serviceName,
            final @Named("fingerprint") String fingerprint,
            @Named("serviceName") String serviceName,
            @Named("apiKeyString") String apiKeyString
    ) throws Exception { // UnauthorizedException or Exception

        /* --- check argument */
        if (fingerprint == null || fingerprint.isEmpty()) {
            throw new IllegalArgumentException("fingerprint not provided");
        }

        ApiKey apiKey = ApiKeysService.checkApiKey(httpServletRequest, serviceName, apiKeyString);
        if (!apiKey.getGetPGPPublicKeyByFingerprintWithApiKey()) {
            throw new UnauthorizedException("API key is not valid for this API method");
        }

        /* Log request: */
        LOG.warning("service name: " + serviceName);
        LOG.warning("Request: @Named(\"fingerprint\") : " + fingerprint);

        /* ---- the same as in API without API key: */

        // GET Key from DataBase by fingerprint:
        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(fingerprint);
        // make key representation, return result
        PGPPublicKeyGeneralView pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(pgpPublicKeyData);

        LOG.warning("Result: " + GSON.toJson(pgpPublicKeyGeneralView));

        return pgpPublicKeyGeneralView;

    } // end of getPGPPublicKeyByFingerprintWithApiKey

    @ApiMethod(
            name = "uploadNewPGPPublicKey",
            path = "uploadNewPGPPublicKey",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public PGPPublicKeyUploadReturn uploadNewPGPPublicKey(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final PGPPublicKeyUploadForm pgpPublicKeyUploadForm
    ) throws Exception {
        // authorization
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        //
        if (pgpPublicKeyUploadForm == null
                || pgpPublicKeyUploadForm.getAsciiArmored() == null
                || pgpPublicKeyUploadForm.getAsciiArmored().length() == 0) {
            throw new Exception("ASCII-armored key is empty");
        }
        // --- LOG request form: (after check if not null)
        LOG.warning(GSON.toJson(pgpPublicKeyUploadForm));

        String asciiArmored = pgpPublicKeyUploadForm.getAsciiArmored();

        PGPPublicKey pgpPublicKey = PGPTools.readPublicKeyFromString(asciiArmored);
        // -> throws IOException, PGPException
        LOG.warning(GSON.toJson(pgpPublicKey));

        PGPPublicKeyData pgpPublicKeyData = PGPTools.checkPublicKey(pgpPublicKey, asciiArmored, cryptonomicaUser);
        pgpPublicKeyData.setUserBirthday(cryptonomicaUser.getBirthday());

        // -- add @Parent value: ---
        pgpPublicKeyData.setCryptonomicaUserKey(Key.create(CryptonomicaUser.class, googleUser.getUserId()));
        // save key
        Key<PGPPublicKeyData> pgpPublicKeyDataKey = ofy().save().entity(pgpPublicKeyData).now();
        // load key from DB and and create return object
        String messageToUser = "Key " + pgpPublicKeyData.getFingerprint() + " saved in data base";
        PGPPublicKeyGeneralView pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(
                ofy().load().key(pgpPublicKeyDataKey).now()
        );
        PGPPublicKeyUploadReturn pgpPublicKeyUploadReturn = new PGPPublicKeyUploadReturn(
                messageToUser,
                pgpPublicKeyGeneralView
        );

        return pgpPublicKeyUploadReturn;
    }

    @ApiMethod(
            name = "searchPGPPublicKeys",
            path = "searchPGPPublicKeys",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // search:
    // 1) by fingerprint
    // 2) by keyID
    // 3) by email
    // 4) by first and last names
    public SearchPGPPublicKeysReturn searchPGPPublicKeys(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final SearchPGPPublicKeysForm searchPGPPublicKeysForm
    ) throws Exception {
        // authorization
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //
        String fingerprint = searchPGPPublicKeysForm.getFingerprint();
        String keyID = searchPGPPublicKeysForm.getKeyID();
        String userID = searchPGPPublicKeysForm.getUserID();
        String firstName = searchPGPPublicKeysForm.getFirstName();
        String lastName = searchPGPPublicKeysForm.getLastName();
        String cryptonomicaUserId = searchPGPPublicKeysForm.getCryptonomicaUserId();
        Email userEmail = null;
        if (searchPGPPublicKeysForm.getUserEmail() != null) {
            userEmail = new Email(searchPGPPublicKeysForm.getUserEmail());
        }

        List<? extends PGPPublicKeyData> result;

        if (fingerprint != null) {
            result = ofy().load()
                    .type(PGPPublicKeyData.class)
                    .filter("fingerprint", fingerprint)
                    .list();
        } else if (keyID != null) {
            result = ofy().load()
                    .type(PGPPublicKeyData.class)
                    .filter("keyID", keyID)
                    .list();
        } else if (userEmail != null) {
            result = ofy().load()
                    .type(PGPPublicKeyData.class)
                    .filter("userEmail", userEmail)
                    .list();
        } else if (firstName != null && lastName != null) {
            result = ofy().load()
                    .type(PGPPublicKeyData.class)
                    .filter("firstName", firstName)
                    .filter("lastName", lastName)
                    .list();
        } else if (cryptonomicaUserId != null) {
            result = ofy().load()
                    .type(PGPPublicKeyData.class)
                    .filter("cryptonomicaUserId", cryptonomicaUserId)
                    .list();
        } else {
            throw new Exception("invalid search query: " + new Gson().toJson(searchPGPPublicKeysForm));
        }

        SearchPGPPublicKeysReturn searchPGPPublicKeysReturn = new SearchPGPPublicKeysReturn();

        if (result.size() > 0) {
            searchPGPPublicKeysReturn.PublicKeyDatasToGeneralViews(
                    new ArrayList<PGPPublicKeyData>(result)
            );
            searchPGPPublicKeysReturn.setMessageToUser("search results:");
        } else {
            searchPGPPublicKeysReturn.PublicKeyDatasToGeneralViews(new ArrayList<PGPPublicKeyData>());
            searchPGPPublicKeysReturn.setMessageToUser("no records found");
        }

        return searchPGPPublicKeysReturn;
    }

    @ApiMethod(
            name = "getMyKeys",
            path = "getMyKeys",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // search:
    // 1) by fingerprint
    // 2) by keyID
    // 3) by email
    // 4) by first and last names
    public SearchPGPPublicKeysReturn getMyKeys(
            // final HttpServletRequest httpServletRequest,
            final User googleUser
    ) throws Exception {
        // authorization
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //
        Key<CryptonomicaUser> cryptonomicaUserKey = Key.create(CryptonomicaUser.class, googleUser.getUserId());
        List<PGPPublicKeyData> result = ofy().load().type(PGPPublicKeyData.class).ancestor(cryptonomicaUserKey).list();
        SearchPGPPublicKeysReturn searchPGPPublicKeysReturn;
        if (result.size() > 0) {
            searchPGPPublicKeysReturn = new SearchPGPPublicKeysReturn(
                    "this is list of your keys, if any:",
                    new ArrayList<>(result)
            );
        } else {
            searchPGPPublicKeysReturn = new SearchPGPPublicKeysReturn(
                    "You have no keys, yet.",
                    new ArrayList<>(result)
            );
        }

        return searchPGPPublicKeysReturn;
    }

    @ApiMethod(
            name = "getUsersKeysByUserId",
            path = "getUsersKeysByUserId",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // get keys by user ID
    public SearchPGPPublicKeysReturn getUsersKeysByUserId(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final GetUsersKeysByUserIdForm getUsersKeysByUserIdForm
    ) throws Exception {
        // authorization
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //
        SearchPGPPublicKeysReturn searchPGPPublicKeysReturn = new SearchPGPPublicKeysReturn(
                "this are keys of user: " + getUsersKeysByUserIdForm.getUserId(),
                new ArrayList<>(
                        ofy().load()
                                .type(PGPPublicKeyData.class)
                                .ancestor(
                                        Key.create(
                                                CryptonomicaUser.class,
                                                getUsersKeysByUserIdForm.getUserId()
                                        )
                                )
                                .list()
                )
        );

        return searchPGPPublicKeysReturn;
    }

    @ApiMethod(
            name = "addFingerprintStrProperties",
            path = "addFingerprintStrProperties",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // temporary method to add new properties to existing entities
    // (fingerprint -> fingerprintStr)
    public StringWrapperObject addFingerprintStrProperties(
            final User googleUser
    ) throws Exception {

        /* Check authorization: */
        UserTools.ensureCryptonomicaOfficer(googleUser);

        /* Load PGPPublicKeyData from DB*/

        List<PGPPublicKeyData> pgpPublicKeyDataList = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .limit(20)
                .list();

        if (pgpPublicKeyDataList.size() > 10) {
            throw new Exception("there are to many keys in the database");
        }

        for (PGPPublicKeyData pgpPublicKeyData : pgpPublicKeyDataList) {
            pgpPublicKeyData.setFingerprintStr(pgpPublicKeyData.getFingerprint());
        }

        Map<Key<PGPPublicKeyData>, PGPPublicKeyData> result = ofy()
                .save()
                .entities(pgpPublicKeyDataList)
                .now();

        String resultJSON = new Gson().toJson(result);

        return new StringWrapperObject(resultJSON);
    }

}
