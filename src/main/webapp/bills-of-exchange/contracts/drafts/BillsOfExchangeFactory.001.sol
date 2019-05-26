pragma solidity >=0.5.8 <0.6.0;

/*
* developed by Cryptonomica Ltd.(cryptonomica.net), 2019
* last version: 2019-05-11
* github: https://github.com/Cryptonomica/
* contract address:
* deployed on block:
*/

/**
* LEGAL:
* aim of this contract is to create a mechanism to draw, transfer and accept negotiable instruments
* that that will be recognized as 'bills of exchange' according at least to following regulations:
*
* 1) Convention providing a Uniform Law for Bills of Exchange and Promissory Notes (Geneva, 7 June 1930):
* https://www.jus.uio.no/lm/bills.of.exchange.and.promissory.notes.convention.1930/doc.html
* https://treaties.un.org/Pages/LONViewDetails.aspx?src=LON&id=552&chapter=30&clang=_en
*
* 2) U.K. Bills of Exchange Act 1882:
* http://www.legislation.gov.uk/ukpga/Vict/45-46/61/section/3
*
* and as a 'draft' according to
* U.S. Uniform Commercial Code
* https://www.law.cornell.edu/ucc/3/3-104
*
* see more on: https://github.com/Cryptonomica/cryptonomica/wiki/electronic-bills-of-exchange
*
* Bills of exchange created with this smart contract are payable to the bearer,
* and can be transferred using Ethereum blockchain (from one blockchain address to another)
*
*/

/* --- LIBRARIES */

/**
 * @title SafeMath
 * @dev Unsigned math operations with safety checks that revert on error
 * source:
 * https://github.com/OpenZeppelin/openzeppelin-solidity/blob/master/contracts/math/SafeMath.sol
 * commit 67bca85 on Apr 25, 2019
 */
library SafeMath {
    /**
     * @dev Multiplies two unsigned integers, reverts on overflow.
     */
    function mul(uint256 a, uint256 b) internal pure returns (uint256) {
        // Gas optimization: this is cheaper than requiring 'a' not being zero, but the
        // benefit is lost if 'b' is also tested.
        // See: https://github.com/OpenZeppelin/openzeppelin-solidity/pull/522
        if (a == 0) {
            return 0;
        }

        uint256 c = a * b;
        require(c / a == b, "SafeMath: multiplication overflow");

        return c;
    }

    /**
     * @dev Integer division of two unsigned integers truncating the quotient, reverts on division by zero.
     */
    function div(uint256 a, uint256 b) internal pure returns (uint256) {
        // Solidity only automatically asserts when dividing by 0
        require(b > 0, "SafeMath: division by zero");
        uint256 c = a / b;
        // assert(a == b * c + a % b); // There is no case in which this doesn't hold

        return c;
    }

    /**
     * @dev Subtracts two unsigned integers, reverts on overflow (i.e. if subtrahend is greater than minuend).
     */
    function sub(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b <= a, "SafeMath: subtraction overflow");
        uint256 c = a - b;

        return c;
    }

    /**
     * @dev Adds two unsigned integers, reverts on overflow.
     */
    function add(uint256 a, uint256 b) internal pure returns (uint256) {
        uint256 c = a + b;
        require(c >= a, "SafeMath: addition overflow");

        return c;
    }

    /**
     * @dev Divides two unsigned integers and returns the remainder (unsigned integer modulo),
     * reverts when dividing by zero.
     */
    function mod(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b != 0, "SafeMath: modulo by zero");
        return a % b;
    }
}

/**
* @title Contract that will work with ERC-677 tokens
* see:
* https://github.com/ethereum/EIPs/issues/677
* https://github.com/smartcontractkit/LinkToken/blob/master/contracts/ERC677Token.sol
*/
contract ERC677Receiver {
    /**
    * The function is added to contracts enabling them to react to receiving tokens within a single transaction.
    * The from parameter is the account which just transferred amount from the token contract. data is available to pass
    * additional parameters, i.e. to indicate what the intention of the transfer is if a contract allows transfers for multiple reasons.
    * @param from address sending tokens
    * @param amount of tokens
    * @param data to send to another contract
    */
    function onTokenTransfer(address from, uint256 amount, bytes calldata data) external returns (bool success);
}

