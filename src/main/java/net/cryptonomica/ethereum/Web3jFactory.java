package net.cryptonomica.ethereum;

import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import net.cryptonomica.api.TestAPI;
import net.cryptonomica.constants.Constants;
import net.cryptonomica.entities.AppSettings;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;

public class Web3jFactory {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(TestAPI.class.getName());
    /* --- Gson: */
    private static final Gson GSON = new Gson();

    public static Web3j getWeb3jObject(final String ethereumNetworkName) throws IOException {

        final String infuraApiKey = ofy()
                .load()
                .key(Key.create(AppSettings.class, "infuraApiKey"))
                .now()
                .getValue();

        final String infuraApiVersion = "v3";

        // final String network = "kovan";
        // final String network = "ropsten";
        // final String network = "mainnet";

        String infuraEndpointURL = "https://" + ethereumNetworkName + ".infura.io/" + infuraApiVersion + "/" + infuraApiKey;

        Web3j web3 = Web3j.build(new HttpService(infuraEndpointURL));

        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        LOG.warning("Ethereum node : " + web3ClientVersion.getWeb3ClientVersion());

        return web3;
    }

    public static Credentials getCredentialsObject(final String keyName) {

        final String ethPrivateKey = ofy()
                .load()
                .key(Key.create(AppSettings.class, keyName))
                .now()
                .getValue();

        Credentials credentials = Credentials.create(ethPrivateKey);
        LOG.warning("ETH address : " + credentials.getAddress());

        return credentials;
    }

    public static CryptonomicaVerification getCryptonomicaVerificationContract() throws IOException {

        // https://etherscan.io/address/0x846942953c3b2A898F10DF1e32763A823bf6b27f
        final String contractAddressMainNet = "0x846942953c3b2A898F10DF1e32763A823bf6b27f";

        // https://ropsten.etherscan.io/address/0x846942953c3b2a898f10df1e32763a823bf6b27f
        final String contractAddressRopsten = "0x846942953c3b2A898F10DF1e32763A823bf6b27f";

        String contractAddress;
        Web3j web3;
        if (Constants.PRODUCTION) {
            contractAddress = contractAddressMainNet;
            web3 = Web3jFactory.getWeb3jObject("mainnet");
        } else {
            contractAddress = contractAddressRopsten;
            web3 = Web3jFactory.getWeb3jObject("ropsten");
        }

        // "0xDADfa63d05D01f536930F1150238283Fe917D28c" is used both for Main Net and for Ropsten
        // https://etherscan.io/address/0xDADfa63d05D01f536930F1150238283Fe917D28c
        // https://ropsten.etherscan.io/address/0xdadfa63d05d01f536930f1150238283fe917d28c
        Credentials credentials = Web3jFactory.getCredentialsObject("0xDADfa63d05D01f536930F1150238283Fe917D28c");

        final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L);

        // see example on https://etherscan.io/tx/0x9774a4eef49870a0ae6f9c79235cc58a3bb1764025da3725aad5692ac2e89fe0
        // (Gas Used By Transaction: 381003)
        final BigInteger GAS_LIMIT_MAX = BigInteger.valueOf(1_000_000); //

        // https://docs.web3j.io/smart_contracts.html#construction-and-deployment
        final CryptonomicaVerification cryptonomicaVerification = CryptonomicaVerification.load(
                contractAddress,
                web3,
                credentials,
                GAS_PRICE,
                GAS_LIMIT_MAX
        );

        LOG.warning("CryptonomicaVerification contract at address: " + cryptonomicaVerification.getContractAddress());

        return cryptonomicaVerification;

    }

    /* ================ TESTS === */

    public static TestContract getTestContract() throws IOException {

        final String contractAddress = "0xcb7c802cac6b547e4ac8115f0834e1db467d1abb";

        Web3j web3 = Web3jFactory.getWeb3jObject("kovan");
        Credentials credentials = Web3jFactory.getCredentialsObject("ethTestPrivateKey");

        final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L); //
        final BigInteger GAS_LIMIT_MAX = BigInteger.valueOf(8_000_000);

        // https://docs.web3j.io/smart_contracts.html#construction-and-deployment
        final TestContract testContract = TestContract.load(
                contractAddress,
                web3,
                credentials,
                GAS_PRICE,
                GAS_LIMIT_MAX
        );

        LOG.warning("contract object for contract at address: " + testContract.getContractAddress());

        return testContract;

    }

}
