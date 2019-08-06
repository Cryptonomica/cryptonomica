pragma solidity 0.5.10;

/*
* @author Cryptonomica Ltd.(cryptonomica.net), 2019
* @version 2019-08-04
* Github: https://github.com/Cryptonomica/
*
* 'CryptoSharesFactory' is a smart contract for creating smart contract for cryptoshares.
* They can be shares of a real corporation. Every share is an ERC20 + ERC223 + ERC677 Token.
* Smart contracts with cryptoshares implement:
* 1) Shareholders identity verification (via Cryptonomica.net) and shareholders ledger.
* 2) Automatic signing of arbitration clause (dispute resolution agreement) by every new shareholder.
* 3) Shareholders voting using 'one share - one vote' principle.
* 4) Dividends distribution. Dividends can be paid in xEUR tokens (https://xeuro.online) or in Ether.
*    Smart contract can receive Ether and xEUR tokens and distribute them to shareholders.
* Shares can be transferred  without restrictions from one Ethereum address to another. But only verified Ethereum
* address represents shareholder. Every share not owned by registered shareholder address is considered 'in transfer',
* shares 'in transfer' can not vote and can not receive dividends.
*
* 'CryptoSharesFactory' charges fee in Ether for creating cryptoshares smart contracts.
*/

/* ========= Libraries */

/**
 * SafeMath library
 * source: https://github.com/OpenZeppelin/openzeppelin-solidity/blob/master/contracts/math/SafeMath.sol
 * version: 2f9ae97 (2019-05-24)
 */
library SafeMath {
    /**
     * dev: Returns the addition of two unsigned integers, reverting on
     * overflow.
     *
     * Counterpart to Solidity's `+` operator.
     *
     * Requirements:
     * - Addition cannot overflow.
     */
    function add(uint256 a, uint256 b) internal pure returns (uint256) {
        uint256 c = a + b;
        require(c >= a, "SafeMath: addition overflow");

        return c;
    }

    /**
     * dev: Returns the subtraction of two unsigned integers, reverting on
     * overflow (when the result is negative).
     *
     * Counterpart to Solidity's `-` operator.
     *
     * Requirements:
     * - Subtraction cannot overflow.
     */
    function sub(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b <= a, "SafeMath: subtraction overflow");
        uint256 c = a - b;

        return c;
    }

    /**
     * dev: Returns the multiplication of two unsigned integers, reverting on
     * overflow.
     *
     * Counterpart to Solidity's `*` operator.
     *
     * Requirements:
     * - Multiplication cannot overflow.
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
     * dev: Returns the integer division of two unsigned integers. Reverts on
     * division by zero. The result is rounded towards zero.
     *
     * Counterpart to Solidity's `/` operator. Note: this function uses a
     * `revert` opcode (which leaves remaining gas untouched) while Solidity
     * uses an invalid opcode to revert (consuming all remaining gas).
     *
     * Requirements:
     * - The divisor cannot be zero.
     */
    function div(uint256 a, uint256 b) internal pure returns (uint256) {
        // Solidity only automatically asserts when dividing by 0
        require(b > 0, "SafeMath: division by zero");
        uint256 c = a / b;
        // assert(a == b * c + a % b); // There is no case in which this doesn't hold

        return c;
    }

    /**
     * dev: Returns the remainder of dividing two unsigned integers. (unsigned integer modulo),
     * Reverts when dividing by zero.
     *
     * Counterpart to Solidity's `%` operator. This function uses a `revert`
     * opcode (which leaves remaining gas untouched) while Solidity uses an
     * invalid opcode to revert (consuming all remaining gas).
     *
     * Requirements:
     * - The divisor cannot be zero.
     */
    function mod(uint256 a, uint256 b) internal pure returns (uint256) {
        require(b != 0, "SafeMath: modulo by zero");
        return a % b;
    }
}

/**
* @title xEUR smart contract
* dev: contract with xEUR tokens, that will be used to pay dividends
*      see: https://xeuro.online
*/
contract XEuro {

    // standard ERC20 balanceOf function
    function balanceOf(address _account) external view returns (uint);

    // standard ERC20 transfer function
    function transfer(address _recipient, uint _amount) external returns (bool);

}

/**
* @title Cryptonomica verification smart contract
* dev: Contract that provides identity verification information for given ETH address
*      see: https://www.cryptonomica.net/#!/verifyEthAddress/
*/
contract CryptonomicaVerification {

    /**
    * @param _address The address to check
    * @return 0 if key certificate is not revoked, or Unix time of revocation
    */
    function revokedOn(address _address) external view returns (uint unixTime);

    /**
    * @param _address The address to check
    * @return Unix time, if not zero and time is later than now - identity verified
    */
    function keyCertificateValidUntil(address _address) external view returns (uint unixTime);
}

/**
* @title Contract that will work with ERC-677 tokens
* dev: see:
*      https://github.com/ethereum/EIPs/issues/677
*      https://github.com/smartcontractkit/LinkToken/blob/master/contracts/ERC677Token.sol
*/
contract ERC677Receiver {
    /**
    * The function is added to contracts enabling them to react to receiving tokens within a single transaction.
    * The from parameter is the account which just transferred amount from the token contract. data is available to pass
    * additional parameters, i.e. to indicate what the intention of the transfer is if a contract allows transfers
    * for multiple reasons.
    * @param from address sending tokens
    * @param amount of tokens
    * @param data to send to another contract
    */
    function onTokenTransfer(address from, uint amount, bytes calldata data) external returns (bool success);
}