/**
* @title Contract that will work with ERC-223 tokens
* see: https://github.com/ethereum/EIPs/issues/223
*/
contract ERC223ReceivingContract {
    /**
     * @dev Standard ERC223 function that will handle incoming token transfers.
     * @param _from  Token sender address.
     * @param _value Amount of tokens.
     * @param _data  Transaction metadata.
     */
    function tokenFallback(address _from, uint _value, bytes calldata _data) external;
}

/**
 * @title Contract that implements:
 * ERC-20  (https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20.md)
 * ERC-223 (https://github.com/ethereum/EIPs/issues/223
 * ERC-677 (https://github.com/ethereum/EIPs/issues/677)
 * overloaded 'approve' function (https://docs.google.com/document/d/1YLPtQxZu1UAvO9cZ1O2RPXBbT0mooh4DYKjA_jp-RLM/)
*/
contract Token {

    using SafeMath for uint256;

    /* --- ERC-20 variables */

    string public name;

    string public symbol;

    uint8 public decimals = 0;

    uint256 public totalSupply;

    mapping(address => uint256) public balanceOf;

    mapping(address => mapping(address => uint256)) public allowance;

    /*
    * stored address that deployed this smart contract to blockchain
    */
    address public creator;

    /**
    * Constructor
    * no args constructor make possible to create contracts with code pre-verified on etherscan.io
    * (once we verify one contract all next contracts with the same code and constructor args will be verified by etherscan)
    */
    constructor() public {
        creator = msg.sender;
    }

    bool private initialized = false;

    /*
    * initializes token: set initial values for erc20 variables
    * assigns all tokens ('totalSupply') to one address ('tokensOwner')
    * @param _name name of the token
    * @param _symbol symbol of the token
    * @param _totalSupply amount of tokens to create
    * @param _tokenOwner address that will initially hold all created tokens
    */
    function initToken(
        string calldata _name,
        string calldata _symbol,
        uint256 _totalSupply,
        address tokenOwner
    ) external returns (bool success){

        require(!initialized, "Token contract was already initialized");
        require(creator == msg.sender, "Only creator can initialize token contract");

        name = _name;
        symbol = _symbol;
        totalSupply = _totalSupply;
        balanceOf[tokenOwner] = totalSupply;
        initialized = true;

        return true;
    }

    /* --- ERC-20 events */

    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#events

    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#transfer-1
    event Transfer(address indexed from, address indexed to, uint256 value);

    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#approval
    event Approval(address indexed _owner, address indexed spender, uint256 value);

    /* --- Events for interaction with other smart contracts */

    event DataSentToAnotherContract(address indexed _from, address indexed _toContract, bytes _extraData);

    /* --- ERC-20 Functions */
    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#methods

    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#transferfrom
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool){

        // Transfers of 0 values MUST be treated as normal transfers and fire the Transfer event (ERC-20)
        // Variables of uint type cannot be negative. Thus, comparing uint variable with zero (greater than or equal) is redundant
        // require(_value >= 0);

        require(_to != address(0));

        // The function SHOULD throw unless the _from account has deliberately authorized the sender of the message via some mechanism
        require(msg.sender == _from || _value <= allowance[_from][msg.sender], "Sender not authorized");

        // check if _from account have required amount
        require(_value <= balanceOf[_from], "Account doesn't have required amount");

        // Subtract from the sender
        balanceOf[_from] = balanceOf[_from].sub(_value);
        // Add the same to the recipient
        balanceOf[_to] = balanceOf[_to].add(_value);

        balanceOf[_from] = balanceOf[_from].sub(_value);
        balanceOf[_to] = balanceOf[_to].add(_value);

        // If allowance used, change allowances correspondingly
        if (_from != msg.sender && _from != address(this)) {
            allowance[_from][msg.sender] = allowance[_from][msg.sender].sub(_value);
        }

        emit Transfer(_from, _to, _value);

        return true;
    } // end of transferFrom

    // https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#transfer
    function transfer(address _to, uint256 _value) public returns (bool success){
        return transferFrom(msg.sender, _to, _value);
    }

    /**
    * overloaded transfer : ERC-223
    * see: https://github.com/ethereum/EIPs/issues/223
    * https://github.com/Dexaran/ERC223-token-standard/blob/Recommended/ERC223_Token.sol
    */
    function transfer(address _to, uint _value, bytes memory _data) public returns (bool success){
        if (transfer(_to, _value)) {
            ERC223ReceivingContract receiver = ERC223ReceivingContract(_to);
            receiver.tokenFallback(msg.sender, _value, _data);
            emit DataSentToAnotherContract(msg.sender, _to, _data);
            return true;
        }
        return false;
    }

    /**
    * ERC-677
    * https://github.com/ethereum/EIPs/issues/677
    * transfer tokens with additional info to another smart contract, and calls its correspondent function
    * @param _to - another smart contract address
    * @param _value - number of tokens
    * @param _extraData - data to send to another contract
    * this is a recommended method to send tokens to smart contracts
    */
    function transferAndCall(address _to, uint256 _value, bytes memory _extraData) public returns (bool success){
        if (transferFrom(msg.sender, _to, _value)) {
            ERC677Receiver receiver = ERC677Receiver(_to);
            if (receiver.onTokenTransfer(msg.sender, _value, _extraData)) {
                emit DataSentToAnotherContract(msg.sender, _to, _extraData);
                return true;
            }
        }
        return false;
    }

    /**
    * the same as above ('transferAndCall'), but for all tokens on user account
    * for example for converting ALL tokens of user account to another tokens
    */
    function transferAllAndCall(address _to, bytes calldata _extraData) external returns (bool){
        return transferAndCall(_to, balanceOf[msg.sender], _extraData);
    }

    /*
    *  https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#approve
    * there is and attack:
    * https://github.com/CORIONplatform/solidity/issues/6,
    * https://drive.google.com/file/d/0ByMtMw2hul0EN3NCaVFHSFdxRzA/view
    * but this function is required by ERC-20:
    * To prevent attack vectors like the one described on https://docs.google.com/document/d/1YLPtQxZu1UAvO9cZ1O2RPXBbT0mooh4DYKjA_jp-RLM/
    * and discussed on https://github.com/ethereum/EIPs/issues/20#issuecomment-263524729 ,
    * clients SHOULD make sure to create user interfaces in such a way that they set the allowance first to 0 before
    * setting it to another value for the same spender.
    * THOUGH The contract itself shouldn’t enforce it, to allow backwards compatibility with contracts deployed before
    *
    * @param _spender The address which will spend the funds.
    * @param _value The amount of tokens to be spent.
    */
    function approve(address _spender, uint256 _value) public returns (bool success){
        allowance[msg.sender][_spender] = _value;
        emit Approval(msg.sender, _spender, _value);
        return true;
    }

    /**
    * Overloaded approve (see https://solidity.readthedocs.io/en/v0.5.7/contracts.html#function-overloading) approve function
    * see https://docs.google.com/document/d/1YLPtQxZu1UAvO9cZ1O2RPXBbT0mooh4DYKjA_jp-RLM/
    */
    function approve(address _spender, uint256 _currentValue, uint256 _value) external returns (bool success){
        require(allowance[msg.sender][_spender] == _currentValue);
        return approve(_spender, _value);
    }

}

