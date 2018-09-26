package net.cryptonomica.ethereum;

import org.apache.commons.codec.binary.StringUtils;
import org.web3j.abi.datatypes.generated.Bytes20;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Hash;

import javax.xml.bind.DatatypeConverter;


public class Web3jServices {

    // see:
    // https://github.com/web3j/web3j/blob/master/utils/src/main/java/org/web3j/crypto/Hash.java#L12
    public static String keccak256FromHexEncodedInputDataAsString(String hexInput) {
        return Hash.sha3(hexInput);
    }

    // see code of:
    // https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html#toString-byte:A-
    // this uses the same idea
    // to convert byte[] to representation like '0xf463b5590fa2a73f5296122649ed544fd1c3eeb35a63b71a883a31e330d22412'
    public static String bytesArrayToHexString(byte[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";
        StringBuilder b = new StringBuilder();
        b.append("0x");
        for (int i = 0; ; i++) {
            b.append(
                    Integer.toHexString(a[i])
            );
            if (i == iMax)
                return b.toString();
        }
    }

    /*
     * see:
     * https://ethereum.stackexchange.com/questions/23549/convert-string-to-bytes32-in-web3j
     * */
    public static Bytes32 stringToBytes32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return new Bytes32(byteValueLen32);
    }

    public static String bytes32toString(Bytes32 bytes32) {
        return StringUtils.newStringUsAscii(bytes32.getValue());
    }

    //
    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("hex string length should be even");
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int b = Integer.parseInt(
                    hexString.substring(index, index + 2), // string
                    16 // radix
            );
            bytes[i] = (byte) b;
        }
        return bytes;
    }

    //  // https://stackoverflow.com/a/5942951/1697878
    public static byte[] hexStringToByteArrayDatatypeConverter(String hexString) {
        return DatatypeConverter.parseHexBinary(hexString);
    }

    // https://stackoverflow.com/a/5942951/1697878
    public static String byteArrayToHexString(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    /*
    * hexString - string like "0D13CC9610D9C5757C06D48D54273971EB45F1E8"
    * we can use this to store OpenPGP key fingerprint as bytes20 in smart contract
    * */
    public static Bytes20 bytes20FromHexString(String hexString){
        byte[] bytes = DatatypeConverter.parseHexBinary(hexString);
        Bytes20 bytes20 = new Bytes20(bytes);
        return bytes20;
    }

    public static String byteArrayAsSting(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (byte b : bytes) {
            stringBuilder.append(
                    // Byte.toString(b)// with negative numbers
                    Byte.toUnsignedInt(b) // to get always positive value (Java8)
            );
            stringBuilder.append(" ");
        }
        // remove last " "
        stringBuilder.delete(stringBuilder.lastIndexOf(" "), stringBuilder.lastIndexOf(" ") + 1);
        stringBuilder.append("]");
        return stringBuilder.toString();
        // result like: [13 19 204 150 16 217 197 117 124 6 212 141 84 39 57 113 235 69 241 232]
    }

    // see: https://stackoverflow.com/a/2817883/1697878
    public static String byteArrayAsHexSting(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (byte b : bytes) {
            stringBuilder.append(
                    String.format("%02X", b)
            );
            stringBuilder.append(" ");
        }
        // remove last " "
        stringBuilder.delete(stringBuilder.lastIndexOf(" "), stringBuilder.lastIndexOf(" ") + 1);
        stringBuilder.append("]");
        return stringBuilder.toString();
        // result like: [0D 13 CC 96 10 D9 C5 75 7C 06 D4 8D 54 27 39 71 EB 45 F1 E8]
    }

}

