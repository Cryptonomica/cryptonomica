package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
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
import net.cryptonomica.service.UserTools;
import org.bouncycastle.openpgp.PGPPublicKey;

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

        /* Validate form: */

        if (fingerprint == null || fingerprint.length() < 40 || fingerprint.length() > 40) {
            throw new Exception("Invalid public key in request");
        }

        /* Load PGPPublicKeyData from DB*/

        List<PGPPublicKeyData> pgpPublicKeyDataList = null;
        pgpPublicKeyDataList = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                // <--- "fingerprintStr"
                // @Id fields cannot be filtered on
                .filter("fingerprintStr", fingerprint.toUpperCase())
                .list();

        LOG.warning("DS search result: " + new Gson().toJson(pgpPublicKeyDataList));

        // if key not found trow an exception
        if (pgpPublicKeyDataList == null || pgpPublicKeyDataList.size() == 0) {
            throw new Exception("Public PGP key not found");
        }

        // check if there is only one key with given fingerprint in the database
        if (pgpPublicKeyDataList.size() > 1) {
            throw new Exception("there are "
                    + pgpPublicKeyDataList.size()
                    + " different keys with fingerprint "
                    + fingerprint.toUpperCase()
                    + "in the database"
            );
        }

        // get key from the list
        PGPPublicKeyData pgpPublicKeyData = pgpPublicKeyDataList.get(0);

        // make key representation, return result
        PGPPublicKeyGeneralView pgpPublicKeyGeneralView = new PGPPublicKeyGeneralView(pgpPublicKeyData);

        LOG.warning("Result: " + new Gson().toJson(pgpPublicKeyGeneralView));

        return pgpPublicKeyGeneralView;
    }

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
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //
        LOG.warning(new Gson().toJson(pgpPublicKeyUploadForm));
        //
        String asciiArmored;

        if (pgpPublicKeyUploadForm.getAsciiArmored() != null
                || pgpPublicKeyUploadForm.getAsciiArmored().length() > 0) {
            asciiArmored = pgpPublicKeyUploadForm.getAsciiArmored();
        } else {
            throw new Exception("ASCII-armored key is empty");
        }

        PGPPublicKey pgpPublicKey = PGPTools.readPublicKeyFromString(asciiArmored); // throws IOException, PGPException

        PGPPublicKeyData pgpPublicKeyData = new PGPPublicKeyData(
                pgpPublicKey,
                asciiArmored,
                googleUser.getUserId()
        );
        // -- add @Parent value: --- ?
        pgpPublicKeyData.setCryptonomicaUserKey(Key.create(CryptonomicaUser.class, googleUser.getUserId()));
        // save key
        Key<PGPPublicKeyData> pgpPublicKeyDataKey = ofy().save().entity(pgpPublicKeyData).now();
        // load key from DB and and create return object
        PGPPublicKeyUploadReturn pgpPublicKeyUploadReturn = new PGPPublicKeyUploadReturn(
                "Key Saved in data base",
                ofy().load().key(pgpPublicKeyDataKey).now()
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