/**
* Contract which provides information if given address is an address of person included in Cryptonomica arbitrators list
*/
// contract CryptonomicaArbitration {
//     /**
//     * @param _address address to check
//     */
//     function isArbitrator(address _address) external view returns (bool result);
// }

/**
* see: https://www.cryptonomica.net/#!/verifyEthAddress/
* in our smart contract:
* 1) every new admin should have a verified identity on cryptonomica.net
* 2) every person tha signs (draw or accept) a bill should be verified
*/
contract CryptonomicaVerification {

    // returns 0 if verification is not revoked
    function revokedOn(address _address) external view returns (uint unixTime);

    function keyCertificateValidUntil(address _address) external view returns (uint unixTime);
}

/*
* universal functions for smart contract managements
*/
contract ManagedContract {

    /*
    * smart contract that provides information about person that owns given Ethereum address/key
    */
    CryptonomicaVerification public cryptonomicaVerification;

    /*
    * ledger of admins
    */
    mapping(address => bool) isAdmin;

    modifier onlyAdmin() {
        require(isAdmin[msg.sender], "Only admin can do that");
        _;
    }

    /**
    * @param from old address
    * @param to new address
    * @param by who made a change
    */
    event CryptonomicaVerificationContractAddressChanged(address from, address to, address indexed by);

    /**
    * @param _newAddress address of new contract to be used to verify identity of new admins
    */
    function changeCryptonomicaVerificationContractAddress(address _newAddress) public onlyAdmin returns (bool success) {

        emit CryptonomicaVerificationContractAddressChanged(address(cryptonomicaVerification), _newAddress, msg.sender);

        cryptonomicaVerification = CryptonomicaVerification(_newAddress);

        return true;
    }

    /**
    * @param added new admin address
    * @param addedBy who added new admin
    */
    event AdminAdded(
        address indexed added,
        address indexed addedBy
    );

    /**
    * @param _newAdmin address of new admin
    */
    function addAdmin(address _newAdmin) public onlyAdmin returns (bool success){

        require(cryptonomicaVerification.keyCertificateValidUntil(_newAdmin) > now, "New admin has to be verified on Cryptonomica.net");

        // revokedOn returns uint256 (unix time), it's 0 if verification is not revoked
        require(cryptonomicaVerification.revokedOn(_newAdmin) == 0, "Verification for this address was revoked, can not add");

        isAdmin[_newAdmin] = true;

        emit AdminAdded(_newAdmin, msg.sender);

        return true;
    }

    /**
    * @param removed removed admin
    * @param removedBy who removed admin
    */
    event AdminRemoved(
        address indexed removed,
        address indexed removedBy
    );

    /**
    * @param _oldAdmin address to remove from admins
    */
    function removeAdmin(address _oldAdmin) external onlyAdmin returns (bool){

        require(msg.sender != _oldAdmin, "Admin can not remove himself");

        isAdmin[_oldAdmin] = false;

        emit AdminRemoved(_oldAdmin, msg.sender);

        return true;
    }

    /* --- financial management */

    /*
    * address to send Ether from this contract
    */
    address payable public withdrawalAddress;

    /*
    * withdrawal address can be fixed (protected from changes),
    */
    bool public withdrawalAddressFixed = false;

    /*
    * @param from old address
    * @param to new address
    * @param changedBy who made this change
    */
    event WithdrawalAddressChanged(address indexed from, address indexed to, address indexed changedBy);

    /*
    * @param _withdrawalAddress address to which funds from this contract will be sent
    */
    function setWithdrawalAddress(address payable _withdrawalAddress) public onlyAdmin returns (bool success) {

        require(!withdrawalAddressFixed, "Withdrawal address already fixed");
        require(_withdrawalAddress != address(0), "Wrong address: 0");
        require(_withdrawalAddress != address(this), "Wrong address: contract itself");

        emit WithdrawalAddressChanged(withdrawalAddress, _withdrawalAddress, msg.sender);

        withdrawalAddress = _withdrawalAddress;

        return true;
    }

    /*
    * this event can be fired one time only
    * @param withdrawalAddressFixedAs address for withdrawal
    * @param who made this (msg.sender)
    */
    event WithdrawalAddressFixed(address withdrawalAddressFixedAs, address fixedBy);

    /**
    * @param _withdrawalAddress address to which funds from this contract will be sent
    */
    function fixWithdrawalAddress(address _withdrawalAddress) external onlyAdmin returns (bool success) {

        // prevents event if already fixed
        require(!withdrawalAddressFixed, "Can't change, address fixed");

        // check, to prevent fixing wrong address
        require(withdrawalAddress == _withdrawalAddress, "Wrong address in argument");

        withdrawalAddressFixed = true;

        emit WithdrawalAddressFixed(withdrawalAddress, msg.sender);

        return true;
    }

    /**
    * @param to address to which ETH was sent
    * @param sumInWei sum sent (in wei)
    * @param by who made withdrawal (msg.sender)
    * @param success if withdrawal was successful
    */
    event Withdrawal(
        address indexed to,
        uint sumInWei,
        address indexed by,
        bool indexed success
    );

    // !!! can be called by any user or contract
    // warning: check for reentrancy vulnerability http://solidity.readthedocs.io/en/develop/security-considerations.html#re-entrancy
    // >>> since we are making a withdrawal to our own contract only there is no possible attack using re-entrancy vulnerability,
    function withdrawAllToWithdrawalAddress() external returns (bool success) {

        // http://solidity.readthedocs.io/en/develop/security-considerations.html#sending-and-receiving-ether
        // about <address>.send(uint256 amount) and <address>.transfer(uint256 amount)
        // see: http://solidity.readthedocs.io/en/latest/units-and-global-variables.html?highlight=transfer#address-related
        // https://ethereum.stackexchange.com/questions/19341/address-send-vs-address-transfer-best-practice-usage

        uint sum = address(this).balance;

        if (!withdrawalAddress.send(sum)) {// makes withdrawal and returns true (success) or false

            emit Withdrawal(withdrawalAddress, sum, msg.sender, false);

            return false;
        }

        emit Withdrawal(withdrawalAddress, sum, msg.sender, true);

        return true;
    }

}

