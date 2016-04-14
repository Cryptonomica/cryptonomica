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
import net.cryptonomica.forms.GeneralSearchUserProfilesForm;
import net.cryptonomica.returns.PGPPublicKeyGeneralView;
import net.cryptonomica.returns.UserProfileGeneralView;
import net.cryptonomica.returns.UserSearchAndViewReturn;
import net.cryptonomica.service.UserTools;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-test.appspot.com/_ah/api/explorer
 * search for users and view user data
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 */

@Api(name = "userSearchAndViewAPI", // The api name must match
// '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "Cryptonomica User Search and View API")
@SuppressWarnings("unused")
public class UserSearchAndViewAPI {

    private static final Logger LOG = Logger.getLogger(
            UserSearchAndViewAPI.class.getName()
    );

    @ApiMethod(
            name = "generalSearchUserProfiles",
            path = "generalSearchUserProfiles",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // returns list of user profiles
    public UserSearchAndViewReturn generalSearchUserProfiles(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final GeneralSearchUserProfilesForm generalSearchUserProfilesForm
    ) throws Exception {

        LOG.info("serch request: " + new Gson().toJson(generalSearchUserProfilesForm));

        // authorization
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        // initialize object for search result:
        List<CryptonomicaUser> cryptonomicaUsersList;
        // set "" value to be null
        if (generalSearchUserProfilesForm.getFirstName() != null && generalSearchUserProfilesForm.getFirstName().equals("")) {
            generalSearchUserProfilesForm.setFirstName(null);
        }
        if (generalSearchUserProfilesForm.getLastName() != null && generalSearchUserProfilesForm.getLastName().equals("")) {
            generalSearchUserProfilesForm.setLastName(null);
        }
        if (generalSearchUserProfilesForm.getEmail() != null && generalSearchUserProfilesForm.getEmail().equals("")) {
            generalSearchUserProfilesForm.setEmail(null);
        }
        // search: first name + last name
        if (generalSearchUserProfilesForm.getFirstName() != null
                && generalSearchUserProfilesForm.getLastName() != null
                && generalSearchUserProfilesForm.getEmail() == null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "firstName",
                            generalSearchUserProfilesForm.getFirstName()
                    )
                    .filter(
                            "lastName",
                            generalSearchUserProfilesForm.getLastName()
                    )
                    .list();
        }
        // first name + last name + email
        else if (generalSearchUserProfilesForm.getFirstName() != null
                && generalSearchUserProfilesForm.getLastName() != null
                && generalSearchUserProfilesForm.getEmail() != null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "firstName",
                            generalSearchUserProfilesForm.getFirstName())
                    .filter(
                            "lastName",
                            generalSearchUserProfilesForm.getLastName())
                    .filter(
                            "email",
                            new Email(generalSearchUserProfilesForm.getEmail()))
                    .list();
        }
        // first name + email
        else if (generalSearchUserProfilesForm.getFirstName() != null
                && generalSearchUserProfilesForm.getLastName() == null
                && generalSearchUserProfilesForm.getEmail() != null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "firstName",
                            generalSearchUserProfilesForm.getFirstName())
                    .filter(
                            "email",
                            new Email(generalSearchUserProfilesForm.getEmail()))
                    .list();
        }
        // last name + email
        else if (generalSearchUserProfilesForm.getFirstName() == null
                && generalSearchUserProfilesForm.getLastName() != null
                && generalSearchUserProfilesForm.getEmail() != null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "lastName",
                            generalSearchUserProfilesForm.getLastName())
                    .filter(
                            "email",
                            new Email(generalSearchUserProfilesForm.getEmail()))
                    .list();
        }
        // email
        else if (generalSearchUserProfilesForm.getFirstName() == null
                && generalSearchUserProfilesForm.getLastName() == null
                && generalSearchUserProfilesForm.getEmail() != null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "email",
                            new Email(generalSearchUserProfilesForm.getEmail())
                    )
                    .list();
        }
        // first name
        else if (generalSearchUserProfilesForm.getFirstName() != null
                && generalSearchUserProfilesForm.getLastName() == null
                && generalSearchUserProfilesForm.getEmail() == null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "firstName",
                            generalSearchUserProfilesForm.getFirstName()
                    )
                    .list();
        }
        // last name
        else if (generalSearchUserProfilesForm.getFirstName() == null
                && generalSearchUserProfilesForm.getLastName() != null
                && generalSearchUserProfilesForm.getEmail() == null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "lastName",
                            generalSearchUserProfilesForm.getLastName()
                    )
                    .list();
        } else {
            throw new Exception("Search query is incorrect or empty");
        }

        //
        UserSearchAndViewReturn userSearchAndViewReturn;
        if (cryptonomicaUsersList != null && !cryptonomicaUsersList.isEmpty()) {
            userSearchAndViewReturn =
                    new UserSearchAndViewReturn(
                            "results found",
                            new ArrayList<>(cryptonomicaUsersList)
                    );
        } else {
            userSearchAndViewReturn =
                    new UserSearchAndViewReturn(
                            "no results found"
                    );
        }

        LOG.info("search result: " + new Gson().toJson(userSearchAndViewReturn));

        return userSearchAndViewReturn;
    } // end of generalSearchUserProfiles @ApiMethod

    @ApiMethod(
            name = "getUserProfileById",
            path = "getUserProfileById",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    // returns list of user profiles
    public UserProfileGeneralView getUserProfileById(
            final User googleUser,
            final @Named("id") String id
    ) throws Exception {

        /* Check authorization: */
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* Validate form: */
        LOG.warning("@Named(\"id\"): " + id);
        if (id == null || id.length() < 1) {
            throw new Exception("User id is empty");
        }

        /* Load CryptonomicaUser from DB */
        // create key
        Key<CryptonomicaUser> cryptonomicaUserKey = Key.create(CryptonomicaUser.class, id);
        // get user with given Id from DB:
        CryptonomicaUser profile = ofy()
                .load()
                .key(cryptonomicaUserKey)
                .now();
        // check result:
        if (profile == null) {
            LOG.warning("User with not found");
            throw new Exception("User not found");
        }

        /* Create UserProfileGeneralView object to return */
        // find his public keys:
        List<PGPPublicKeyData> pgpPublicKeyDataList = ofy()
                .load()
                .type(PGPPublicKeyData.class)
// .parent(cryptonomicaUserKey). // can't do that: https://groups.google.com/forum/#!topic/objectify-appengine/DztrKogBXDw
                .filter("cryptonomicaUserId", id)
                .list();
        // check keys found, if no webSafeString add and store in DB :
        LOG.info("pgpPublicKeyDataList: " + new Gson().toJson(pgpPublicKeyDataList));
        for (PGPPublicKeyData k : pgpPublicKeyDataList) {
            if (k.getWebSafeString() == null) {

                if (k.getCryptonomicaUserKey() != null) {
                    k.setWebSafeString(
                            Key.create(k.getCryptonomicaUserKey(), PGPPublicKeyData.class, k.getFingerprint())
                                    .toWebSafeString()
                    );
                } else {
                    k.setWebSafeString(   // for old keys without parent
                            Key.create(PGPPublicKeyData.class, k.getFingerprint())
                                    .toWebSafeString()
                    );
                }
                ofy().save().entity(k); // async !
            }
        }
        // create obj to return:
        UserProfileGeneralView userProfileGeneralView = new UserProfileGeneralView(
                profile,
                pgpPublicKeyDataList
        );
        LOG.warning("userProfileGeneralView: "
                + userProfileGeneralView.toJson());

        return userProfileGeneralView;
    } // end of getUserProfileById @ApiMethod


    @ApiMethod( // <<< --- not used, use getMyUserData in CryptonomicaUserAPI
            name = "getMyProfile",
            path = "getMyProfile",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // returns list of user profiles
    public UserProfileGeneralView getMyProfile(
            final User googleUser
    ) throws Exception {
        // ensure authorization
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        //
        Key<CryptonomicaUser> myKey = Key.create(
                CryptonomicaUser.class, googleUser.getUserId());
        CryptonomicaUser profile = ofy().load()
                .key(myKey)
                .now();
        UserProfileGeneralView userProfileGeneralView =
                new UserProfileGeneralView(profile);
        List<PGPPublicKeyData> pgpPublicKeyDataList = ofy().load()
                .type(PGPPublicKeyData.class)
                .ancestor(myKey)
                .list();
        ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViews =
                new ArrayList<>();
        for (PGPPublicKeyData pgpPublicKeyData : pgpPublicKeyDataList) {
            pgpPublicKeyGeneralViews.add(
                    new PGPPublicKeyGeneralView(pgpPublicKeyData));
        }

        userProfileGeneralView.setPgpPublicKeyGeneralViews(
                pgpPublicKeyGeneralViews
        );

        return userProfileGeneralView;
    } // end of getUserProfileById @ApiMethod

} // end of class
