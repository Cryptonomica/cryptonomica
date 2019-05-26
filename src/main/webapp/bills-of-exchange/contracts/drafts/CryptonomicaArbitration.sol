pragma solidity >=0.5.4 <0.6.0;

/*
developed by Cryptonomica Ltd. (cryptonomica.net), 2019
last version: 2019-02-24
github: https://github.com/Cryptonomica/
*/

contract CryptonomicaVerification {
    function keyCertificateValidUntil(address) public view returns (uint);
}

contract CryptonomicaArbitration {

    CryptonomicaVerification cryptonomicaVerificationContract;

    string public arbitrationClause = "Any dispute, controversy or claim arising out of or relating to contract referring to this arbitration clause, or the breach, termination or invalidity of that contract, shall be settled by arbitration in accordance with the Cryptonomica Arbitration Rules (https://github.com/Cryptonomica/arbitration-rules) in the version in effect at the time of the filing of the claim.";

    address public owner; // smart contract owner (super admin)
    mapping(address => bool) public isArbitrator;

    constructor() public {
        cryptonomicaVerificationContract = CryptonomicaVerification(
            0x846942953c3b2A898F10DF1e32763A823bf6b27f // Ropsten
        );
        // require(cryptonomicaVerificationContract.keyCertificateValidUntil(msg.sender) > now);
        owner = msg.sender;
        isArbitrator[msg.sender] = true;
    }

    /* -------------------- Administrative functions : ---------------------- */

    bool public verificationCheckOn = false;

    function turnVerificationCheckOn() public {
        require(msg.sender == owner);
        require(!verificationCheckOn);
        verificationCheckOn = true;
    }

    // to avoid mistakes: owner (super admin) should be changed in two steps
    // change is valid when accepted from new owner address
    address private newOwner;
    // only owner
    function changeOwnerStart(address _newOwner) public {
        require(msg.sender == owner);
        if (verificationCheckOn) {
            // new owner should have a valid identity verification
            require(cryptonomicaVerificationContract.keyCertificateValidUntil(_newOwner) > now);
        }

        newOwner = _newOwner;
        emit ChangeOwnerStarted(msg.sender, _newOwner);
    } //
    event ChangeOwnerStarted (address indexed startedBy, address indexed newOwner);
    // only by new owner
    function changeOwnerAccept() public {
        require(msg.sender == newOwner);
        if (verificationCheckOn) {
            // new owner should have a valid identity verification
            require(cryptonomicaVerificationContract.keyCertificateValidUntil(msg.sender) > now);
        }
        // event here:
        emit OwnerChanged(owner, newOwner);
        owner = newOwner;
    } //
    event OwnerChanged(address indexed from, address indexed to);

    // only owner
    function addArbitrator(address _address) public {

        require(msg.sender == owner);
        if (verificationCheckOn) {
            // new arbitrator should have a valid identity verification
            require(cryptonomicaVerificationContract.keyCertificateValidUntil(msg.sender) > now);
        }
        isArbitrator[_address] = true;
        emit ArbitratorAdded(_address, msg.sender);

    } //
    event ArbitratorAdded (address indexed added, address indexed addedBy);
    // only owner
    function removeArbitrator(address _address) public {
        require(msg.sender == owner);
        isArbitrator[_address] = false;
        emit ArbitratorRemoved(_address, msg.sender);
    } //
    event ArbitratorRemoved(address indexed removed, address indexed removedBy);

}
