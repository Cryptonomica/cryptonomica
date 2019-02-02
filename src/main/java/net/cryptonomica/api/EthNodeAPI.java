package net.cryptonomica.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.AppSettings;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import net.cryptonomica.entities.VerificationRequestDataFromSC;
import net.cryptonomica.ethereum.CryptonomicaVerification;
import net.cryptonomica.ethereum.Web3jFactory;
import net.cryptonomica.ethereum.Web3jServices;
import net.cryptonomica.pgp.PGPTools;
import net.cryptonomica.returns.BooleanWrapperObject;
import net.cryptonomica.returns.StringWrapperObject;
import net.cryptonomica.returns.VerificationStruct;
import net.cryptonomica.service.DateAndTimeService;
import net.cryptonomica.service.HttpService;
import net.cryptonomica.service.UserTools;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;

// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries
// see:
// https://github.com/objectify/objectify/wiki/Queries#executing-queries

/**
 * explore on: {sandbox-cryptonomica || cryptonomica-server}.appspot.com/_ah/api/explorer
 * ! - API should be registered in  web.xml (<param-name>services</param-name>)
 * ! - on frontend API should be loaded in app.js - app.run()
 */

@Api(name = "ethNodeAPI", // The api name must match '[a-z]+[A-Za-z0-9]*'
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
        description = "API for working with Ethereum node")

public class EthNodeAPI {

    private static final Logger LOG = Logger.getLogger(EthNodeAPI.class.getName());
    private static final Gson GSON = new Gson();

    /*------------------------------------------------------------------------*/
    /* ---------------- for https://tomcatweb3j.cryptonomica.net -------------*/
    /*------------------------------------------------------------------------*/

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

    /*------------------------------------------------------------------------*/
    /* ---------------- for INFURA  ----------=============================---*/
    /*------------------------------------------------------------------------*/

