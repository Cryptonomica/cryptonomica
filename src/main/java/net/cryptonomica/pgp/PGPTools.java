package net.cryptonomica.pgp;

import com.google.appengine.api.datastore.Email;
import com.google.common.base.Splitter;
import net.cryptonomica.entities.CryptonomicaUser;
import net.cryptonomica.entities.PGPPublicKeyData;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;

/**
 * Created by viktor on 11/01/16. - modified 2016-04-05 (
 * was a bug reading userID in key DSA + ElGamal in BC 1.54 )
 */
public class PGPTools {

    /* ---- Logger */
    private static final Logger LOG = Logger.getLogger(PGPTools.class.getName());

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

    /// NEW:
    public static PGPPublicKey readPublicKey(InputStream iKeyStream) throws IOException {
        PGPPublicKeyRing newKey = new PGPPublicKeyRing(new ArmoredInputStream(iKeyStream));
        return newKey.getPublicKey();
    }

    public static PGPPublicKey readPublicKeyFromString(String armoredPublicPGPkeyBlock) throws IOException, PGPException {

        InputStream in = new ByteArrayInputStream(armoredPublicPGPkeyBlock.getBytes());

        PGPPublicKey pgpPublicKey = readPublicKey(in);

        in.close();

        return pgpPublicKey;
    }

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

        if(!firstNameFromAccount.equals(firstNameFromKey) || !lastNameFromAccount.equals(lastNameFromKey)){
            throw new Exception("First and last name in key should be exactly as first and last name in account");
        }

        // --- check key creation date/time:
        Date creationTime = pgpPublicKey.getCreationTime();
        if (creationTime.after(new Date())){
            throw new Exception("Invalid key creation Date/Time");
        }

        // -- bits size check:
        if(pgpPublicKeyData.getBitStrength() < 2048){
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
}
