/**
 * Source Code first verified at https://etherscan.io on Thursday, May 23, 2019
 (UTC) */

pragma solidity >=0.5.8 <0.6.0;

/*
* 0xE48BC3dB5b512d4A3e3Cd388bE541Be7202285B5
* https://ropsten.etherscan.io/address/0xe48bc3db5b512d4a3e3cd388be541be7202285b5
*
*/

contract CryptonomicaVerificationMockUp {

    struct Verification {
        // all string have to be <= 32 chars
        string fingerprint; // ................................................0
        uint keyCertificateValidUntil; // .....................................1
        string firstName; // ..................................................2
        string lastName;// ....................................................3
        uint birthDate; //  ...................................................4
        string nationality; //  ...............................................5
        uint verificationAddedOn;// ...........................................6
        uint revokedOn; // ....................................................7
        string signedString; //................................................8
        // uint256 signedStringUploadedOnUnixTime; //... Stack too deep
    }

    // see:
    // https://ethereum.stackexchange.com/questions/3609/returning-a-struct-and-reading-via-web3
    function verification(address) public view returns (
        string memory fingerprint,
        uint,
        string memory firstName,
        string memory lastName,
        uint birthDate,
        string memory nationality,
        uint verificationAddedOn,
        uint revokedOn,
        string memory signedString
    ){
        return (
        "0A0B0A0B0A0B0A0B0A0B0A0B0A0B0A0B0A0B0A0B",
        now + 1 days,
        "John",
        "Doe",
        0,
        "XX",
        now - 30 days,
        0,
        "signed string"
        );
    }

    /**
    * @param _address The address to check
    * @return Unix time
    * > here returns always now + 1 days
    */
    function keyCertificateValidUntil(address _address) external view returns (uint unixTime){
        return now + 1 days;
    }

    /**
    * @param _address The address to check
    * @return 0 if key certificate is not revoked, or Unix time of revocation
    * > here returns 0 always
    */
    function revokedOn(address _address) external view returns (uint unixTime){
        return 0;
    }

}