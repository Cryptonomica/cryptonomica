package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.*;
import net.cryptonomica.forms.AddNotaryForm;
import net.cryptonomica.forms.VerifyPGPPublicKeyForm;
import net.cryptonomica.pgp.PGPTools;
import net.cryptonomica.returns.*;
import net.cryptonomica.service.UserTools;
import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - API should be loaded in app.js - app.run()
 * * in this API:
 */
@Api(name = "notaryAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "API for Notary")
@SuppressWarnings("unused")
public class NotaryAPI {

    /* Logger */
    private static final Logger LOG = Logger.getLogger(NotaryAPI.class.getName());

    /* Printers */
    private static final DatePrinter DATE_PRINTER = FastDateFormat.getInstance("yyyy-MM-dd");
    private static final DatePrinter DATE_TIME_PRINTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss zzz");

    @ApiMethod(
            name = "addOrRewriteNotary",
            path = "addOrRewriteNotary",
            httpMethod = ApiMethod.HttpMethod.POST)
    @SuppressWarnings("unused")
    // add or rewrite "Notary" info to user.
    public NotaryGeneralView addOrRewriteNotary(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final AddNotaryForm addNotaryForm // TODO: add form verification (required fields not empty)
    ) throws Exception {
        LOG.warning("addNotaryForm: ");
        LOG.warning(new Gson().toJson(addNotaryForm));

        /* -- check authorization */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureNotaryOrCryptonomicaOfficer(googleUser);
        // Check if notary exist?
        // ? -- what happens if notary already exists? -- it will be overwritten -- but we do not loose existing
        // notary licenses and verifications, but loose references to them from the notary class
        // create and save notary licence from addNotaryForm

        /* verify addNotaryForm */
        if ((addNotaryForm.getNotaryInfo().length() < 1 || addNotaryForm.getNotaryInfo() == null)
                || (addNotaryForm.getLicenceIssuedBy().length() < 1 || addNotaryForm.getLicenceIssuedBy() == null)
                || (addNotaryForm.getLicenceCountry().length() < 1 || addNotaryForm.getLicenceCountry() == null)
                || (addNotaryForm.getLicenceIssuedOn() == null)
                || (addNotaryForm.getLicenceInfo() == null)
                || (addNotaryForm.getNotaryInfo().length() < 1 || addNotaryForm.getNotaryInfo() == null)
                ) {
            throw new Exception("Notary form is not complete or invalid");
        }

        /* create and store new NotaryLicence */
        NotaryLicence notaryLicence = new NotaryLicence(addNotaryForm);
        Key<NotaryLicence> notaryLicenceKey = ofy()
                .save() //
                .entity(notaryLicence)
                .now();
        notaryLicence = ofy()
                .load()
                .key(notaryLicenceKey)
                .now();
        LOG.warning("notaryLicence form DB: " + new Gson().toJson(notaryLicence));

        /* create and save new Verification */
        Verification verification = new Verification(
                addNotaryForm,
                cryptonomicaUser,
                notaryLicenceKey);

        Key<Verification> verificationKey = ofy()
                .save()
                .entity(verification)
                .now();

        verification = ofy()
                .load()
                .key(verificationKey)
                .now();

        LOG.warning("Verification from DB: " + new Gson().toJson(verification));

        // add Verification to notary licence and save license
        notaryLicence.setVerification(verificationKey);
        notaryLicenceKey = ofy()
                .save()
                .entity(notaryLicence)
                .now();
        notaryLicence = ofy()
                .load()
                .key(notaryLicenceKey)
                .now();

        /*  create and save new Notary  */
        Notary notary = new Notary(addNotaryForm, notaryLicenceKey);

        Key<Notary> notaryKey = ofy()
                .save()
                .entity(notary)
                .now();

        notary = ofy()
                .load()
                .key(notaryKey)
                .now();

        LOG.warning("Notary from DB: " + new Gson().toJson(notary));

        // load notary licenses list // TODO: modify connection Notary - NotaryLicence
        List<NotaryLicence> notaryLicenceList = ofy()
                .load()
                .type(NotaryLicence.class)
                .ancestor(
                        Key.create(CryptonomicaUser.class, notary.getId())
                )
                .list();
        // transform Notary licenses list to licenceGeneralView lis
        ArrayList<NotaryLicenceGeneralView> notaryLicenceGeneralViewArrayList = new ArrayList<>();
        // create verification list
        List<Verification> verificationList = new ArrayList<>();
        verificationList.add(verification);
        //
        notaryLicenceGeneralViewArrayList.add(new NotaryLicenceGeneralView(notaryLicence, verificationList));


        List<PGPPublicKeyData> pgpPublicKeyDataList = ofy().load().type(PGPPublicKeyData.class).ancestor(notary).list();
        ArrayList<PGPPublicKeyGeneralView> pgpPublicKeyGeneralViewArrayList = new ArrayList<>();

        // create ArrayList of PGP Public Keys representations:
        for (PGPPublicKeyData pgpPublicKeyData : pgpPublicKeyDataList) {
            pgpPublicKeyGeneralViewArrayList.add(new PGPPublicKeyGeneralView(pgpPublicKeyData));
        }
        LOG.warning("ArrayList<PGPPublicKeyGeneralView>: " + new Gson().toJson(pgpPublicKeyGeneralViewArrayList));
        // load Cryptonomica user from db
        CryptonomicaUser notaryCryptonomicaUser = ofy()
                .load()
                .key(
                        Key.create(CryptonomicaUser.class, addNotaryForm.getNotaryCryptonomicaUserID())
                ).now();
        LOG.warning("notaryCryptonomicaUser from DB: " + new Gson().toJson(notaryCryptonomicaUser));
        // set active user and set notary and save in DB:
        notaryCryptonomicaUser.setActive(Boolean.TRUE); // < TODO: connect to payment
        //
        Boolean alreadyWasANotary;
        if (notaryCryptonomicaUser.getNotary() != null && notaryCryptonomicaUser.getNotary()) {
            alreadyWasANotary = true;
        } else {
            alreadyWasANotary = false;
        }
        notaryCryptonomicaUser.setNotary(Boolean.TRUE);
        ofy().save().entity(notaryCryptonomicaUser);

        // transfer cryptonomica user data from DB to UserProfileGeneralView
        UserProfileGeneralView userProfileGeneralView = new UserProfileGeneralView(
                notaryCryptonomicaUser,
                pgpPublicKeyDataList
        );
        LOG.warning("UserProfileGeneralView: " + new Gson().toJson(userProfileGeneralView));

        NotaryGeneralView notaryGeneralView = new NotaryGeneralView(
                userProfileGeneralView,
                notary,
                notaryLicenceGeneralViewArrayList);

        LOG.warning("Notary General View to return: " + new Gson().toJson(notaryGeneralView));

        // send an email to notary:
        final Queue queue = QueueFactory.getDefaultQueue();

        if (alreadyWasANotary) {
            queue.add(
                    TaskOptions.Builder
                            .withUrl(
                                    "/_ah/SendGridServlet")
                            .param("email",
                                    notaryCryptonomicaUser.getEmail().getEmail()
                            )
                            .param("messageSubject",
                                    "Your notary data changed on Cryptonomica server"
                            )
                            .param("messageText",
                                    notaryCryptonomicaUser.getFirstName() + " " + notaryCryptonomicaUser.getLastName() + ", \n"
                                            + "On "
                                            + DATE_TIME_PRINTER.format(verification.getVerifiedOn())
                                            + " your notary data were changed on Cryptonomica server:\n\n"
                                            + "Notary info: \n" + notary.getInfo() + "\n"
                                            + "Notary country code: \n" + notaryLicence.getCountry() + "\n"
                                            + "Notary licence issued by: \n" + notaryLicence.getIssuedBy() + "\n"
                                            + "Notary licence issued on: \n" + DATE_PRINTER.format(notaryLicence.getIssuedOn()) + "\n"
                                            + "Information about your notary data verification: \n" + verification.getVerificationInfo() + "\n"
                                            + "\n"
                                            + "changes made by: \n"
                                            + cryptonomicaUser.getFirstName() + " " + cryptonomicaUser.getLastName() + "\n\n"
                                            + "if you think it's wrong or it is an error, please write to admin@cryptonomica.net \n"
                            )
            );
        } else {
            queue.add(
                    TaskOptions.Builder
                            .withUrl(
                                    "/_ah/SendGridServlet")
                            .param("email",
                                    notaryCryptonomicaUser.getEmail().getEmail()
                            )
                            .param("messageSubject",
                                    "You were authorized as a notary on Cryptonomica server"
                            )
                            .param("messageText",
                                    "CONGRATULATION! \n\n"
                                            + notaryCryptonomicaUser.getFirstName() + " " + notaryCryptonomicaUser.getLastName() + ", \n\n"
                                            + "You were authorized as a notary on Cryptonomica server!\n\n"

                                            + "Information about your notary data verification: \n\n"
                                            + "Notary info: \n\n" + notary.getInfo() + "\n\n"
                                            + "Notary country code: \n\n" + notaryLicence.getCountry() + "\n\n"
                                            + "Notary licence issued by: \n\n" + notaryLicence.getIssuedBy() + "\n\n"
                                            + "Notary licence issued on: \n" + DATE_PRINTER.format(notaryLicence.getIssuedOn()) + "\n\n"
                                            + "Information about your notary data verification: \n" + verification.getVerificationInfo() + "\n\n"

                                            + "Verification made by: \n\n"
                                            + cryptonomicaUser.getFirstName() + " " + cryptonomicaUser.getLastName()
                            )
            );
        }
        return notaryGeneralView;
    } // end @ApiMethod addOrRewriteNotary

    @ApiMethod(
            name = "verifyPGPPublicKey",
            path = "verifyPGPPublicKey",
            httpMethod = ApiMethod.HttpMethod.POST)
    @SuppressWarnings("unused")
    public VerifyPGPPublicKeyReturn verifyPGPPublicKey(
            final User verifyingPerson, // google user
            final VerifyPGPPublicKeyForm verifyPGPPublicKeyForm // 4 properties
    ) throws Exception {
        /* Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureNotaryOrCryptonomicaOfficer(verifyingPerson);

        /* Validate form (3 properties): */
        // 1)
        /*
        if (verifyPGPPublicKeyForm.getWebSafeString() == null
                || verifyPGPPublicKeyForm.getWebSafeString().length() < 1) {
            LOG.warning("VerifyPGPPublicKeyForm.webSafeString: "
                    + verifyPGPPublicKeyForm.getWebSafeString());
            throw new Exception("PGP public key not not identified");
        }
        */
        if (verifyPGPPublicKeyForm.getFingerprint() == null
                || verifyPGPPublicKeyForm.getFingerprint().length() < 1) {
            LOG.warning("VerifyPGPPublicKeyForm.getFingerprint(): "
                    + verifyPGPPublicKeyForm.getFingerprint());
            throw new Exception("Key fingerprint missing or empty");
        }
        // 2)
        if (verifyPGPPublicKeyForm.getVerificationInfo() == null
                || verifyPGPPublicKeyForm.getVerificationInfo().length() < 1) {
            LOG.warning("verifyPGPPublicKeyForm.verificationInfo: " +
                    verifyPGPPublicKeyForm.getVerificationInfo());
            throw new Exception("Public key verification info is missing");
        }
        // 3)
        if (verifyPGPPublicKeyForm.getBasedOnDocument() == null
                || verifyPGPPublicKeyForm.getBasedOnDocument().length() < 1) {
            throw new Exception("Document not specified");
        }

        /* Load PGPPublicKeyData */
        /*
        Key<PGPPublicKeyData> pgpPublicKeyDataKEY = Key.create(verifyPGPPublicKeyForm.getFingerprint());
        PGPPublicKeyData pgpPublicKeyData = ofy()
                .load()
                .key(pgpPublicKeyDataKEY)
                .now();
        */

        // GET Key from DataBase by fingerprint:
        String fingerprint = verifyPGPPublicKeyForm.getFingerprint();
        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(fingerprint);

        /* Check if key paid */
        if (pgpPublicKeyData.getPaid() == null || pgpPublicKeyData.getPaid() == Boolean.FALSE) {
            throw new Exception("This key is unpaid, and can not be verified. Please make a payment first.");
        }

        // create verification
        Key<PGPPublicKeyData> pgpPublicKeyDataKEY = Key.create(pgpPublicKeyData);
        // from:
        // http://static.javadoc.io/com.googlecode.objectify/objectify/5.1.12/com/googlecode/objectify/Key.html#create-T-
        // create
        //public static <T> Key<T> create(T pojo)
        // Create a key from a registered POJO entity.
        // TODO: check if works correctly
        Verification verification = new Verification(
                verifyPGPPublicKeyForm, // verification data
                cryptonomicaUser,       // verified by
                pgpPublicKeyDataKEY     // key of the entity to be verified
        );
        // save in DB and load verification to assign an ID (@Id private Long Id)
        Key<Verification> verificationKey = ofy()
                .save()
                .entity(verification)
                .now();
        // load saved entity from database:
        verification = ofy() //
                .load()
                .key(verificationKey)
                .now();
        LOG.warning("saved verification: " + verification.toJson());
        // add verification to PGPPublicKeyData obj, save it to DB and load from DB
        pgpPublicKeyData.setActive(Boolean.TRUE);
        pgpPublicKeyData.setVerified(Boolean.TRUE);
        pgpPublicKeyData.addVerification(verificationKey.toWebSafeString());
        ofy().save().entity(pgpPublicKeyData).now(); // sync - so we can show verified key instantly
        pgpPublicKeyData = ofy().load().key(pgpPublicKeyDataKEY).now(); // ensure we have info from DB (? )

        // get CryptonomicaUser obj for Key Owner (? what for? )
        Key<CryptonomicaUser> keyOwnerKEY = Key.create(CryptonomicaUser.class, pgpPublicKeyData.getCryptonomicaUserId());
        CryptonomicaUser keyOwner = ofy()
                .load()
                .key(keyOwnerKEY)
                .now();
        // set active, save to DB:
        keyOwner.setActive(Boolean.TRUE);
        ofy().save().entity(keyOwner); // async

        VerifyPGPPublicKeyReturn verifyPGPPublicKeyReturn = new VerifyPGPPublicKeyReturn(
                "verification saved",
                new VerificationGeneralView(verification),
                new PGPPublicKeyGeneralView(pgpPublicKeyData),
                new UserProfileGeneralView(keyOwner)
        );
        LOG.warning("verifyPGPPublicKeyReturn" + new Gson().toJson(verifyPGPPublicKeyReturn));

        /* Send an email to key owner: */
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email", // 1
                                keyOwner.getEmail().getEmail()
                        )
                        .param("emailCC", // 2
                                cryptonomicaUser.getEmail().getEmail()
                        )
                        .param("messageSubject", // 3
                                "Your public key " + pgpPublicKeyData.getKeyID() + " verified"
                        )
                        .param("messageText", // 4
                                "CONGRATULATION! \n\n"
                                        + keyOwner.getFirstName() + " " + keyOwner.getLastName() + ", \n\n"
                                        + "Your key: "
                                        + pgpPublicKeyData.getFingerprint()
                                        + " was verified on Cryptonomica server!\n\n"

                                        + "Information about key verification: \n\n"

                                        + "Verified by: " + cryptonomicaUser.getFirstName()
                                        + " " + cryptonomicaUser.getLastName() + "\n\n"
                                        + "Verified on: " + verification.getVerifiedOn() + "\n\n"
                                        + "Verification based on following document(s): "
                                        + verification.getBasedOnDocument() + "\n\n"
                                        + "Verification info: " + verification.getVerificationInfo() + "\n\n"
                                        + "\n\n"
                                        + "Best regards, \n\n"
                                        + "Cryptonomica team\n\n"
                                        + "if you think it's wrong or it is an error, please write to admin@cryptonomica.net \n"
                        )
        );
        /* ----end email sending */

        return verifyPGPPublicKeyReturn;

    } // end of verifyPGPPublicKey()
}