/**
* @title Contract that will work ERC223 'transfer' function
* dev: see: https://github.com/ethereum/EIPs/issues/223
*/
contract ERC223ReceivingContract {
    /**
     * @notice Standard ERC223 function that will handle incoming token transfers.
     * @param _from  Token sender address.
     * @param _value Amount of tokens.
     * @param _data  Transaction metadata.
     */
    function tokenFallback(address _from, uint _value, bytes calldata _data) external;
}

/*
* @title Cryptoshares smart contract
* @notice Contract to manage shares of a corporation on the blockchain
*         Shares are implemented as ERC20/ERC223 tokens.
*         Contract also implements dividends distribution, shareholders voting, dispute resolution agreement
*/
contract CryptoShares {

    using SafeMath for uint256;

    uint public contractNumberInTheLedger;

    /* ---- Identity verification for shareholders */

    CryptonomicaVerification public cryptonomicaVerification;

    function addressIsVerifiedByCryptonomica(address _address) public view returns (bool){

        return cryptonomicaVerification.keyCertificateValidUntil(_address) > now && cryptonomicaVerification.revokedOn(_address) == 0;

    }

    /* ---- xEUR contract */

    /*
    * We will pay dividends in xEUR as well as in Ether
    * see: https://xeuro.online
    */
    XEuro public xEuro;

    /* --- ERC-20 variables */

    string public name;

    string public symbol;

    uint8 public constant decimals = 0;

    uint public totalSupply;

    mapping(address => uint) public balanceOf;

    mapping(address => mapping(address => uint)) public allowance;

    /**
    * stored address that deployed this smart contract to blockchain
    * it used only in 'init' function
    */
    address public creator;

    /**
    * Constructor
    * no args constructor make possible to create contracts with code pre-verified on etherscan.io
    * (once we verify one contract, all next contracts with the same code and constructor args will be verified on etherscan)
    */
    constructor() public {
        creator = msg.sender;
    }

    /* --- ERC-20 events */

    event Transfer(address indexed from, address indexed to, uint value);

    event Approval(address indexed _owner, address indexed spender, uint value);

    /* --- Events for interaction with other smart contracts */

    /**
    * @param _from Address that sent transaction
    * @param _toContract Receiver (smart contract)
    * @param _extraData Data sent
    */
    event DataSentToAnotherContract(address indexed _from, address indexed _toContract, bytes indexed _extraData);

    /* ===== Arbitration (Dispute Resolution) =======*/

    /*
    * Every shareholder to be registered has to sign dispute resolution agreement.
    */

    /**
    * Arbitration clause (dispute resolution agreement) text
    * see: https://en.wikipedia.org/wiki/Arbitration_clause
    * https://en.wikipedia.org/wiki/Arbitration#Arbitration_agreement
    */
    string public disputeResolutionAgreement =
    "Any dispute, controversy or claim arising out of or relating to this smart contract, including transfer of shares managed by this smart contract or ownership of the shares, or any voting managed by this smart contract shall be settled by arbitration in accordance with the Cryptonomica Arbitration Rules (https://github.com/Cryptonomica/arbitration-rules) in the version in effect at the time of the filing of the claim.";

    /**
    * number of signatures under disputeResolution agreement
    */
    uint256 public disputeResolutionAgreementSignaturesCounter;

    /**
    * dev: This struct represent a signature under dispute resolution agreement.
    *
    * @param signatoryAddress Ethereum address of the person, that signed the agreement
    * @param signatoryName Legal name of the person that signed agreement. This can be a name of a legal or physical person
    * @param signatoryRegistrationNumber Registration number of legal entity, or ID number of physical person
    * @param signatoryAddress Address of the signatory (country/State, ZIP/postal code, city, street, house/building number,
    * apartment/office number)
    */
    struct Signature {
        address signatoryRepresentedBy;
        string signatoryName;
        string signatoryRegistrationNumber;
        string signatoryAddress;
        uint signedOnUnixTime;
    }

    // signature number => signature data (struct)
    mapping(uint256 => Signature) public disputeResolutionAgreementSignatures;

    /**
    * dev: Event to be emitted when Dispute Resolution agreement was signed by new person.
    *
    * @param signatureNumber Number of the signature (see 'disputeResolutionAgreementSignaturesCounter')
    * @param signatoryRepresentedBy Ethereum address of the person who signed disputeResolution agreement
    * @param signatoryName Name of the person who signed disputeResolution agreement
    * @param signatoryRegistrationNumber Registration number of legal entity, or ID number of physical person
    * @param signatoryAddress Address of the signatory (country/State, ZIP/postal code, city, street, house/building number,
    * apartment/office number)
    */
    event disputeResolutionAgreementSigned(
        uint256 indexed signatureNumber,
        address signatoryRepresentedBy,
        string signatoryName,
        string signatoryRegistrationNumber,
        string signatoryAddress,
        uint signedOnUnixTime
    );

    /**
    * @param _signatoryName Name of the person who signed disputeResolution agreement
    * @param _signatoryRegistrationNumber Registration number of legal entity, or ID number of physical person
    * @param _signatoryAddress Address of the signatory (country/State, ZIP/postal code, city, street, house/building number,
    * apartment/office number)
    */
    function signDisputeResolutionAgreement(
        string memory _signatoryName,
        string memory _signatoryRegistrationNumber,
        string memory _signatoryAddress
    ) private {

        require(
            addressIsVerifiedByCryptonomica(msg.sender),
            "Signer has to be verified on Cryptonomica.net"
        );

        disputeResolutionAgreementSignaturesCounter++;

        disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryRepresentedBy = msg.sender;
        disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryName = _signatoryName;
        disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryRegistrationNumber = _signatoryRegistrationNumber;
        disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryAddress = _signatoryAddress;
        disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signedOnUnixTime = now;

        emit disputeResolutionAgreementSigned(
            disputeResolutionAgreementSignaturesCounter,
            disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryRepresentedBy,
            disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryName,
            disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryRegistrationNumber,
            disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signatoryAddress,
            disputeResolutionAgreementSignatures[disputeResolutionAgreementSignaturesCounter].signedOnUnixTime
        );

    }

    /* ===== Shareholders management =============== */

    /**
    * dev: counts all shareholders in smart contract history
    */
    uint public shareholdersCounter;

    /**
    * dev: keeps address for each shareholder ID/number (according to shareholdersCounter)
    * if zero -> not a registered shareholder
    */
    mapping(address => uint) public shareholderID;

    struct Shareholder {
        uint shareholderID;                     // 1
        address shareholderEthereumAddress;     // 2
        string shareholderName;                 // 3
        string shareholderRegistrationNumber;   // 4
        string shareholderAddress;              // 5
        bool shareholderIsLegalPerson;          // 6
        string linkToSignersAuthorityToRepresentTheShareholder; // 7
        uint balanceOf;                         // 8
    }

    mapping(uint => Shareholder) public shareholdersLedgerByIdNumber;
    mapping(address => Shareholder) public shareholdersLedgerByEthAddress;

    /**
    * dev: revers to shareholderID mapping, this returns address for each shareholder number
    */
    mapping(uint => address payable) public shareholderEthereumAddress;

    /*
    * Legal name of the shareholder
    */
    mapping(address => string) public shareholderName;

    /*
    * Registration number for legal person, or personal ID number for physical person
    */
    mapping(address => string) public shareholderRegistrationNumber;

    /*
    * Legal address of the shareholder
    * (country/State, city, street, building/house number, apartment/office number)
    */
    mapping(address => string) public shareholderAddress;

    /*
    * Indicates if shareholder is a physical or legal person
    */
    mapping(address => bool) public shareholderIsLegalPerson;


    /*
    * Link to document or ledger with the information confirming
    * signers authority to represent the shareholder
    */
    mapping(address => string) public linkToSignersAuthorityToRepresentTheShareholder;

    /*
    * shows if dividend distribution is in process (true) or finished (false)
    */
    bool public payDividendsIsRunning = false;

    /*
    * Unix time of the moment last dividends round was finished
    */
    uint public lastDividendsPaidOn;

    /**
    * Time in seconds between dividends distribution rounds.
    * Next round can be started only if the specified number of seconds has elapsed since the end of the previous round.
    * See: https://en.wikipedia.org/wiki/Dividend#Dividend_frequency
    */
    uint public dividendsPeriod;

    /*
    * Sum in wei to pay to each share in current dividends distribution round.
    * Should be the same for every payment in one round, even the balance changes while round is running.
    */
    uint public sumWeiToPayForOneToken;

    /*
    * Sum in xEUR to pay to each share in current dividends distribution round
    * Should be the same for every payment in one round, even the balance changes while round is running.
    */
    uint public sumXEurToPayForOneToken;

    /**
    * Number of last (or current) round of dividends payout
    * We count all historical dividends payouts
    */
    uint public lastDividendsRound;

    /**
    * EHT address of next shareholder that make a payment to.
    */
    uint nextShareholderToPayDividends;

    /*
    * @param dividendsRound Number of dividends distribution round
    * @param startedBy ETH address that started round (if time to pay dividends, can be started by any ETH address)
    * @param totalWei Sum in wei that has to be distributed in this round
    * @param totalXEur Sum in xEUR that has to be distributed in this round
    */
    event DividendsPaymentsStarted(
        uint indexed dividendsRound,
        address indexed startedBy,
        uint totalWei,
        uint totalXEur
    );

    /**
    * @param dividendsRound Number of dividends distribution round
    */
    event DividendsPaymentsFinished(
        uint indexed dividendsRound
    );

    /**
    * dev: Info about the payment in ETH made to next shareholder
    */
    event DividendsPaymentEther (
        bool indexed success,
        address indexed to,
        uint shareholderID,
        uint sumWei,
        uint indexed dividendsRound
    );

    /**
    * dev: Info about the payment in xEUR made to next shareholder
    */
    event DividendsPaymentXEuro (
        bool indexed success,
        address indexed to,
        uint shareholderID,
        uint sumXEuro,
        uint indexed dividendsRound
    );

    /*
    * dev: Info about new person added to shareholders ledger
    */
    event shareholderAdded(
        uint shareholderID,
        address shareholderEthereumAddress,
        bool isLegalPerson,
        string shareholderName,
        string shareholderRegistrationNumber,
        string shareholderAddress,
        uint shares
    );

    /*
    * @notice Using this function a token holder can register himself as shareholder.
    * Any share (token) not owned of registered shareholder is considered 'in transfer'.
    * Shares 'in transfer' can not vote and does not receive dividends.
    * In the ledger (variables: 'shareholderID', 'shareholderEthereumAddress' and so on ) we store data of all
    * historical shareholders,
    * and they persist even if a shareholder transferred all his shares and his 'balanceOf' is zero.
    * @param _isLegalPerson This indicates if new shareholder is a legal person or physical person.
    * @param _shareholderName Legal name of the shareholder. If this is different from name registered in Cryptonomica
    * verification smart contract, we consider a person registered in Cryptonomica smart contract representative of the
    * shareholder.
    * @param _shareholderRegistrationNumber Registration number of a legal person or personal ID number for physical person.
    * @param  _shareholderAddress Shareholder's legal address (country/state, street, building/house number, apartment/office number)
    */
    function registerAsShareholderAndSignArbitrationAgreement(
        bool _isLegalPerson,
        string calldata _shareholderName,
        string calldata _shareholderRegistrationNumber,
        string calldata _shareholderAddress,
        string calldata _linkToSignersAuthorityToRepresentTheShareholder
    ) external returns (bool success){

        require(
            balanceOf[msg.sender] > 0,
            "To be registered address has to hold at least one token/share"
        );

        require(
            addressIsVerifiedByCryptonomica(msg.sender),
            "Shareholder address has to be verified on Cryptonomica"
        );

        /* allow change shareholder data (if entered incorrectly or some data changed) > */
        //        require(
        //            shareholderID[msg.sender] == 0,
        //            "This address already registered as shareholder address"
        //        );

        shareholdersCounter++;

        // 1
        shareholderID[msg.sender] = shareholdersCounter;
        // 2
        shareholderEthereumAddress[shareholdersCounter] = msg.sender;
        // 3
        shareholderName[msg.sender] = _shareholderName;
        // 4
        shareholderRegistrationNumber[msg.sender] = _shareholderRegistrationNumber;
        // 5
        shareholderAddress[msg.sender] = _shareholderAddress;
        // 6
        shareholderIsLegalPerson[msg.sender] = _isLegalPerson;
        // 7
        linkToSignersAuthorityToRepresentTheShareholder[msg.sender] = _linkToSignersAuthorityToRepresentTheShareholder;

        // 1
        shareholdersLedgerByIdNumber[shareholdersCounter].shareholderID = shareholderID[msg.sender];
        // 2
        shareholdersLedgerByIdNumber[shareholdersCounter].shareholderEthereumAddress = shareholderEthereumAddress[shareholdersCounter];
        // 3
        shareholdersLedgerByIdNumber[shareholdersCounter].shareholderName = shareholderName[msg.sender];
        // 4
        shareholdersLedgerByIdNumber[shareholdersCounter].shareholderRegistrationNumber = shareholderRegistrationNumber[msg.sender];
        // 5
        shareholdersLedgerByIdNumber[shareholdersCounter].shareholderAddress = shareholderAddress[msg.sender];
        // 6
        shareholdersLedgerByIdNumber[shareholdersCounter].shareholderIsLegalPerson = shareholderIsLegalPerson[msg.sender];
        // 7
        shareholdersLedgerByIdNumber[shareholdersCounter].linkToSignersAuthorityToRepresentTheShareholder = linkToSignersAuthorityToRepresentTheShareholder[msg.sender];
        // 8
        shareholdersLedgerByIdNumber[shareholdersCounter].balanceOf = balanceOf[msg.sender];

        /* copy struct  */
        shareholdersLedgerByEthAddress[msg.sender] = shareholdersLedgerByIdNumber[shareholdersCounter];

        emit shareholderAdded(
        // 1
            shareholderID[msg.sender],
        // 2
            msg.sender,
        // 3
            shareholderIsLegalPerson[msg.sender],
        // 4
            shareholderName[msg.sender],
        // 5
            shareholderRegistrationNumber[msg.sender],
        // 6
            shareholderAddress[msg.sender],
        // 7
            balanceOf[msg.sender]
        );

        signDisputeResolutionAgreement(
            shareholderName[msg.sender],
            shareholderRegistrationNumber[msg.sender],
            shareholderAddress[msg.sender]
        );

        return true;
    }

    /*
    * @notice This function starts dividend payout round, and can be started from any address if the time has come.
    */
    function startDividendsPayments() external returns (bool) {

        require(!payDividendsIsRunning, "Already running");
        require(now.sub(lastDividendsPaidOn) > dividendsPeriod, "To early to start");

        sumWeiToPayForOneToken = address(this).balance.div(totalSupply);

        uint xEuroBalance = xEuro.balanceOf(address(this));
        sumXEurToPayForOneToken = xEuroBalance.div(totalSupply);

        payDividendsIsRunning = true;
        lastDividendsRound++;

        emit DividendsPaymentsStarted(
            lastDividendsRound,
            msg.sender,
            address(this).balance,
            xEuroBalance
        );

        return true;
    }

    /*
    * @notice This function pays dividends due to the next shareholder.
    * dev: This functions have be called by external script.
    * External script can be run by any person interested in distributing dividends.
    * Script code is open source and published on smart contract's web site and/or on Github.
    * Technically this functions can be run also manually (acceptable option for small number of shareholders)
    */
    function payDividendsToNext() external returns (bool success) {

        require(payDividendsIsRunning, "Dividends payments round is not open");

        nextShareholderToPayDividends++;

        if (nextShareholderToPayDividends <= shareholdersCounter) {

            address payable to = shareholderEthereumAddress[nextShareholderToPayDividends];

            if (balanceOf[to] > 0) {

                if (sumWeiToPayForOneToken > 0) {

                    uint sumWeiToPay = sumWeiToPayForOneToken.mul(balanceOf[to]);

                    // 'send' is the low-level counterpart of 'transfer'.
                    // If the execution fails, the current contract will not stop with an exception, but 'send' will return false.
                    // https://solidity.readthedocs.io/en/v0.5.10/types.html?highlight=send#members-of-addresses
                    // So we use 'send' and not 'transfer' to ensure that execution continues even if sending ether fails.

                    bool result = to.send(sumWeiToPay);

                    emit DividendsPaymentEther(result, to, sumWeiToPay, nextShareholderToPayDividends, lastDividendsRound);

                }

                if (sumXEurToPayForOneToken > 0) {

                    uint sumXEuroToPay = sumWeiToPayForOneToken.mul(balanceOf[to]);

                    bool result = xEuro.transfer(to, sumXEuroToPay);

                    emit DividendsPaymentXEuro(result, to, sumXEuroToPay, nextShareholderToPayDividends, lastDividendsRound);

                }

            }

        } else {

            lastDividendsPaidOn = now;
            payDividendsIsRunning = false;
            nextShareholderToPayDividends = 0;

            emit DividendsPaymentsFinished(
                lastDividendsRound
            );
        }

        return true;
    }

    /* ============= ERC20 functions ============ */

    /**
    * dev:
    * https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md#approve
    * there is an attack:
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
    function approve(address _spender, uint _value) public returns (bool success){
        allowance[msg.sender][_spender] = _value;
        emit Approval(msg.sender, _spender, _value);
        return true;
    }

    /**
    * @notice Approve another address to spend tokens from the tokenholder account
    * dev: Overloaded approve function
    *      See https://docs.google.com/document/d/1YLPtQxZu1UAvO9cZ1O2RPXBbT0mooh4DYKjA_jp-RLM/
    * @param _spender The address which will spend the funds.
    * @param _currentValue The current value of allowance for spender
    * @param _value The amount of tokens to be spent.
    */
    function approve(address _spender, uint _currentValue, uint _value) external returns (bool success){
        require(
            allowance[msg.sender][_spender] == _currentValue,
            "Current value in contract is different than provided current value"
        );
        return approve(_spender, _value);
    }

    /**
    * dev: Checks and changes balances and allowances for transfer functions
    */
    function _transferFrom(address _from, address _to, uint _value) private returns (bool success) {

        require(_to != address(0), "_to was 0x0 address");

        require(!payDividendsIsRunning, "Transfers blocked while dividends are distributed");

        require(_from == msg.sender || _value <= allowance[_from][msg.sender], "Sender not authorized");

        // check if _from account have required amount, if not throw an exception
        require(_value <= balanceOf[_from], "Account doesn't have required amount");

        balanceOf[_from] = balanceOf[_from].sub(_value);
        balanceOf[_to] = balanceOf[_to].add(_value);

        if (shareholdersLedgerByEthAddress[_from].shareholderID > 0) {
            shareholdersLedgerByEthAddress[_from].balanceOf = balanceOf[_from];
            shareholdersLedgerByIdNumber[shareholdersLedgerByEthAddress[_from].shareholderID].balanceOf = balanceOf[_from];
        }
        if (shareholdersLedgerByEthAddress[_to].shareholderID > 0) {
            shareholdersLedgerByEthAddress[_to].balanceOf = balanceOf[_to];
            shareholdersLedgerByIdNumber[shareholdersLedgerByEthAddress[_to].shareholderID].balanceOf = balanceOf[_to];
        }

        // If allowance used, change allowances correspondingly
        if (_from != msg.sender) {
            allowance[_from][msg.sender] = allowance[_from][msg.sender].sub(_value);
        }

        emit Transfer(_from, _to, _value);

        return true;
    }

    /*
    * dev: Calls '.tokenFallback' function if token receiver is a contract
    */
    function _erc223Call(address _to, uint _value, bytes memory _data) private returns (bool success) {

        uint codeLength;

        assembly {
        // Retrieve the size of the code on target address, this needs assembly .
            codeLength := extcodesize(_to)
        }

        if (codeLength > 0) {
            ERC223ReceivingContract receiver = ERC223ReceivingContract(_to);
            receiver.tokenFallback(msg.sender, _value, _data);
            emit DataSentToAnotherContract(msg.sender, _to, _data);
        }

        return true;

    }

    /**
     * @notice Transfers tokens from one address to another.
     *         Sender can send own tokens or or those tokens that he is allowed to transfer.
     * dev: Transfer the specified amount of tokens to the specified address.
     *      Invokes the `tokenFallback` function if the recipient is a contract.
     *      The token transfer fails if the recipient is a contract
     *      but does not implement the `tokenFallback` function
     *      or the fallback function to receive funds.
     * @param _to  Receiver address.
     * @param _value Amount of tokens that will be transferred.
     */
    function transferFrom(address _from, address _to, uint _value) public returns (bool success){

        _transferFrom(_from, _to, _value);

        // see:
        // https://github.com/Dexaran/ERC223-token-standard/pull/54
        // https://github.com/Dexaran/ERC223-token-standard/issues/53
        bytes memory empty = hex"00000000";

        return _erc223Call(_to, _value, empty);

    } // end of transferFrom

    /*
    * @notice Transfer tokens from tx sender address to another address.
    *         The rest is similar to the 'transferFrom' function.
    */
    function transfer(address _to, uint _value) public returns (bool success){
        return transferFrom(msg.sender, _to, _value);
    }

    /**
    * @notice (ERC223) Transfers tokens to another address with additional info.
    * dev: Overloaded 'transfer' function (ERC223 standard)
    *      See: https://github.com/ethereum/EIPs/issues/223
    *      https://github.com/Dexaran/ERC223-token-standard/blob/Recommended/ERC223_Token.sol
    * @param _to    Receiver address.
    * @param _value Amount of tokens that will be transferred.
    * @param _data  Transaction metadata.
    */
    function transfer(address _to, uint _value, bytes calldata _data) external returns (bool success){

        _transferFrom(msg.sender, _to, _value);

        return _erc223Call(_to, _value, _data);
    }

    /**
    * @notice (ERC677) Transfers tokens with additional info to another smart contract, and calls its correspondent function.
    * dev: ERC677 'transferAndCall' function
    *      see: https://github.com/ethereum/EIPs/issues/677
    *
    * @param _to Another smart contract address (receiver)
    * @param _value Number of tokens to transfer
    * @param _extraData Data to send to another contract
    */
    function transferAndCall(address _to, uint _value, bytes calldata _extraData) external returns (bool success){

        _transferFrom(msg.sender, _to, _value);

        ERC677Receiver receiver = ERC677Receiver(_to);
        if (receiver.onTokenTransfer(msg.sender, _value, _extraData)) {
            emit DataSentToAnotherContract(msg.sender, _to, _extraData);
            return true;
        }

        return false;
    }

    /* ============= VOTING ================ */

    /*
    * This smart contract implements voting for shareholders of ERC20 tokens based on the principle
    * "one share - one vote"
    * It requires external script to count votes.
    *
    * Rules:
    * To start a voting an address have to own at lest one share.
    * To start a voting, voting creator must provide:
    * 1) text of the proposal,
    * 2) number of the block on which voting will be finished and results have to be calculated.
    *
    * Every proposal for a contract receives a sequence number that serves as a proposal ID.
    * To vote 'for' or 'against' voter has to provide proposal ID.
    *
    * In most scenarios only votes 'for' can be used, who did not voted 'for' can be considered as voted 'against'.
    * But our dApp also supports votes 'against'
    *
    * To calculate results we collect all voted addresses by an external script, which is also open sourced.
    * Than we check their balances in tokens on resulting block, and and sum up the voices.
    * Thus, for the results, the number of tokens of the voter at the moment of voting does not matter
    * (it should just has at least one).
    * What matters is the number of tokens on the voter's address on the block where the results should calculated.
    *
    * It's like https://www.cryptonomica.net/eth-vote/, but:
    * 1) for this contract only
    * 2) to start voting or to vote an address has to be registered as a shareholder, not just to have tokens
    *
    */

    // Counts all voting created in this smart contract
    uint public votingCounterForContract;

    // proposal id => text of the proposal
    mapping(uint => string) public proposalText;

    // proposal id => number of voters
    mapping(uint => uint256) public numberOfVotersFor;
    mapping(uint => uint256) public numberOfVotersAgainst;

    // proposal id => (voter id => voter address)
    mapping(uint => mapping(uint256 => address)) public votedFor;
    mapping(uint => mapping(uint256 => address)) public votedAgainst;

    // proposal id => (voter address => voter has voted already)
    mapping(uint => mapping(address => bool)) public boolVotedFor;
    mapping(uint => mapping(address => bool)) public boolVotedAgainst;

    // proposal ID => block number
    mapping(uint => uint) public resultsInBlock;

    /*
    * @param proposalId Number of this voting, according to 'votingCounterForContract'
    * @param by Address that created proposal
    * @param proposalText Text of the proposal
    * @param resultsInBlock Block to calculate results of voting.
    */
    event Proposal(
        uint indexed proposalId,
        address indexed by,
        string proposalText,
        uint indexed resultsInBlock
    );

    // to run function an address has to be registered as a shareholder and own at least one share
    modifier onlyShareholder() {

        require(shareholderID[msg.sender] != 0 && balanceOf[msg.sender] > 0,
            "Only shareholder can do that"
        );

        _;
    }

    /*
    * @notice Creates proposal for voting.
    * @param _proposalText Text of the proposal.
    * @param _resultsInBlock Number of block on which results will be counted.
    */
    function createVoting(
        string calldata _proposalText,
        uint _resultsInBlock
    ) onlyShareholder external returns (bool success){

        require(
            _resultsInBlock > block.number,
            "Block for results should be later than current block"
        );

        votingCounterForContract++;

        proposalText[votingCounterForContract] = _proposalText;
        resultsInBlock[votingCounterForContract] = _resultsInBlock;

        emit Proposal(votingCounterForContract, msg.sender, proposalText[votingCounterForContract], resultsInBlock[votingCounterForContract]);

        return true;
    }

    // Vote 'for' received
    event VoteFor(
        uint indexed proposalId,
        address indexed by
    );

    // Vote 'against' received
    event VoteAgainst(
        uint indexed proposalId,
        address indexed by
    );

    /*
    * @notice Vote for the proposal
    * @param _proposalId Id (number) of the proposal
    */
    function voteFor(uint256 _proposalId) onlyShareholder external returns (bool success){

        require(
            resultsInBlock[_proposalId] > block.number,
            "Voting already finished"
        );

        require(
            !boolVotedFor[_proposalId][msg.sender] && !boolVotedAgainst[_proposalId][msg.sender],
            "Already voted"
        );

        numberOfVotersFor[_proposalId] = numberOfVotersFor[_proposalId] + 1;

        uint voterId = numberOfVotersFor[_proposalId];

        votedFor[_proposalId][voterId] = msg.sender;

        boolVotedFor[_proposalId][msg.sender] = true;

        emit VoteFor(_proposalId, msg.sender);

        return true;
    }

    /*
    * @notice Vote against the proposal
    * @param _proposalId Id (number) of the proposal
    */
    function voteAgainst(uint256 _proposalId) onlyShareholder external returns (bool success){

        require(
            resultsInBlock[_proposalId] > block.number,
            "Voting finished"
        );

        require(
            !boolVotedFor[_proposalId][msg.sender] && !boolVotedAgainst[_proposalId][msg.sender],
            "Already voted"
        );

        numberOfVotersAgainst[_proposalId] = numberOfVotersAgainst[_proposalId] + 1;

        uint voterId = numberOfVotersAgainst[_proposalId];

        votedAgainst[_proposalId][voterId] = msg.sender;

        boolVotedAgainst[_proposalId][msg.sender] = true;

        emit VoteAgainst(_proposalId, msg.sender);

        return true;
    }

    /*
    * This contract can receive Ether from any address.
    * Received Ether will be distributed as dividends to shareholders.
    */
    function() external payable {
        //
    }

    /* ============= Contract initialization
    * dev: initializes token: set initial values for erc20 variables
    *      assigns all tokens ('totalSupply') to one address ('tokenOwner')
    * @param _name Name of the token
    * @param _symbol Symbol of the token
    * @param _totalSupply Amount of tokens to create
    * @param _tokenOwner Address that will initially hold all created tokens
    * @param _dividendsPeriod Period in seconds between finish of the previous dividends round and start of the next.
    *        On test net can be small.
    * @param _xEurContractAddress Address of contract with xEUR tokens
    *        (can be different for test net, where we use mock up contract)
    * @param _cryptonomicaVerificationContractAddress Address of the Cryptonomica verification smart contract
    *        (can be different for test net, where we use mock up contract)
    */
    function initToken(
        uint _contractNumberInTheLedger,
        string calldata _name,
        string calldata _symbol,
        uint _totalSupply,
        uint _dividendsPeriod,
        address _xEurContractAddress,
        address _cryptonomicaVerificationContractAddress
    ) external returns (bool success) {

        require(msg.sender == creator, "Only creator can initialize token contract");
        require(_totalSupply > 0, "Number of tokens can not be zero");
        require(totalSupply == 0, "Contract already initialized");

        contractNumberInTheLedger = _contractNumberInTheLedger;

        name = _name;
        symbol = _symbol;
        totalSupply = _totalSupply;
        balanceOf[msg.sender] = totalSupply;
        emit Transfer(address(0), msg.sender, _totalSupply);

        xEuro = XEuro(_xEurContractAddress);
        cryptonomicaVerification = CryptonomicaVerification(_cryptonomicaVerificationContractAddress);

        dividendsPeriod = _dividendsPeriod;

        return true;
    }

    /*
    * overloaded initToken
    * all tokens assigned to '_tokenOwner' instead of msg.sender
    */
    function initToken(
        uint _contractNumberInTheLedger,
        string calldata _name,
        string calldata _symbol,
        uint _totalSupply,
        uint _dividendsPeriod,
        address _xEurContractAddress,
        address _cryptonomicaVerificationContractAddress,
        address _tokenOwner
    ) external returns (bool success) {

        require(msg.sender == creator, "Only creator can initialize token contract");
        require(_totalSupply > 0, "Number of tokens can not be zero");
        require(totalSupply == 0, "Contract already initialized");

        contractNumberInTheLedger = _contractNumberInTheLedger;

        name = _name;
        symbol = _symbol;
        totalSupply = _totalSupply;
        balanceOf[_tokenOwner] = totalSupply;
        emit Transfer(address(0), _tokenOwner, _totalSupply);

        xEuro = XEuro(_xEurContractAddress);
        cryptonomicaVerification = CryptonomicaVerification(_cryptonomicaVerificationContractAddress);

        dividendsPeriod = _dividendsPeriod;

        return true;
    }

}

