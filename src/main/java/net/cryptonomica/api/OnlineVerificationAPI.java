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
import net.cryptonomica.entities.*;
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
        description = "Online Verification API")

public class OnlineVerificationAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(OnlineVerificationAPI.class.getName());
    /* --- Gson */
    private static final Gson GSON = new Gson();

    /* --- Get online verification info by OpenPGP Public Key fingerprint  : */
    @ApiMethod(
            name = "getOnlineVerificationByFingerprint",
            path = "getOnlineVerificationByFingerprint",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public OnlineVerification getOnlineVerificationByFingerprint(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("fingerprint") String fingerprint
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

                /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new BadRequestException("fingerprint is missing or invalid");
        }

        PGPPublicKeyData pgpPublicKeyData = null;
        pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint)
                .first()
                .now();

        if (pgpPublicKeyData == null) {
            throw new NotFoundException("Key with fingerprint " + fingerprint + " not found");
        }

        /* --- Check authorization: */
        // only allowed users can get verification data:
        CryptonomicaUser requester = UserTools.ensureCryptonomicaRegisteredUser(googleUser);
        LOG.warning(GSON.toJson(requester));

        OnlineVerification onlineVerification = ofy().load().key(Key.create(OnlineVerification.class, fingerprint)).now();
        if (onlineVerification == null) {
            onlineVerification = new OnlineVerification();
        }

        if (
                requester.getUserId().equalsIgnoreCase(pgpPublicKeyData.getCryptonomicaUserId())
                        || (requester.getCryptonomicaOfficer() != null && requester.getCryptonomicaOfficer())
                        || (requester.getNotary() != null && requester.getNotary())
                        || (onlineVerification.getAllowedUsers().contains(requester.getUserId()))
                ) {
            LOG.warning(
                    "user " + requester.getUserId() + "is allowed to get online verification data for key " + fingerprint
            );
        } else {
            throw new UnauthorizedException(
                    "you are not allowed to get online verification data for key " + fingerprint
            );
        }

        LOG.warning(GSON.toJson(onlineVerification));

        return onlineVerification;
    } // end of getOnlineVerificationByFingerprint

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

    @ApiMethod(
            name = "getDocumentsUploadKey",
            path = "getDocumentsUploadKey",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject getDocumentsUploadKey(
            final HttpServletRequest httpServletRequest,
            final User googleUser
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check authorization: */
        final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        String randomString = RandomStringUtils.randomAlphanumeric(33);

        // create documentUploadKey
        VerificationDocumentsUploadKey verificationDocumentsUploadKey =
                new VerificationDocumentsUploadKey(cryptonomicaUser.getGoogleUser(), randomString);

        // save entity
        Key<VerificationDocumentsUploadKey> key = ofy()
                .save()
                .entity(verificationDocumentsUploadKey)
                .now();

        verificationDocumentsUploadKey = ofy() //
                .load()
                .key(key)
                .now();

        String documentsUploadKey = verificationDocumentsUploadKey.getDocumentsUploadKey();

        LOG.warning("documentsUploadKey: " + documentsUploadKey);

        return new StringWrapperObject(documentsUploadKey);

    } // end: getDocumentsUploadKey


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
