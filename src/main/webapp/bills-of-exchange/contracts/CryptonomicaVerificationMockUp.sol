pragma solidity >=0.5.7 <0.6.0;

/*
https://ropsten.etherscan.io/address/0x7f05e9807f509281a8db8f8b5230b89a96650087
deployed on block: 5452681
*/

contract CryptonomicaVerificationMockUp {

    constructor() public {}

    function keyCertificateValidUntil(address) public view returns (uint){
        return now + 1 days;
    }

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
}
