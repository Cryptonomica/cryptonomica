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
import net.cryptonomica.pgp.PGPTools;
import net.cryptonomica.returns.BooleanWrapperObject;
import net.cryptonomica.returns.OnlineVerificationView;
import net.cryptonomica.returns.OnlineVerificationsList;
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
 * explore on: {sandbox-cryptonomica || cryptonomica-server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - on frontend API should be loaded in app.js - app.run()
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

    private static OnlineVerification loadOnlineVerification(String fingerprint) throws NotFoundException {

        OnlineVerification onlineVerification = ofy()
                .load()
                .key(
                        Key.create(OnlineVerification.class, fingerprint)
                )
                .now();

        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVeriication entity for fingerprint "
                    + fingerprint
                    + " does not exist in data base"
            );
        }

        return onlineVerification;
    }

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
    ) throws Exception {

        /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new BadRequestException("fingerprint is missing or invalid");
        }

        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(fingerprint);

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
            // if verification data were requested by key owner:
            if (requester.getUserId().equalsIgnoreCase(pgpPublicKeyData.getCryptonomicaUserId())) {
                onlineVerification = new OnlineVerification(pgpPublicKeyData);
                ofy()
                        .save()
                        .entity(onlineVerification)
                        .now();
            } else { // if requested by other users:
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
                verificationDocumentArrayList,
                pgpPublicKeyData.getUserID()
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

    /* --- Approve online verification (for Cryptonomica Compliance Officer)  */
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
            throw new NotFoundException("OnlineVerification entity for fingerprint "
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

        // pgpPublicKeyData.setOnlineVerificationFinished(Boolean.TRUE); // >> this should be made in StripePaymentsAPI
        if (onlineVerification.getNationality() == null) {
            throw new IllegalArgumentException("User nationality not provided");
        } else {
            pgpPublicKeyData.setNationality(onlineVerification.getNationality().toUpperCase());
        }

        if (onlineVerification.getBirthday() == null) {
            throw new IllegalArgumentException("User birthday not provided");
        } else {
            pgpPublicKeyData.setUserBirthday(onlineVerification.getBirthday());
        }

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
                                        // + "https://cryptonomica.net/#/onlineVerificationView/" + fingerprint + "\n"
                                        + "https://cryptonomica.net/#!/onlineVerificationView/" + fingerprint + "\n"
                                        + "(information is not public, you have to login with your google account "
                                        + onlineVerification.getUserEmail().getEmail()
                                        + ")\n"
                                        + "Should you have any questions, please, do not hesitate to contact us"
                                        + " via support@cryptonomica.net or telegram https://t.me/cryptonomicanet \n\n"
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

    @ApiMethod(
            name = "getWaitingForVerification",
            path = "getWaitingForVerification",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public OnlineVerificationsList getWaitingForVerification(final User googleUser) throws UnauthorizedException {

        /* >>>>> (!!!!!!) Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        OnlineVerificationsList result = new OnlineVerificationsList();

/*
To avoid having to scan the entire index table, the query mechanism relies on
all of a query's potential results being adjacent to one another in the index.
To satisfy this constraint, a single query may not use inequality comparisons
(LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, NOT_EQUAL)
on more than one property across all of its filters
[Source : https://cloud.google.com/appengine/docs/standard/java/datastore/query-restrictions ]
https://stackoverflow.com/questions/21001371/why-gae-datastore-not-support-multiple-inequality-filter-on-different-properties
*/
        List<OnlineVerification> onlineVerifications = ofy().load().type(OnlineVerification.class)
                .filter("onlineVerificationFinished", true)
                // .filter("onlineVerificationDataVerified", false) // < can be null
                .filter("onlineVerificationDataVerified !=", true)
                .list();

        result.setOnlineVerifications(
                new ArrayList<>(onlineVerifications)
        );

        result.setCounter(onlineVerifications.size());

        return result;

    } // end of:  public StatsView getStatsByDate(


    /* allows admin to change user name */
    @ApiMethod(
            name = "changeName",
            path = "changeName",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject changeName(
            final User googleUser,
            @Named("firstName") String firstName,
            @Named("lastName") String lastName,
            @Named("fingerprint") String fingerprint,
            final @Named("userID") String userID
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws Exception {

        /* --- Check authorization : CRYPTONOMICA OFFICER ONLY !!! */
        CryptonomicaUser admin = UserTools.ensureCryptonomicaOfficer(googleUser);

        // format input
        firstName = firstName.toLowerCase();
        lastName = lastName.toLowerCase();
        fingerprint = fingerprint.toUpperCase();

        LOG.warning("ChangeName request. Parameters: " + firstName + " " + lastName + " " + fingerprint + " " + userID);

        /* --- load entities */
        // 1
        CryptonomicaUser cryptonomicaUser = ofy()
                .load()
                .key(
                        Key.create(CryptonomicaUser.class, userID)
                )
                .now();
        String userEmail = cryptonomicaUser.getEmail().getEmail().toLowerCase();
        // 2
        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(fingerprint);
        // 3
        OnlineVerification onlineVerification = loadOnlineVerification(fingerprint);

        /* --- change entities*/
        // 1
        String oldFirstName = cryptonomicaUser.getFirstName();
        cryptonomicaUser.setFirstName(firstName);
        String oldLastName = cryptonomicaUser.getLastName();
        cryptonomicaUser.setLastName(lastName);
        // 2
        pgpPublicKeyData.setFirstName(firstName);
        pgpPublicKeyData.setLastName(lastName);
        // 3
        onlineVerification.setFirstName(firstName);
        onlineVerification.setLastName(lastName);

        /* --- record changes */
        CryptonomicaLog cryptonomicaLog = new CryptonomicaLog();
        cryptonomicaLog.setChangedBy(admin.getUserId());
        cryptonomicaLog.setChangedByEmail(admin.getEmail().getEmail());
        cryptonomicaLog.setUserWhoseDataIsChanged(cryptonomicaUser.getUserId());
        cryptonomicaLog.setUserWhoseDataIsChangedEmail(userEmail);
        String action =
                "name in profile changed from " + oldFirstName + " " + oldFirstName + " to " + firstName + " " + lastName;
        cryptonomicaLog.setAction(action);

        /* --- save entities */
        try {
            ofy().save().entity(cryptonomicaUser).now();
            ofy().save().entity(pgpPublicKeyData).now();
            ofy().save().entity(onlineVerification).now();
            ofy().save().entity(cryptonomicaLog).now();
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            throw new Exception("Error saving data to DB: " + e.getMessage());
        }

        /* --- send message to user */
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder
                .withUrl("/_ah/SendGridServlet")
                .param("email", userEmail)
                .param("emailCC", Constants.adminEmailAddress)
                .param("messageSubject", Constants.emailSubjectPrefix + "profile changes: name changed")
                .param("messageText",
                        "Dear " + pgpPublicKeyData.getFirstName() + " ,\n\n"
                                + "Following changes were made in your profile: \n"
                                + action + "\n\n"
                                + "Should you have any questions, please, do not hesitate to contact us via "
                                + Constants.supportEmailAddress
                                + " or telegram https://t.me/cryptonomicanet \n\n"
                                + "Best regards, \n\n"
                                + "Cryptonomica team\n\n"
                                + new Date().toString()
                                + "\n\n"
                                + "if you think it's wrong or it is an error, "
                                + "please write to" + Constants.supportEmailAddress + "\n\n"
                )
        );

        // create and return result object:
        BooleanWrapperObject result = new BooleanWrapperObject(true, action);
        return result;

    } // end of changeName() ;

    /* allows admin to add name on credit/debit card */
    @ApiMethod(
            name = "addNameOnCard",
            path = "addNameOnCard",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject addNameOnCard(
            final User googleUser,
            final @Named("nameOnCard") String nameOnCard,
            final @Named("fingerprint") String fingerprint
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws Exception {

        /* --- Check authorization : CRYPTONOMICA OFFICER ONLY !!! */
        CryptonomicaUser admin = UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- get PGPPublicKeyData object and make changes */
        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(fingerprint);
        pgpPublicKeyData.setNameOnCard(
                nameOnCard.toLowerCase()
        );

        /* --- record changes */
        CryptonomicaLog cryptonomicaLog = new CryptonomicaLog();
        cryptonomicaLog.setChangedBy(admin.getUserId());
        cryptonomicaLog.setChangedByEmail(admin.getEmail().getEmail().toLowerCase());
        String userEmail = pgpPublicKeyData.getUserEmail().getEmail().toLowerCase();
        cryptonomicaLog.setUserWhoseDataIsChanged(pgpPublicKeyData.getCryptonomicaUserId());
        cryptonomicaLog.setUserWhoseDataIsChangedEmail(userEmail);
        String action = "Name on card: " + nameOnCard.toLowerCase();
        cryptonomicaLog.setAction(action);

        /* --- save entities */
        try {
            ofy().save().entity(pgpPublicKeyData).now();
            ofy().save().entity(cryptonomicaLog).now();
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            throw new Exception("Error saving data to DB: " + e.getMessage());
        }

        /* --- send message to user */
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder
                .withUrl("/_ah/SendGridServlet")
                .param("email", userEmail)
                .param("emailCC", Constants.adminEmailAddress)
                .param("messageSubject", Constants.emailSubjectPrefix + "profile changes: payment method")
                .param("messageText",
                        "Dear " + pgpPublicKeyData.getFirstName() + ",\n\n"
                                + "Now you can pay for verification of your public key certificate " + pgpPublicKeyData.getKeyID()
                                + " with credit/debit card with the name '" + nameOnCard.toLowerCase() + "'\n\n"
                                + "Please go to \n"
                                + "https://cryptonomica.net/#!/onlineVerification/" + pgpPublicKeyData.getFingerprint() + "\n"
                                + "and continue verification\n\n"
                                + "Should you have any questions, please, do not hesitate to contact us via "
                                + Constants.supportEmailAddress
                                + " or telegram https://t.me/cryptonomicanet \n\n"
                                + "Best regards, \n\n"
                                + "Cryptonomica team\n\n"
                                + new Date().toString()
                                + "\n\n"
                                + "if you think it's wrong or it is an error, "
                                + "please write to" + Constants.supportEmailAddress + "\n\n"
                )
        );

        return new BooleanWrapperObject(true, action);
    } // end of addNameOnCard ;


    /* Allows admin to delete user profile.
     * For example if name in key is completely different from name in passport, or if user requested deletion of the
     * profile before key verification is finished.
     * */
    @ApiMethod(
            name = "deleteProfile",
            path = "deleteProfile",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject deleteProfile(
            final User googleUser,
            final @Named("userID") String userID,
            final @Named("reason") String reason
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws Exception {

        /* --- Check authorization : CRYPTONOMICA OFFICER ONLY !!! */
        CryptonomicaUser admin = UserTools.ensureCryptonomicaOfficer(googleUser);

        LOG.warning("Delete profile request request. Parameters: " + userID);

        CryptonomicaUser cryptonomicaUser = ofy()
                .load()
                .key(
                        Key.create(CryptonomicaUser.class, userID)
                )
                .now();

        String userEmail;
        if (cryptonomicaUser == null) {
            userEmail = "<email not found>";
        } else {
            userEmail = cryptonomicaUser.getEmail().getEmail();
        }

        /* ---- (1) Delete all OpenPGP public keys: */
        List<PGPPublicKeyData> userKeysList = ofy().load().type(PGPPublicKeyData.class).filter("cryptonomicaUserId", userID).list();
        if (userKeysList != null && !userKeysList.isEmpty()) {
            CryptonomicaLog cryptonomicaLogDeleteUserKeys = new CryptonomicaLog(
                    "all public keys of user :" + userEmail + " were deleted",
                    userID,
                    userEmail,
                    admin.getUserId(),
                    admin.getEmail().getEmail()
            );
            for (PGPPublicKeyData pgpPublicKeyData : userKeysList) {
                ofy().delete().entity(pgpPublicKeyData); // async
                cryptonomicaLogDeleteUserKeys.addChangedEntityStringRepresentation(
                        pgpPublicKeyData.toJSON()
                );
            }
            ofy().save().entity(cryptonomicaLogDeleteUserKeys); // async
        }

        /* --- (2) Delete all OnlineVerification entities */

        List<OnlineVerification> onlineVerificationList = ofy().load().type(OnlineVerification.class).filter("cryptonomicaUserId", userID).list();
        if (onlineVerificationList != null && !onlineVerificationList.isEmpty()) {
            CryptonomicaLog cryptonomicaLogDeleteOnlineVerifications = new CryptonomicaLog(
                    "all OnlineVerificationObjects of user :" + userEmail + " were deleted",
                    userID,
                    userEmail,
                    admin.getUserId(),
                    admin.getEmail().getEmail()
            );
            for (OnlineVerification onlineVerification : onlineVerificationList) {
                ofy().delete().entity(onlineVerification); // async
                cryptonomicaLogDeleteOnlineVerifications.addChangedEntityStringRepresentation(
                        onlineVerification.toJSON()
                );
            }
            ofy().save().entity(cryptonomicaLogDeleteOnlineVerifications); // async
        }

        /* --- (3) Delete profile */
        if (cryptonomicaUser != null) {

            CryptonomicaLog cryptonomicaLogDeleteUserProfile = new CryptonomicaLog(
                    "The user profile :" + userEmail + " was deleted",
                    userID,
                    userEmail,
                    admin.getUserId(),
                    admin.getEmail().getEmail()
            );
            cryptonomicaLogDeleteUserProfile.addChangedEntityStringRepresentation(
                    cryptonomicaUser.toJSON()
            );
            ofy().save().entity(cryptonomicaLogDeleteUserProfile); // async
            ofy().delete().key(Key.create(CryptonomicaUser.class, userID)); // async
        }

        /* ---- send message to user: */
        final Queue queue = QueueFactory.getDefaultQueue();
        if (userEmail == null || userEmail.equalsIgnoreCase("<email not found>")) {
            userEmail = Constants.adminEmailAddress;
        }
        queue.add(TaskOptions.Builder
                .withUrl("/_ah/SendGridServlet")
                .param("email", userEmail)
                .param("emailCC", Constants.adminEmailAddress)
                .param("messageSubject", Constants.emailSubjectPrefix + userID + " (" + userEmail + ") " + "was deleted")
                .param("messageText",
                        "Dear user, \n"
                                + "your profile " + userID + " (" + userEmail + ") was deleted from " + Constants.host + "\n\n"
                                + "Reason: \n"
                                + reason + "\n\n"
                                + "Should you have any questions, please, do not hesitate to contact us via "
                                + Constants.supportEmailAddress + " or telegram https://t.me/cryptonomicanet \n\n"
                                + "Best regards, \n\n"
                                + "Cryptonomica team\n\n"
                                + new Date().toString() + "\n\n"
                                + "if you think it's wrong or it is an error, please write to "
                                + Constants.supportEmailAddress + "\n\n"
                )
        );

        // create result object:
        BooleanWrapperObject result = new BooleanWrapperObject(
                true,
                "profile " + userID + " (" + userEmail + ") was deleted from " + Constants.host
        );
        return result;
    } // end of deleteProfile();

    @ApiMethod(
            name = "removeVideoWithMessage",
            path = "removeVideoWithMessage",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject removeVideoWithMessage(
            final User googleUser,
            final @Named("messageToUser") String messageToUser,
            final @Named("fingerprint") String fingerprint
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException {

        /* --- Check authorization : CRYPTONOMICA OFFICER ONLY !!! */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- Check input: */
        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new IllegalArgumentException("fingerprint is missing or invalid");
        }

        // Check if OnlineVerification entity exists:
        OnlineVerification onlineVerification = ofy()
                .load().key(Key.create(OnlineVerification.class, fingerprint)).now();
        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVerification entity for fingerprint "
                    + fingerprint
                    + " does not exist in data base"
            );
        } else if (onlineVerification.getOnlineVerificationDataVerified() != null
                && onlineVerification.getOnlineVerificationDataVerified()) {
            throw new BadRequestException("OnlineVerification already approved");
        } else if (onlineVerification.getVerificationVideoId() == null) {
            // >> temporary removed:
            // throw new BadRequestException("No video stored for this verification, nothing to remove");
        }

        onlineVerification.setOnlineVerificationFinished(Boolean.FALSE);
        onlineVerification.setVerificationVideoId(null);

        // save data to data store:
        ofy()
                .save()
                .entity(onlineVerification)
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
                                Constants.emailSubjectPrefix + onlineVerification.getKeyID() + " your video cannot be accepted "

                        )
                        .param("messageText",
                                "Dear "
                                        + onlineVerification.getFirstName().toUpperCase() + " ,\n\n"
                                        + "Video you uploaded for verification of public key certificate with fingerprint : \n"
                                        + fingerprint + "\n"
                                        + "cannot be accepted\n\n"
                                        + "Reason: \n"
                                        + messageToUser
                                        + "\n\n"
                                        + "Please go to \n"
                                        + Constants.host + "/#!/onlineVerification/" + fingerprint + "\n"
                                        + "and upload new video.\n\n"
                                        + "Should you have any questions, please, do not hesitate to contact us via "
                                        + Constants.supportEmailAddress + " or telegram https://t.me/cryptonomicanet \n\n"
                                        + "Best regards, \n\n"
                                        + "Cryptonomica team\n\n"
                                        + new Date().toString()
                                        + "\n\n"
                                        + "if you think it's wrong or it is an error, "
                                        + "please write to" + Constants.supportEmailAddress + "\n\n"
                        )
        );

        // create result object:
        BooleanWrapperObject result = new BooleanWrapperObject(true,
                "video removed"
        );

        return result;

    } // end of removeVideoWithMessage ;

}