/* =================== FACTORY */

/*
* dev: Universal functions for smart contract management
*/
contract ManagedContract {

    /*
    * dev: smart contract that provides information about person that owns given Ethereum address/key
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
    * @param from Old address
    * @param to New address
    * @param by Who made a change
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
    * @param added New admin address
    * @param addedBy Who added new admin
    */
    event AdminAdded(
        address indexed added,
        address indexed addedBy
    );

    /**
    * @param _newAdmin Address of new admin
    */
    function addAdmin(address _newAdmin) public onlyAdmin returns (bool success){

        require(
            cryptonomicaVerification.keyCertificateValidUntil(_newAdmin) > now,
            "New admin has to be verified on Cryptonomica.net"
        );

        // revokedOn returns uint256 (unix time), it's 0 if verification is not revoked
        require(
            cryptonomicaVerification.revokedOn(_newAdmin) == 0,
            "Verification for this address was revoked, can not add"
        );

        isAdmin[_newAdmin] = true;

        emit AdminAdded(_newAdmin, msg.sender);

        return true;
    }

    /**
    * @param removed Removed admin address
    * @param removedBy Who removed admin
    */
    event AdminRemoved(
        address indexed removed,
        address indexed removedBy
    );

    /**
    * @param _oldAdmin Address to remove from admins
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
    * @param from Old address
    * @param to New address
    * @param changedBy Who made this change
    */
    event WithdrawalAddressChanged(address indexed from, address indexed to, address indexed changedBy);

    /*
    * @param _withdrawalAddress address to which funds from this contract will be sent
    */
    function setWithdrawalAddress(address payable _withdrawalAddress) public onlyAdmin returns (bool success) {

        require(!withdrawalAddressFixed, "Withdrawal address already fixed");
        require(_withdrawalAddress != address(0), "Wrong address: 0x0");
        require(_withdrawalAddress != address(this), "Wrong address: contract itself");

        emit WithdrawalAddressChanged(withdrawalAddress, _withdrawalAddress, msg.sender);

        withdrawalAddress = _withdrawalAddress;

        return true;
    }

    /*
    * @param withdrawalAddressFixedAs Address for withdrawal
    * @param fixedBy Address who made this change (msg.sender)
    *
    * This event can be fired one time only
    */
    event WithdrawalAddressFixed(address indexed withdrawalAddressFixedAs, address indexed fixedBy);

    /**
    * @param _withdrawalAddress Address to which funds from this contract will be sent
    * This function can be called one time only.
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

    /**
    * !!! can be called by any user or contract
    * possible warning: check for reentrancy vulnerability http://solidity.readthedocs.io/en/develop/security-considerations.html#re-entrancy
    * >>> since we are making a withdrawal to our own contract/address only there is no possible attack using re-entrancy vulnerability
    */
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

/*
* This is a model contract where some paid service provided and there is a price
* (in main function we can check if msg.value >= price)
*/
contract ManagedContractWithPaidService is ManagedContract {

    uint256 public price;

    /*
    * @param from The old price
    * @param to The new price
    * @param by Who changed the price
    */
    event PriceChanged(uint256 from, uint256 to, address indexed by);

    /*
    * @param _newPrice The new price for the service
    */
    function changePrice(uint256 _newPrice) public onlyAdmin returns (bool success){
        emit PriceChanged(price, _newPrice, msg.sender);
        price = _newPrice;
        return true;
    }

}

/*
* dev: Smart contract to deploy crypto shares smart contracts and maintain a ledger of deployed contracts
*/
contract CryptoSharesFactory is ManagedContractWithPaidService {

    /* ---- xEUR ---- */
    address public xEurContractAddress;

    /**
    * @param from Old address
    * @param to New address
    * @param by Who made a change
    */
    event XEuroContractAddressChanged(address from, address to, address indexed by);

    /**
    * @param _newAddress address of new contract to be used
    */
    function changeXEuroContractAddress(address _newAddress) public onlyAdmin returns (bool success) {

        emit XEuroContractAddressChanged(xEurContractAddress, _newAddress, msg.sender);

        xEurContractAddress = _newAddress;

        return true;
    }

    /* ---- Constructor ---- */

    constructor() public {

        isAdmin[msg.sender] = true;

        changePrice(0.2 ether);

        setWithdrawalAddress(msg.sender);

        // Ropsten: > mock up contract, verification always valid for any address
        changeCryptonomicaVerificationContractAddress(0xE48BC3dB5b512d4A3e3Cd388bE541Be7202285B5);
        // TODO: change in production to https://etherscan.io/address/0x846942953c3b2A898F10DF1e32763A823bf6b27f <<<<<<<
        // changeCryptonomicaVerificationContractAddress(0x846942953c3b2A898F10DF1e32763A823bf6b27f);

        // Ropsten: > mock up contract, allows to create tokens for every address
        changeXEuroContractAddress(0x9a2A6C32352d85c9fcC5ff0f91fCB9CE42c15030);
        // TODO: change in production to https://etherscan.io/address/0xe577e0B200d00eBdecbFc1cd3F7E8E04C70476BE <<<<<<<
        // changeXEuroContractAddress(0xe577e0B200d00eBdecbFc1cd3F7E8E04C70476BE)

    } // end of constructor()

    /* ---- Creating and managing CryptoShares smart contracts ---- */

    // counts deployed contracts
    uint public cryptoSharesContractsCounter;

    /*
    * Contain information about deployed contract;
    */
    struct CryptoSharesContract {
        uint contractId;
        address contractAddress;
        string name; // the same as token name
        string symbol; // the same as token symbol
        uint totalSupply; // the same as token totalSupply
        uint dividendsPeriod;
    }

    event NewCryptoSharesContractCreated(
        uint contractId,
        address contractAddress,
        string name, // the same as token name
        string symbol, // the same as token symbol
        uint totalSupply, // the same as token totalSupply
        uint dividendsPeriod
    );

    mapping(uint => CryptoSharesContract) public cryptoSharesContractsLedger;

    function createCryptoSharesContract(
        string calldata _name,
        string calldata _symbol,
        uint _totalSupply,
        uint _dividendsPeriodInSeconds
    ) external payable returns (bool success){

        require(msg.value >= price);

        CryptoShares cryptoSharesContract = new CryptoShares();
        cryptoSharesContractsCounter++;

        bool tokenInitiationResult = cryptoSharesContract.initToken(
            cryptoSharesContractsCounter,
            _name,
            _symbol,
            _totalSupply,
            _dividendsPeriodInSeconds,
            xEurContractAddress,
            address(cryptonomicaVerification),
            msg.sender
        );

        if (tokenInitiationResult) {

            cryptoSharesContractsCounter;
            cryptoSharesContractsLedger[cryptoSharesContractsCounter].contractId = cryptoSharesContractsCounter;
            cryptoSharesContractsLedger[cryptoSharesContractsCounter].contractAddress = address(cryptoSharesContract);
            cryptoSharesContractsLedger[cryptoSharesContractsCounter].name = cryptoSharesContract.name();
            cryptoSharesContractsLedger[cryptoSharesContractsCounter].symbol = cryptoSharesContract.symbol();
            cryptoSharesContractsLedger[cryptoSharesContractsCounter].totalSupply = cryptoSharesContract.totalSupply();
            cryptoSharesContractsLedger[cryptoSharesContractsCounter].dividendsPeriod = cryptoSharesContract.dividendsPeriod();

            emit NewCryptoSharesContractCreated(
                cryptoSharesContractsLedger[cryptoSharesContractsCounter].contractId,
                cryptoSharesContractsLedger[cryptoSharesContractsCounter].contractAddress,
                cryptoSharesContractsLedger[cryptoSharesContractsCounter].name,
                cryptoSharesContractsLedger[cryptoSharesContractsCounter].symbol,
                cryptoSharesContractsLedger[cryptoSharesContractsCounter].totalSupply,
                cryptoSharesContractsLedger[cryptoSharesContractsCounter].dividendsPeriod
            );

            return true;
        }

        return false;
    } // end of function createCryptoSharesContract

}
