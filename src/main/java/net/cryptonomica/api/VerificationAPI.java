package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.googlecode.objectify.Key;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Message;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.Verification;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.returns.VerificationGeneralView;
import net.cryptonomica.service.TwilioUtils;
import net.cryptonomica.service.UserTools;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * cryptonomica-server.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 * * in this API:
 */
@Api(name = "verificationAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "Verification API")

public class VerificationAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(VerificationAPI.class.getName());

    /* --- Get verification info by verification ID: */
    @ApiMethod(
            name = "getVerificationByID",
            path = "getVerificationByID",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public VerificationGeneralView getVerificationByID(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("verificationID") String verificationID
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- Check input: */
        if (verificationID == null || verificationID.equals("")) {
            throw new BadRequestException("Verification ID missing");
        }

        /* --- Load verification entity from DS: */
        Verification verification = ofy()
                .load()
                .key(Key.create(Verification.class, verificationID))
                .now();
        if (verification == null) {
            throw new NotFoundException("Verification info not found");
        }

        /* --- Create new verification info representation: */
        VerificationGeneralView verificationGeneralView = new VerificationGeneralView(verification);
        LOG.warning(new Gson().toJson(verificationGeneralView));

        return verificationGeneralView;
    } // end: getVerificationByID

    /* --- Get verification info by web-safe key string: */
    @ApiMethod(
            name = "getVerificationByWebSafeString",
            path = "getVerificationByWebSafeString",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public VerificationGeneralView getVerificationByWebSafeString(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("verificationWebSafeString") String verificationWebSafeString
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- Check input: */
        if (verificationWebSafeString == null || verificationWebSafeString.equals("")) {
            throw new BadRequestException("Verification ID missing");
        }

        /* --- Load verification entity from DS: */
        Key<Verification> verificationKey = Key.create(verificationWebSafeString);
        Verification verification = ofy()
                .load()
                .key(verificationKey)
                .now();
        if (verification == null) {
            throw new NotFoundException("Verification info not found");
        }

        /* --- Create new verification info representation: */
        VerificationGeneralView verificationGeneralView = new VerificationGeneralView(verification);
        LOG.warning(new Gson().toJson(verificationGeneralView));

        return verificationGeneralView;
    }

}
