package net.cryptonomica.testing;

import net.cryptonomica.pgp.PGPTools;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

/**
 * Created by viktor on 18/07/17
 */
public class TestPGPTools {

    public static String publicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "Version: GnuPG v2\n" +
            "\n" +
            "mQENBFh3xoIBCAC5X05PdGXugxPP30G9YxcEBXes6o5GdboltS+tXcSm4iXPmhKZ\n" +
            "pEHn5ibvbfINWSTy36/+ezCi20Mt8D493tXX0ngY4m4k8bvIaX7aKvNOQAEbYrha\n" +
            "yODz7Xd4eaBY1hAbQnTY6dKKuHTP7zL8RhLwD9/rlKmVzEYiznVecNvJOtVHNbrm\n" +
            "xNlZb/DfxcNcTrzISFG9AVVWd7RvDS/GIrEZ2a92a6kwWHrlA8wf97VhyyLwHIij\n" +
            "XG2wTiJ67ZP7ZNKNABJcIscBpB/jC1LdnVNEosdlE9+xSQLIQDv6tfm/xS9KfbPz\n" +
            "J30cxYgtK3nvwm3OFR5R9JfA8dVgY28H20/pABEBAAG0N1Zpa3RvciBBZ2V5ZXYg\n" +
            "PGFnZXlldkBpbnRlcm5hdGlvbmFsLWFyYml0cmF0aW9uLm9yZy51az6JAT0EEwEI\n" +
            "ACcFAlh3xoICGyMFCQPB9B4FCwkIBwIGFQgJCgsCBBYCAwECHgECF4AACgkQansh\n" +
            "4oRMeYA8DAf5AUwxQ1x/pfgWZnzJHVCksEPneKp4AGF0dzWXV61yvy7DqD+/jmct\n" +
            "+xCAAYDO45t/YFnQy0fmHucn6q6u23DOTSHW/ct6V6yUi3q6G/shuZEsAgeXHCSd\n" +
            "zN1hKQhrs44fe7DxwSh5oO1iGZrwkcp25P+XaCb+B28PceLfWyptztWZjuSI1jxM\n" +
            "SyCMxSiFrpLNx4c8834lcbDh7PigvSI8H4Efr8UBr8nE/SKKrsmOLEuAIAV1DtR6\n" +
            "hvJSKV12kumA14Tbk77ffyllxVkTr2USrD3ovr4WYHZQRIUVampo1Uom1HN+guM8\n" +
            "v/O5KEzB5wEfwBXgvse57i5XfbgAIgTAn7kBDQRYd8aCAQgAtU51JM5D3l0q1H1l\n" +
            "fupw+CgwqR3cRmi3J5aaRYC7sW4H8cASHoqe7LsJmPYHs565S1yX8rlA54kLGP0G\n" +
            "/pwDF6ukWclQXU7UhrUruU25zU6JPgmz8ObWgrgc0JS0Z9hzUg7rdJ2+Vy78rUkG\n" +
            "PxZVYUeQpKR0tTtVknY+LIFo50xeH9TLyLZ0QqjoTLC/6BZHVUJOOSLJzv2rHt8P\n" +
            "jifa+NtoxnSKNgu/k+4HstAn1XFf6Hkz5Y6XF/5NuqAntM9ob7IfWCrSZ8qvHqzj\n" +
            "rlaD6ikcMt/Q1QBLjEOuKqvJddrogSZ8PFF3fuNTwMePySaX3Nq/9+fk0EIgOkT4\n" +
            "us0I7QARAQABiQElBBgBCAAPBQJYd8aCAhsMBQkDwfQeAAoJEGp7IeKETHmAIGsH\n" +
            "/3buCvMuX3e2j/HXpGNtcoBJE+cNmxkbjjxcNRu6ioVJ5hpO5aqNTXYAafXuaEt5\n" +
            "twQA+rPqotgYvOioIYNSyVCluxPpIK92/Cxlj8N2QniF1FnjG3l2yG/h8zNKQTP1\n" +
            "d4F5TSmiewKIEQgb2lTnDLnnNz6flLQ71Jwpa8XwLd8gQaJapIinBFvanDSVmZq1\n" +
            "3MukTV22SGUYnPQfd28wOnu9+fhb5/wN2+0zaNMi7eCaPfYrLb+7osI69fTyMNvt\n" +
            "DbmbGxVhGD512HMHXuyUq6ImCN9CBW34eEALDvI1tu6HVegtSYOQKCnhNv/OxvYS\n" +
            "R1JKhyWR4/TAV0FCVqUVy20=\n" +
            "=QR1p\n" +
            "-----END PGP PUBLIC KEY BLOCK-----";

    public static String signedString1 = "-----BEGIN PGP SIGNED MESSAGE-----\n" +
            "Hash: SHA256\n" +
            "\n" +
            "0xecb6b43927394073b072b263e4ead230f1a6987bcab9f067918192d6bdccc8c1\n" +
            "-----BEGIN PGP SIGNATURE-----\n" +
            "Version: GnuPG v2\n" +
            "\n" +
            "iQEcBAEBCAAGBQJZbW5IAAoJEGp7IeKETHmADTsIAJpELl5bSanJ9fxMuUDQSm3/\n" +
            "n5jCgaBLh0XbA122rEm/6KZLb6jRk771u8NUTrCNky2xSRvCCV+CFKvQ1qOQ0m3j\n" +
            "GTDFRqfs3TYMG0nShFA8k9+wXZI+d/iM7DnOxwLO5JM/lGViPs2i6pYEOHf0Jenl\n" +
            "CS2vslcu0IILduq0mmljKXjcqtRu1whatFtXU/inCV1grAfQJHuI6ZmdDg4ntTUj\n" +
            "mCGrDWrAQfsrgQtlnqeIs/4U8gLQFjlsBhJKstLKbkGnVbHtmwZDmq69dzrLUuwO\n" +
            "T2pTl/i4YZ02bGxo8Lx3zvDXZocwtRrMtfR4koeFHt4ctgIIIQ/6dZaWg4rbiO0=\n" +
            "=uOl8\n" +
            "-----END PGP SIGNATURE-----";

