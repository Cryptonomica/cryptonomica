package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.ApiKey;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.forms.GeneralSearchUserProfilesForm;
import net.cryptonomica.returns.PGPPublicKeyGeneralView;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.returns.UserProfileGeneralView;
import net.cryptonomica.returns.UserSearchAndViewReturn;
import net.cryptonomica.service.ApiKeysService;
import net.cryptonomica.service.UserTools;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: {sandbox-cryptonomica || cryptonomica-server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - on frontend API should be loaded in app.js - app.run()
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
                            generalSearchUserProfilesForm.getFirstName().toLowerCase()
                    )
                    .filter(
                            "lastName",
                            generalSearchUserProfilesForm.getLastName().toLowerCase()
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
                            generalSearchUserProfilesForm.getFirstName().toLowerCase()
                    )
                    .filter(
                            "lastName",
                            generalSearchUserProfilesForm.getLastName().toLowerCase()
                    )
                    .filter(
                            "email",
                            new Email(
                                    generalSearchUserProfilesForm.getEmail().toLowerCase()
                            )
                    )
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
                            generalSearchUserProfilesForm.getFirstName().toLowerCase()
                    )
                    .filter(
                            "email",
                            new Email(
                                    generalSearchUserProfilesForm.getEmail().toLowerCase()
                            )
                    )
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
                            generalSearchUserProfilesForm.getLastName().toLowerCase()
                    )
                    .filter(
                            "email",
                            new Email(
                                    generalSearchUserProfilesForm.getEmail().toLowerCase()
                            )
                    )
                    .list();
        }

        // email only
        else if (generalSearchUserProfilesForm.getFirstName() == null
                && generalSearchUserProfilesForm.getLastName() == null
                && generalSearchUserProfilesForm.getEmail() != null) {
            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "email",
                            new Email(
                                    generalSearchUserProfilesForm.getEmail().toLowerCase()
                            )
                    )
                    .list();
        }

        // first name only
        else if (generalSearchUserProfilesForm.getFirstName() != null
                && generalSearchUserProfilesForm.getLastName() == null
                && generalSearchUserProfilesForm.getEmail() == null) {

            cryptonomicaUsersList = ofy().load()
                    .type(CryptonomicaUser.class)
                    .filter(
                            "firstName",
                            generalSearchUserProfilesForm.getFirstName().toLowerCase()
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
                            generalSearchUserProfilesForm.getLastName().toLowerCase()
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
    public UserProfileGeneralView getUserProfileById(
            final User googleUser,
            final @Named("id") String id
    ) throws Exception {

        /* Check authorization: */
        UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        return UserTools.getUserProfileById(id);
    } // end of getUserProfileById @ApiMethod


    @ApiMethod(
            name = "getUserProfileByIdWithApiKey",
            path = "getUserProfileByIdWithApiKey",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public UserProfileGeneralView getUserProfileByIdWithApiKey(
            final HttpServletRequest httpServletRequest,
            final @Named("id") String id,
            @Named("serviceName") @Nullable String serviceName, // can be in params or headers
            @Named("apiKeyString") @Nullable String apiKeyString // can be in params or headers
    ) throws Exception {

        /* Check authorization: */
        // > also checks serviceName, apiKeyString in request headers:
        ApiKey apiKey = ApiKeysService.checkApiKey(httpServletRequest, serviceName, apiKeyString);


        if (!apiKey.getCanGetUserDataByUserId()) {
            throw new UnauthorizedException("API key is not valid for this API method");
        }

        return UserTools.getUserProfileById(id);
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


    @ApiMethod(
            name = "echo",
            path = "echo",
            httpMethod = ApiMethod.HttpMethod.GET)
    /* >>>>>>>>>>>>>> this is for testing only
     * curl -H "Content-Type: application/json" -X GET -d '{"message":"hello world"}' https://cryptonomica-server.appspot.com/_ah/api/userSearchAndViewAPI/v1/echo
     * */
    public StringWrapperObject echo(
            @Named("message") String message,
            @Named("n") @Nullable Integer n
    ) {
        StringWrapperObject stringWrapperObject = new StringWrapperObject();

        if (n != null && n >= 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(message);
            }
            stringWrapperObject.setMessage(sb.toString());
        }
        return stringWrapperObject;
    }
    // [END echo_method]

} // end of class
