package net.cryptonomica.service;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.Login;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.returns.UserProfileGeneralView;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;


/**
 * Methods, mostly static and void, for authentication, authorization, registerign logins ect.
 */
public class UserTools {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(
            UserTools.class.getName()
    );

    public static void ensureGoogleAuth(final User googleUser) throws UnauthorizedException {
        if (googleUser == null) {
            LOG.warning("User not logged in");
            throw new UnauthorizedException(
                    "Authorization required. (You may need to reload/refresh this page and login with your Google account)"
            );
        }
    } // end of ensureGoogleAuth method

    public static Login registerLogin(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final String userAction) {
        // Register login of this Cryptonomica User:
        Login login = new Login();
        String userIP = httpServletRequest.getHeader("X-FORWARDED-FOR"); // IP
        if (userIP == null) {
            userIP = httpServletRequest.getRemoteAddr();
        }
        // Get an UserAgentStringParser and analyze the requesting client
        // see example on: http://uadetector.sourceforge.net/usage.html
        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        String userAgentString = httpServletRequest.getHeader("User-Agent");
        ReadableUserAgent agent = parser.parse(userAgentString);
        String userBrowser = agent.getName();
        String userOS = agent.getOperatingSystem().getName();
        //
        String userProviderOrg = null;
        String userProviderHost = null;
        String country = null;
        String region = null;
        String city = null;

        JSONObject ipInfoIoJSON = GetJSONfromURL.getIpInfoIo(userIP);

        if (ipInfoIoJSON != null) {
            try {
                userProviderOrg = ipInfoIoJSON.getString("org");
            } catch (JSONException e) {
                userProviderOrg = e.getMessage();
            }

            try {
                userProviderHost = ipInfoIoJSON.getString("hostname");
            } catch (JSONException e) {
                userProviderHost = e.getMessage();
            }
            try {
                country = ipInfoIoJSON.getString("country");
            } catch (JSONException e) {
                country = e.getMessage();
            }
            try {
                region = ipInfoIoJSON.getString("region");
            } catch (JSONException e) {
                region = e.getMessage();
            }
            try {
                city = ipInfoIoJSON.getString("city");
            } catch (JSONException e) {
                city = e.getMessage();
            }

        }
        login.setCryptonomicaUserKey(Key.create(CryptonomicaUser.class, googleUser.getUserId()));
        login.setLoginEmail(new Email(googleUser.getEmail()));
        login.setLoginDate(new Date());
        login.setIP(userIP);
        login.setUserBrowser(userBrowser);
        login.setUserOS(userOS);
        login.setHostname(userProviderHost);
        login.setCity(city);
        login.setRegion(region);
        login.setCountry(country);
        login.setProvider(userProviderOrg);
        login.setUserAgentString(userAgentString);
        // --- save Login:
        Key<Login> loginKey = ofy().save().entity(login).now();
        login = ofy().load().key(loginKey).now();
        return login;
    }

    public static void ensureNewCryptonomicaUser(final User googleUser) throws Exception {
        ensureGoogleAuth(googleUser);
        String userId = googleUser.getUserId();
        Key<CryptonomicaUser> userKey = Key.create(CryptonomicaUser.class, userId);
        CryptonomicaUser cryptonomicaUser = null;
        cryptonomicaUser = ofy().load().key(userKey).now();
        if (cryptonomicaUser != null) {
            throw new Exception("user is already registered");
        }
    }

    public static CryptonomicaUser loadCryptonomicaUser(final String cryptonomicaUserId) throws Exception {
        CryptonomicaUser cryptonomicaUser = ofy()
                .load()
                .key(Key.create(CryptonomicaUser.class, cryptonomicaUserId))
                .now();
        if (cryptonomicaUser == null) {
            throw new Exception("Cryptonomica user with ID " + cryptonomicaUserId + " not found in DB");
        }
        return cryptonomicaUser;
    }

    /* --- Check if user is registered user: */
    public static CryptonomicaUser ensureCryptonomicaRegisteredUser(final User googleUser)
            throws UnauthorizedException {

        ensureGoogleAuth(googleUser);

        CryptonomicaUser cryptonomicaUser = null;
        try {
            cryptonomicaUser = ofy()
                    .load()
                    .key(Key.create(CryptonomicaUser.class, googleUser.getUserId()))
                    .now();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }

        if (cryptonomicaUser == null) {
            throw new UnauthorizedException("You are not registered on Cryptonomica server");
        }
        return cryptonomicaUser;
    } // end of ensureCryptonomicaRegisteredUser method

    /* --- Check if user is an IACC officer: */
    public static CryptonomicaUser ensureCryptonomicaOfficer(final User googleUser)
            throws UnauthorizedException {
        //
        CryptonomicaUser cryptonomicaUser = ensureCryptonomicaRegisteredUser(googleUser);

        LOG.warning("cryptonomicaUser.getCryptonomicaOfficer(): " + cryptonomicaUser.getCryptonomicaOfficer());

        if (cryptonomicaUser.getCryptonomicaOfficer() == null || !cryptonomicaUser.getCryptonomicaOfficer()) {
            throw new UnauthorizedException("You are not a Cryptonomica officer");
        }

        return cryptonomicaUser;
    } // end of ensureCryptonomicaOfficer method

    /* --- Check if user is a notary or IACC officer: */
    public static CryptonomicaUser ensureNotaryOrCryptonomicaOfficer(final User googleUser)
            throws UnauthorizedException {
        //
        CryptonomicaUser cryptonomicaUser = ensureCryptonomicaRegisteredUser(googleUser);
        //
        LOG.warning("cryptonomicaUser: ");
        LOG.warning(new Gson().toJson(cryptonomicaUser));

        // if (cryptonomicaOfficer == null && notary == null) {
        LOG.warning("cryptonomicaUser.getCryptonomicaOfficer(): " + cryptonomicaUser.getCryptonomicaOfficer());
        LOG.warning("cryptonomicaUser.getNotary(): " + cryptonomicaUser.getNotary());
        // if isCryptonomicaOfficer and isNotary are both false or null:
        if ((cryptonomicaUser.getCryptonomicaOfficer() == null || !cryptonomicaUser.getCryptonomicaOfficer())
                && (cryptonomicaUser.getNotary() == null || !cryptonomicaUser.getNotary())) {
            throw new UnauthorizedException("You are not a Notary or Cryptonomica officer");
        }
        return cryptonomicaUser;
    } // end of ensureNotaryOrCryptonomicaOfficer method

    public static UserProfileGeneralView getUserProfileById(String id) {

        /* Validate input : */

        LOG.warning("id: " + id);

        if (id == null || id.length() < 1) {
            throw new IllegalArgumentException("User id is null or empty");
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
            throw new IllegalArgumentException("User not found");
        }

        /* Create UserProfileGeneralView object to return */
        // find his public keys:
        List<PGPPublicKeyData> pgpPublicKeyDataList = ofy()
                .load()
                .type(PGPPublicKeyData.class)
// .parent(cryptonomicaUserKey). // can't do that: https://groups.google.com/forum/#!topic/objectify-appengine/DztrKogBXDw
                .filter("cryptonomicaUserId", id)
                .list();

        // check keys found, if no webSafeString add and store in DB : // not needed anymore
        /*
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
        */


        // create obj to return:
        UserProfileGeneralView userProfileGeneralView = new UserProfileGeneralView(
                profile,
                pgpPublicKeyDataList
        );

        // LOG.warning("userProfileGeneralView: " + userProfileGeneralView.toJson());

        return userProfileGeneralView;
    }
}
