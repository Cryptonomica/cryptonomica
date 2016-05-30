package net.cryptonomica.service;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.Login;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
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
            throw new UnauthorizedException("Authorization required");
        }
    } // end of ensureGoogleAuth method

    public static Login registerLogin(
            final HttpServletRequest httpServletRequest,
            final User googleUser) {
        // Register login of this Cryptonomica User:
        Login login = new Login();
        String userIP = httpServletRequest.getHeader("X-FORWARDED-FOR"); // IP
        if (userIP == null) {
            userIP = httpServletRequest.getRemoteAddr();
        }
        // Get an UserAgentStringParser and analyze the requesting client
        // see example on: http://uadetector.sourceforge.net/usage.html
        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        ReadableUserAgent agent = parser.parse(httpServletRequest.getHeader("User-Agent"));
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
}