contract ManagedContractWithPaidService is ManagedContract {

    uint256 public price;

    event PriceChanged(uint256 from, uint256 to, address indexed by);

    function changePrice(uint256 _newPrice) public onlyAdmin returns (bool){
        emit PriceChanged(price, _newPrice, msg.sender);
        price = _newPrice;
        return true;
    }
}

//contract ManagedContractUsingCryptonomicaServices is ManagedContract {
//    /* --- Cryptonomica Verification Contract */
//
//    CryptonomicaVerification public cryptonomicaVerification;
//
//    event CryptonomicaVerificationContractAddressChanged(address from, address to, address indexed by);
//
//    function changeCryptonomicaVerificationContractAddress(address _newCryptonomicaVerificationContractAddress) public onlyAdmin returns (bool){
//
//        emit CryptonomicaVerificationContractAddressChanged(address(cryptonomicaVerification), _newCryptonomicaVerificationContractAddress, msg.sender);
//
//        cryptonomicaVerification = CryptonomicaVerification(
//            _newCryptonomicaVerificationContractAddress
//        );
//        return true;
//    }
//
//    /* --- Cryptonomica Arbitration Contract */
//
//    CryptonomicaArbitration public cryptonomicaArbitration;
//
//    event CryptonomicaArbitrationContractAddressChanged(address from, address to, address indexed by);
//
//    function changeCryptonomicaArbitrationContractAddress(address _newCryptonomicaArbitrationContractAddress) public onlyAdmin returns (bool){
//
//        emit CryptonomicaArbitrationContractAddressChanged(address(cryptonomicaArbitration), _newCryptonomicaArbitrationContractAddress, msg.sender);
//
//        cryptonomicaArbitration = CryptonomicaArbitration(
//            _newCryptonomicaArbitrationContractAddress
//        );
//        return true;
//    }
//}

