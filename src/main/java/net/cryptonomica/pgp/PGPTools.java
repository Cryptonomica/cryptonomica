package net.cryptonomica.pgp;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by viktor on 11/01/16. - modified 2016-04-05 (
 * was a bug reading userID in key DSA + ElGamal in BC 1.54 )
 */
public class PGPTools {

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
//    public static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
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
//    <!-- was a but reading userID in key DSA + ElGamal -->

    /// NEW:
    public static PGPPublicKey readPublicKey(InputStream iKeyStream) throws IOException {
        PGPPublicKeyRing newKey = new PGPPublicKeyRing(new ArmoredInputStream(iKeyStream));
        return newKey.getPublicKey();
    }

    public static PGPPublicKey readPublicKeyFromString (String armoredPublicPGPkeyBlock) throws IOException, PGPException {

        InputStream in = new ByteArrayInputStream(armoredPublicPGPkeyBlock.getBytes());

        PGPPublicKey pgpPublicKey = readPublicKey(in);

        in.close();

        return pgpPublicKey;
    }
}
