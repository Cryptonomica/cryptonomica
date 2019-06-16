pragma solidity 0.5.9;

/**
 * Cryptonomica EthID Tokens smart contract
 * Implements ERC20, ERC223, ERC677
 * developed by Cryptonomica Ltd., 2019
 * version 2.0 , last change 2019-06-16
 * deployed on Ropsten: https://ropsten.etherscan.io/address/0xd9fbcb890aeafbd37852f614689b0c6d9ff05024
 * github: https://github.com/Cryptonomica/
 *
 * This smart contract implements share revenue (share income) mechanism as follows:
 *
 * This contract collects ETH from other contract(s).That is, he receives periodic transfers of funds from other contract(s).
 * When a contract is deployed the finite amount of tokens (ERC20/223/677) is created.
 * Every token owner at any moment can burn his/her tokens and receive a share/part of ETH contained in
 * this smart contract on the moment tokens were burned that corresponds to share of the burned tokens in the total number
 * of tokens.
 *
 * The idea is that in this way the funds in the contract become more and more, and the number of tokens less and less.
 * Thus, we lay the mechanism for the constant growth of the price of the token in the market.
 */


/* ---- Libraries */
/**
 * @title SafeMath
 * @dev Math operations with safety checks that revert on error
 * see: https://openzeppelin.org/api/docs/math_SafeMath.html
 * source: https://github.com/OpenZeppelin/openzeppelin-solidity/blob/v2.0.0/contracts/math/SafeMath.sol
 * commit 2f9ae97 (May 24, 2019)
 */