    @ApiMethod(
            name = "verifyEthAddressInfura",
            path = "verifyEthAddressInfura",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    @SuppressWarnings("unused")
    public TransactionReceipt verifyEthAddressInfura(
            // final HttpServletRequest httpServletRequest,
            final User googleUser,
            final @Named("ethereumAcc") String ethereumAcc
    ) throws Exception {

        // ensure registered user
        CryptonomicaUser cryptonomicaUser = UserTools.ensureCryptonomicaRegisteredUser(googleUser);

        /* --- check form --- */
        LOG.warning("ethereumAcc : " + ethereumAcc);
        if (ethereumAcc.isEmpty()) {
            throw new IllegalArgumentException("Provided ETH account address is empty");
        } else if (ethereumAcc.length() > 42) {
            throw new IllegalArgumentException("ETH address length should be 42 characters");
        } else if (ethereumAcc.indexOf("0x") != 0) {
            throw new IllegalArgumentException("ETH address should begin with '0x' ");
        }

        Web3j web3j = Web3jFactory.getWeb3jObject("mainnet");
        CryptonomicaVerification cryptonomicaVerification = Web3jFactory.getCryptonomicaVerificationContract();

        String signedString = cryptonomicaVerification.signedString(ethereumAcc).send();
        LOG.warning("signedString from smart contract:");
        LOG.warning(signedString);

        if (signedString == null || signedString.isEmpty()) {
            throw new IllegalArgumentException("No signed string uploaded from address " + ethereumAcc);
        }

        if (!signedString.toLowerCase().contains("I hereby confirm that the address".toLowerCase())
                || !signedString.toLowerCase().contains(ethereumAcc.toLowerCase())
                || !signedString.toLowerCase().contains("is my Ethereum address".toLowerCase())
        ) {
            throw new IllegalArgumentException("Uploaded string does not contain required text");
        }

        String unverifiedFingerprint = cryptonomicaVerification.unverifiedFingerprint(ethereumAcc).send();
        LOG.warning("unverifiedFingerprint from smart conract:");
        LOG.warning(unverifiedFingerprint);

        if (unverifiedFingerprint == null || unverifiedFingerprint.isEmpty()) {
            throw new IllegalArgumentException("No fingerprint for "
                    + ethereumAcc
                    + " found in smart contract. Can not proceed with verification");
        }

        /* --- get PGPPublicKeyData from DB */

        PGPPublicKeyData pgpPublicKeyData =
                PGPTools.getPGPPublicKeyDataFromDataBaseByFingerprint(unverifiedFingerprint);

        /*  --- check if OpenPGP key registered and verified on Cryptonomica */
        if (pgpPublicKeyData == null) {
            throw new NotFoundException("OpenPGP key "
                    + unverifiedFingerprint
                    + " not registered on Cryptonomica. Can not process with ETH address verification for "
                    + ethereumAcc);
        }

        Boolean keyVerifiedOffline = pgpPublicKeyData.getVerifiedOffline();
        if (keyVerifiedOffline == null) {
            keyVerifiedOffline = Boolean.FALSE;
        }
        Boolean keyVerifiedOnline = pgpPublicKeyData.getVerifiedOnline();
        if (keyVerifiedOnline == null) {
            keyVerifiedOnline = Boolean.FALSE;
        }
        if (!keyVerifiedOffline && !keyVerifiedOnline) {
            throw new IllegalArgumentException("Owner of the OpenPGP key "
                    + pgpPublicKeyData.getFingerprint()
                    + " not verified. Can not process with ETH address verification for "
                    + ethereumAcc
            );
        }

        String asciiArmoredPublicKey = pgpPublicKeyData.getAsciiArmored().getValue();
        LOG.warning("asciiArmoredPublicKey:");
        LOG.warning(asciiArmoredPublicKey);

        try {
            PGPTools.verifySignedString(signedString, asciiArmoredPublicKey);
        } catch (PGPException e) {

            throw new PGPException(
                    "Signature verification of following text:  "
                            + signedString
                            + "   with key "
                            + unverifiedFingerprint
                            + " was failed. "
                            + e.getMessage()
            );
        }

        /* --- if signature verified (i.e. exception above not thrown), place data to smart contract */

        String txHash = null;
        TransactionReceipt txReceipt = null; // < result
        CryptonomicaUser cryptonomicaUserData = UserTools.loadCryptonomicaUser(pgpPublicKeyData.getCryptonomicaUserId());
        try {
/* Solidity:
function addVerificationData(
    address _acc, //
    string _fingerprint, // "57A5FEE5A34D563B4B85ADF3CE369FD9E77173E5"
    bytes20 _fingerprintBytes20, // "0x57A5FEE5A34D563B4B85ADF3CE369FD9E77173E5"
    uint _keyCertificateValidUntil, //
    string _firstName, //
    string _lastName, //
    uint _birthDate, //
    string _nationality) public {
*/
            txHash = cryptonomicaVerification.addVerificationData(
                    ethereumAcc,
                    unverifiedFingerprint,
                    Web3jServices.bytes20FromHexString(unverifiedFingerprint).getValue(),
                    BigInteger.valueOf(pgpPublicKeyData.getExp().getTime() / 1000),
                    pgpPublicKeyData.getFirstName(),
                    pgpPublicKeyData.getLastName(),

                    // BigInteger.valueOf(pgpPublicKeyData.getUserBirthday().getTime() / 1000),
                    /* */                               // 2019-01-
                    BigInteger.valueOf(
                            DateAndTimeService.utcUnixTimeFromYearMonthDay(
                                    cryptonomicaUserData.getBirthdayYear(),
                                    cryptonomicaUserData.getBirthdayMonth(),
                                    cryptonomicaUserData.getBirthdayDay()
                            )
                    ),

                    pgpPublicKeyData.getNationality()
            ).send().getTransactionHash();

            LOG.warning("tx send, tx hash: " + txHash);

            // https://docs.web3j.io/trouble.html#i-m-submitting-a-transaction-but-it-s-not-being-mined
            // you loop through the following expecting to eventually get a receipt once the transaction is mined
            Optional<TransactionReceipt> optionalTxReceipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            while (!optionalTxReceipt.isPresent()) {
                optionalTxReceipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            }
            txReceipt = optionalTxReceipt.get();
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            throw new IOException("Please check contract manually, transaction may be failed");
        }

        return txReceipt;
        /*
{
 "transactionReceipt": {
  "transactionHash": "0x95c8758d5cd47cee0b52728d922b4504445bcb0336bb547fc3008dc6f2d36ef6",
  "transactionIndex": 0,
  "blockHash": "0xa4f13410cf440605013d3786bf1f7e94b0211337894bb07ec4d1eb39fba1b4cc",
  "blockNumber": 8844339,
  "cumulativeGasUsed": 28634,
  "gasUsed": 28634,
  "status": "0x1",
  "logs": [
   {
    "removed": false,
    "logIndex": 0,
    "transactionIndex": 0,
    "transactionHash": "0x95c8758d5cd47cee0b52728d922b4504445bcb0336bb547fc3008dc6f2d36ef6",
    "blockHash": "0xa4f13410cf440605013d3786bf1f7e94b0211337894bb07ec4d1eb39fba1b4cc",
    "blockNumber": 8844339,
    "address": "0xcb7c802cac6b547e4ac8115f0834e1db467d1abb",
    "data": "0x0000000000000000000000000000000000000000000000000000000000000013000000000000000000000000000000000000000000000000000000000000000b",
    "type": "mined",
    "topics": [
     "0xcf19d10be998a11a4f3dffa95dd7fd6f55bf303a251f296c3f2e3278302c90b4",
     "0x00000000000000000000000007bad6bda22a830f58fda19eba45552c44168600"
    ],
    "transactionIndexRaw": "0x0",
    "blockNumberRaw": "0x86f433",
    "logIndexRaw": "0x0"
   }
  ],
  "logsBloom": "0x00000000002000000000000000000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000002000000000000000000000000000000000000000000000000000000800001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004000000000004000000000000080000000000000000000000000000000000000000000000",
  "statusOK": true,
  "transactionIndexRaw": "0x0",
  "blockNumberRaw": "0x86f433",
  "cumulativeGasUsedRaw": "0x6fda",
  "gasUsedRaw": "0x6fda"
 }
        */

    }

}