    private static String plaintext = "0xecb6b43927394073b072b263e4ead230f1a6987bcab9f067918192d6bdccc8c1";
    // signedString2 > Cannot recognize input data
    // private static String signedString2 = "-----BEGIN PGP SIGNED MESSAGE----- Hash: SHA256 0xecb6b43927394073b072b263e4ead230f1a6987bcab9f067918192d6bdccc8c1 -----BEGIN PGP SIGNATURE----- Version: GnuPG v2 iQEcBAEBCAAGBQJZbW5IAAoJEGp7IeKETHmADTsIAJpELl5bSanJ9fxMuUDQSm3/ n5jCgaBLh0XbA122rEm/6KZLb6jRk771u8NUTrCNky2xSRvCCV+CFKvQ1qOQ0m3j GTDFRqfs3TYMG0nShFA8k9+wXZI+d/iM7DnOxwLO5JM/lGViPs2i6pYEOHf0Jenl CS2vslcu0IILduq0mmljKXjcqtRu1whatFtXU/inCV1grAfQJHuI6ZmdDg4ntTUj mCGrDWrAQfsrgQtlnqeIs/4U8gLQFjlsBhJKstLKbkGnVbHtmwZDmq69dzrLUuwO T2pTl/i4YZ02bGxo8Lx3zvDXZocwtRrMtfR4koeFHt4ctgIIIQ/6dZaWg4rbiO0= =uOl8 -----END PGP SIGNATURE-----";
    private static String signedString3 =
            "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                    "Hash: SHA256\n" +
                    "\n" +
                    "0xf5695854e7c7121ca7f84e65b122b3b1f3e9fc503ee2f4b9bc45846a04f20ef7\n" +
                    "-----BEGIN PGP SIGNATURE-----\n" +
                    "Version: GnuPG v2\n" +
                    "\n" +
                    "iQEcBAEBCAAGBQJaAu3fAAoJEGp7IeKETHmA4b4H/03CqZBsqQwzqx4qWY8Xo94r\n" +
                    "Ha7fk4FBO1Fe8h+zVT6C3MCZc8ZX2B0SsQafGNZMhugDpUCds+DPO2zf7wguBk+U\n" +
                    "SvtQ3xi5iicT0RubcV2hI4jhbL4DvVBv7DeCab44HxAfl7leW3Ft6LAmrR/Hcrc7\n" +
                    "njo6Nz3T/NvSFNaQXBK1uP8bCZMhc+zqOg5C9uEAVNtKa2iPTm6vbuQGDdp9UKBg\n" +
                    "BI6OrTYF4QGeUlxL2A6DpRiZfX0NXlPXjLL0b34fc05xWVXWJjhAw85VgdQwIgmH\n" +
                    "zBQMjW3e9dnDJodrosl+TQ1LGkQLy+R6BPX5JoBDa59F0ncyfO+Z+ILrv+hLLbQ=\n" +
                    "=TEa2\n" +
                    "-----END PGP SIGNATURE-----";

    public static void main(String[] args) {
        // // see: https://stackoverflow.com/questions/9660967/bouncy-castle-no-such-provider-exception
        // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try {
            Boolean result1 = PGPTools.verifySignedString(signedString1, publicKey);
            System.out.println("result: " + result1);

        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            Boolean result2 = PGPTools.verifySignedString(signedString2, publicKey);
//            System.out.println("result: " + result2);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            PGPPublicKey pgpPublicKey = PGPTools.readPublicKeyFromString(publicKey);

            byte[] fingerprintByteArray = pgpPublicKey.getFingerprint();
            String fingerprintStr = DatatypeConverter.printHexBinary(fingerprintByteArray);
            byte[] fingerprintByteArrayParsedFromStr = DatatypeConverter.parseHexBinary(fingerprintStr);

            System.out.println("fingerprintStr: " + fingerprintStr);

            System.out.println("fingerprintByteArray.length:" + fingerprintByteArray.length);
            System.out.println("fingerprintByteArrayParsedFromStr.length:" + fingerprintByteArrayParsedFromStr.length);

            System.out.println(
                    "fingerprintByteArray.equals(DatatypeConverter.parseHexBinary(fingerprintStr)): " +
                            fingerprintByteArray.equals(DatatypeConverter.parseHexBinary(fingerprintStr))
                    // false
                    // "Cause they're not equal, ie: they're different arrays with equal elements inside"
                    // see: https://stackoverflow.com/a/9499610/1697878
            );

            System.out.println(
                    // see: https://stackoverflow.com/a/9499597/1697878
                    "Arrays.equals(fingerprintByteArray, fingerprintByteArrayParsedFromStr): " +
                            Arrays.equals(fingerprintByteArray, fingerprintByteArrayParsedFromStr)
            );

            System.out.println("fingerprintByteArray.toString(): " + fingerprintByteArray.toString());

            System.out.println(
                    // see: https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java#comment12709256_9855338
                    "DatatypeConverter.printHexBinary(fingerprintByteArray): " + DatatypeConverter.printHexBinary(fingerprintByteArray)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
