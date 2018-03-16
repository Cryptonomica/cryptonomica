package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.returns.UserProfileGeneralView;
import net.cryptonomica.service.UserTools;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 *
 */

@Api(name = "cryptonomicaUserAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "Cryptonomica User API: check if user already registered ect.")
//
public class CryptonomicaUserAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(CryptonomicaUserAPI.class.getName());

    /* ------ */
    @ApiMethod(
            name = "ensureCryptonomicaRegisteredUser",
            path = "ensureCryptonomicaRegisteredUser",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // check if user is not already registered;
    public void ensureNewCryptonomicaUser(
            User googleUser
    ) throws Exception {
        // ensure 1) user login, 2) not already registered:
        UserTools.ensureCryptonomicaRegisteredUser(googleUser); // ?? may be also register login?

    }  // end of ensureNewCryptonomicaUser @ApiMethod

    /* ----- */
    @ApiMethod(
            name = "getMyUserData",
            path = "getMyUserData",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public UserProfileGeneralView getMyUserData(
            final HttpServletRequest httpServletRequest,
            final User googleUser) {
        //
        UserProfileGeneralView userProfileGeneralView = null;
        CryptonomicaUser cryptonomicaUser = null;
        //
        if (googleUser == null) {
            userProfileGeneralView = new UserProfileGeneralView();
            userProfileGeneralView.setLoggedIn(false);
            return userProfileGeneralView;
        }
        try {
            cryptonomicaUser = ofy()
                    .load()
                    .key(Key.create(CryptonomicaUser.class, googleUser.getUserId()))
                    .now();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
        if (cryptonomicaUser == null) {
            userProfileGeneralView = new UserProfileGeneralView();
            userProfileGeneralView.setLoggedIn(true);
            userProfileGeneralView.setRegisteredCryptonomicaUser(false);
            return userProfileGeneralView;
        } else {
            userProfileGeneralView = new UserProfileGeneralView(cryptonomicaUser);
            userProfileGeneralView.setLoggedIn(true);
            userProfileGeneralView.setRegisteredCryptonomicaUser(true);
            return userProfileGeneralView;
        }

    } // end getMyUserData()

}
