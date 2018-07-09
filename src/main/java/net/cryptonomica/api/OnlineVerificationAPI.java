package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.googlecode.objectify.Key;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Message;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.*;
import net.cryptonomica.returns.BooleanWrapperObject;
import net.cryptonomica.returns.OnlineVerificationView;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.service.TwilioUtils;
import net.cryptonomica.service.UserTools;
import org.apache.commons.lang3.RandomStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public OnlineVerificationView getOnlineVerificationByFingerprint(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("fingerprint") String fingerprint
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new BadRequestException("fingerprint is missing or invalid");
        }

        PGPPublicKeyData pgpPublicKeyData = ofy()
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
        CryptonomicaUser requester = UserTools.ensureCryptonomicaRegisteredUser(googleUser); // << registered user
        LOG.warning(GSON.toJson(requester));

        // >>>>>>>>>>>>>>>>>> New OnlineVerifications are created here !!!
        // (user first have to request verification to make changes to it)
        OnlineVerification onlineVerification = ofy()
                .load()
                .key(Key.create(OnlineVerification.class, fingerprint))
                .now();

        if (onlineVerification == null) {
            if (requester.getUserId().equalsIgnoreCase(pgpPublicKeyData.getCryptonomicaUserId())) {
                onlineVerification = new OnlineVerification(pgpPublicKeyData);
                ofy()
                        .save()
                        .entity(onlineVerification)
                        .now();
            } else {
                throw new NotFoundException("Online verification data for fingerprint " + fingerprint + " not found");
            }
        }

        if (requester.getUserId().equalsIgnoreCase(pgpPublicKeyData.getCryptonomicaUserId())
                || (requester.getCryptonomicaOfficer() != null && requester.getCryptonomicaOfficer())
                // || (requester.getNotary() != null && requester.getNotary()) // TODO: should all notaries have access?
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

        ArrayList<VerificationDocument> verificationDocumentArrayList = new ArrayList<>();
        int verificationDocumentsListSize = ofy()
                .load()
                .type(VerificationDocument.class)
                .filter("fingerprint", fingerprint)
                .filter("hidden", false)
                .list()
                .size();
        LOG.warning("verificationDocumentsListSize: " + verificationDocumentsListSize);

        if (verificationDocumentsListSize > 0) {
            List<VerificationDocument> verificationDocumentList = ofy()
                    .load()
                    .type(VerificationDocument.class)
                    .filter("fingerprint", fingerprint)
                    .filter("hidden", false)
                    .list();
            verificationDocumentArrayList.addAll(verificationDocumentList);
            LOG.warning(GSON.toJson(verificationDocumentArrayList));
        }

        OnlineVerificationView onlineVerificationView = new OnlineVerificationView(
                onlineVerification,
                verificationDocumentArrayList
        );
        LOG.warning("onlineVerificationView:");
        LOG.warning(onlineVerificationView.toString());

        return onlineVerificationView;
    } // end of getOnlineVerificationByFingerprint

    // /* --- Get uploaded verification document IDs by OpenPGP Public Key fingerprint  : */
    @ApiMethod(
            name = "getVerificationDocumentIDsByFingerprint",
            path = "getVerificationDocumentIDsByFingerprint",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    /* this is only for cryptonomica officers */
    public ArrayList<VerificationDocument> getVerificationDocumentIDsByFingerprint(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("fingerprint") String fingerprint
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException {

        /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new BadRequestException("fingerprint is missing or invalid");
        }

        /* --- Check authorization: */
        // only CryptonomicaOfficer allowed to get all documents data:
        CryptonomicaUser requester = UserTools.ensureCryptonomicaOfficer(googleUser); // << !CryptonomicaOfficer
        LOG.warning(GSON.toJson(requester));

        OnlineVerification onlineVerification = ofy().load().key(Key.create(OnlineVerification.class, fingerprint)).now();
        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVerification for with fingerprint " + fingerprint + " not found");
        }
        // LOG.warning(GSON.toJson(onlineVerification));

        PGPPublicKeyData pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint)
                .first()
                .now();
        if (pgpPublicKeyData == null) {
            throw new NotFoundException("Key with fingerprint " + fingerprint + " not found");
        }

        ArrayList<VerificationDocument> verificationDocumentArrayList = (ArrayList<VerificationDocument>) ofy()
                .load()
                .type(VerificationDocument.class)
                .filter("fingerprint", fingerprint)
                // .filter("hidden", false) // - not here
                .list();

        // ArrayList can be returned, see:
        // https://cloud.google.com/endpoints/docs/frameworks/java/parameter-and-return-types
        // - "Any array or java.util.Collection of a parameter type"
        return verificationDocumentArrayList; // << remember: only CryptonomicaOfficer
    } // end of getVerificationDocumentIDsByFingerprint();

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

    /* --- Send SMS : */
    @ApiMethod(
            name = "sendSms",
            path = "sendSms",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject sendSms(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("phoneNumber") String phoneNumber,
            // in international format, f.e. +972523333333
            final @Named("fingerprint") String fingerprint

            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException, TwilioRestException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // --- create SMS:
        String smsMessage = RandomStringUtils.randomNumeric(7);
        LOG.warning("smsMessage: " + smsMessage);

        // --- store SMS:
        PhoneVerification phoneVerification = null;
        phoneVerification = ofy().load().key(Key.create(PhoneVerification.class, fingerprint)).now();
        if (phoneVerification == null) {
            phoneVerification = new PhoneVerification(fingerprint);
        }
        if (phoneVerification.getVerified()) {
            throw new BadRequestException("Phone already verified for this OpenPGP public key " + fingerprint);
        }

        phoneVerification.setPhoneNumber(phoneNumber);
        phoneVerification.setUserEmail(cryptonomicaUser.getEmail());
        phoneVerification.setSmsMessage(smsMessage);
        phoneVerification.setFailedVerificationAttemps(0);
        phoneVerification.setSmsMessageSend(new Date());
        LOG.warning(GSON.toJson(phoneVerification));

        /* --- Send SMS */
        // via:
        Message message = TwilioUtils.sendSms(phoneNumber, smsMessage);
        LOG.warning(message.toJSON());

        /* --- Save phoneVerification */
        ofy().save().entity(phoneVerification).now();

        return new StringWrapperObject("SMS message send successfully");

    } // end of sendTestSms();

    /* --- Check SMS  */
    @ApiMethod(
            name = "checkSms",
            path = "checkSms",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject checkSms(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("smsMessage") String smsMessage,
            final @Named("fingerprint") String fingerprint

            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException, TwilioRestException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- Check if OnlineVerificaiton entity exists */
        OnlineVerification onlineVerification = ofy()
                .load().key(Key.create(OnlineVerification.class, fingerprint)).now();
        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVerification entity does not exist in data base");
        }

        // --- store SMS:
        PhoneVerification phoneVerification = null;
        phoneVerification = ofy().load().key(Key.create(PhoneVerification.class, fingerprint)).now();

        if (phoneVerification == null) {
            throw new NotFoundException("Send sms message not found for key " + fingerprint);
        }

        LOG.warning("phoneVerification.getSmsMessage(): " + phoneVerification.getSmsMessage());
        LOG.warning("smsMessage: " + smsMessage);
        Boolean verificationResult = phoneVerification.getSmsMessage().toString().equalsIgnoreCase(smsMessage);
        phoneVerification.setVerified(verificationResult);
        StringWrapperObject result = new StringWrapperObject();
        if (verificationResult) {
            result.setMessage("Phone verified!");
        } else {
            phoneVerification.setFailedVerificationAttemps(phoneVerification.getFailedVerificationAttemps() + 1);
            ofy().save().entity(phoneVerification).now();
            if (phoneVerification.getFailedVerificationAttemps() >= 3) {
                throw new BadRequestException("The number of attempts is exhausted. Please resend new sms");
            } else {
                throw new BadRequestException("Code does not much. It was attempt # "
                        + phoneVerification.getFailedVerificationAttemps());
            }
        }

        // save phone verification
        ofy().save().entity(phoneVerification).now();

        // record to verification
        onlineVerification.setPhoneNumber(phoneVerification.getPhoneNumber());
        ofy().save().entity(onlineVerification).now();


        return result;

    } // end of checkSms();

    // /* --- Accept terms  */
    @ApiMethod(
            name = "acceptTerms",
            path = "acceptTerms",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject acceptTerms(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("termsAccepted") Boolean termsAccepted,
            final @Named("fingerprint") String fingerprint

            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new BadRequestException("fingerprint is missing or invalid");
        }

        if (termsAccepted == null || !termsAccepted) {
            throw new IllegalArgumentException("You have to accept terms to process with online verification");
        }

        /* --- Check if OnlineVerificaiton entity exists */
        OnlineVerification onlineVerification = ofy()
                .load().key(Key.create(OnlineVerification.class, fingerprint)).now();
        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVeriication entity for fingerprint "
                    + fingerprint
                    + " does not exist in data base"
            );
        }

        onlineVerification.setTermsAccepted(termsAccepted);

        ofy().save().entity(onlineVerification).now();

        return new BooleanWrapperObject(termsAccepted, "Terms accepted");

    } // end of acceptTerms();

    // /* --- Approve online verification (for Cryptonomica Complience Officer)  */
    @ApiMethod(
            name = "approve",
            path = "approve",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject approve(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("onlineVerificationApproved") Boolean onlineVerificationApproved,
            final @Named("verificationNotes") String verificationNotes,
            final @Named("fingerprint") String fingerprint
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException {

        /* --- Check authorization : CRYPTONOMICA OFFICER ONLY !!! */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new BadRequestException("fingerprint is missing or invalid");
        }

        if (onlineVerificationApproved == null) {
            throw new IllegalArgumentException("onlineVerificationApproved is null");
        } else if (!onlineVerificationApproved) {
            throw new BadRequestException("onlineVerificationApproved (checkbox): false");
        }

        // Check if OnlineVerification entity exists:
        OnlineVerification onlineVerification = ofy()
                .load().key(Key.create(OnlineVerification.class, fingerprint)).now();
        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVeriication entity for fingerprint "
                    + fingerprint
                    + " does not exist in data base"
            );
        } else if (onlineVerification.getOnlineVerificationDataVerified() != null
                && onlineVerification.getOnlineVerificationDataVerified()) {
            throw new BadRequestException("OnlineVerification already approved");
        }

        // mark Online Verification as approved:
        onlineVerification.setOnlineVerificationDataVerified(onlineVerificationApproved); // <<<<<< !!!
        onlineVerification.setVerifiedById(googleUser.getUserId());
        onlineVerification.setVerifiedByFirstNameLastName(
                cryptonomicaUser.getFirstName() + " " + cryptonomicaUser.getLastName()
        );
        onlineVerification.setVerifiedOn(new Date());
        onlineVerification.setVerificationNotes(verificationNotes);
        // mark key as verified:
        PGPPublicKeyData pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint.toUpperCase())
                .first()
                .now();
        if (pgpPublicKeyData == null) {
            throw new NotFoundException("Key with fingerprint " + fingerprint + " not found");
        }
        //
        // pgpPublicKeyData.setOnlineVerificationFinished(Boolean.TRUE); // >> this should be made in StripePaymentsAPI
        pgpPublicKeyData.setNationality(onlineVerification.getNationality().toUpperCase());
        pgpPublicKeyData.setVerifiedOnline(Boolean.TRUE); // <<< NEW

        // save data to data store:
        ofy()
                .save()
                .entity(onlineVerification)
                .now();
        ofy()
                .save()
                .entity(pgpPublicKeyData)
                .now();

        // Send email to user:
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email",
                                onlineVerification.getUserEmail().getEmail()
                        )
                        .param("emailCC",
                                "verification@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "[cryptonomica] Online verification for key: "
                                        + onlineVerification.getKeyID()
                                        + " approved")
                        .param("messageText",
                                "Congratulation! \n\n"
                                        + onlineVerification.getFirstName().toUpperCase() + " "
                                        + onlineVerification.getLastName().toUpperCase() + ",\n\n"
                                        + "your request for online verification for key with fingerprint : "
                                        + fingerprint + " approved! \n\n"
                                        + "See verification information on:\n"
                                        + "https://cryptonomica.net/#/onlineVerificationView/" + fingerprint + "\n"
                                        + "(information is not public, you have to login with your google account "
                                        + onlineVerification.getUserEmail().getEmail()
                                        + ")\n\n"
                                        + "Best regards, \n\n"
                                        + "Cryptonomica team\n\n"
                                        + new Date().toString()
                                        + "\n\n"
                                        + "if you think it's wrong or it is an error, "
                                        + "please write to support@cryptonomica.net\n\n"
                        )
        );

        // create result object:
        BooleanWrapperObject result = new BooleanWrapperObject(onlineVerificationApproved,
                "Online Verification for key " + fingerprint + " approved"
        );

        return result;

    } // end of approve ;


}
