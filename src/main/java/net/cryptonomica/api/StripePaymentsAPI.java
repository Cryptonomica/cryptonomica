package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.*;
import net.cryptonomica.forms.CreatePromoCodesForm;
import net.cryptonomica.forms.StripePaymentForm;
import net.cryptonomica.returns.*;
import net.cryptonomica.service.ApiKeysUtils;
import net.cryptonomica.service.UserTools;
import org.apache.commons.lang3.RandomStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries


/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 */
@Api(name = "stripePaymentsAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "payments with Stripe")
//
public class StripePaymentsAPI {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(StripePaymentsAPI.class.getName());

    /* --- Gson */
    private static final Gson GSON = new Gson();

    /* UTILITY FUNCTIONS */

    private static Integer applyDiscount(
            final Integer priceInCents,
            final Integer discountInPercent
    ) {
        Integer discount = (priceInCents / 100) * discountInPercent;

        Integer priceInCentsAfterDiscount = priceInCents - discount;
        if (priceInCentsAfterDiscount < 100) {
            priceInCentsAfterDiscount = 100;
        }
        return priceInCentsAfterDiscount;
    } // end of applyDiscount

    private static void invalidatePromoCode(
            final String promoCodeStr,
            final User googleUser,
            final String fingerprint
    ) {
        PromoCode promoCode = ofy()
                .load()
                .key(Key.create(PromoCode.class, promoCodeStr))
                .now();

        promoCode.setUsed(true);
        promoCode.setUsedBy(new Email(googleUser.getEmail()));
        promoCode.setUsedOn(new Date());
        promoCode.setUsedForFingerprint(fingerprint);

        ofy().save().entity(promoCode); // async without .now()

    } // end of invalidatePromoCode

    private Integer calculatePriceForKeyVerification(
            final PGPPublicKeyData pgpPublicKeyData,
            final OnlineVerification onlineVerification,
            final User googleUser
    ) throws Exception {

        /* basic prices: */

        Integer priceForOneYearInEUR = 60;
        Integer priceForOneYerInCents = priceForOneYearInEUR * 100;
        Integer discountInPercentForTwoYears = 20;
        Integer priceForTwoYearsInCents =
                (priceForOneYerInCents * 2) / 100 * (100 - discountInPercentForTwoYears);
        // basic price for 1 y: EUR 60
        // basic price for 2 y: EUR 96

        /* promo code */
        final String promoCodeStr = onlineVerification.getPromoCode();
        Integer discountInPercent = 0;
        if (promoCodeStr != null && !promoCodeStr.isEmpty()) {
            PromoCode promoCode = ofy()
                    .load()
                    .key(Key.create(PromoCode.class, promoCodeStr))
                    .now();
            if (promoCode == null) {
                throw new Exception("promo code is not valid");
            }
            if (promoCode.getUsed()) {
                throw new Exception("promo code is already used");
            }
            if (promoCode.getValidUntil() != null && promoCode.getValidUntil().after(new Date())) {
                throw new Exception("promo code expired");
            }
            if (promoCode.getValidOnlyForUsers() != null && !promoCode.getValidOnlyForUsers().isEmpty()) {
                if (googleUser == null || googleUser.getEmail() == null || googleUser.getEmail().isEmpty()) {
                    throw new Exception("server was unable to identify the user");
                }
                if (!promoCode.getValidOnlyForUsers().contains(new Email(googleUser.getEmail()))) {
                    throw new Exception("promo code is not valid for this user");
                }
            }

            //
            if (promoCode.getValidOnlyForCountries() != null && !promoCode.getValidOnlyForCountries().isEmpty()) {
                if (!promoCode.getValidOnlyForCountries().contains(onlineVerification.getNationality())) {
                    throw new Exception("promo code not valid for your country");
                }
            }

            if (promoCode.getDiscountInPercent() != null && promoCode.getDiscountInPercent() > 0) {
                discountInPercent = promoCode.getDiscountInPercent();
            }
        }

        /* calculate price */

        Integer priceInCents = null;
        Date today = new Date();
        // Calculating the difference between two Java date instances:
        // http://stackoverflow.com/a/3491723/1697878
        int diffInDays = (int) ((pgpPublicKeyData.getExp().getTime() - today.getTime())
                / (1000 * 60 * 60 * 24));

        LOG.warning("Today: " + today
                + "; Key exp.: " + pgpPublicKeyData.getExp()
                + "; diff in days: " + diffInDays
        );

        if (diffInDays > 365) {
            priceInCents = priceForOneYerInCents; // in EUR cents
        } else {
            priceInCents = priceForTwoYearsInCents; // in EUR cents
        }

        if (discountInPercent != null && discountInPercent > 0) {
            priceInCents = applyDiscount(priceInCents, discountInPercent);
        }

        if (priceInCents == null || priceInCents < 100) {
            throw new Exception("price calculation error");
        }

        LOG.warning("price: EUR " + priceInCents);

        return priceInCents;
    } // end of calculatePriceForKeyVerification

    /* API METHODS */

    @ApiMethod(
            name = "getPriceForKeyVerification",
            path = "getPriceForKeyVerification",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public IntegerWrapperObject getPriceForKeyVerification(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("fingerprint") String fingerprint,
            final @Named("promoCode") String promoCode
    )
            throws Exception {

        /* --- Ensure cryptonomica registered user */
        final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- record login: */
        Login login = UserTools.registerLogin(httpServletRequest, googleUser);

        /* --- create return object */
        IntegerWrapperObject result = new IntegerWrapperObject();

        /* --- get OpenPGP key data */
        PGPPublicKeyData pgpPublicKeyData = null;
        pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint)
                .first()
                .now();
        //
        if (pgpPublicKeyData == null) {
            throw new Exception("OpenPGP public key certificate not found in our database for " + fingerprint);
        }

        // get OnlineVerification object
        OnlineVerification onlineVerification = ofy()
                .load()
                .key(Key.create(OnlineVerification.class, fingerprint))
                .now();
        if (onlineVerification == null) {
            LOG.severe("OnlineVerification record not found in data base");
            throw new Exception("online verification record not found in data base, "
                    + "please start online verification from the beginning");
        }

        if (promoCode != null && !promoCode.isEmpty()) {
            onlineVerification.setPromoCode(promoCode);
        }

        // calculate price for key verification
        // TODO: implement promo code
        Integer priceInCents = calculatePriceForKeyVerification(
                pgpPublicKeyData,
                onlineVerification,
                googleUser
        );

        result.setNumber(priceInCents);

        /*
        StripePaymentForKeyVerification stripePaymentForKeyVerification = ofy()
                .load()
                .key(Key.create(StripePaymentForKeyVerification.class, fingerprint))
                .now();
        */

        LOG.warning("result: " + GSON.toJson(result));

        return result;

    } // end of getPriceForKeyVerification()

    @ApiMethod(
            name = "processStripePayment",
            path = "processStripePayment",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StripePaymentReturn processStripePayment(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final StripePaymentForm stripePaymentForm
    ) throws Exception {

        // Set your secret key: remember to change this to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/account/apikeys
        // final String STRIPE_SECRET_KEY = ApiKeysUtils.getApiKey("StripeTestSecretKey");
        final String STRIPE_SECRET_KEY = ApiKeysUtils.getApiKey("StripeLiveSecretKey");

        /* --- Ensure cryptonomica registered user */
        final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- record user login */
        Login login = UserTools.registerLogin(httpServletRequest, googleUser);
        LOG.warning(GSON.toJson(new LoginView(login)));

        // log info:
        LOG.warning("cryptonomicaUser: " + cryptonomicaUser.getEmail().getEmail());
        LOG.warning("key fingerprint: " + stripePaymentForm.getFingerprint());

        /* --- log form: */
        /* !!!!!!!!!!!!!!!!!!! comment in production !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
        // LOG.warning(GSON.toJson(stripePaymentForm)); // <<<<<<<<<<<<<<<<<<<<<<<!!!!!!!!!!!!!!!!!!!!!!!
        // Do not save card data in DB or to logs when working with real (non-test) cards numbers !!!!!!!

        /* --- create return object */
        StripePaymentReturn stripePaymentReturn = new StripePaymentReturn();

        /* --- get and check OpenPGP key data */
        final String fingerprint = stripePaymentForm.getFingerprint();
        PGPPublicKeyData pgpPublicKeyData = null;
        pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint)
                .first()
                .now();
        if (pgpPublicKeyData == null) {
            throw new Exception("OpenPGP public key certificate not found in our database for " + fingerprint);
        }
        if (pgpPublicKeyData.getPaid() != null && pgpPublicKeyData.getPaid()) { // <<<
            throw new Exception("Verification of key " + fingerprint + " already paid");
        }

        /* --- get and check OnlineVerification object */
        OnlineVerification onlineVerification = ofy()
                .load()
                .key(Key.create(OnlineVerification.class, fingerprint))
                .now();
        if (onlineVerification == null) {
            LOG.severe("OnlineVerification record not found in data base");
            throw new Exception("online verification record not found in data base, "
                    + "please start online verification from the beginning");
        }

        /* --- check name on card */
        String nameOnCard;
        if (pgpPublicKeyData.getNameOnCard() != null) {
            nameOnCard = pgpPublicKeyData.getNameOnCard();
        } else if (
                !stripePaymentForm.getCardHolderFirstName().equalsIgnoreCase(pgpPublicKeyData.getFirstName())
                        || !stripePaymentForm.getCardHolderLastName().equalsIgnoreCase(pgpPublicKeyData.getLastName())
                ) {
            throw new Exception(
                    "You have to pay with credit card with the same first and last name as in your OpenPGP key."
                            + " If your card has different spelling of your name than passport,"
                            + " please write to support@cryptonomica.net "
                            + "(include spelling of your name in passport and on the card,"
                            + " but do not send card number or CVC via email)"
            );
        } else {
            nameOnCard = pgpPublicKeyData.getFirstName() + " " + pgpPublicKeyData.getLastName();
        }

        /* --- Check promo code and calculate price  */
        final String promoCode = stripePaymentForm.getPromoCode();
        if (promoCode != null && !promoCode.isEmpty()) {
            onlineVerification.setPromoCode(promoCode);
        }
        Integer priceInCents = calculatePriceForKeyVerification(
                pgpPublicKeyData,
                onlineVerification,
                googleUser
        );

        /* --- make code for payment (chargeCode) :*/
        final String chargeCode = RandomStringUtils.randomNumeric(7);
        LOG.warning("chargeCode: " + chargeCode); //


        /* --- process payment */

        // see example on:
        // https://github.com/stripe/stripe-java#usage
        RequestOptions requestOptions =
                (new RequestOptions.RequestOptionsBuilder())
                        .setApiKey(STRIPE_SECRET_KEY)
                        .build();
        // --- cardMap
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("number", stripePaymentForm.getCardNumber()); // String
        cardMap.put("exp_month", stripePaymentForm.getCardExpMonth()); // Integer
        cardMap.put("exp_year", stripePaymentForm.getCardExpYear()); // Integer
        cardMap.put("name", nameOnCard);
        //  --- chargeMap
        Map<String, Object> chargeMap = new HashMap<>();
        chargeMap.put("card", cardMap);

        //  amount - a positive integer in the smallest currency unit (e.g., 100 cents to charge $1.00
        chargeMap.put("amount", priceInCents); //

        // chargeMap.put("currency", "usd");
        chargeMap.put("currency", "eur");
        //
        // https://stripe.com/docs/api/java#create_charge-statement_descriptor
        // An arbitrary string to be displayed on your customer's credit card statement.
        // This may be up to 22 characters. As an example, if your website is RunClub and the item you're charging for
        // is a race ticket, you may want to specify a statement_descriptor of RunClub 5K race ticket.
        // The statement description may not include <>"' characters, and will appear on your customer's statement in
        // capital letters. Non-ASCII characters are automatically stripped.
        // While most banks display this information consistently, some may display it incorrectly or not at all.
        chargeMap.put(
                "statement_descriptor",
                chargeCode     // 7 characters
                        + " CRYPTONOMICA "         // 13 characters
        );
        // https://stripe.com/docs/api/java#create_charge-description
        // An arbitrary string which you can attach to a charge object.
        // It is displayed when in the web interface alongside the charge.
        // Note that if you use Stripe to send automatic email receipts to your customers,
        // your receipt emails will include the description of the charge(s) that they are describing.
        chargeMap.put("description", "Key " + pgpPublicKeyData.getKeyID() + " verification");
        // https://stripe.com/docs/api/java#create_charge-receipt_email
        // The email address to send this charge's receipt to.
        // The receipt will not be sent until the charge is paid.
        // If this charge is for a customer, the email address specified here will override the customer's email address.
        // Receipts will not be sent for test mode charges.
        // If receipt_email is specified for a charge in live mode, a receipt will be sent regardless of your email settings.
        chargeMap.put("receipt_email", cryptonomicaUser.getEmail().getEmail());

        // -- get Charge object:
        Charge charge = Charge.create(chargeMap, requestOptions);

        String chargeStr = charge.toString(); // Charge obj has custom toString()
        LOG.warning("chargeStr: " + chargeStr);
        String chargeJsonStr = GSON.toJson(charge);
        LOG.warning("chargeJsonStr: " + chargeJsonStr);

        if (charge.getStatus().equalsIgnoreCase("succeeded")) {
            stripePaymentReturn.setResult(true);
            stripePaymentReturn.setMessageToUser("Payment succeeded");
        } else {
            stripePaymentReturn.setResult(false);
            stripePaymentReturn.setMessageToUser("Payment not succeeded");
        }

        /* --- invalidate promo code */

        invalidatePromoCode(promoCode, googleUser, fingerprint);

        /* --- create and store payment information*/

        StripePaymentForKeyVerification stripePaymentForKeyVerification = new StripePaymentForKeyVerification();
        stripePaymentForKeyVerification.setChargeId(charge.getId());
        stripePaymentForKeyVerification.setChargeJsonStr(chargeJsonStr);
        stripePaymentForKeyVerification.setChargeStr(chargeStr);
        stripePaymentForKeyVerification.setFingerprint(fingerprint);
        stripePaymentForKeyVerification.setCryptonomicaUserId(cryptonomicaUser.getUserId());
        stripePaymentForKeyVerification.setUserEmail(cryptonomicaUser.getEmail());
        stripePaymentForKeyVerification.setPaymentVerificationCode(chargeCode);
        stripePaymentForKeyVerification.setChargeLoginEntityId(login.getId());
        stripePaymentForKeyVerification.setPromoCodeUsed(promoCode);

        Key<StripePaymentForKeyVerification> entityKey
                = ofy().save().entity(stripePaymentForKeyVerification).now();

        onlineVerification.setPaymentMade(Boolean.TRUE);
        onlineVerification.setStripePaymentForKeyVerificationId(stripePaymentForKeyVerification.getId());

        ofy().save().entity(onlineVerification).now(); //

        return stripePaymentReturn;

    } // end of processStripePayment

    @ApiMethod(
            name = "checkPaymentVerificationCode",
            path = "checkPaymentVerificationCode",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject checkPaymentVerificationCode(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("fingerprint") String fingerprint,
            final @Named("paymentVerificationCode") String paymentVerificationCode
    )
            throws Exception {

        /* --- Ensure cryptonomica registered user */
        final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* record login: */
        Login login = UserTools.registerLogin(httpServletRequest, googleUser);

        /* --- Check form: */
        LOG.warning("paymentVerificationCode from user: " + paymentVerificationCode);

        /* --- create return object */
        StringWrapperObject result = new StringWrapperObject();

        /* --- get OpenPGP key data */
        PGPPublicKeyData pgpPublicKeyData = null;
        pgpPublicKeyData = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", fingerprint)
                .first()
                .now();
        //
        if (pgpPublicKeyData == null) {
            throw new Exception("OpenPGP public key certificate not found in our database for " + fingerprint);
        }
        //
        if (!pgpPublicKeyData.getUserEmail().getEmail().equalsIgnoreCase(cryptonomicaUser.getEmail().getEmail())) {
            throw new Exception("Google Account email and email in key certificate does not much");
        }
        // first check for null:
        if (pgpPublicKeyData.getPaid() != null && pgpPublicKeyData.getPaid()) {
            throw new Exception("Verification of key " + fingerprint + " already paid");
        }
        //
        StripePaymentForKeyVerification stripePaymentForKeyVerification = null;
        List<StripePaymentForKeyVerification> paymentForKeyVerificationList = ofy()
                .load()
                .type(StripePaymentForKeyVerification.class)
                .filter("fingerprint", fingerprint)
                .list();
        if (paymentForKeyVerificationList == null || paymentForKeyVerificationList.size() < 1) {
            throw new Exception("No payments for verification of the key " + fingerprint + "found");
        } else if (paymentForKeyVerificationList.size() > 1) {
            throw new Exception("Multiple payments exist for the key: " + fingerprint
                    + ", please write to support@cryptonomica.net");
        } else {
            stripePaymentForKeyVerification = paymentForKeyVerificationList.get(0);
        }

        OnlineVerification onlineVerification = ofy()
                .load()
                .key(Key.create(OnlineVerification.class, fingerprint))
                .now();

        if (onlineVerification == null) {
            throw new NotFoundException("OnlineVerification record not found in data base");
        }

        if (stripePaymentForKeyVerification.getPaymentVerificationCode()
                .equalsIgnoreCase(paymentVerificationCode)) {

            stripePaymentForKeyVerification.setVerified(true);

            stripePaymentForKeyVerification.setCheckPaymentVerificationCodeLoginEntityId(login.getId());
            ofy().save().entity(stripePaymentForKeyVerification); // async

            // add data to PGP Public Key and save:
            pgpPublicKeyData.setPaid(true);
            ofy().save().entity(pgpPublicKeyData).now();

            // add data to OnlineVerification and save:
            // onlineVerification.setPaimentVerified(Boolean.TRUE);
            onlineVerification.setPaymentVerified(Boolean.TRUE);
            onlineVerification.setOnlineVerificationFinished(Boolean.TRUE); // << ready to manual review
            ofy().save().entity(onlineVerification); // async
            result.setMessage("Code verified!");
        } else {
            stripePaymentForKeyVerification.setFailedVerificationAttemps(
                    stripePaymentForKeyVerification.getFailedVerificationAttemps() + 1
            );
            if (stripePaymentForKeyVerification.getFailedVerificationAttemps() >= 5) {
                throw new Exception("The number of attempts is exhausted."
                        + " Please, write to support@cryptonomica.net");
            } else {
                throw new Exception("Code does not much. It was attempt # "
                        + stripePaymentForKeyVerification.getFailedVerificationAttemps());
            }
        }

        // On this step user completed entering verification data, send email to compliance:
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

        /* --- send email to cryptonomica compliance officers and to user  */

        final Queue queue = QueueFactory.getDefaultQueue();

        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email",
                                "verification@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "[verification] Request for online verification: "
                                        + onlineVerificationView.getUserEmail()
                        )
                        .param("messageText",
                                "New request for online verification received: \n\n"
                                        + "see entered data on:\n"
                                        + "https://cryptonomica.net/#/onlineVerificationView/"
                                        + fingerprint + "\n\n"
                                        + "verification request data in JSON format: \n\n"
                                        + prettyGson.toJson(onlineVerificationView)
                        )
        );

        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email", onlineVerificationView.getUserEmail())
                        .param("messageSubject",
                                "[cryptonomica] Your request for online verification")
                        .param("messageText",

                                "Your request for online verification received: \n\n"
                                        + "see entered data on:\n"
                                        + "https://cryptonomica.net/#/onlineVerificationView/"
                                        + fingerprint + "\n\n"

                                        + "You entered all required data. Please wait for data verification by our compliance officer.\n\n"
                                        + "\n\n"
                                        + "Best regards, \n\n"
                                        + "Cryptonomica team\n\n"
                                        + new Date().toString()
                                        + "\n\n"
                                        + "if you think it's wrong or it is an error, please write to admin@cryptonomica.net \n"
                        )
        );

        return result;

    } // end of paymentVerificationCode

    @ApiMethod(
            name = "createPromoCodes",
            path = "createPromoCodes",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public CreatePromoCodesReturn createPromoCodes(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final CreatePromoCodesForm createPromoCodesForm
    ) throws Exception {

        final CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        CreatePromoCodesReturn createPromoCodesReturn = new CreatePromoCodesReturn();

        Integer numberOfPromoCodesToCreate = createPromoCodesForm.getNumberOfPromoCodesToCreate();
        if (numberOfPromoCodesToCreate == null || numberOfPromoCodesToCreate <= 0) {
            numberOfPromoCodesToCreate = 1;
        }
        for (int i = 1; i < numberOfPromoCodesToCreate; i++) {
            PromoCode promoCode = new PromoCode();
            ofy().save().entity(promoCode); // async
            createPromoCodesReturn.addPromoCode(promoCode);
        }

        return createPromoCodesReturn;

    } // end of createPromoCodes()

}