/**
 * This contract represents a bunch of bills of exchange issued by one person
 * at the same time and on the same conditions
 */
contract BillsOfExchange is Token {

    /* ---- Bill of Exchange requisites: */

    uint256 public billsOfExchangeContractNumber;

    // person who issues the bill (drawer).
    string public drawerName;

    // Ethereum address of the signer
    // address payable public drawerRepresentedBy;
    address public drawerRepresentedBy;

    // link to information about signers authority to represent the drawer
    string public linkToSignersAuthorityToRepresentTheDrawer;

    // to
    // (the name of the person who is to pay)
    string public drawee;
    address public draweeSignerAddress;
    string  public linkToSignersAuthorityToRepresentTheDrawee;

    string public description = "Every token (ERC20 token without allowance functions) in this smart contract is a bill of exchange in blank - payable to bearer (bearer is the owner of the Ethereum address witch holds the tokens, or the person he/she represents), but not to order - that means no endorsement possible and the token holder can only transfer the token (bill of exchange in blank) itself using functions of this smart contract. In the case of the Ethereum blockchain fork, the blockchain that has the highest hashrate is considered valid, and all others are not considered a valid registry; bill payment a valid registry settles bill even if valid blockchain (hashrate) changes after the payment. All Ethereum test networks are not valid registries";

    string public order = "pay to bearer (tokenholder), but not to order, the sum defined for each token in currency defined in 'currency' (according to ISO 4217 standard; or XAU for for one troy ounce of gold, XBT or BTC for Bitcoin, ETH for Ether, DASH for Dash, ZEC for Zcash, XRP for Ripple, XMR for Monero)";

    // a statement of the time of payment
    uint256 public timeOfPaymentUnixTime; // if the same time as issuedOnUnixTime - "at sight"

    // A statement of the date and of the place where the bill is issued
    uint256 public issuedOnUnixTime;
    string public placeWhereTheBillIsIssued; //  i.e. "London, U.K.";

    // a statement of the place where payment is to be made;
    // what meaning does it have if bill is electronic?
    // usually it is an address of the payer
    string public placeWherePaymentIsToBeMade;

    // https://en.wikipedia.org/wiki/ISO_4217
    string public currency; // for example: "EUR", "USD"
    uint256 public sumToBePaidForEveryToken; //

    /* --- arbitration */
    // CryptonomicaArbitration public cryptonomicaArbitration;

    string public arbitrationAgreement =
    "Any dispute, controversy or claim arising out of or relating to this bill(s) of exchange, including invalidity thereof and payments based on this bill(s), shall be settled by arbitration in accordance with the Cryptonomica Arbitration Rules (https://github.com/Cryptonomica/arbitration-rules) in the version in effect at the time of the filing of the claim";
    uint256 public arbitrationAgreementSignaturesCounter;
    mapping(uint256 => address) public arbitrationAgreementSignatures;
    mapping(uint256 => string) public arbitrationAgreementSignatories;

    event ArbitrationAgreementSigned(
        uint256 signatureNumber,
        string signedBy,
        address representedBy,
        uint256 signedOn
    );//
    function signArbitrationAgreement(address _signatoryAddress, string memory _signatoryName) private returns (bool success){
        arbitrationAgreementSignaturesCounter++;
        arbitrationAgreementSignatures[arbitrationAgreementSignaturesCounter] = _signatoryAddress;
        arbitrationAgreementSignatories[arbitrationAgreementSignaturesCounter] = _signatoryName;
        emit ArbitrationAgreementSigned(arbitrationAgreementSignaturesCounter, _signatoryName, _signatoryAddress, now);
        return true;
    }

    /* --- Other functions */

    function initBillsOfExchange(
        uint _billsOfExchangeContractNumber,
        string calldata _currency, // for example: "EUR", "USD"
        uint256 _sumToBePaidForEveryToken,
        string calldata _drawerName, // person who issues the bill (drawer)
        address _drawerRepresentedBy, //
        string calldata _linkToSignersAuthorityToRepresentTheDrawer, //
        string calldata _drawee, // the name of the person who is to pay (can be the same as drawer)
        address _draweeSignerAddress //
    // CryptonomicaArbitration _cryptonomicaArbitration
    ) external returns (bool success) {

        require(msg.sender == creator);

        billsOfExchangeContractNumber = _billsOfExchangeContractNumber;

        // https://en.wikipedia.org/wiki/ISO_4217
        currency = _currency;

        sumToBePaidForEveryToken = _sumToBePaidForEveryToken;

        // person who issues the bill (drawer)
        drawerName = _drawerName;
        drawerRepresentedBy = _drawerRepresentedBy;
        linkToSignersAuthorityToRepresentTheDrawer = _linkToSignersAuthorityToRepresentTheDrawer;

        // to
        // (the name of the person who is to pay)
        drawee = _drawee;
        draweeSignerAddress = _draweeSignerAddress;

        // cryptonomicaArbitration = _cryptonomicaArbitration;
        signArbitrationAgreement(drawerRepresentedBy, drawerName);

        return true;
    }

    // not included in 'init' because of exception: 'Stack too deep, try using fewer variables.'
    function setPlacesAndTime(
        uint256 _timeOfPaymentUnixTime, // if now 'at sight'
        string calldata _placeWhereTheBillIsIssued,
        string calldata _placeWherePaymentIsToBeMade
    ) external returns (bool success) {
        require(msg.sender == creator);
        // A statement of the date and of the place where the bill is issued
        issuedOnUnixTime = now;
        if (_timeOfPaymentUnixTime == 0) {
            timeOfPaymentUnixTime = now;
            // "at sight"
        } else {
            timeOfPaymentUnixTime = _timeOfPaymentUnixTime;
        }
        placeWhereTheBillIsIssued = _placeWhereTheBillIsIssued;
        placeWherePaymentIsToBeMade = _placeWherePaymentIsToBeMade;
        return true;
    }

    uint256 public acceptedOnUnixTime;

    event Acceptance(
        uint256 _acceptedOnUnixTime,
        string _drawee,
        address draweeRepresentedBy
    );

    // function for drawee to accept bill of exchange
    // see:
    // http://www.legislation.gov.uk/ukpga/Vict/45-46/61/section/17
    // https://www.jus.uio.no/lm/bills.of.exchange.and.promissory.notes.convention.1930/doc.html#69
    function accept(string memory _linkToSignersAuthorityToRepresentTheDrawee) public returns (address) {

        require(msg.sender == draweeSignerAddress);
        // require(msg.value>=price);

        linkToSignersAuthorityToRepresentTheDrawee = _linkToSignersAuthorityToRepresentTheDrawee;
        signArbitrationAgreement(msg.sender, drawee);
        acceptedOnUnixTime = now;
        emit Acceptance(acceptedOnUnixTime, drawee, msg.sender);

        return address(this);
    }

    /* ---- Payment */
    // see:
    // https://www.jus.uio.no/lm/bills.of.exchange.and.promissory.notes.convention.1930/doc.html#134

    mapping(address => uint256) public billsPresentedForPayment;
    mapping(address => string) public paymentDetails;

    event BillsPresentedForPayment(
        uint256 onUnixTime,
        address by,
        uint256 numberOfTokens,
        string payToDetails
    );

    function presentBillsForPayment(
        uint256 _value, // number of tokens (bills of exchange) to pay
        string memory _payTo // optional payments details (can also be sent in private message)
    ) public returns (bool){

        require(_value > 0);
        // only token holder (not using allowance):
        require(_value <= balanceOf[msg.sender]);

        // Subtract from the sender's balance
        balanceOf[msg.sender] = balanceOf[msg.sender].sub(_value);
        // Add the same to the sender's billsPresentedForPayment
        billsPresentedForPayment[msg.sender] = billsPresentedForPayment[msg.sender].add(_value);
        // update paymentDetails:
        paymentDetails[msg.sender] = _payTo;

        emit BillsPresentedForPayment(now, msg.sender, _value, _payTo);
        return true;
    }

    // payment can be confirmed by bills of exchange holder or, in case of dispute, by arbitrator
    // tokens will be burned, and corresponding sum of ETH used for back up obligations will be returned
    function confirmPaymentsFor(uint256 _value) public returns (bool){

        require(_value > 0);
        require(
        // billsPresentedForPayment[_payee] >= _value || cryptonomicaArbitration.isArbitrator(msg.sender)
            billsPresentedForPayment[msg.sender] >= _value
        );

        // burn tokens:
        billsPresentedForPayment[msg.sender] = billsPresentedForPayment[msg.sender].sub(_value);
        totalSupply = totalSupply.sub(_value);

        // if contract backed up by ETH, release part of the funds:
        // if (address(this).balance > 0) {
        //     // transfer weiBalance / totalSupply * _value back to drawer
        //     address to = address(drawerRepresentedBy);
        //     uint256 sumToPay = address(this).balance.mul(_value).div(totalSupply);
        //     to.transfer(sumToPay);
        // }

        return true;
    }

    // function confirmPayments(uint256 _value) public returns (bool){
    //     return confirmPaymentsFor(msg.sender, _value);
    // }

}



