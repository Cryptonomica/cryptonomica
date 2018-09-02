package net.cryptonomica.pgp;

import com.google.gson.Gson;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.cryptonomica.service.OfyService.ofy;

/**
 * Created by viktor on 11/01/16. - modified 2016-04-05 (
 * was a bug (?) reading userID in key DSA + ElGamal in BC 1.54 )
 */
public class PGPTools {

    /* ---- Logger */
    private static final Logger LOG = Logger.getLogger(PGPTools.class.getName());
    /* --- Gson: */
    private static final Gson GSON = new Gson();

//    <!-- was a bug reading userID in key DSA + ElGamal --> :
//    /**
//     * A simple routine that opens a key ring file and loads the first available key
//     * suitable for encryption.
//     *
//     * @param input data stream containing the public key data
//     * @return the first public key found.
//     * @throws IOException
//     * @throws PGPException
//     */
//    public static PGPPublicKey readPublicKeyFromInputStream (InputStream input) throws IOException, PGPException {
//        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
//                PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());
//        //
//        // we just loop through the collection till we find a key suitable for encryption, in the real
//        // world you would probably want to be a bit smarter about this.
//        Iterator keyRingIter = pgpPub.getKeyRings();
//        while (keyRingIter.hasNext()) {
//            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();
//
//            Iterator keyIter = keyRing.getPublicKeys();
//            while (keyIter.hasNext()) {
//                PGPPublicKey key = (PGPPublicKey) keyIter.next();
//
//                if (key.isEncryptionKey()) {
//                    return key;
//                }
//            }
//        }
//
//        throw new IllegalArgumentException("Can't find encryption key in key ring.");
//    }
//    <!-- was a bug reading userID in key DSA + ElGamal -->


    private static PGPPublicKey readPublicKey(InputStream iKeyStream) throws IOException {
        PGPPublicKeyRing newKey = new PGPPublicKeyRing(new ArmoredInputStream(iKeyStream));
        return newKey.getPublicKey();
    }

    public static PGPPublicKey readPublicKeyFromString(String armoredPublicPGPkeyBlock)
            throws IOException, PGPException {

        InputStream in = new ByteArrayInputStream(armoredPublicPGPkeyBlock.getBytes());
        PGPPublicKey pgpPublicKey = readPublicKey(in);
        in.close();
        return pgpPublicKey;
    }

    /* this can be used to check if provided OpenPGP public key
     * contains all required information to be stored in DataBase
     * */
    public static PGPPublicKeyData checkPublicKey(final PGPPublicKey pgpPublicKey,
                                                  final String asciiArmored,
                                                  final CryptonomicaUser cryptonomicaUser)
            throws Exception {

        // -- email check:
        PGPPublicKeyData pgpPublicKeyData = new PGPPublicKeyData(pgpPublicKey,
                asciiArmored,
                cryptonomicaUser.getUserId()
        );
        String userEmailFromAccount = cryptonomicaUser.getEmail().getEmail().toLowerCase();
        String userEmailFromKey = pgpPublicKeyData.getUserEmail().getEmail().toLowerCase();

        if (!userEmailFromKey.equals(userEmailFromAccount)) {
            throw new Exception("Email in the key's user ID should be the same as in account");
        }

        // --- first and last name check:
        String firstNameFromAccount = cryptonomicaUser.getFirstName().toLowerCase();
        String lastNameFromAccount = cryptonomicaUser.getLastName().toLowerCase();

        String firstNameFromKey = pgpPublicKeyData.getFirstName().toLowerCase();
        String lastNameFromKey = pgpPublicKeyData.getLastName().toLowerCase();

        if (!firstNameFromAccount.equals(firstNameFromKey) || !lastNameFromAccount.equals(lastNameFromKey)) {
            throw new Exception("First and last name in key should be exactly as first and last name in account");
        }

        // --- check key creation date/time:
        Date creationTime = pgpPublicKey.getCreationTime();
        if (creationTime.after(new Date())) {
            throw new Exception("Invalid key creation Date/Time");
        }

        // -- bits size check:
        if (pgpPublicKeyData.getBitStrength() < 2048) {
            throw new Exception("Key Strength (bits size) should be min 2048 bits");
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
            throw new Exception("The key with this fingerprint ("
                    + pgpPublicKeyData.getFingerprint()
                    + ") already registered");
        }

        // if no Exceptions:
        return pgpPublicKeyData;

    } // end of checkPublicKey()

