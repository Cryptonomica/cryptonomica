package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.AppSettings;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.entities.VerificationRequestDataFromSC;
import net.cryptonomica.forms.EthAddDocForm;
import net.cryptonomica.forms.GetDocBySha256Form;
import net.cryptonomica.pgp.PGPTools;
import net.cryptonomica.returns.BooleanWrapperObject;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.returns.VerificationStruct;
import net.cryptonomica.service.HttpService;
import net.cryptonomica.service.UserTools;
import org.apache.commons.text.WordUtils;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;

// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: cryptonomica-{test || server}.appspot.com/_ah/api/explorer
 * <p>
 * [+] - API should be registered in  web.xml (<param-name>services</param-name>)
 * [ ] - API should be loaded in app.js - app.run()
 * <p>
 * * in this API:
 */

@Api(name = "ethNodeAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "API for working with Ethereum node")

public class EthNodeAPI {

    private static final Logger LOG = Logger.getLogger(EthNodeAPI.class.getName());
    private static final Gson GSON = new Gson();

    @ApiMethod(
            name = "addDoc",
            path = "addDoc",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // stores provided document on the Ethereum blockchain
    public Object ethAddDoc(
//            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final EthAddDocForm ethAddDocForm //
    ) throws IllegalArgumentException, UnauthorizedException {

        // ensure registered user ( - may be later only for verified users):
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // check form:
        LOG.warning("ethAddDocForm" + ethAddDocForm + " from " + cryptonomicaUser.getEmail().getEmail());
        if (ethAddDocForm == null
                || ethAddDocForm.getDocText() == null
                || ethAddDocForm.getDocText().length() < 12
                || ethAddDocForm.getDocText().equals("")
                ) {
            throw new IllegalArgumentException("Provided text is to short or empty");
        }

        if (ethAddDocForm.getDocText() != null && ethAddDocForm.getDocText().length() > 1700) {
            throw new IllegalArgumentException("Provided text is to long");
        }

        /* ---- Send request to Ethereum node: */
        // make request obj:
        EthNodeAPI.AddDocRequestObj addDocRequestObj = new EthNodeAPI.AddDocRequestObj();

        String ethnodeApiKey = ofy()
                .load()
                .key(Key.create(AppSettings.class, "EthnodeApiKey"))
                .now().getValue();

        addDocRequestObj.setApikey(ethnodeApiKey);
        addDocRequestObj.setPublisher(cryptonomicaUser.getEmail().getEmail());
        addDocRequestObj.setText(ethAddDocForm.getDocText());

        String urlAddress = "https://ethnode.cryptonomica.net/api/proofofexistence-add";
        HTTPResponse httpResponse = HttpService.postWithPayload(
                urlAddress,
                addDocRequestObj.publisher,
                addDocRequestObj.text,
                ethnodeApiKey
        );

        LOG.warning("httpResponse: " + new Gson().toJson(httpResponse));
        // httpResponse: {"responseCode":200,"headers":[{"name":"Content-Type","value":"application/json"},
        // {"name":"Date","value":"Mon, 11 Jul 2016 00:55:47 GMT"},{"name":"Connection","value":"keep-alive"},
        // {"name":"Content-Length","value":"3448"}],"combinedHeadersMap":{"Content-Type":"application/json","Date":
        // "Mon, 11 Jul 2016 00:55:47 GMT","Connection":"keep-alive","Content-Length":"3448"},
        // "content":[123,34,116,120,72,97,115,104,34,58,34,48,120,99
        // --- valid JSON with headers, and 'content' encoded

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);

        // Test:
        Object resObj = new Gson().fromJson(httpResponseContentString, Object.class);

        LOG.warning("resObj: " + new Gson().toJson(resObj));
        // resObj: {"txHash":"0xc1897ca491e7dec1537be5935734d863f0c17e3e154f22fa06a7e4d66384b6e2","tx":{"blockHash":"
        // -- valid JSON !!!

        // if success send an email to user:
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
                                "You have stored document on blockchain")
                        .param("messageText",
                                "Hello! \n\n"
                                        + WordUtils.capitalize(cryptonomicaUser.getFirstName()) + " "
                                        + WordUtils.capitalize(cryptonomicaUser.getLastName()) + ",\n\n"
                                        + "You sent a document to the blockchain!" + "\n\n"
                                        + "Document text: " + "\n"
                                        + ethAddDocForm.getDocText() + "\n\n"
                                        + "with the following result: " + "\n"
                                        + httpResponseContentString + "\n\n"
                                        + "Best regards, \n\n"
                                        + "Cryptonomica team\n\n"
                                        + "if you think it's wrong or it is an error, please write to admin@cryptonomica.net \n"
                        )
        );
        //
        return resObj;
    } //

    /* ---- AddDocRequestObj Class: */

    @ApiMethod(
            name = "getDocBySha256",
            path = "getDocBySha256",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    // get doc by sha256
    public Object ethGetDocBySha256(
//            final HttpServletRequest httpServletRequest,
            final User googleUser,
            final GetDocBySha256Form getDocBySha256Form
    ) throws IllegalArgumentException, UnauthorizedException {

        // ensure registered user ( - may be later only for verificated):
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // check form:
        LOG.warning("getDocBySha256Form" + getDocBySha256Form);
        if (getDocBySha256Form == null
                || getDocBySha256Form.getSha256() == null
                || getDocBySha256Form.getSha256().length() < 64
                || getDocBySha256Form.getSha256().equals("")
                ) {
            throw new IllegalArgumentException("Provided text is to short or empty");
        }

        if (getDocBySha256Form.getSha256() != null && getDocBySha256Form.getSha256().length() > 64) {
            throw new IllegalArgumentException("Provided hash is to long");
        }

        /* ---- Send request to Ethereum node: */

        String ethnodeApiKey = ofy()
                .load()
                .key(Key.create(AppSettings.class, "EthnodeApiKey"))
                .now().getValue();

        String urlAddress = "https://ethnode.cryptonomica.net/api/proofofexistence-get";

        HTTPResponse httpResponse = HttpService.getDocBySha256(
                urlAddress,
                "0x" + getDocBySha256Form.getSha256(),
                ethnodeApiKey
        );

        LOG.warning("httpResponse: " + new Gson().toJson(httpResponse));

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);

        // Test:
        Object resObj = new Gson().fromJson(httpResponseContentString, Object.class);
        LOG.warning("resObj: " + new Gson().toJson(resObj));

        return resObj;
    } //

    private class AddDocRequestObj implements Serializable {

        private String apikey;
        private String publisher; // email
        private String text; // text to store

        AddDocRequestObj() {
        }

        public String getApikey() {
            return apikey;
        }

        void setApikey(String apikey) {
            this.apikey = apikey;
        }

        public String getPublisher() {
            return publisher;
        }

        void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    } // end AddDocRequestObj



    /*------------------------------------------------------------------------*/
    /* ---------------- for https://tomcatweb3j.cryptonomica.net -------------*/
    /*------------------------------------------------------------------------*/

    private class TestEntity {

        String string;
        Integer integer;
        Boolean aBoolean;
        Object object;

        public TestEntity() {
        }

        public TestEntity(String string, Integer integer, Boolean aBoolean, Object object) {
            this.string = string;
            this.integer = integer;
            this.aBoolean = aBoolean;
            this.object = object;
        }
    }


    @ApiMethod(
            name = "requestTestingServlet",
            path = "requestTestingServlet",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public StringWrapperObject requestTestingServlet(
            final User googleUser
    ) throws IllegalArgumentException, UnauthorizedException {

        // (!) for Cryptonomica officers only:
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaOfficer(googleUser);

        String urlHost = "https://tomcatweb3j.cryptonomica.net";
        String urlPath = "/TestingServlet";
        String urlAddress = urlHost + urlPath;
        String postRequestBody = "testBodyParameterName=" + "testBodyParameterValue";
        String headerName = "testHeaderName";
        String heaserValue = "testHeaderValue";

        // (!!!!) DO NOT USE API KEY IN TEST REQUEST
        HTTPResponse httpResponse = HttpService.postRequestWithCustomHeader(
                urlAddress,
                postRequestBody,
                headerName,
                heaserValue
        );

        // LOG.warning("httpResponse: " + new Gson().toJson(httpResponse));

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);
        LOG.warning("httpResponseContentString:");
        LOG.warning(httpResponseContentString);

        // Test:
        // Object resObj = new Gson().fromJson(httpResponseContentString, Object.class); // --- exception
        TestEntity testEntity = new Gson().fromJson(httpResponseContentString, TestEntity.class); // --- works
        LOG.warning("testEntity: " + new Gson().toJson(testEntity));
        //  testEntity: {"string":"some string","integer":33,"aBoolean":true,"object":{}} (EthNodeAPI.java:309)
        LOG.warning("testEntity.string: " + testEntity.string);
        LOG.warning("testEntity.integer: " + testEntity.integer);
        LOG.warning("testEntity.aBoolean: " + testEntity.aBoolean);
        LOG.warning("testEntity.object: " + testEntity.object);

        LOG.warning("httpResponseContentString: ");
        LOG.warning(httpResponseContentString);
        // net.cryptonomica.api.EthNodeAPI requestTestingServlet: {"string":"some string","integer":33,"aBoolean":true,"object":{}} (EthNodeAPI.java:317)

        // return resObj;
        return new StringWrapperObject(httpResponseContentString);
    } //

    @ApiMethod(
            name = "getVerificationFromSC",
            path = "getVerificationFromSC",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public VerificationStruct getVerificationDataFromSmartContract(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("ethereumAcc") String ethereumAcc
    ) throws IllegalArgumentException, UnauthorizedException {

        // ensure registered user ( - may be later only for verified):
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // check form:
        LOG.warning("ethereumAcc" + ethereumAcc);

        if (ethereumAcc == null || ethereumAcc.equals("")) {
            throw new IllegalArgumentException("Provided text is to short or empty");
        }

        String tomcatWeb3jAPIkey = ofy()
                .load()
                .key(Key.create(AppSettings.class, "tomcatweb3jAPIkey"))
                .now()
                .getValue();

        String urlHost = "https://tomcatweb3j.cryptonomica.net";
        String urlPath = "/getVerification";
        String urlAddress = urlHost + urlPath;

        // HashMap<String, String> queryMap = new HashMap<>();
        // queryMap.put("address", ethereumAcc);
        String postRequestBody = "address=" + ethereumAcc;

        HTTPResponse httpResponse = HttpService.postRequestWithAPIkey(
                urlAddress,
                postRequestBody,
                tomcatWeb3jAPIkey
        );

        // LOG.warning("httpResponse: " + new Gson().toJson(httpResponse));

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);

        // Test:
        // Object resObj = new Gson().fromJson(httpResponseContentString, Object.class); // --- exception
        // LOG.warning("resObj: " + new Gson().toJson(resObj));

        LOG.warning("httpResponseContentString: " + httpResponseContentString);

        VerificationStruct verificationStruct = GSON.fromJson(httpResponseContentString, VerificationStruct.class);

        /* header:
        content-type:  application/json;charset=utf-8
        body:
        {
            "fingerprint": "",
            "keyCertificateValidUntil": 0,
            "firstName": "",
            "lastName": "",
            "birthDate": 0,
            "nationality": "",
            "verificationAddedOn": 0,
            "revokedOn": 0
        }
        */
        return verificationStruct;
    }

    @ApiMethod(
            name = "getVerificationFromSCbyFingerprint",
            path = "getVerificationFromSCbyFingerprint",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    @SuppressWarnings("unused")
    public StringWrapperObject getVerificationDataFromSmartContractByFingerprint(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("fingerprint") String fingerprint
    ) throws IllegalArgumentException, UnauthorizedException {

        // ensure registered user ( - may be later only for verified):
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // check form:
        LOG.warning("fingerprint" + fingerprint);

        if (fingerprint == null || fingerprint.equals("") || fingerprint.length() != 40) {
            throw new IllegalArgumentException("Provided fingerprint is not valid");
        }

        String tomcatWeb3jAPIkey = ofy()
                .load()
                .key(Key.create(AppSettings.class, "tomcatweb3jAPIkey"))
                .now()
                .getValue();

        String urlHost = "https://tomcatweb3j.cryptonomica.net";
        String urlPath = "/getVerification";
        String urlAddress = urlHost + urlPath;

        // HashMap<String, String> queryMap = new HashMap<>();
        // queryMap.put("address", ethereumAcc);
        String postRequestBody = "fingerprint=" + fingerprint;

        HTTPResponse httpResponse = HttpService.postRequestWithAPIkey(
                urlAddress,
                postRequestBody,
                tomcatWeb3jAPIkey
        );

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);
        LOG.warning("httpResponseContentString: " + httpResponseContentString);
        return new StringWrapperObject(httpResponseContentString);
    }


    @ApiMethod(
            name = "verifyEthAddress",
            path = "verifyEthAddress",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public BooleanWrapperObject verifyEthAddress(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("ethereumAcc") String ethereumAcc
    ) throws IllegalArgumentException, UnauthorizedException, Exception {

        BooleanWrapperObject result = new BooleanWrapperObject();

        // ensure registered user ( - may be later only for verified):
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        // check form:
        LOG.warning("ethereumAcc" + ethereumAcc);

        if (ethereumAcc == null || ethereumAcc.equals("")) {
            throw new IllegalArgumentException("Provided text is to short or empty");
        }

        String tomcatWeb3jAPIkey = ofy()
                .load()
                .key(Key.create(AppSettings.class, "tomcatweb3jAPIkey"))
                .now()
                .getValue();

        String urlHost = "https://tomcatweb3j.cryptonomica.net";
        String urlPath = "/GetVerificationRequestDataServlet";
        String urlAddress = urlHost + urlPath;

        // HashMap<String, String> queryMap = new HashMap<>();
        // queryMap.put("address", ethereumAcc);
        String postRequestBody = "address=" + ethereumAcc;

        HTTPResponse httpResponse = HttpService.postRequestWithAPIkey(
                urlAddress,
                postRequestBody,
                tomcatWeb3jAPIkey
        );

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);

        // Test:
        // Object resObj = new Gson().fromJson(httpResponseContentString, Object.class); // --- exception
        // LOG.warning("resObj: " + new Gson().toJson(resObj));

        LOG.warning("httpResponseContentString: " + httpResponseContentString);

        VerificationRequestDataFromSC verificationRequestDataFromSC = GSON.fromJson(
                httpResponseContentString,
                VerificationRequestDataFromSC.class
        );

        // GET Key from DataBase by fingerprint:
        String unverifiedFingerprint = verificationRequestDataFromSC.getUnverifiedFingerprint();
        String signedString = verificationRequestDataFromSC.getSignedString();

        PGPPublicKeyData pgpPublicKeyData = PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(unverifiedFingerprint);

        Boolean keyVerifiedOffline = pgpPublicKeyData.getVerifiedOffline();
        if (keyVerifiedOffline == null) {
            keyVerifiedOffline = Boolean.FALSE;
        }
        // Boolean keyVerifiedOffline = pgpPublicKeyData.getVerified();
        Boolean keyVerifiedOnline = pgpPublicKeyData.getVerifiedOnline();
        if (keyVerifiedOnline == null) {
            keyVerifiedOnline = Boolean.FALSE;
        }
        //  Boolean keyVerifiedOnline = pgpPublicKeyData.getOnlineVerificationFinished();

        if (!keyVerifiedOffline && !keyVerifiedOnline) {
            throw new Exception("Owner of the OpenPGP key "
                    + pgpPublicKeyData.getFingerprint()
                    + " not verified. Can not process with ETH address verification for "
                    + ethereumAcc
            );
        }

        PGPPublicKey publicKey = PGPTools.readPublicKeyFromString(pgpPublicKeyData.getAsciiArmored().getValue());

        result.setResult(
                PGPTools.verifyText(signedString, publicKey)
        );

        if (result.getResult()) {

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("acc", ethereumAcc);
            parameterMap.put("fingerprint", unverifiedFingerprint);
            // https://stackoverflow.com/questions/7784421/getting-unix-timestamp-from-date
            Long keyCertificateValidUntilUnixTimeLong = pgpPublicKeyData.getExp().getTime() / 1000;
            Integer keyCertificateValidUntilUnixTime = keyCertificateValidUntilUnixTimeLong.intValue();
            parameterMap.put("keyCertificateValidUntil", keyCertificateValidUntilUnixTime.toString());
            parameterMap.put("firstName", pgpPublicKeyData.getFirstName());
            parameterMap.put("lastName", pgpPublicKeyData.getLastName());

            if (pgpPublicKeyData.getUserBirthday() != null) { // for testing with old keys only
                Long birthDateUnixTimeLong = pgpPublicKeyData.getUserBirthday().getTime() / 1000;
                Integer birthDateUnixTime = birthDateUnixTimeLong.intValue();
                parameterMap.put("birthDate", birthDateUnixTime.toString());
            } else {
                parameterMap.put("birthDate", "null");
            }

            if (pgpPublicKeyData.getNationality() != null) { // for testing with old keys only
                parameterMap.put("nationality", pgpPublicKeyData.getNationality());
            } else {
                parameterMap.put("nationality", "null");
            }

            LOG.warning("parameterMap: ");
            LOG.warning(GSON.toJson(parameterMap));

            HTTPResponse httpResponseFromAddVerificationDataServlet = HttpService.makePostRequestWithParametersMapAndApiKey(
                    "https://tomcatweb3j.cryptonomica.net/addVerificationData",
                    tomcatWeb3jAPIkey,
                    parameterMap
            );

            try {

                byte[] httpResponseContentBytesFromAddVerificationDataServlet = httpResponseFromAddVerificationDataServlet.getContent();

                String httpResponseContentStringAddVerificationDataServlet = new String(
                        httpResponseContentBytesFromAddVerificationDataServlet,
                        StandardCharsets.UTF_8
                );

                LOG.warning(httpResponseContentStringAddVerificationDataServlet);

                result.setMessage(
                        httpResponseContentStringAddVerificationDataServlet // tx receipt
                );

            } catch (Exception e) {
                LOG.severe(e.getMessage());
                result.setMessage(
                        "Can not check transaction. Please check smart contract data manually"
                );
            } // end of: catch

        } // end of: if (result.getResult()) {..

        LOG.warning("result:");
        LOG.warning(GSON.toJson(result));

        return result;
    }

}
