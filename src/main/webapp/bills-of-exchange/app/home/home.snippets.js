
(function setUpContractData() {
    $scope.contractAddress = "0x6Daf0C43ce36F68A80b42b6C1957376150AbC401";
    $scope.contractDeployedOnBlock = "5449982";
    $scope.deployedOnNetworkId = 3;
    $scope.contractABI = [{"constant":false,"inputs":[{"name":"_contract","type":"address"}],"name":"backUpByETH","outputs":[{"name":"","type":"bool"}],"payable":true,"stateMutability":"payable","type":"function"},{"constant":false,"inputs":[{"name":"_totalSupply","type":"uint256"},{"name":"_currency","type":"string"},{"name":"_sumToBePaidForEveryToken","type":"uint256"},{"name":"_drawerName","type":"string"},{"name":"_linkToSignersAuthorityToRepresentTheDrawer","type":"string"},{"name":"_drawee","type":"string"},{"name":"_draweeSignerAddress","type":"address"},{"name":"_timeOfPaymentUnixTime","type":"uint256"},{"name":"_placeWhereTheBillIsIssued","type":"string"},{"name":"_placeWherePaymentIsToBeMade","type":"string"}],"name":"createBillsOfExchange","outputs":[{"name":"","type":"address"}],"payable":true,"stateMutability":"payable","type":"function"},{"constant":false,"inputs":[{"name":"_oldAdmin","type":"address"}],"name":"removeOwner","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"cryptonomicaVerification","outputs":[{"name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"name":"","type":"uint256"}],"name":"billsOfExchangeContractsLedger","outputs":[{"name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"_newAdmin","type":"address"}],"name":"addAdmin","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"name":"_newCryptonomicaVerificationContractAddress","type":"address"}],"name":"changeCryptonomicaVerificationContractAddress","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"price","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"_newPrice","type":"uint256"}],"name":"changePrice","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"cryptonomicaArbitration","outputs":[{"name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"name":"_newCryptonomicaArbitrationContractAddress","type":"address"}],"name":"changeCryptonomicaArbitrationContractAddress","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"billsOfExchangeContractsCounter","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"inputs":[],"payable":false,"stateMutability":"nonpayable","type":"constructor"},{"anonymous":false,"inputs":[{"indexed":false,"name":"from","type":"uint256"},{"indexed":false,"name":"to","type":"uint256"},{"indexed":true,"name":"by","type":"address"}],"name":"PriceChanged","type":"event"},{"anonymous":false,"inputs":[{"indexed":false,"name":"from","type":"address"},{"indexed":false,"name":"to","type":"address"},{"indexed":true,"name":"by","type":"address"}],"name":"CryptonomicaVerificationContractAddressChanged","type":"event"},{"anonymous":false,"inputs":[{"indexed":false,"name":"from","type":"address"},{"indexed":false,"name":"to","type":"address"},{"indexed":true,"name":"by","type":"address"}],"name":"CryptonomicaArbitrationContractAddressChanged","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"added","type":"address"},{"indexed":true,"name":"addedBy","type":"address"}],"name":"AdminAdded","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"removed","type":"address"},{"indexed":true,"name":"removedBy","type":"address"}],"name":"AdminRemoved","type":"event"}];
    $scope.verificationContractABI = [{"constant":true,"inputs":[{"name":"","type":"address"}],"name":"keyCertificateValidUntil","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"name":"","type":"address"}],"name":"verification","outputs":[{"name":"fingerprint","type":"string"},{"name":"","type":"uint256"},{"name":"firstName","type":"string"},{"name":"lastName","type":"string"},{"name":"birthDate","type":"uint256"},{"name":"nationality","type":"string"},{"name":"verificationAddedOn","type":"uint256"},{"name":"revokedOn","type":"uint256"},{"name":"signedString","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"inputs":[],"payable":false,"stateMutability":"nonpayable","type":"constructor"}];
    $scope.verificationContractAddress = "0x7f05e9807f509281a8db8f8b5230b89a96650087";
})();

$scope.toggleDraweeIsTheSameAsDrawer = function () {
    if ($scope.draweeIsTheSameAsDrawerCheckbox) {
        $scope.billsOfExchange.drawee = $scope.billsOfExchange.drawerName;
    } else {
        $scope.billsOfExchange.drawee = null;
    }
};

$scope.togglePlacesAreTheSame = function () {
    if ($scope.placesAreTheSameCheckbox) {
        $scope.billsOfExchange.placeWherePaymentIsToBeMade = $scope.billsOfExchange.placeWhereTheBillIsIssued;
    } else {
        $scope.billsOfExchange.placeWherePaymentIsToBeMade = null;
    }
    // $scope.draweeIsTheSameAsDrawerCheckbox = !$scope.draweeIsTheSameAsDrawerCheckbox; //
};

$scope.toggleAtSightCheckbox = function () {
    if ($scope.atSightCheckbox) {
        $scope.billsOfExchange.timeOfPaymentUnixTime = 0;
    } else {
        $scope.billsOfExchange.timeOfPaymentUnixTime = $rootScope.$rootScope.unixTimeFromDate(new Date());
    }
};


if ($rootScope.currentNetwork.network_id === $scope.deployedOnNetworkId) {
    $scope.contract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
        $scope.contractABI,
        $scope.contractAddress
    );
    $scope.verificationContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
        $scope.verificationContractABI,
        $scope.verificationContractAddress
    );
    if (window.ethereum && window.ethereum.selectedAddress) {
        $scope.contract.from = window.ethereum.selectedAddress;
        $scope.verificationContract.from = window.ethereum.selectedAddress;
    } else if ($rootScope.web3 && $rootScope.web3.eth && $rootScope.web3.eth.defaultAccount) {
        $scope.contract.from = $rootScope.web3.eth.defaultAccount;
        $scope.verificationContract.from = $rootScope.web3.eth.defaultAccount;
    }
    // $log.debug("$scope.contract:");
    // $log.debug($scope.contract);
    $scope.$apply();
    $log.debug("contract address: ", $scope.contract._address);
} else {
    $scope.setAlertDanger(
        "This smart contract does not work on "
        + $rootScope.currentNetwork.networkName
        + " please change to "
        + $rootScope.networks[$scope.deployedOnNetworkId].networkName
    );
}