library SafeMath {
    /**
     * @dev Returns the addition of two unsigned integers, reverting on
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
     * @dev Returns the subtraction of two unsigned integers, reverting on
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
     * @dev Returns the multiplication of two unsigned integers, reverting on
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
     * @dev Returns the integer division of two unsigned integers. Reverts on
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
     * @dev Returns the remainder of dividing two unsigned integers. (unsigned integer modulo),
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

/* --- "Interfaces" */

/**
* @title Contract that will work with ERC-677 tokens
* see:
* https://github.com/ethereum/EIPs/issues/677
* https://github.com/smartcontractkit/LinkToken/blob/master/contracts/ERC677Token.sol
*/
contract ERC677Receiver {
    /**
    * The function is added to contracts enabling them to react to receiving tokens within a single transaction.
    * The from parameter is the account which just transferred amount from the token contract, data is available to pass
    * additional parameters, i.e. to indicate what the intention of the transfer is if a contract allows transfers for
    * multiple reasons.
    * @param from address sending tokens
    * @param amount of tokens
    * @param data to send to another contract
    */
    function onTokenTransfer(address from, uint256 amount, bytes calldata data) external returns (bool success);
}

/**
* Contract that will work with 'transfer' function following ERC223 standard
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

/*
*
* this is expected from another contracts if it wants to spend tokens of behalf of the token owner in our contract
* see:
* https://ethereum.stackexchange.com/questions/43158/anyone-knows-what-does-this-contract-code-mean
*
*/
contract AllowanceRecipient {
    /*
     * @param _from  Token sender address.
     * @param _value Amount of tokens
     * @param _tokensContractAddress Address of smart contract that manages tokens
     * @param _data  Transaction metadata.
     */
    function receiveApproval(
        address _from, uint256 _value, address _tokensContractAddress, bytes memory _extraData
    ) public returns (bool);
}

/* -------- ///////// Main Contract /////// ----------- */

contract EthIdTokens {

    using SafeMath for uint256;

    /* --- ERC-20 variables */

    string public name = "Cryptonomica EthID Tokens";

    string public symbol = "EthID";

    /* 0 is intentionally */
    uint8 public decimals = 0;

    uint256 public totalSupply;

    mapping(address => uint256) public balanceOf;

    mapping(address => mapping(address => uint256)) public allowance;

    /* --- ERC-20 events */

    event Transfer(address indexed from, address indexed to, uint256 value);

    event Approval(address indexed _owner, address indexed spender, uint256 value);

    /* --- Events for interaction with other smart contracts */
    event DataSentToAnotherContract(address indexed _from, address indexed _toContract, bytes _extraData);

    /* --- administrative variable and functions */

    address payable public owner; // smart contract owner

    // to avoid mistakes: contract owner should be changed in two steps
    // change is valid when accepted from new owner address
    address payable private newOwner;

    function changeOwnerStart(address payable _newOwner) public {
        // only owner
        require(msg.sender == owner,
            "Only contract owner can call this function"
        );

        require(_newOwner != address(0),
            "New owner's address can not be zero-address"
        );

        newOwner = _newOwner;

        emit ChangeOwnerStarted(msg.sender, _newOwner);
    } //
    event ChangeOwnerStarted (address indexed startedBy, address indexed newOwner);

    function changeOwnerAccept() public {
        // only by new owner
        require(msg.sender == newOwner,
            "Only contract owner can call this function"
        );
        // event here:
        emit OwnerChanged(owner, newOwner);
        owner = newOwner;
    } //
    event OwnerChanged(address indexed from, address indexed to);

    /* --- Constructor */

    constructor() public {
        // can be hardcoded in production:
        owner = msg.sender;
        // 100M :
        totalSupply = 100 * 1000000;
        balanceOf[owner] = totalSupply;
    }

    /* --- IncomeShare */

    /*
    * @param to Address that received ETH payment from this smart contract
    * @param tokensBurned Amount of tokens burned to receive a share of the ETH collected in this smart contract
    * @param sumInWeiPaid Sum sent (to address 'to')
    */
    event IncomeSharePaid(address indexed to, uint256 tokensBurned, uint256 sumInWeiPaid);

    /*
    * @param valueInTokens Amount of tokens to burn to get share of the income
    * (a share from all ETH collected in this smart contract)
    */
    function takeIncomeShare(uint256 valueInTokens) public returns (bool) {

        require(address(this).balance > 0,
            "No ETH on this contract's balance"
        );
        require(totalSupply > 0,
            "No tokens left in this smart contract"
        );

        require(balanceOf[msg.sender] >= valueInTokens,
            "Account doesn't have required amount"
        );

        // uint256 sumToPay = (address(this).balance / totalSupply).mul(valueInTokens);
        uint256 sumToPay = (address(this).balance).mul(valueInTokens).div(totalSupply);

        totalSupply = totalSupply.sub(valueInTokens);
        balanceOf[msg.sender] = balanceOf[msg.sender].sub(valueInTokens);

        msg.sender.transfer(sumToPay);

        emit Transfer(msg.sender, address(0), valueInTokens);
        emit IncomeSharePaid(msg.sender, valueInTokens, sumToPay);

        return true;
    }

    // only if all tokens are burned:
    event WithdrawalByOwner(uint256 value, address indexed to); //
    function withdrawAllByOwner() public {
        // only owner:
        require(msg.sender == owner,
            "Only contract owner can call this function"
        );
        // only if all tokens already burned:
        require(totalSupply == 0,
            "Can not be called if totalSupply not zero"
        );

        uint256 sumToWithdraw = address(this).balance;
        owner.transfer(sumToWithdraw);
        emit WithdrawalByOwner(sumToWithdraw, owner);
    }

    /* --- Token transfer functions  */

    function transferFrom(address _from, address _to, uint256 _value) public returns (bool){

        require(_to != address(0),
            "Token receiver address can not be zero-address"
        );

        if (_to == address(this)) {
            // this is intentionally:
            require(_from == msg.sender,
                "Only token owner can burn tokens to get share of the income"
            );
            return takeIncomeShare(_value);
        }

        // Transfers of 0 values MUST be treated as normal transfers and fire the Transfer event (ERC-20)
        // Variables of uint type cannot be negative. Thus, comparing uint variable with zero (greater than or equal) is redundant
        // require(_value >= 0);

        // The function SHOULD throw unless the _from account has deliberately authorized the sender of the message via some mechanism
        require(msg.sender == _from || _value <= allowance[_from][msg.sender],
            "Sender not authorized"
        );

        // check if _from account have required amount
        require(_value <= balanceOf[_from],
            "Account doesn't have required amount"
        );

        // Subtract from the sender and add the same to the recipient
        balanceOf[_from] = balanceOf[_from].sub(_value);
        balanceOf[_to] = balanceOf[_to].add(_value);

        // If allowance used, change allowances correspondingly
        if (_from != msg.sender) {
            allowance[_from][msg.sender] = allowance[_from][msg.sender].sub(_value);
        }

        /*
        * ERC223:
        * if sending tokens to contract other than this contract itself,
        * prevent sending tokens to contract that can not handle tokens transfer
        * see: https://github.com/ethereum/EIPs/issues/223
        */
        uint256 codeLength;
        assembly {
            codeLength := extcodesize(_to)
        }
        if (codeLength > 0) {
            bytes memory empty;
            ERC223ReceivingContract receiver = ERC223ReceivingContract(_to);
            receiver.tokenFallback(msg.sender, _value, empty);
        }

        emit Transfer(_from, _to, _value);

        return true;
    } // end of transferFrom

    function transfer(address _to, uint256 _value) public returns (bool success){
        return transferFrom(msg.sender, _to, _value);
    }

    /**
    * ERC-223: overloaded transfer
    * see: https://github.com/ethereum/EIPs/issues/223
    * https://github.com/Dexaran/ERC223-token-standard/blob/Recommended/ERC223_Token.sol
    */
    function transfer(address _to, uint _value, bytes calldata _data) external returns (bool success){
        if (transfer(_to, _value)) {// < here we change balances, and only after that call receiver token fallback func
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
    * @param _to Another smart contract address (receiver)
    * @param _value Number of tokens to transfer
    * @param _extraData Data to send to another contract
    *
    * This function is a recommended method to send tokens to smart contracts.
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

    // for example for converting ALL tokens on user account to another tokens
    function transferAllAndCall(address _to, bytes calldata _extraData) external returns (bool success){
        return transferAndCall(_to, balanceOf[msg.sender], _extraData);
    }


    /*
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
    function approve(address _spender, uint256 _value) public returns (bool success){
        allowance[msg.sender][_spender] = _value;
        emit Approval(msg.sender, _spender, _value);
        return true;
    }

    /**
    * Overloaded approve function
    * see https://docs.google.com/document/d/1YLPtQxZu1UAvO9cZ1O2RPXBbT0mooh4DYKjA_jp-RLM/
    * @param _spender The address which will spend the funds.
    * @param _currentValue The current value of allowance for spender
    * @param _value The amount of tokens to be spent.
    */
    function approve(address _spender, uint256 _currentValue, uint256 _value) external returns (bool success){
        require(
            allowance[msg.sender][_spender] == _currentValue,
            "Current value in contract is different than provided current value"
        );
        return approve(_spender, _value);
    }

    /*  ---------- Interaction with other contracts  */

    /* User can allow another smart contract to spend some tokens in his behalf
    *  (this function should be called by user itself)
    *  @param _spender another contract's address
    *  @param _value number of tokens
    *  @param _extraData Data that can be sent from user to another contract to be processed
    *  bytes - dynamically-sized byte array,
    *  see http://solidity.readthedocs.io/en/v0.4.15/types.html#dynamically-sized-byte-array
    *  see possible attack information in comments to function 'approve'
    *  > this may be used, for example, to convert pre-ICO tokens to ICO tokens, or
    *    to convert some tokens to other tokens
    */
    function approveAndCall(address _spender, uint256 _value, bytes memory _extraData) public returns (bool) {

        approve(_spender, _value);

        // 'spender' is another contract that implements code as prescribed in 'allowanceRecipient' above
        AllowanceRecipient spender = AllowanceRecipient(_spender);

        // our contract calls 'receiveApproval' function of another contract ('allowanceRecipient') to send information about
        // allowance and data sent by user
        // 'this' is this (our) contract address
        if (spender.receiveApproval(msg.sender, _value, address(this), _extraData)) {
            emit DataSentToAnotherContract(msg.sender, _spender, _extraData);
            return true;
        }
        return false;
    } // end of approveAndCall

    // for convenience:
    function approveAllAndCall(address _spender, bytes calldata _extraData) external returns (bool success) {
        return approveAndCall(_spender, balanceOf[msg.sender], _extraData);
    }

    /* ---- Receive payments */

    /*
    * This contract should accept payments from anyone. This was made intentionally.
    */
    function() external payable {
        // intentionally: no code here, so we can use standard transfer function in other contracts
        // to to send funds here
    }

}
