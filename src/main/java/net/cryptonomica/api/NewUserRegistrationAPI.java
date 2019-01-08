package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.Login;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.forms.NewUserRegistrationForm;
import net.cryptonomica.pgp.PGPTools;
import net.cryptonomica.returns.LoginView;
import net.cryptonomica.returns.NewUserRegistrationReturn;
import net.cryptonomica.returns.PGPPublicKeyGeneralView;
import net.cryptonomica.returns.UserProfileGeneralView;
import net.cryptonomica.service.UserTools;
import org.bouncycastle.openpgp.PGPPublicKey;

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

@Api(name = "newUserRegistrationAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "Cryptonomica User Registration API")
public class NewUserRegistrationAPI {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(NewUserRegistrationAPI.class.getName());

    @ApiMethod(
            name = "registerNewUser",
            path = "registerNewUser",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // creates a new cryptonomica user and saves him/her to database
    public NewUserRegistrationReturn registerNewUser(
            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final NewUserRegistrationForm newUserRegistrationForm
    ) throws Exception {
        /* --- Ensure 1) user login, 2) not already registered:: */
        UserTools.ensureNewCryptonomicaUser(googleUser);
        /* --- Check form:*/
        if (newUserRegistrationForm.getArmoredPublicPGPkeyBlock() == null
                || newUserRegistrationForm.getArmoredPublicPGPkeyBlock().length() == 0) {
            throw new Exception("ASCII-armored PGP public key can not be empty");
            // } else if (newUserRegistrationForm.getUserInfo() == null) {
            //    throw new Exception("Info can not be empty");
        } else if (newUserRegistrationForm.getBirthday() == null) {
            throw new Exception("Birthdate can not be empty");
        }

        /* --- user BirthDate */
        Date userBirthDate = newUserRegistrationForm.getBirthday();
        // TODO: add check

        /* --- create PGPPublicKey from armored PGP key block: */
        String userId = googleUser.getUserId();
        String armoredPublicPGPkeyBlock = newUserRegistrationForm.getArmoredPublicPGPkeyBlock();
        LOG.warning("[armoredPublicPGPkeyBlock]:");
        LOG.warning(
                armoredPublicPGPkeyBlock
        );
        PGPPublicKey pgpPublicKey = PGPTools.readPublicKeyFromString(armoredPublicPGPkeyBlock);

        // create PGPPublicKeyData (Entity in DS) from PGPPublicKey:
        PGPPublicKeyData pgpPublicKeyData = new PGPPublicKeyData(
                pgpPublicKey,
                armoredPublicPGPkeyBlock,
                userId);
        pgpPublicKeyData.setUserBirthday(userBirthDate);

        /* --- Check PGPPublic Key: */

        // --- check key creation date/time:
        Date creationTime = pgpPublicKey.getCreationTime();
        if (creationTime.after(new Date())) {
            throw new Exception("Invalid key creation Date/Time");
        }

        // -- bits size check:
        if (pgpPublicKeyData.getBitStrength() < 2048) {
            throw new Exception("Key Strength (bits size) should be min 2048 bits");
        }

        // -- email check:
        if (!pgpPublicKeyData.getUserEmail().getEmail().toLowerCase().equals(
                googleUser.getEmail().toLowerCase()
        )
                ) {
            throw new Exception("Email in the key's user ID should be the same as in account");
        }

        // -- key validity period check
        Integer validDays = pgpPublicKey.getValidDays();
        if (validDays > 366 * 2) {
            throw new Exception("This key valid for more than 2 years");
        } else if (validDays <= 0) { //
            throw new Exception("This key's validity term is incorrect");
        }

        // --- check for dublicates in DS:
        List<PGPPublicKeyData> duplicates = ofy().load()
                .type(PGPPublicKeyData.class)
                .filter("fingerprintStr", pgpPublicKeyData.getFingerprint())
                .list();
        if (!duplicates.isEmpty()) {
            throw new Exception("The key with fingerprint"
                    + pgpPublicKeyData.getFingerprint()
                    + "already registered");
        }

        // create CryptonomicaUser:
        CryptonomicaUser cryptonomicaUser = new CryptonomicaUser(
                googleUser,
                pgpPublicKeyData,
                newUserRegistrationForm);
        // save new user and his key
        Key<CryptonomicaUser> cryptonomicaUserKey = ofy()
                .save()
                .entity(cryptonomicaUser)
                .now();
        cryptonomicaUser = ofy() // ?
                .load()
                .key(cryptonomicaUserKey)
                .now();

        Key<PGPPublicKeyData> pgpPublicKeyDataKey = ofy()
                .save()
                .entity(pgpPublicKeyData)
                .now();
        pgpPublicKeyData = ofy()
                .load()
                .key(pgpPublicKeyDataKey)
                .now();
        //
        String userAction = "net.cryptonomica.api.NewUserRegistrationAPI.registerNewUser";
        Login login = UserTools.registerLogin(httpServletRequest, googleUser, userAction);
        //

        ArrayList<PGPPublicKeyData> pgpPublicKeyDataArrayList = new ArrayList<>();
        pgpPublicKeyDataArrayList.add(pgpPublicKeyData);

        UserProfileGeneralView userProfileGeneralView = new UserProfileGeneralView(
                cryptonomicaUser,
                pgpPublicKeyDataArrayList
        );
        userProfileGeneralView.setRegisteredCryptonomicaUser(Boolean.TRUE); // - for $rootScope.currentUser
        //
        String messageToUser;
        if (cryptonomicaUser != null) {
            messageToUser = "User created successful";
        } else {
            messageToUser = "Error creating new user";
        }
        NewUserRegistrationReturn result = new NewUserRegistrationReturn(
                messageToUser,
                new PGPPublicKeyGeneralView(pgpPublicKeyData),
                userProfileGeneralView,
                new LoginView(login)
        );
        // send an email to user:
        final Queue queue = QueueFactory.getDefaultQueue();
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email",
                                googleUser.getEmail()
                        )
                        .param("messageSubject",
                                "You are registered on Cryptonomica server")
                        .param("messageText",
                                "Congratulation! \n\n"
                                        + userProfileGeneralView.getFirstName().toUpperCase() + " "
                                        + userProfileGeneralView.getLastName().toUpperCase() + ",\n\n"
                                        + "You are registered on Cryptonomica server" + "\n\n"
                                        + "To verify your key online:" + "\n"
                                        + "1) go to 'My profile', 2) click on key ID and go to page with key data , "
                                        + "3) click green button 'Verify online' and follow instructions provided by web application"
                                        + "\n\n"
                                        + "Should you have any questions, please, do not hesitate to contact us"
                                        + " via support@cryptonomica.net or telegram https://t.me/cryptonomicanet \n\n"
                                        + "Best regards, \n\n"
                                        + "Cryptonomica team\n\n"
                                        + new Date().toString()
                                        + "\n\n"
                                        + "if you think it's wrong or it is an error, please write to admin@cryptonomica.net \n"
                        )
        );
        //
        return result;
    } // end registerNewUser @ApiMethod
}
