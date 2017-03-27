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
import net.cryptonomica.entities.OnlineVerification;
import net.cryptonomica.entities.VideoUploadKey;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.service.TwilioUtils;
import net.cryptonomica.service.UserTools;
import org.apache.commons.lang3.RandomStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * cryptonomica-server.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>) +
 * ! - API should be loaded in app.js - app.run() +
 * * in this API:
 */
@Api(name = "onlineVerificationAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "Onine Verification API")

public class OnlineVerificationAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(OnlineVerificationAPI.class.getName());

    /* --- Get verification info by verification ID: */
    @ApiMethod(
            name = "getOnlineVerificationByID",
            path = "getOnlineVerificationByID",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public OnlineVerification getOnlineVerificationByID(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("onlineVerificationID") String onlineVerificationID
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check authorization: */
        final CryptonomicaUser cryptonomicaOfficer = UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- Check input: */
        if (onlineVerificationID == null || onlineVerificationID.equals("")) {
            throw new BadRequestException("Online Verification ID missing");
        }

        /// ---------------

        /* --- Load verification entity from DS: */
        OnlineVerification onlineVerification = ofy()
                .load()
                .key(Key.create(OnlineVerification.class, onlineVerificationID))
                .now();
        if (onlineVerificationID == null) {
            throw new NotFoundException("Verification info not found");
        }

        /* --- Create new verification info representation: */

        LOG.warning(new Gson().toJson(onlineVerification));

        return onlineVerification;
    } // end: getVerificationByID

    /* --- Get verification info by verification ID: */
    @ApiMethod(
            name = "getVideoUploadKey",
            path = "getVideoUploadKey",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject getVideoUploadKey(
            final HttpServletRequest httpServletRequest,
            final User googleUser
            // final @Named("onlineVerificationID") String onlineVerificationID
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check authorization: */
        final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // /* --- Check input: */
        // if (onlineVerificationID == null || onlineVerificationID.equals("")) {
        //     throw new BadRequestException("Online Verification ID missing");
        // }

        String randomString = RandomStringUtils.randomAlphanumeric(33);

        // create videoUploadKey
        VideoUploadKey videoUploadKey = new VideoUploadKey(cryptonomicaUser.getGoogleUser(), randomString);
        // save entity
        Key<VideoUploadKey> videoUploadKeyKey = ofy()
                .save()
                .entity(videoUploadKey)
                .now();
        videoUploadKey = ofy() //
                .load()
                .key(videoUploadKeyKey)
                .now();

        String videoUploadKeyString = videoUploadKey.getVideoUploadKey();

        LOG.warning("videoUploadKeyString: " + videoUploadKeyString);

        return new StringWrapperObject(videoUploadKeyString);

    } // end: getVideoUploadKey

    // @ApiMethod(
    //         name = "uploadVideo",
    //         path = "uploadVideo",
    //         httpMethod = ApiMethod.HttpMethod.POST
    // )
    // @SuppressWarnings("unused")
    // public StringWrapperObject uploadVideo(
    //         final HttpServletRequest httpServletRequest,
    //         final User googleUser,
    //         final @Named("fingerprint") String fingerprint
    //         // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    // ) throws UnauthorizedException, BadRequestException, NotFoundException, IOException, ServletException {
    //
    //     /* --- Check authorization: */
    //     final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);
    //
    //     /* --- Check input: */
    //     if (fingerprint == null || fingerprint.equals("")) {
    //         throw new BadRequestException("fingerprint is missing");
    //     }
    //
    //     /* check if key belongs to user (check by fingerprint) */
    //     // TODO:
    //
    //
    //     String jsonResponseStr = CloudStorageServiceOld.uploadFileToCloudStorage(httpServletRequest);
    //     LOG.warning(jsonResponseStr);
    //     return new StringWrapperObject(jsonResponseStr);
    //
    // } // end: uploadVideo

    /* --- Test SMS service: */
    @ApiMethod(
            name = "sendTestSms",
            path = "sendTestSms",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject sendTestSms(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("phoneNumber") String phoneNumber,
            final @Named("smsMessage") String smsMessage
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException, TwilioRestException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- Send SMS */
        Message message = TwilioUtils.sendSms(phoneNumber, smsMessage);

        return new StringWrapperObject(message.toJSON());

    } // end of sendTestSms();

}
