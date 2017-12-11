package net.cryptonomica.returns;

//import org.web3j.tuples.generated.Tuple8;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * like struct in smart contract
 */
public class VerificationStruct implements Serializable {

    /* - Solidity:
    struct Verification {
    string fingerprint; // ..................................................0
    uint keyCertificateValidUntil; // .......................................1
    string firstName; // ....................................................2
    string lastName;// ......................................................3
    uint birthDate; //  .....................................................4
    string nationality; //  .................................................5
    uint verificationAddedOn;// .............................................6
    uint revokedOn; // ......................................................7
    }
    */

    private String fingerprint; // .....................................................0
    private Integer keyCertificateValidUntil; // .......................................1
    private String firstName; // .......................................................2
    private String lastName;// .........................................................3
    private Integer birthDate; //  .....................................................4
    private String nationality; //  ....................................................5
    private Integer verificationAddedOn;// .............................................6
    private Integer revokedOn; // ......................................................7

    /* ----- Constructors */

    public VerificationStruct() {
    }

    public VerificationStruct(
            String fingerprint, // .....................................................0
            Integer keyCertificateValidUntil, // .......................................1
            String firstName, // .......................................................2
            String lastName, // ........................................................3
            Integer birthDate, //  .....................................................4
            String nationality, //  ....................................................5
            Integer verificationAddedOn,// .............................................6
            Integer revokedOn // .......................................................7
    ) {
        this.fingerprint = fingerprint;
        this.keyCertificateValidUntil = keyCertificateValidUntil;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.verificationAddedOn = verificationAddedOn;
        this.revokedOn = revokedOn;
    }

    /*
    public Verification(List<Type> verificationStruct) {
        this.fingerprint = (String) verificationStruct.get(0).getValue();
        this.keyCertificateValidUntil = (Integer) verificationStruct.get(1).getValue();
        this.firstName = (String) verificationStruct.get(2).getValue();
        this.lastName = (String) verificationStruct.get(3).getValue();
        this.birthDate = (Integer) verificationStruct.get(4).getValue();
        this.nationality = (String) verificationStruct.get(5).getValue();
        this.verificationAddedOn = (Integer) verificationStruct.get(6).getValue();
        this.revokedOn = (Integer) verificationStruct.get(7).getValue();
    }*/

    /*
    public Verification(Tuple8<String, BigInteger, String, String, BigInteger, String, BigInteger, BigInteger> tuple8) {
        this.fingerprint = tuple8.getValue1();
        this.keyCertificateValidUntil = tuple8.getValue2().intValueExact();
        this.firstName = tuple8.getValue3();
        this.lastName = tuple8.getValue4();
        this.birthDate = tuple8.getValue5().intValueExact();
        this.nationality = tuple8.getValue6();
        this.verificationAddedOn = tuple8.getValue7().intValueExact();
        this.revokedOn = tuple8.getValue8().intValueExact();
    }
    */

    /* ---- to String */

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /* ----- Getters and Setters */

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Integer getKeyCertificateValidUntil() {
        return keyCertificateValidUntil;
    }

    public void setKeyCertificateValidUntil(Integer keyCertificateValidUntil) {
        this.keyCertificateValidUntil = keyCertificateValidUntil;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Integer birthDate) {
        this.birthDate = birthDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Integer getVerificationAddedOn() {
        return verificationAddedOn;
    }

    public void setVerificationAddedOn(Integer verificationAddedOn) {
        this.verificationAddedOn = verificationAddedOn;
    }

    public Integer getRevokedOn() {
        return revokedOn;
    }

    public void setRevokedOn(Integer revokedOn) {
        this.revokedOn = revokedOn;
    }
}
