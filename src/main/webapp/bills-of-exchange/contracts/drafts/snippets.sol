pragma solidity ^0.4.0;

contract snippets {

    function snippets(){

    }

    //    /* TODO:  ---- Payment */
    //    // see:
    //    // https://www.jus.uio.no/lm/bills.of.exchange.and.promissory.notes.convention.1930/doc.html#134
    //
    //    mapping(address => uint256) public billsPresentedForPayment;
    //    mapping(address => string) public paymentDetails;
    //
    //    event BillsPresentedForPayment(
    //        uint256 onUnixTime,
    //        address by,
    //        uint256 numberOfTokens,
    //        string payToDetails
    //    );

    // function presentBillsForPayment(
    //     uint256 _value, // number of tokens (bills of exchange) to pay
    //     string memory _payTo // optional payments details (can also be sent in private message)
    // ) public returns (bool){

    //     require(_value > 0);
    //     // only token holder (not using allowance):
    //     require(_value <= balanceOf[msg.sender]);

    //     // Subtract from the sender's balance
    //     balanceOf[msg.sender] = balanceOf[msg.sender].sub(_value);
    //     // Add the same to the sender's billsPresentedForPayment
    //     billsPresentedForPayment[msg.sender] = billsPresentedForPayment[msg.sender].add(_value);
    //     // update paymentDetails:
    //     paymentDetails[msg.sender] = _payTo;

    //     emit BillsPresentedForPayment(now, msg.sender, _value, _payTo);
    //     return true;
    // }

    // payment can be confirmed by bills of exchange holder or, in case of dispute, by arbitrator
    // tokens will be burned, and corresponding sum of ETH used for back up obligations will be returned
    // function confirmPaymentsFor(uint256 _value) public returns (bool){

    //     require(_value > 0);
    //     require(
    //         // billsPresentedForPayment[_payee] >= _value || cryptonomicaArbitration.isArbitrator(msg.sender)
    //         billsPresentedForPayment[msg.sender] >= _value
    //     );

    //     // burn tokens:
    //     billsPresentedForPayment[msg.sender] = billsPresentedForPayment[msg.sender].sub(_value);
    //     totalSupply = totalSupply.sub(_value);

    //     // if contract backed up by ETH, release part of the funds:
    //     // if (address(this).balance > 0) {
    //     //     // transfer weiBalance / totalSupply * _value back to drawer
    //     //     address to = address(drawerRepresentedBy);
    //     //     uint256 sumToPay = address(this).balance.mul(_value).div(totalSupply);
    //     //     to.transfer(sumToPay);
    //     // }

    //     return true;
    // }

    // function confirmPayments(uint256 _value) public returns (bool){
    //     return confirmPaymentsFor(msg.sender, _value);
    // }

    // /*
    // * this function is to create bills of exchange and accept them by the drawer
    // */
    // function createAndAcceptBillsOfExchange(
    //     string calldata _name,
    //     uint256 _totalSupply,
    //     string calldata _currency, // for example: "EUR", "USD"
    //     uint256 _sumToBePaidForEveryToken,
    //     string calldata _drawerName, // person who issues the bill (drawer)
    //     string calldata _linkToSignersAuthorityToRepresentTheDrawer,
    // // string memory _drawee, // in this case the same as drawer
    // // address _draweeSignerAddress,
    //     uint256 _timeOfPaymentUnixTime, // if now 'at sight'
    //     string calldata _placeWhereTheBillIsIssued,
    //     string calldata _placeWherePaymentIsToBeMade
    // ) external payable returns (address newBillsOfExhangeContractAddress) {

    //     address billsOfExchangeAddress = createBillsOfExchange(
    //         // _name,
    //         _totalSupply,
    //         _currency,
    //         _sumToBePaidForEveryToken,
    //         _drawerName,
    //         _linkToSignersAuthorityToRepresentTheDrawer,
    //         _drawerName, // string memory _drawee,
    //         msg.sender, // address _draweeSignerAddress,
    //         _timeOfPaymentUnixTime, // if now 'at sight'
    //         _placeWhereTheBillIsIssued,
    //         _placeWherePaymentIsToBeMade
    //     );
    //     BillsOfExchange billsOfExchange = BillsOfExchange(billsOfExchangeAddress);
    //     billsOfExchange.accept(_linkToSignersAuthorityToRepresentTheDrawer);
    //     return address(billsOfExchange);
    // }


}