    public static PGPPublicKeyData getPGPPublicKeyDataFromDataBaseByFingerprint(String fingerprint)
            throws Exception {

        /* Validate fingerprint */
        if (fingerprint == null || fingerprint.length() == 0) {
            throw new IllegalArgumentException("fingerprint is null or empty string");
        }

        if (fingerprint.length() < 40) {
            throw new IllegalArgumentException(fingerprint + " length is less than 40 symbols");
        } else if (fingerprint.length() > 40) {
            throw new IllegalArgumentException(fingerprint + " length is more than 40 symbols");
        }

        // see: https://stackoverflow.com/questions/33467536/how-to-check-if-a-string-is-made-only-of-letters-and-numbers
        if (!fingerprint.matches("[a-zA-Z0-9]*")) {
            throw new IllegalArgumentException("fingerprint can contain only letters and numbers");
        }

        /* Load PGPPublicKeyData from DB by fingerprint */
        List<PGPPublicKeyData> pgpPublicKeyDataList = null;
        pgpPublicKeyDataList = ofy()
                .load()
                .type(PGPPublicKeyData.class)
                // <--- "fingerprintStr"
                // @Id fields cannot be filtered on
                .filter("fingerprintStr", fingerprint.toUpperCase())
                .list();

        LOG.warning("DB search result by fingerprint: " + GSON.toJson(pgpPublicKeyDataList));
        // if key not found trow an exception
        if (pgpPublicKeyDataList == null || pgpPublicKeyDataList.size() == 0) {
            throw new IllegalArgumentException("Public PGP key with fingerprint "
                    + fingerprint.toUpperCase()
                    + " was not found in DataBase");
        }
        // check if there is only one key with given fingerprint in the database
        if (pgpPublicKeyDataList.size() > 1) {
            throw new Exception("there are "
                    + pgpPublicKeyDataList.size()
                    + " different keys with fingerprint "
                    + fingerprint.toUpperCase()
                    + "in the database"
            );
        }
        // get key from the list
        PGPPublicKeyData pgpPublicKeyData = pgpPublicKeyDataList.get(0);
        return pgpPublicKeyData;
    } // end of getPGPPublicKeyDataFromDataBaseByFingerprint()

    // ---------------------------- NEW (2017-05-29):
    public static Boolean verifySignedString(
            String signedString,
            String pgpPublicKeyAsciiArmored // pgpPublicKeyData.getAsciiArmored().getValue()
    ) throws Exception {
        PGPPublicKey publicKey = readPublicKeyFromString(pgpPublicKeyAsciiArmored);
        Boolean result = verifyText(signedString, publicKey);
        return result;
    }

    public static Boolean verifyText(String plainText, PGPPublicKey publicKey) throws Exception {

        String pattern = "-----BEGIN PGP SIGNED MESSAGE-----\\r?\\n.*?\\r?\\n\\r?\\n(.*)\\r?\\n(-----BEGIN PGP SIGNATURE-----\\r?\\n.*-----END PGP SIGNATURE-----)";

        Pattern regex = Pattern.compile(pattern, Pattern.CANON_EQ | Pattern.DOTALL);

        Matcher regexMatcher = regex.matcher(plainText);

        // if input test is a signed plaintext
        if (regexMatcher.find()) {

            String signedDataStr = regexMatcher.group(1);
            LOG.warning("signedDataStr: ");
            LOG.warning(signedDataStr);

            String signatureStr = regexMatcher.group(2);
            LOG.warning("signatureStr: ");
            LOG.warning(signatureStr);

            ByteArrayInputStream signedDataIn = new ByteArrayInputStream(signedDataStr.getBytes("UTF8"));
            ByteArrayInputStream signatureIn = new ByteArrayInputStream(signatureStr.getBytes("UTF8"));

            Boolean result = verifyFile(signedDataIn, signatureIn, publicKey);
            LOG.warning("verification result: " + result);

            return result;
        }

        throw new Exception("Cannot recognize input data");
    }


    public static Boolean verifyFile(
            InputStream signedDataIn, // signed data
            InputStream signatureIn, //  signature
            PGPPublicKey pgpPublicKey) //  key
            throws Exception {
        signatureIn = PGPUtil.getDecoderStream(signatureIn);
        //dataIn = PGPUtil.getDecoderStream(dataIn); // not needed
        PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(
                signatureIn,
                new JcaKeyFingerprintCalculator() // <<<< TODO: check if this is correct
        );

        PGPSignatureList pgpSignatureList = null;
        Object o;

        // get adn check: pgpObjectFactory.nextObject()
        try {
            o = pgpObjectFactory.nextObject();
            if (o == null)
                throw new Exception("pgpObjectFactory.nextObject() returned null");
        } catch (Exception ex) {

            throw new Exception("Invalid input data"); //
        }

        if (o instanceof PGPCompressedData) {

            PGPCompressedData pgpCompressedData = (PGPCompressedData) o;
            pgpObjectFactory = new PGPObjectFactory(
                    pgpCompressedData.getDataStream(),
                    new JcaKeyFingerprintCalculator() // <<<< TODO: check if this is correct
            );
            pgpSignatureList = (PGPSignatureList) pgpObjectFactory.nextObject();

        } else {
            pgpSignatureList = (PGPSignatureList) o;
        }

        int ch;

        // A PGP signatureObject
        // https://www.borelly.net/cb/docs/javaBC-1.4.8/pg/index.html?org/bouncycastle/openpgp/PGPSignature.html
        PGPSignature signatureObject = pgpSignatureList.get(0);

        if (pgpPublicKey == null)
            throw new Exception("Cannot find key 0x"
                    + Integer.toHexString((int) signatureObject.getKeyID()).toUpperCase()
                    + " in the pubring"
            );

        // signatureObject.initVerify(
        //  pgpPublicKey,
        //  "BC"
        //  );
        // https://www.borelly.net/cb/docs/javaBC-1.4.8/pg/org/bouncycastle/openpgp/PGPSignature.html#initVerify(org.bouncycastle.openpgp.PGPPublicKey,%20java.security.Provider)
        // Deprecated. use init(PGPContentVerifierBuilderProvider, PGPPublicKey)

        signatureObject.init(
                new JcaPGPContentVerifierBuilderProvider(),
                pgpPublicKey
        );

        while ((ch = signedDataIn.read()) >= 0) {
            signatureObject.update((byte) ch);
        }

        if (signatureObject.verify()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }

    } // end of verifyFile()


}
