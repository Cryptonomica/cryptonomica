package net.cryptonomica.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.i18n.phonenumbers.NumberParseException;
import com.googlecode.objectify.Key;
import com.twilio.sdk.TwilioRestException;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.OnlineVerification;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.entities.TestEntity;
import net.cryptonomica.ethereum.TestContract;
import net.cryptonomica.ethereum.Web3jFactory;
import net.cryptonomica.returns.IntegerWrapperObject;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.returns.Web3jTransactionReceiptWrapper;
import net.cryptonomica.service.TwilioUtils;
import net.cryptonomica.service.UserTools;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;


/**
 * explore on: {sandbox-cryptonomica || cryptonomica-server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - on frontend API should be loaded in app.js - app.run()
 */
// [START echo_api_annotation]
@Api(
        name = "testAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        description = "For testing",
        namespace =
        @ApiNamespace(
                ownerDomain = "cryptonomica-server.appspot.com",
                ownerName = "cryptonomica-server.appspot.com",
                packagePath = ""
        ),
        // [START_EXCLUDE]
        issuers = {
                @ApiIssuer(
                        name = "firebase", // TODO: ?
                        issuer = "https://securetoken.google.com/cryptonomica-server",
                        jwksUri = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com")
        }
        // [END_EXCLUDE]
)
// [END echo_api_annotation]
public class TestAPI {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(TestAPI.class.getName());
    /* --- Gson: */
    private static final Gson GSON = new Gson();

    /**
     * Echoes the received message back. If n is a non-negative integer, the message is copied that
     * many times in the returned message.
     * <p>
     * Note that name is specified and will override the default name of "{class name}.{method
     * name}". For example, the default is "echo.echo".
     * <p>
     * Note that httpMethod is not specified. This will default to a reasonable HTTP method
     * depending on the API method name. In this case, the HTTP method will default to POST.
     */
    // [START echo_method]
    @ApiMethod(name = "echo")
    public Message echo(Message message, @Named("n") @Nullable Integer n) {
        return doEcho(message, n);
    }
    // [END echo_method]

    /**
     * Echoes the received message back. If n is a non-negative integer, the message is copied that
     * many times in the returned message.
     * <p>
     * Note that name is specified and will override the default name of "{class name}.{method
     * name}". For example, the default is "echo.echo".
     * <p>
     * Note that httpMethod is not specified. This will default to a reasonable HTTP method
     * depending on the API method name. In this case, the HTTP method will default to POST.
     */
    // [START echo_path]
    @SuppressWarnings("unused")
    @ApiMethod(name = "echo_path_parameter", path = "echo/{n}")
    public Message echoPathParameter(Message message, @Named("n") int n) {
        return doEcho(message, n);
    }
    // [END echo_path]

    /**
     * Echoes the received message back. If n is a non-negative integer, the message is copied that
     * many times in the returned message.
     * <p>
     * Note that name is specified and will override the default name of "{class name}.{method
     * name}". For example, the default is "echo.echo".
     * <p>
     * Note that httpMethod is not specified. This will default to a reasonable HTTP method
     * depending on the API method name. In this case, the HTTP method will default to POST.
     */
    // [START echo_api_key]
    @SuppressWarnings("unused")
    @ApiMethod(name = "echo_api_key", path = "echo_api_key", apiKeyRequired = AnnotationBoolean.TRUE)
    public Message echoApiKey(Message message,
                              @Named("n") @Nullable Integer n
    ) {
        return doEcho(message, n);
    }
    // [END echo_api_key]

    private Message doEcho(Message message, Integer n) {
        if (n != null && n >= 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(message.getMessage());
            }
            message.setMessage(sb.toString());
        }
        return message;
    }

    @ApiMethod(
            name = "sendTestEmail",
            path = "sendTestEmail",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // creates a new cryptonomica user and saves him/her to database
    public Message sendTestEmail(
            final HttpServletRequest httpServletRequest,
            final com.google.appengine.api.users.User googleUser,
            final @Named("email") String email,
            final @Named("message") String message

    ) throws Exception {

        /* --- (!!!) only cryptonomica officers  */
        UserTools.ensureCryptonomicaOfficer(googleUser);

        // send an email to user:
        final Queue queue = QueueFactory.getDefaultQueue();
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email",
                                email
                        )
                        .param("messageSubject",
                                "test message from Cryptonomica")
                        .param("messageText",
                                "[this is a test message from Cryptonomica] \n\n"
                                        + message
                                        + "\n\n"
                                        + "if you think it's wrong or it is an error, please write to admin@cryptonomica.net \n"
                        )
        );

        return new Message(message);
    } // end


    /* --- Test SMS service: */
    @ApiMethod(
            name = "sendTestSms",
            path = "sendTestSms",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject sendTestSms(
            // final HttpServletRequest httpServletRequest,
            final com.google.appengine.api.users.User googleUser,
            final @Named("phoneNumber") String phoneNumber,
            final @Named("smsMessage") String smsMessage
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws UnauthorizedException, BadRequestException, NotFoundException, NumberParseException,
            IllegalArgumentException, TwilioRestException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        /* --- Send SMS */
        com.twilio.sdk.resource.instance.Message message = TwilioUtils.sendSms(phoneNumber, smsMessage);

        return new StringWrapperObject(message.toJSON());

    } // end of sendTestSms();

    /**
     * Gets the authenticated user's email. If the user is not authenticated, this will return an HTTP
     * 401.
     * <p>
     * Note that name is not specified. This will default to "{class name}.{method name}". For
     * example, the default is "echo.getUserEmail".
     * <p>
     * Note that httpMethod is not required here. Without httpMethod, this will default to GET due
     * to the API method name. httpMethod is added here for example purposes.
     */
    // [START google_id_token_auth]
    @SuppressWarnings("unused")
    @ApiMethod(
            name = "getUserEmail",
            path = "getUserEmail",
            httpMethod = ApiMethod.HttpMethod.GET
            // authenticators = {EspAuthenticator.class} // <<< causes (user == null) in APIs Explorer authorized requests
            // audiences = {"YOUR_OAUTH_CLIENT_ID"},
            // clientIds = {"YOUR_OAUTH_CLIENT_ID"}
    )
    public Email getUserEmail(final User user) throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }
        Email response = new Email();
        response.setEmail(user.getEmail());
        return response;
    }
    // [END google_id_token_auth]

    //=========== TestEntity:
    @SuppressWarnings("unused")
    @ApiMethod(
            name = "createTestEntity",
            path = "createTestEntity",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public TestEntity createTestEntity(
            final User googleUser,
            final @Named("entityName") String entityName,
            final @Named("booleanProperty") @Nullable Boolean booleanProperty,
            final @Named("stringProperty") @Nullable String stringProperty,
            final @Named("dateProperty") @Nullable Date dateProperty // enter in format: 2018-09-13
            /*
            POST https://cryptonomica-server.appspot.com/_ah/api/testAPI/v1/createTestEntity?entityName=testEntity1&booleanProperty=true&stringProperty=some+text&dateProperty=2015-07-11
            * */
    ) throws UnauthorizedException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        TestEntity testEntity = new TestEntity();
        //
        testEntity.setEntityCreatedBy(googleUser);
        testEntity.setEntityCreatedOn(new Date());

        LOG.warning(testEntity.getEntityCreatedOn().toString());
        LOG.warning(testEntity.getEntityCreatedOnYear().toString());
        LOG.warning(testEntity.getEntityCreatedOnMonth().toString());
        LOG.warning(testEntity.getEntityCreatedOnDay().toString());
        //
        testEntity.setEntityName(entityName);
        testEntity.setBooleanProperty(booleanProperty);
        testEntity.setStringProperty(stringProperty);
        testEntity.setDateProperty(dateProperty);

        ofy().save().entity(testEntity).now();

        Key<TestEntity> testEntityKey = Key.create(TestEntity.class, entityName);
        TestEntity response = ofy().load().key(testEntityKey).now();

        LOG.warning(GSON.toJson(response));

        return response;
        /*
        {
            "entityName": "testEntity1",
                "booleanProperty": true,
                "stringProperty": "some text",
                "dateProperty": "2015-07-11T00:00:00.000Z",
                "entityCreatedOn": "2018-09-13T03:24:27.705Z",
                "entityCreatedBy": {
                "email": "user@gmail.com",
                    "authDomain": "gmail.com",
                    "userId": "1234567890....",
                    "nickname": "user@gmail.com"
        }
        */
    }

    @SuppressWarnings("unused")
    @ApiMethod(
            name = "findTestEntitiesByDate",
            path = "findTestEntitiesByDate",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public ArrayList<TestEntity> findTestEntitiesByDate(
            final User googleUser,
//            final @Named("entityName") String entityName,
            final @Named("year") Integer year,
            final @Named("month") @Nullable Integer month,
            final @Named("day") @Nullable Integer day
            /*
             * */
    ) throws UnauthorizedException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        List<TestEntity> listOfEntities = null;
        if (month == null && day == null) {
            listOfEntities = ofy().load().type(TestEntity.class).filter("entityCreatedOnYear ==", year).list();

        } else if (day == null) {
            listOfEntities = ofy().load().type(TestEntity.class)
                    .filter("entityCreatedOnYear ==", year)
                    .filter("entityCreatedOnMonth ==", month)
                    .list();
        } else {
            listOfEntities = ofy().load().type(TestEntity.class)
                    .filter("entityCreatedOnYear ==", year)
                    .filter("entityCreatedOnMonth ==", month)
                    .filter("entityCreatedOnDay ==", day)
                    .list();
        }
        return new ArrayList<>(listOfEntities);
    }

    @SuppressWarnings("unused")
    @ApiMethod(
            name = "countTestEntitiesByDate",
            path = "countTestEntitiesByDate",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public IntegerWrapperObject countTestEntitiesByDate(
            final User googleUser,
//            final @Named("entityName") String entityName,
            final @Named("year") Integer year,
            final @Named("month") @Nullable Integer month,
            final @Named("day") @Nullable Integer day
            /*
             * */
    ) throws UnauthorizedException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        Integer result = null;
        if (month == null && day == null) {
            result = ofy().load().type(TestEntity.class).filter("entityCreatedOnYear ==", year).count();

        } else if (day == null) {
            result = ofy().load().type(TestEntity.class)
                    .filter("entityCreatedOnYear ==", year)
                    .filter("entityCreatedOnMonth ==", month)
                    .count();
        } else {
            result = ofy().load().type(TestEntity.class)
                    .filter("entityCreatedOnYear ==", year)
                    .filter("entityCreatedOnMonth ==", month)
                    .filter("entityCreatedOnDay ==", day)
                    .count();
        }
        return new IntegerWrapperObject(result);
    }

    @SuppressWarnings("unused")
    @ApiMethod(
            name = "testResponse",
            path = "testResponse",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    // HashMap<String, Integer> as response works!!! (endpoints.framework.version: 2.0.9)
    public HashMap<String, Integer> testResponse(
            // Injected types: (see: https://cloud.google.com/endpoints/docs/frameworks/java/parameter-and-return-types)
            final com.google.appengine.api.users.User googleUser,
            final javax.servlet.http.HttpServletRequest request,
            final javax.servlet.ServletContext servletContext
            /*
             * */
    ) throws UnauthorizedException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        // https://docs.oracle.com/javaee/7/api/javax/servlet/ServletContext.html
        // Enumeration attributeNames = servletContext.getAttributeNames();

        HashMap<String, Integer> result = new HashMap<>();
        Integer integer = 33;
        result.put("Test Key", integer);

        return result;
    }

    /* Allows admin to delete user profile.
     * For example if name in key is completely different from name in passport, or if user requested deletion of the
     * profile before key verification is finished.
     * */
    @ApiMethod(
            name = "testConvertToJSON",
            path = "testConvertToJSON",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public ArrayList<String> testConvertToJSON(
            final User googleUser,
            final @Named("userID") String userID
            // see: https://cloud.google.com/appengine/docs/java/endpoints/exceptions
    ) throws Exception {

        /* --- Check authorization : CRYPTONOMICA OFFICER ONLY !!! */
        CryptonomicaUser admin = UserTools.ensureCryptonomicaOfficer(googleUser);


        ArrayList<String> result = new ArrayList<>();

        /* ---- (1) Delete all OpenPGP public keys: */
        List<PGPPublicKeyData> userKeysList = ofy().load().type(PGPPublicKeyData.class).filter("cryptonomicaUserId", userID).list();
        if (userKeysList != null && !userKeysList.isEmpty()) {
            result.add(userKeysList.get(0).toJSON());
        }

        /* --- (2) Delete all OnlineVerification entities */

        List<OnlineVerification> onlineVerificationList = ofy().load().type(OnlineVerification.class).filter("cryptonomicaUserId", userID).list();
        if (onlineVerificationList != null && !onlineVerificationList.isEmpty()) {
            result.add(onlineVerificationList.get(0).toJSON());
        }

        /* --- (3) Delete profile */
        CryptonomicaUser cryptonomicaUser = ofy().load().key(Key.create(CryptonomicaUser.class, userID)).now();
        if (cryptonomicaUser != null) {
            result.add(cryptonomicaUser.toJSON());
        }

        return result;

    } // end of testConvertToJSON();


    @SuppressWarnings("unused")
    @ApiMethod(
            name = "setIntegerValueInTestContract",
            path = "setIntegerValueInTestContract",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    // HashMap<String, Integer> as response works!!! (endpoints.framework.version: 2.0.9)
    public Web3jTransactionReceiptWrapper setIntegerValueInTestContract(
            // Injected types: (see: https://cloud.google.com/endpoints/docs/frameworks/java/parameter-and-return-types)
            final com.google.appengine.api.users.User googleUser,
            // final javax.servlet.http.HttpServletRequest request,
            // final javax.servlet.ServletContext servletContext,
            final @Named("integerValue") Integer integerValue
            /*
             * */
    ) throws UnauthorizedException, IOException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

//        final String infuraApiKey = ofy()
//                .load()
//                .key(Key.create(AppSettings.class, "infuraApiKey"))
//                .now()
//                .getValue();
//        final String infuraApiVersion = "v3";
//        final String network = "kovan";
//        String infuraEndpointURL = "https://" + network + ".infura.io/" + infuraApiVersion + "/" + infuraApiKey;
//        Web3j web3 = Web3j.build(new HttpService(infuraEndpointURL));
//
//        try {
//            Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
//            LOG.warning("Ethereum node : " + web3ClientVersion.getWeb3ClientVersion());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Web3j web3 = Web3jFactory.getWeb3jObject("kovan");

//        final String ethPrivateKey = ofy()
//                .load()
//                .key(Key.create(AppSettings.class, "ethTestPrivateKey"))
//                .now()
//                .getValue();
//        Credentials credentials = Credentials.create(ethPrivateKey);
//        LOG.warning("eth address : " + credentials.getAddress());

//        Credentials credentials = Web3jFactory.getCredentialsObject("ethTestPrivateKey");
//
//        final String contractAddress = "0xcb7c802cac6b547e4ac8115f0834e1db467d1abb";
//
//        final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L); //
//        final BigInteger GAS_LIMIT_MAX = BigInteger.valueOf(8_000_000);
//
//        // https://docs.web3j.io/smart_contracts.html#construction-and-deployment
//        TestContract testContract = TestContract.load(
//                contractAddress,
//                web3,
//                credentials,
//                GAS_PRICE,
//                GAS_LIMIT_MAX
//        );

        TestContract testContract = Web3jFactory.getTestContract();

        String txHash = null;
        TransactionReceipt txReceipt = null;
        Integer integerValueFromContract = null;
        try {
            txHash = testContract.setIntegerValue(BigInteger.valueOf(integerValue)).send().getTransactionHash();

            // https://docs.web3j.io/trouble.html#i-m-submitting-a-transaction-but-it-s-not-being-mined
            // you loop through the following expecting to eventually get a receipt once the transaction is mined

            Optional<TransactionReceipt> optionalTxReceipt = web3.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            // System.out.println("optional : " + optionalTxReceipt.toString());
            // System.out.println("optional.isPresent() : " + optionalTxReceipt.isPresent());
            while (!optionalTxReceipt.isPresent()) {
                optionalTxReceipt = web3.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            }

            txReceipt = optionalTxReceipt.get();

            integerValueFromContract = testContract.integerValue().send().intValue();

        } catch (Exception e) {
            LOG.severe(e.getMessage());
            throw new IOException("Please check contract manually, transaction may be failed");
        }

        String txReceiptStr = txReceipt.toString();
        LOG.warning(txReceiptStr);

        // result:
        Web3jTransactionReceiptWrapper result = new Web3jTransactionReceiptWrapper();
        result.setIntegerValue(integerValueFromContract);
        result.setTransactionReceipt(txReceipt);

        return result;
    }


    @SuppressWarnings("unused")
    @ApiMethod(
            name = "networkInfo",
            path = "networkInfo",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public HashMap<String, String> networkInfo(
            // final HttpServletRequest request,
            final User googleUser
    ) throws UnauthorizedException, IOException, URISyntaxException {

        /* --- Check authorization: */
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        HashMap<String, String> result = new HashMap<>();

        if (SystemProperty.environment != null && SystemProperty.environment.value() != null
                && SystemProperty.version != null
                && SystemProperty.applicationVersion != null
                && SystemProperty.applicationId != null) {
            result.put("SystemProperty.version", SystemProperty.version.get());
            result.put("SystemProperty.applicationVersion", SystemProperty.applicationVersion.get());
            result.put("SystemProperty.applicationId", SystemProperty.applicationId.get());
            result.put("SystemProperty.environment.value()", SystemProperty.environment.value().value());
        }

        // final String network = "kovan";
        // final String network = "ropsten";
        final String network = "mainnet";

        Web3j web3 = Web3jFactory.getWeb3jObject(network);

        result.put("web3jClientVersion", web3.web3ClientVersion().send().getWeb3ClientVersion());
        result.put("Ethereum network ID", web3.netVersion().send().getNetVersion());
        result.put("eth protocol version", web3.ethProtocolVersion().send().getProtocolVersion());
        result.put("netPeerCount()", web3.netPeerCount().send().getQuantity().toString());

        return result;
    }


}

/* ================= classes: */

/**
 * The email bean that will be used in the getUserEmail response.
 */
class Email {

    private String email;

    public Email() {
        //
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

/**
 * The message bean that will be used in the echo request and response.
 */
class Message {

    private String message;

    public Message() {
    }

    /* see: https://stackoverflow.com/a/27148958/1697878 */
    public Message(@JsonProperty("message") String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