/*
BillsOfExchangeFactory :
https://ropsten.etherscan.io/address/0xa535386ffa1019a3816730960eef0f5a88ede4a2
*/
//contract BillsOfExchangeFactory is ManagedContractWithPaidService, ManagedContractUsingCryptonomicaServices {
contract BillsOfExchangeFactory is ManagedContractWithPaidService {

    using SafeMath for uint256;

    /* --- Constructor */

    constructor() public {
        isAdmin[msg.sender] = true;
        require(changePrice(0.01 ether));
        // Ropsten: > verification always valid for any address
        require(changeCryptonomicaVerificationContractAddress(0x60E4196aba63e7ecC95280B6245b937011246A09));
        // Ropsten:
        // require(changeCryptonomicaArbitrationContractAddress(0x5DA4AC71400Bfd5Ad967c2C892fDD1D404a7F18a));
    }

    /**
    * every bills of exchange contract will have a number
    */
    uint256 public billsOfExchangeContractsCounter;

    /**
    * ledger bills of exchange contract number => bills of exchange contract address
    */
    mapping(uint256 => address) public billsOfExchangeContractsLedger;

    function createBillsOfExchange(
        string memory _name,
        uint256 _totalSupply,
        string memory _currency, // for example: "EUR", "USD"
        uint256 _sumToBePaidForEveryToken,
    //
        string memory _drawerName, // person who issues the bill (drawer)
    // address _drawerRepresentedBy, // <<< msg.sender
        string memory _linkToSignersAuthorityToRepresentTheDrawer,
    // to
    // (the name of the person who is to pay)
        string memory _drawee,
        address _draweeSignerAddress,
    // string memory _linkToSignersAuthorityToRepresentTheDrawee,

        uint256 _timeOfPaymentUnixTime, // if now 'at sight'

    // A statement of the date and of the place where the bill is issued
        string memory _placeWhereTheBillIsIssued,
        string memory _placeWherePaymentIsToBeMade

    ) public payable returns (address newBillsOfExhangeContractAddress) {

        require(msg.value >= price, "Payment sent was lower than the price for creating Bills of Exchange");

        // signer should have valid identity verification
        require(cryptonomicaVerification.keyCertificateValidUntil(msg.sender) > now);
        require(cryptonomicaVerification.keyCertificateValidUntil(_draweeSignerAddress) > now);


        BillsOfExchange billsOfExchange = new BillsOfExchange();
        billsOfExchangeContractsCounter++;
        billsOfExchangeContractsLedger[billsOfExchangeContractsCounter] = address(billsOfExchange);


        billsOfExchange.initToken(
            _name, //
            _name, // symbol
            _totalSupply,
            msg.sender // tokenOwner (drawer or drawer representative)
        );

        billsOfExchange.initBillsOfExchange(
            _currency,
            _sumToBePaidForEveryToken,
            _drawerName,
            msg.sender,
            _linkToSignersAuthorityToRepresentTheDrawer,
            _drawee,
            _draweeSignerAddress,
            cryptonomicaVerification
        );

        billsOfExchange.setPlacesAndTime(_timeOfPaymentUnixTime, _placeWhereTheBillIsIssued, _placeWherePaymentIsToBeMade);

        return address(billsOfExchange);
    }

    /*
    * this function is to create bills of exchange and accept them by the drawer
    */
    function createAndAcceptBillsOfExchange(
        string calldata _name,
        uint256 _totalSupply,
        string calldata _currency, // for example: "EUR", "USD"
        uint256 _sumToBePaidForEveryToken,
        string calldata _drawerName, // person who issues the bill (drawer)
        string calldata _linkToSignersAuthorityToRepresentTheDrawer,
    // string memory _drawee, // in this case the same as drawer
    // address _draweeSignerAddress,
        uint256 _timeOfPaymentUnixTime, // if now 'at sight'
        string calldata _placeWhereTheBillIsIssued,
        string calldata _placeWherePaymentIsToBeMade
    ) external payable returns (address newBillsOfExhangeContractAddress) {

        address billsOfExchangeAddress = createBillsOfExchange(
            _name,
            _totalSupply,
            _currency,
            _sumToBePaidForEveryToken,
            _drawerName,
            _linkToSignersAuthorityToRepresentTheDrawer,
            _drawerName, // string memory _drawee,
            msg.sender, // address _draweeSignerAddress,
            _timeOfPaymentUnixTime, // if now 'at sight'
            _placeWhereTheBillIsIssued,
            _placeWherePaymentIsToBeMade
        );
        BillsOfExchange billsOfExchange = BillsOfExchange(billsOfExchangeAddress);
        billsOfExchange.accept(_linkToSignersAuthorityToRepresentTheDrawer);
        return address(billsOfExchange);
    }


}
