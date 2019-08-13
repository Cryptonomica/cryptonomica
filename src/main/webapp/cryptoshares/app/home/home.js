(function () {

    'use strict';

// console.log("home.js");

    const controller = angular.module("app.home", []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
    controller.config(function ($logProvider) {
        // $logProvider.debugEnabled(false);
        $logProvider.debugEnabled(true);
    });

    controller.controller("app.home", [
        '$scope',
        '$rootScope',
        '$http',
        '$timeout', // for ngProgressFactory
        '$log',
        '$location',
        '$state',
        '$stateParams',
        function homeCtrl($scope,
                          $rootScope,
                          $http,
                          $timeout, // for ngProgressFactory
                          $log,
                          $location,
                          $state,
                          $stateParams
        ) {

            // $log.debug("homeCtrl started");

            if ($rootScope.devModeOn) {
                window.myScope = $scope;
            }

            if ($location.host() === "localhost") {
                $scope.thisUrl = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/cryptoshares/#!/";
            } else {
                $scope.thisUrl = $location.protocol() + "://" + $location.host() + "/cryptoshares/#!/"
            }

            $log.debug($scope.thisUrl);
            // $log.debug($location.absUrl());

            (function setUpSemanticUI() {
                // activate tabs:
                $('.menu .item').tab();

                $scope.changeTabToWork = function () {
                    $.tab('change tab', "workTab");

                    $("#homeTab").removeClass("active");
                    $("#createTab").removeClass("active");
                    $("#helpTab").removeClass("active");

                    $("#workTab").addClass("active");
                };

            })();

            (function alerts() {
                $scope.alertDanger = null;  // red
                $scope.alertWarning = null; // yellow
                $scope.alertInfo = null;    // blue
                $scope.alertSuccess = null; // green
                $scope.alertMessage = null; // grey

                $scope.setAlertDanger = function (message) {
                    $scope.alertDanger = message;
                    $log.debug("$scope.alertDanger:", $scope.alertDanger);
                    // $scope.$apply(); // < needed
                    $scope.goTo("alertDanger");
                };

                $scope.setAlertWarning = function (message) {
                    $scope.alertWarning = message;
                    $log.debug("$scope.alertWarning:", $scope.alertWarning);
                    // $scope.$apply(); //
                    $scope.goTo("alertWarning");
                };

                $scope.setAlertInfo = function (message) {
                    $scope.alertInfo = message;
                    $log.debug("$scope.alertInfo:", $scope.alertInfo);
                    // $scope.$apply();
                    $scope.goTo("alertInfo");
                };

                $scope.setAlertSuccess = function (message) {
                    $scope.alertSuccess = message;
                    $log.debug("$scope.alertSuccess:", $scope.alertSuccess);
                    // $scope.$apply();
                    $scope.goTo("alertSuccess");
                };

                $scope.setAlertMessage = function (message, header) {
                    $scope.alertMessage = {};
                    $scope.alertMessage.header = header;
                    $scope.alertMessage.message = message;
                    $log.debug("$scope.alertMessage:", $scope.alertMessage);
                    // $scope.$apply();
                    $scope.goTo("alertMessage");
                };

            })();

            /* -- check user browser */
            // https://developer.mozilla.org/en-US/docs/Web/API/Window/navigator
            // https://developer.mozilla.org/en-US/docs/Web/API/Navigator
            // $log.debug("$window.navigator.userAgent:");
            // $log.debug($window.navigator.userAgent);
            if (window.navigator.userAgent.indexOf("MSIE") !== -1 || window.navigator.userAgent.indexOf("Trident") !== -1) {
                $scope.setAlertDanger(
                    "Microsoft Explorer is not supported. To use this web application please open it in modern browser"
                );
                return;
            }

            // -------------------------

            (function setUpContractsData() {

                $scope.deployedOnNetworksArray = [
                    // 1, // MainNet
                    3 // Ropsten
                ];

                /* ---- Factory Contract ---- */
                $scope.factoryContractAbiPath = "contracts/compiled/cryptoshares/CryptoSharesFactory.abi";
                $scope.factoryContractAddress = {
                    3: "0xb96858931B6A9FA16B46fAE5f006e4466911c324"
                };

                // where to start fetching events
                $scope.factoryContractDeployedOnBlock = {
                    3: 6180258
                };

                /* ---- CryptoShares Contract ---- */

                $scope.cryptosharesAbiPath = "contracts/compiled/cryptoshares/CryptoShares.abi";

                /* ---- Verification Contract ---- */

                $scope.verificationContractAbiPath = {
                    1: "contracts/compiled/cryptonomicaverification/CryptonomicaVerification.abi",
                    3: "contracts/compiled/cryptonomicaverificationmockup/CryptonomicaVerificationMockUp.abi"
                };

                $scope.verificationContractAddress = {
                    1: "0x846942953c3b2A898F10DF1e32763A823bf6b27f",
                    3: "0xE48BC3dB5b512d4A3e3Cd388bE541Be7202285B5"
                };

                /* ---- xEUR Contract*/
                $scope.xEuroContractAbiPath = {
                    1: "contracts/compiled/xeuro/xEuro.abi",
                    3: "contracts/compiled/xeuromockup/xEurMockUp.abi"
                };
                $scope.xEuroContractAddress = {
                    1: "0xe577e0B200d00eBdecbFc1cd3F7E8E04C70476BE",
                    3: "0x9a2A6C32352d85c9fcC5ff0f91fCB9CE42c15030"
                }

            })();

            (function connect() {
                // https://github.com/MetaMask/metamask-extension/issues/5699#issuecomment-445480857
                if (window.ethereum) {

                    // $log.debug("window.ethereum:");
                    // $log.debug(window.ethereum);

                    $rootScope.web3 = new Web3(ethereum);

                    // $log.debug('web3: ');
                    // $log.debug($rootScope.web3);

                    if (typeof window.ethereum.selectedAddress === 'undefined') { // privacy mode on

                        (async function () {
                            try {
                                // Request account access if needed
                                // This method returns a Promise that resolves to an array of user accounts once access is approved for a given dapp.
                                await window.ethereum.enable();
                                // Accounts now exposed
                                // $rootScope.web3.eth.accounts[0] > does not work
                                // but you have already: web3.eth.defaultAccount
                                // $log.debug("$rootScope.web3.eth.defaultAccount : ");
                                // $log.debug($rootScope.web3.eth.defaultAccount);
                                $rootScope.noConnectionToNodeError = false;

                                main();

                            } catch (error) {
                                // if user denied account access:
                                // {"message":"User denied account authorization","code":4001}
                                $log.error(error);
                                // $scope.setAlertDanger(
                                //     "User denied MetaMask connect. Web application can not be started."
                                //     + "If you wish to use this webapp, please, reload the page and connect MetaMask"
                                // );
                                if (error.message && error.code === 4001) {
                                    error = "User denied MetaMask connect. Web application can not be started."
                                        + " If you wish to use this webapp, please, reload the page and connect MetaMask";
                                }
                                $scope.setAlertDanger(error);
                                $scope.$apply(); // <<< needed here!
                            }

                        })();

                    } else { // privacy mode off or already connected

                        $rootScope.noConnectionToNodeError = false;

                        main();
                    }

                } else if (window.web3) {
                    $log.debug("old web3 browser detected");
                    $rootScope.noConnectionToNodeError = false;
                    $rootScope.web3 = new Web3(web3.currentProvider);
                    // Accounts always exposed

                    if (!$rootScope.web3.eth.defaultAccount && $rootScope.web3.eth.accounts && $rootScope.web3.eth.accounts[0]) {
                        $rootScope.web3.eth.defaultAccount = $rootScope.web3.eth.accounts[0];
                        $log.debug("$rootScope.web3.eth.defaultAccount : ");
                        $log.debug($rootScope.web3.eth.defaultAccount);
                    }

                    main();

                } else { // Non-dapp browsers:

                    $log.error('Non-Ethereum browser detected. You should consider trying MetaMask!');
                    // $scope.setAlertDanger("This web application requires Ethereum connection. Please install MetaMask.io browser plugin");
                    // > or use <no-connection-to-node-error></no-connection-to-node-error> directive
                    $rootScope.noConnectionToNodeError = true;
                }

            })();

            function main() {

                // $log.debug("'main' function started");

                $rootScope.web3.eth.net.getId()
                    .then((result) => {

                        $rootScope.currentNetwork = {};

                        $rootScope.currentNetwork.network_id = parseInt(result);
                        if (result === 1 || result === 3 || result === 4 || result === 42) {
                            $rootScope.currentNetwork.network_id = result;
                            $rootScope.currentNetwork.networkName = $rootScope.networks[result].networkName;
                            $rootScope.currentNetwork.etherscanLinkPrefix = $rootScope.networks[result].etherscanLinkPrefix;
                            $rootScope.currentNetwork.etherscanApiLink = $rootScope.networks[result].etherscanApiLink;
                        } else {
                            $rootScope.currentNetwork.networkName = "unknown network";
                            $rootScope.currentNetwork.etherscanLinkPrefix = "blockchain_explorer";
                            $rootScope.currentNetwork.etherscanApiLink = "blockchainExplorerAPI";
                            $scope.setAlertWarning("Unknown Ethereum network. Network ID: " + result);
                        }

                        if (result === 1) { // MainNet
                            $rootScope.production = true;
                        } else {
                            $rootScope.production = false;
                        }

                        // $log.debug("$rootScope.currentNetwork.networkName:", $rootScope.currentNetwork.networkName);
                        $rootScope.$apply(); // needed here

                        if ($scope.deployedOnNetworksArray.includes($rootScope.currentNetwork.network_id)) {

                            return $http.get($scope.factoryContractAbiPath);

                        } else {

                            $scope.setAlertDanger(
                                "This smart contract does not work on "
                                + $rootScope.networks[$rootScope.currentNetwork.network_id].networkName
                                + " please change to "
                                + $rootScope.networks[$scope.deployedOnNetworksArray[0]].networkName
                            );
                            throw "This contract not deployed on the network with id: " + $rootScope.currentNetwork.network_id;
                        }

                    })
                    .then((value) => {
                        $scope.factoryContractABI = value.data; // ABI
                        return $http.get($scope.verificationContractAbiPath[$rootScope.currentNetwork.network_id]);
                    })
                    .then((value) => {
                        $scope.verificationContractABI = value.data; // ABI
                        return $http.get($scope.cryptosharesAbiPath);
                    })
                    .then((value) => {
                        $scope.cryptosharesContractABI = value.data; // ABI
                        return $http.get($scope.xEuroContractAbiPath[$rootScope.currentNetwork.network_id]);
                    })
                    .then((value) => {

                        /* ---- create contract instances */

                        $scope.xEuroContractABI = value.data;// ABI

                        $scope.factoryContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                            $scope.factoryContractABI,
                            $scope.factoryContractAddress[$rootScope.currentNetwork.network_id]
                        );

                        $scope.verificationContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                            $scope.verificationContractABI,
                            $scope.verificationContractAddress[$rootScope.currentNetwork.network_id]
                        );

                        $scope.xEuroContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                            $scope.xEuroContractABI,
                            $scope.xEuroContractAddress[$rootScope.currentNetwork.network_id]
                        );

                        // https://web3js.readthedocs.io/en/v1.2.0/web3-eth.html#getaccounts
                        return $rootScope.web3.eth.getAccounts();
                    })
                    .then(function (accounts) {

                        if (window.ethereum && window.ethereum.selectedAddress) {
                            $rootScope.web3.eth.defaultAccount = window.ethereum.selectedAddress;
                        } else if (accounts && accounts.length && accounts.length > 0) {
                            // https://web3js.readthedocs.io/en/1.0/web3-eth.html#id20
                            $rootScope.web3.eth.defaultAccount = accounts[0];
                        }

                        if (!$rootScope.web3.eth.defaultAccount) {
                            $scope.setAlertDanger(
                                "Ethereum account not recognized."
                                + " If you use MetaMask please login to MetaMask and connect to this site."
                            );
                        }

                        setUpContract();
                    })
                    .catch(function (error) {
                        $log.error("error:");
                        $log.error(error);
                    })
                    .finally(() => {
                        $scope.$apply();
                    })

            } // end of main()

            function setUpContract() {

                $scope.factoryContractData = {};
                $scope.cryptoSharesContractData = {};

                $scope.getCryptoSharesContractsCounterIsWorking = false;
                $scope.getCryptoSharesContractsCounter = function () {
                    $scope.getCryptoSharesContractsCounterIsWorking = true;
                    $scope.factoryContract.methods.cryptoSharesContractsCounter().call()
                        .then((cryptoSharesContractsCounter) => {

                            $scope.factoryContractData.cryptoSharesContractsCounter = cryptoSharesContractsCounter;

                            // $log.debug(
                            //     "$scope.factoryContractData.cryptoSharesContractsCounter:",
                            //     $scope.factoryContractData.cryptoSharesContractsCounter
                            // );

                        })
                        .catch((error) => {
                            $log.error("$scope.getCryptoSharesContractsCounter :");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.getCryptoSharesContractsCounterIsWorking = false;
                            $scope.$apply();
                        })
                };

                $scope.getPriceIsWorking = false;
                $scope.getPrice = function () {
                    $scope.getPriceIsWorking = true;
                    $scope.factoryContract.methods.price().call()
                        .then((price) => {

                            $scope.priceWei = parseInt(price);
                            $scope.priceETH = $rootScope.web3.utils.fromWei(price, "ether");

                            // $log.debug(
                            //     "$scope.price:", $scope.priceWei, "wei,",
                            //     $scope.priceETH, "ETH"
                            // );

                        })
                        .catch((error) => {
                            $log.error("$scope.getPrice :");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.getPriceIsWorking = false;
                            $scope.$apply();
                        })
                };

                $scope.getDisputeResolutionAgreement = function () {
                    $scope.factoryContract.methods.disputeResolutionAgreement().call()
                        .then((result) => {
                            $scope.disputeResolutionAgreement = result;
                        })
                        .catch((error) => {
                            $log.error("$scope.disputeResolutionAgreement ERROR :");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.$apply();
                        })
                };

                /* ----- refresh factory data ---- */
                $scope.refreshFactoryData = function () {
                    $scope.getPrice();
                    $scope.getCryptoSharesContractsCounter();
                    $scope.getDisputeResolutionAgreement();
                    // $scope.getXEurContractAddress();
                };
                $scope.refreshFactoryData();

                $scope.cryptoSharesContractDataFromLedger = {};
                $scope.getCryptoSharesContractsLedgerEntityIsWorking = false;
                $scope.getCryptoSharesContractsLedgerEntity = function (number) {
                    $scope.getCryptoSharesContractsLedgerEntityIsWorking = true;
                    $scope.factoryContract.methods.cryptoSharesContractsLedger(number).call()
                        .then((cryptoSharesContract) => {

                            $scope.cryptoSharesContractDataFromLedger = cryptoSharesContract;

                            $log.debug(
                                "$scope.cryptoSharesContractDataFromLedger :",
                                $scope.cryptoSharesContractDataFromLedger
                            );
                        })
                        .catch((error) => {
                            $log.error("$scope.cryptoSharesContractDataFromLedger :");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.getCryptoSharesContractsLedgerEntityIsWorking = false;
                            $scope.$apply();
                        })
                };

                /* ---- create new cryptoshares contract ---- */

                // see: https://stackoverflow.com/a/36099084/1697878
                $scope.parseSeconds = function (seconds) {

                    $scope.parseSecondsResult = {};

                    seconds = parseInt(seconds);
                    // let seconds = $scope.createCryptoSharesContractForm.dividendsPeriod;

                    $scope.parseSecondsResult.days = Math.floor(seconds / (3600 * 24));

                    seconds -= $scope.parseSecondsResult.days * 3600 * 24;

                    $scope.parseSecondsResult.hours = Math.floor(seconds / 3600);

                    seconds -= $scope.parseSecondsResult.hours * 3600;

                    $scope.parseSecondsResult.minutes = Math.floor(seconds / 60);

                    seconds -= $scope.parseSecondsResult.minutes * 60;

                    $scope.parseSecondsResult.seconds = seconds;

                    // console.log($scope.parseSecondsResult);

                    // $scope.$apply(); // > error
                };

                $scope.createCryptoSharesContractForm = {};
                $scope.createCryptoSharesContractForm.name = "Test Corporation Ltd.";
                $scope.createCryptoSharesContractForm.symbol = "TST";
                $scope.createCryptoSharesContractForm.totalSupply = 1000;
                $scope.createCryptoSharesContractForm.dividendsPeriod = 60 * 60 * 1; // 1 hour
                $scope.parseSeconds($scope.createCryptoSharesContractForm.dividendsPeriod);
                $rootScope.$apply();

                $scope.clearCreateCryptoSharesContractForm = function () {
                    $scope.createCryptoSharesContractForm = {};
                };

                $scope.createCryptoSharesContractIsWorking = false;
                $scope.createCryptoSharesContract = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);

                    $scope.createCryptoSharesContractIsWorking = true;

                    let txOptions = {
                        from: $rootScope.web3.eth.defaultAccount,
                        value: $scope.priceWei
                    };

                    // $log.log(txOptions);

                    $scope.createCryptoSharesContractTxReceipt = null;
                    $scope.factoryContract.methods.createCryptoSharesContract(
                        $scope.createCryptoSharesContractForm.name,
                        $scope.createCryptoSharesContractForm.symbol,
                        $scope.createCryptoSharesContractForm.totalSupply,
                        $scope.createCryptoSharesContractForm.dividendsPeriod
                    ).send(txOptions)
                        .then((receipt) => {

                            $scope.createCryptoSharesContractTxReceipt = receipt;

                            $scope.setAlertSuccess(
                                "New cryptoshares contract created at Ethereum address:  "
                                + receipt.events[0].address
                            );

                            const newCryptoSharesContractId = parseInt(receipt.events.NewCryptoSharesContractCreated.returnValues.contractId);

                            $log.debug("new contract ID:", newCryptoSharesContractId);

                            $log.debug("receipt: ");
                            $log.debug(receipt);

                            $scope.getCryptoSharesContractsCounter(); // update counter
                            $scope.switchToCryptosharesContractNumber(newCryptoSharesContractId);

                            // see:
                            // https://semantic-ui.com/modules/tab.html#/usage
                            // ! > https://github.com/Semantic-Org/Semantic-UI/issues/3544
                            $scope.changeTabToWork();
                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.createCryptoSharesContractIsWorking = false;
                            $scope.$apply();
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                };

                $scope.instantiateCryptoSharesContractIsWorking = false;
                $scope.instantiateCryptoSharesContract = function (cryptoSharesContractAddress) {

                    // $log.debug("$scope.instantiateCryptoSharesContract started");

                    $scope.cryptoSharesContractData = {};
                    $scope.showSmartContractData = true;

                    $scope.cryptosharesContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                        $scope.cryptosharesContractABI,
                        cryptoSharesContractAddress
                    );

                    $log.debug("$scope.cryptosharesContract:");
                    $log.debug($scope.cryptosharesContract);

                    $scope.getName = function () {
                        $scope.cryptosharesContract.methods.name().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.name = result;
                                $scope.$apply();
                            })
                    };

                    $scope.getSymbol = function () {
                        $scope.cryptosharesContract.methods.symbol().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.symbol = result;
                                $scope.$apply();
                            })
                    };

                    $scope.getTotalSupply = function () {
                        $scope.cryptosharesContract.methods.totalSupply().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.totalSupply = parseInt(result);
                                $scope.$apply();
                            })
                    };

                    $scope.getContractNumberInTheLedger = function () {
                        $scope.cryptosharesContract.methods.contractNumberInTheLedger().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.contractNumberInTheLedger = parseInt(result);
                                $scope.$apply();
                            })
                    };

                    $scope.getShareholdersCounter = function () {
                        $scope.cryptosharesContract.methods.shareholdersCounter().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.shareholdersCounter = parseInt(result);
                                $scope.$apply();
                            })
                    };

                    $scope.getMyShareholdersDataIsWorking = false;
                    $scope.getMyShareholderData = function () {
                        $scope.getMyShareholdersDataIsWorking = true;
                        $scope.cryptosharesContract.methods.shareholdersLedgerByEthAddress(
                            $rootScope.web3.eth.defaultAccount
                        )
                            .call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.myShareholderData = result;

                                if (result.shareholderID && result.shareholderID > 0) {
                                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.isLegalPerson = result.shareholderIsLegalPerson;
                                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderName = result.shareholderName;
                                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderRegistrationNumber = result.shareholderRegistrationNumber;
                                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderAddress = result.shareholderAddress;
                                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.linkToSignersAuthorityToRepresentTheShareholder = result.linkToSignersAuthorityToRepresentTheShareholder;
                                }

                                $log.debug("$scope.cryptoSharesContractData.myShareholderData:");
                                $log.debug($scope.cryptoSharesContractData.myShareholderData);
                                return $scope.verificationContract.methods.verification(
                                    $rootScope.web3.eth.defaultAccount
                                ).call()
                            })
                            .then((result) => {
                                $scope.cryptoSharesContractData.myShareholderData.representative = result;
                                if (parseInt(result.verificationAddedOn) > 0 && parseInt(result.revokedOn) === 0) {
                                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.representative =
                                        result.firstName + " " + result.lastName + " (ETH address: " + $rootScope.web3.eth.defaultAccount + ")"
                                }
                            })
                            .catch((error) => {
                                $log.error("$scope.getMyShareholderData ERROR :");
                                $log.error(error);
                            })
                            .finally(() => {
                                $scope.getMyShareholdersDataIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.getRegisteredSharesIsWorking = false;
                    $scope.getRegisteredShares = function () {
                        $scope.getRegisteredSharesIsWorking = true;
                        $scope.cryptosharesContract.methods.registeredShares()
                            .call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.registeredShares = parseInt(result);
                            })
                            .catch((error) => {
                                $log.error("$scope.getRegisteredShares ERROR :");
                                $log.error(error);
                            })
                            .finally(() => {
                                $scope.getRegisteredSharesIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.getDisputeResolutionAgreementSignaturesCounterIsWorking = false;
                    $scope.getDisputeResolutionAgreementSignaturesCounter = function () {
                        $scope.getDisputeResolutionAgreementSignaturesCounterIsWorking = true;
                        $scope.cryptosharesContract.methods.disputeResolutionAgreementSignaturesCounter().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.disputeResolutionAgreementSignaturesCounter = parseInt(result);
                            })
                            .finally(() => {
                                $scope.getDisputeResolutionAgreementSignaturesCounterIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.cryptoSharesContractData.signatures = [];
                    $scope.signatureNumber = 1;
                    $scope.getSignatureNumberIsWorking = false;
                    $scope.getSignatureNumber = function (signatureNumber) {

                        $scope.getSignatureNumberError = null;

                        if (!$scope.cryptoSharesContractData.signatures[signatureNumber]) {

                            $scope.getSignatureNumberIsWorking = true;
                            $scope.getSignatureNumberError = null;

                            $scope.cryptosharesContract.methods.disputeResolutionAgreementSignaturesByNumber(signatureNumber).call()
                                .then((result) => {
                                    if (result && result.signatoryRepresentedBy !== "0x0000000000000000000000000000000000000000") {
                                        if (result.signedOnUnixTime) {
                                            result.signedOnUnixTime = parseInt(result.signedOnUnixTime);
                                            result.signedOn = $rootScope.dateFromUnixTime(result.signedOnUnixTime);
                                        }
                                        $scope.cryptoSharesContractData.signatures[signatureNumber] = result;

                                        // $log.debug($scope.cryptoSharesContractData.signatures[signatureNumber]);

                                    } else {
                                        $scope.getSignatureNumberError =
                                            "No signature number " + signatureNumber;
                                    }
                                })
                                .finally(() => {
                                    $scope.getSignatureNumberIsWorking = false;
                                    $scope.$apply();
                                })
                        } else {
                            // $scope.$apply(); // < error
                        }
                    };

                    $scope.getDisputeResolutionAgreement = function () {
                        $scope.cryptosharesContract.methods.disputeResolutionAgreement().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.disputeResolutionAgreement = result;
                                $scope.$apply();
                            })
                    };

                    $scope.refreshTokenBalanceOfDefaultAccountIsWorking = false;
                    $scope.refreshTokenBalanceOfDefaultAccount = function () {
                        if ($rootScope.web3.eth.defaultAccount) {

                            $scope.refreshTokenBalanceOfDefaultAccountIsWorking = true;

                            $scope.cryptosharesContract.methods.balanceOf($rootScope.web3.eth.defaultAccount).call()
                                .then(function (result) {
                                    $scope.cryptoSharesContractData.balanceOfDefaultAccount = parseInt(result);

                                    // $log.debug("$scope.cryptoSharesContractData.balanceOfDefaultAccount :",
                                    //     $scope.cryptoSharesContractData.balanceOfDefaultAccount
                                    // );
                                })
                                .catch(function (error) {
                                    $log.debug("$scope.refreshTokenBalanceOfDefaultAccount ERROR:");
                                    $log.debug(error);
                                })
                                .finally(function () {
                                    $scope.refreshTokenBalanceOfDefaultAccountIsWorking = false;
                                    $scope.$apply();
                                })
                        }
                    };

                    $scope.refreshEthBalanceOfContractIsWorking = false;
                    $scope.refreshEthBalanceOfContract = function () {
                        $scope.refreshEthBalanceOfContractIsWorking = true;

                        $rootScope.web3.eth.getBalance($scope.cryptosharesContract._address)
                            .then(function (result) {

                                // $log.debug("$rootScope.web3.eth.getBalance(" + $scope.cryptosharesContract._address + ") result:");
                                // $log.debug(result);

                                if (parseInt(result) > 0) {
                                    $scope.cryptoSharesContractData.weiBalanceOfContract = parseInt(result);
                                    $scope.cryptoSharesContractData.ethBalanceOfContract = $rootScope.web3.utils.fromWei(result, "ether");
                                } else {
                                    $scope.cryptoSharesContractData.weiBalanceOfContract = 0;
                                    $scope.cryptoSharesContractData.ethBalanceOfContract = 0;
                                }

                                // $scope.cryptoSharesContractData.weiBalanceOfContract = parseInt(result);
                                // $scope.cryptoSharesContractData.ethBalanceOfContract = $rootScope.web3.utils.fromWei(result, "ether");
                            })
                            .catch(function (error) {
                                $log.error("$scope.refreshEthBalanceOfContract ERROR:");
                                $log.error(error);
                            })
                            .finally(function () {
                                $scope.refreshEthBalanceOfContractIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.refreshXEuroBalanceOfContractIsWorking = false;
                    $scope.refreshXEuroBalanceOfContract = function () {
                        $scope.refreshXEuroBalanceOfContractIsWorking = true;

                        $scope.xEuroContract.methods.balanceOf($scope.cryptosharesContract._address).call()
                            .then(function (result) {
                                $scope.cryptoSharesContractData.xEuroBalanceOfContract = parseInt(result);
                            })
                            .catch(function (error) {
                                $log.error(" $scope.refreshXEuroBalanceOfContract ERROR:");
                                $log.error(error);
                            })
                            .finally(function () {
                                $scope.refreshXEuroBalanceOfContractIsWorking = false;
                                $scope.addXEURToContractIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.refreshXEuroBalanceOfDefaultAccountIsWorking = false;
                    $scope.refreshXEuroBalanceOfDefaultAccount = function () {
                        $scope.refreshXEuroBalanceOfDefaultAccountIsWorking = true;

                        $scope.xEuroContract.methods.balanceOf($rootScope.web3.eth.defaultAccount).call()
                            .then(function (result) {
                                $scope.xEuroBalanceOfDefaultAccount = parseInt(result);
                            })
                            .catch(function (error) {
                                $log.error("$scope.refreshXEuroBalanceOfDefaultAccount ERROR:");
                                $log.error(error);
                            })
                            .finally(function () {
                                $scope.refreshXEuroBalanceOfDefaultAccountIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.refreshEthBalanceOfDefaultAccountIsWorking = false;
                    $scope.refreshEthBalanceOfDefaultAccount = function () {

                        if ($rootScope.web3.eth.defaultAccount) {

                            $scope.refreshEthBalanceOfDefaultAccountIsWorking = true;

                            $rootScope.web3.eth.getBalance($rootScope.web3.eth.defaultAccount)
                                .then(function (result) {
                                    $scope.weiBalanceOfDefaultAccount = parseInt(result);
                                    $scope.ethBalanceOfDefaultAccount = $rootScope.web3.utils.fromWei(result, "ether");

                                    // $log.debug("$scope.ethBalanceOfDefaultAccount :",
                                    //     $scope.ethBalanceOfDefaultAccount
                                    // );
                                })
                                .catch(function (error) {
                                    $log.error("$scope.ethbalanceOfDefaultAccount ERROR:");
                                    $log.error(error);
                                })
                                .finally(function () {
                                    $scope.refreshEthBalanceOfDefaultAccountIsWorking = false;
                                    $scope.addEtherToContractIsWorking = false;
                                    $scope.$apply();
                                })
                        }
                    };

                    $scope.addEtherValue = 0;
                    $scope.addEtherToContractIsWorking = false;
                    $scope.addEtherToContract = function () {
                        $scope.addEtherToContractIsWorking = true;
                        let valueInWei = $rootScope.web3.utils.toWei($scope.addEtherValue.toString(), 'ether');

                        $scope.cryptosharesContract.methods.addEtherToContract()
                            .send({
                                from: $rootScope.web3.eth.defaultAccount,
                                value: valueInWei
                            })
                            .then((receipt) => {

                                $log.debug('addEtherToContract receipt:');
                                $log.debug(receipt);

                                $scope.refreshEthBalanceOfDefaultAccount();
                                $scope.refreshTokenBalanceOfDefaultAccount();
                                $scope.refreshXEuroBalanceOfContract();
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                // $scope.addEtherToContractIsWorking = false;
                            })
                    };

                    $scope.addXEURValue = 0;
                    $scope.addXEURToContractIsWorking = false;
                    $scope.addXEURToContract = function () {
                        $scope.addXEURToContractIsWorking = true;

                        $scope.xEuroContract.methods.transfer(
                            $scope.cryptosharesContract._address,
                            $scope.addXEURValue
                        )
                            .send({
                                from: $rootScope.web3.eth.defaultAccount,
                            })
                            .then((receipt) => {

                                $log.debug('$scope.addXEURToContract receipt:');
                                $log.debug(receipt);

                                $scope.refreshEthBalanceOfDefaultAccount();
                                $scope.refreshXEuroBalanceOfDefaultAccount();
                                $scope.refreshXEuroBalanceOfContract();
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                // $scope.addEtherToContractIsWorking = false;
                            })
                    };

                    $scope.registerAsShareholderAndSignArbitrationAgreementIsWorking = false;
                    $scope.registerAsShareholderAndSignArbitrationAgreementForm = {};
                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.isLegalPerson = true;
                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderName = "Shareholder Company Ltd.";
                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderRegistrationNumber = "12345678";
                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderAddress = "22 Main St., Test City, 1234, United Kingdom";
                    $scope.registerAsShareholderAndSignArbitrationAgreementForm.linkToSignersAuthorityToRepresentTheShareholder = "https://beta.companieshouse.gov.uk/company/12345678";

                    $scope.registerAsShareholderAndSignArbitrationAgreement = () => {

                        $rootScope.progressbar.start();

                        // $timeout($rootScope.progressbar.complete(), 1000)
                        $scope.registerAsShareholderAndSignArbitrationAgreementIsWorking = true;

                        $scope.registerAsShareholderAndSignArbitrationAgreementTxReceipt = null;

                        $scope.cryptosharesContract.methods.registerAsShareholderAndSignArbitrationAgreement(
                            $scope.registerAsShareholderAndSignArbitrationAgreementForm.isLegalPerson,
                            $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderName,
                            $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderRegistrationNumber,
                            $scope.registerAsShareholderAndSignArbitrationAgreementForm.shareholderAddress,
                            $scope.registerAsShareholderAndSignArbitrationAgreementForm.linkToSignersAuthorityToRepresentTheShareholder
                        ).send({from: $rootScope.web3.eth.defaultAccount})
                            .then((receipt) => {

                                $scope.registerAsShareholderAndSignArbitrationAgreementTxReceipt = receipt;

                                // $log.debug("$scope.registerAsShareholderAndSignArbitrationAgreementTxReceipt :");
                                // $log.debug($scope.registerAsShareholderAndSignArbitrationAgreementTxReceipt);

                                $scope.getMyShareholderData();
                                $scope.getDisputeResolutionAgreementSignaturesCounter();
                                $scope.getShareholdersLedger();
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                $scope.registerAsShareholderAndSignArbitrationAgreementIsWorking = false;
                                $scope.refreshContractData();
                                // $rootScope.progressbar.start();
                                $timeout($rootScope.progressbar.complete(), 1000);
                            })
                    };

                    /* ================ SHAREHOLDERS LEDGER ======================= */

                    const getInfoFromVerificationContract = function (_shareholderNumberInLocalLedger, address, cryptosharesContractAddress) {

                        const shareholderNumberInLocalLedger = _shareholderNumberInLocalLedger;

                        if (!$scope.verificationContract) {
                            $log.error("verification contract not initialized");
                            return;
                        }

                        if (!$scope.cryptoSharesContractData.shareholdersLedger) {
                            $log.error("$scope.cryptoSharesContractData.shareholdersLedger was not initialized");
                        }

                        // check if we work with right contract
                        if (cryptosharesContractAddress !== $scope.cryptosharesContract._address) {
                            $log.error("request was made for another contract");
                            return;
                        }
                        $scope.verificationContract.methods.verification(address).call()
                            .then((result) => {
                                $log.debug(address, "info from verification contract:");
                                $log.debug(result);

                                // add data:
                                $scope.cryptoSharesContractData.shareholdersLedger[shareholderNumberInLocalLedger].representative
                                    = result;
                            })
                            .catch((error) => {
                                $log.error("getInfoFromVerificationContract ERROR :");
                                $log.error(error);
                            })
                            .finally(() => {
                                $scope.$apply();
                            })

                    };

                    $scope.getShareholdersLedgerIsWorking = false;
                    $scope.showShareholdersLedger = false;
                    $scope.expandShareholderEntity = [];
                    $scope.getShareholdersLedger = function () {

                        $scope.getShareholdersLedgerIsWorking = true;
                        $rootScope.progressbar.start();
                        // $timeout($rootScope.progressbar.complete(), 1000)

                        $scope.getRegisteredShares();

                        $scope.cryptoSharesContractData.shareholdersLedger = [];
                        $scope.cryptoSharesContractData.shareholdersLedgerAll = [];

                        $scope.cryptosharesContract.methods.shareholdersCounter().call()
                            .then((result) => {

                                result = parseInt(result);
                                $scope.cryptoSharesContractData.shareholdersCounter = result;

                                // $log.debug("Shareholders counter:", $scope.cryptoSharesContractData.shareholdersCounter);

                                // $log.debug("$scope.cryptoSharesContractData.shareholdersCounter:",
                                //     $scope.cryptoSharesContractData.shareholdersCounter
                                // );

                                if (result > 0) {
                                    $scope.showShareholdersLedger = true;
                                    for (let i = 1; i <= result; i++) {

                                        $scope.cryptosharesContract.methods.shareholdersLedgerByIdNumber(i).call()
                                            .then((shareholder) => {

                                                $scope.cryptoSharesContractData.shareholdersLedgerAll.push(shareholder);

                                                if (parseInt(shareholder.balanceOf) > 0) {

                                                    // The push() method adds new items to the end of an array, and returns the new length.
                                                    const arrayLength = $scope.cryptoSharesContractData.shareholdersLedger.push(shareholder);
                                                    const shareholderNumber = arrayLength - 1;

                                                    getInfoFromVerificationContract(
                                                        shareholderNumber,
                                                        shareholder.shareholderEthereumAddress,
                                                        $scope.cryptosharesContract._address
                                                    );
                                                }

                                                // finish:
                                                if ($scope.cryptoSharesContractData.shareholdersLedgerAll.length === $scope.cryptoSharesContractData.shareholdersCounter) {

                                                    drawPieChart();

                                                    $scope.getShareholdersLedgerIsWorking = false;
                                                    $timeout($rootScope.progressbar.complete(), 1000);

                                                    $scope.$apply();
                                                }
                                            })
                                    }
                                } else {
                                    $scope.getShareholdersLedgerIsWorking = false;
                                    $timeout($rootScope.progressbar.complete(), 1000);
                                    $scope.$apply();
                                }

                            })
                            .catch(() => {
                                $log.error("$scope.getShareholdersLedger  ERROR:");
                                $log.error(error);
                                $scope.getShareholdersLedgerIsWorking = false;
                                $timeout($rootScope.progressbar.complete(), 1000);
                                $scope.$apply();
                            });
                    };

                    /* ----- Shareholders Pie Chart ----  */

                    const createShareholdersBalancesArray = function () { // for the chart

                        // $log.log('createShareholdersBalancesArray started');

                        const arrayForChart = [
                            ['Shareholder name', 'Shares']
                        ];

                        let sharesOfRegisteredShareholders = 0;

                        for (let i = 0; i < $scope.cryptoSharesContractData.shareholdersLedger.length; i++) {

                            let shareholder = $scope.cryptoSharesContractData.shareholdersLedger[i];

                            const arrToAdd = [
                                shareholder.shareholderName,
                                parseInt(shareholder.balanceOf)
                            ];

                            sharesOfRegisteredShareholders = sharesOfRegisteredShareholders + parseInt(shareholder.balanceOf);

                            arrayForChart.push(arrToAdd);
                        }

                        let sharesInTransfer = parseInt($scope.cryptoSharesContractData.totalSupply) - sharesOfRegisteredShareholders;

                        $scope.cryptoSharesContractData.sharesOfRegisteredShareholders = sharesOfRegisteredShareholders;
                        $scope.cryptoSharesContractData.sharesInTransfer = sharesInTransfer;

                        arrayForChart.push(['Shares in transfer', sharesInTransfer]);

                        return arrayForChart;

                    };

                    // https://google-developers.appspot.com/chart/interactive/docs/gallery/piechart
                    $scope.showPieChart = false;
                    $scope.toggleShowPieChart = function () {

                        if (!$scope.showPieChart) {
                            drawPieChart();
                            $scope.showPieChart = true;
                        } else {
                            $scope.showPieChart = false;
                        }
                    };

                    const drawPieChart = function () {

                        // $log.debug('drawPieChart: ');

                        // check if google.charts is avaiable:
                        if (typeof google == 'undefined' || typeof google.charts == 'undefined') {
                            $log.error('google.charts not loaded');
                            return;
                        }
                        //
                        google.charts.load('current', {'packages': ['corechart']});
                        google.charts.setOnLoadCallback(drawChart);

                        function drawChart() {

                            var data = google.visualization.arrayToDataTable(
                                createShareholdersBalancesArray()
                            );

                            const options = {
                                // title: 'Shares distribution',
                                is3D: true,
                            };

                            const chart = new google.visualization.PieChart(
                                document.getElementById('piechart')
                            );

                            chart.draw(
                                data,
                                options
                            );

                        }

                    };

                    /* ================ VOTING ================= */

                    $scope.createVotingForm = {};
                    $scope.createVotingForm.proposalText = "This is a mock up proposal to test voting functionality.";

                    $scope.votingLoadForm = {};
                    $scope.votingLoadForm.proposalId = 1;

                    // keeps data for existing voting
                    $scope.voting = {};

                    $scope.averageBlockTimeInSeconds = 15;

                    $scope.refreshCurrentBlockNumber = function () {
                        $scope.refreshCurrentBlockNumberIsWorking = true;
                        $rootScope.web3.eth.getBlockNumber()
                            .then((blockNumber) => {
                                $scope.currentBlockNumber = parseInt(blockNumber);
                                // $log.debug("last block number:", $rootScope.currentBlockNumber);
                            })
                            .catch((error) => {
                                $log.error(error);
                            })
                            .finally(() => {
                                $scope.refreshCurrentBlockNumberIsWorking = false;
                                $scope.$apply();
                            });
                    };

                    $rootScope.web3.eth.getBlockNumber()
                        .then((blockNumber) => {
                            $scope.currentBlockNumber = parseInt(blockNumber);
                            $scope.createVotingForm.resultsInBlock = parseInt(blockNumber) + 200; // +200 blocks
                        })
                        .catch((error) => {
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.$apply();
                        });

                    $scope.getVotingCounterForContractIsWorking = false;
                    $scope.getVotingCounterForContract = function () {
                        $scope.getVotingCounterForContractIsWorking = true;
                        $scope.cryptosharesContract.methods.votingCounterForContract().call()
                            .then((result) => {
                                $scope.votingCounterForContract = parseInt(result);
                            })
                            .catch((error) => {
                                $log.error(error);
                            })
                            .finally(() => {
                                $scope.getVotingCounterForContractIsWorking = false;
                                $scope.$apply();
                            });
                    };

                    $scope.createVotingIsWorking = false;
                    $scope.createVoting = function () {

                        $scope.createVotingIsWorking = true;

                        $scope.voting = {};
                        $scope.lastCreateVotingTxHash = null;

                        $scope.cryptosharesContract.methods.shareholdersLedgerByEthAddress(
                            $rootScope.web3.eth.defaultAccount
                        )
                            .call()
                            .then((result) => {

                                $scope.currentShareholder = result;

                                $log.debug("$scope.currentShareholder:");
                                $log.debug($scope.currentShareholder);

                                if (parseInt(result.shareholderID) === 0) {
                                    throw "To send a proposal for voting your ETH address should be verified and registered as a shareholder";
                                } else if (parseInt(result.balanceOf) === 0) {
                                    throw "To send a proposal for voting you should have at least one share.";
                                }

                                return $scope.cryptosharesContract.methods.createVoting(
                                    $scope.createVotingForm.proposalText,
                                    $scope.createVotingForm.resultsInBlock,
                                ).send({from: $rootScope.web3.eth.defaultAccount});
                            })
                            .then((receipt) => {

                                $log.debug("'create voting' tx receipt:");
                                $log.debug(receipt);

                                if (receipt && receipt.events && receipt.events.Proposal.returnValues) {

                                    $scope.lastCreateVotingTxHash = receipt.transactionHash;

                                    const proposalEvent = receipt.events.Proposal.returnValues; // < ! all strings

                                    $scope.createVotingForm.createVotingMessageSuccess =
                                        "You created a proposal with the text: '"
                                        + proposalEvent.proposalText
                                        + "', with proposal ID: "
                                        + proposalEvent.proposalId;

                                    // $scope.setAlertSuccess(
                                    //     "You created a proposal with the text: '"
                                    //     + proposalEvent.proposalText
                                    //     + "', with proposal ID: "
                                    //     + proposalEvent.proposalId
                                    // );

                                    $scope.votingLoadForm.proposalId = parseInt(proposalEvent.proposalId);

                                    $scope.loadVoting($scope.votingLoadForm.proposalId);
                                }

                            })
                            .catch((error) => {
                                $log.error(error);
                                // $scope.setAlertDanger(error);
                                $scope.votingLoadForm.createVotingMessageError = error;
                            })
                            .finally(() => {
                                $scope.createVotingIsWorking = false;
                                $scope.refreshContractData();
                            })
                    };

                    $scope.loadVotingIsWorking = false;
                    $scope.loadVoting = function (proposalId) {

                        $scope.voting = {};
                        $scope.noVotingWithThisNumber = false;

                        $scope.voting.proposalId = parseInt(proposalId);

                        if (isNaN($scope.voting.proposalId) || $scope.voting.proposalId <= 0) {
                            $scope.noVotingWithThisNumber = true;
                            return;
                        }

                        $scope.loadVotingIsWorking = true;

                        $scope.cryptosharesContract.methods.votingCounterForContract().call()
                            .then((result) => {

                                $scope.voting.votingCounterForContract = parseInt(result);

                                if ($scope.voting.proposalId > result) {
                                    $scope.noVotingWithThisNumber = true;
                                    throw "There is no proposal/voting with id/number "
                                    + $scope.voting.proposalId
                                    + " in this smart contract "
                                }

                                return $scope.cryptosharesContract.methods.proposalText($scope.voting.proposalId).call();
                            })
                            .then((result) => {
                                $scope.voting.proposalText = result;
                                return $scope.cryptosharesContract.methods.resultsInBlock($scope.voting.proposalId).call();
                            })
                            .then((result) => {
                                $scope.voting.resultsInBlock = parseInt(result);
                                return $scope.cryptosharesContract.methods.boolVotedFor($scope.voting.proposalId, $rootScope.web3.eth.defaultAccount).call();

                            })
                            .then((result) => {

                                $scope.voting.boolVotedFor = result;

                                return $scope.cryptosharesContract.methods.boolVotedAgainst($scope.voting.proposalId, $rootScope.web3.eth.defaultAccount).call();
                            })
                            .then((result) => {

                                $scope.voting.boolVotedAgainst = result;

                                if ($scope.voting.boolVotedFor || $scope.voting.boolVotedAgainst) {
                                    $scope.voting.alreadyVoted = true;
                                }

                                /*
                                if ($location.host() === "localhost") {
                                    $scope.voting.thisUrl = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/#!/" + "?contract=" + $scope.erc20Contract._address + "&proposal=" + $scope.voting.proposalId;
                                } else {
                                    $scope.voting.thisUrl = $location.protocol() + "://" + $location.host() + "/#!/" + "?contract=" + $scope.erc20Contract._address + "&proposal=" + $scope.voting.proposalId;
                                }
                                $log.debug("$scope.voting.thisUrl:", $scope.voting.thisUrl);

                                $scope.voting.copyUrl = function () {
                                    ngClipboard.toClipboard($scope.voting.thisUrl);
                                };
                                */

                                return $rootScope.web3.eth.getBlockNumber();
                            })
                            .then((result) => {

                                $scope.currentBlockNumber = parseInt(result);

                                $log.debug("current block number updated to:", $scope.currentBlockNumber);

                                if ($scope.currentBlockNumber >= $scope.voting.resultsInBlock) {
                                    $scope.voting.votingFinished = true;
                                    $log.debug("voting", $scope.voting.proposalId, "is finished", $scope.voting.votingFinished);
                                    $scope.calculateVotingResults();
                                } else {
                                    $scope.loadVotingIsWorking = false;
                                }
                            })

                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                                $scope.loadVotingIsWorking = false;
                            })
                            .finally(() => {
                                // $scope.loadVotingIsWorking = false; // not here
                                $scope.$apply();
                                // $log.debug("$scope.voting :");
                                // $log.debug($scope.voting);
                            })
                    };

                    $scope.voteForIsWorking = false;
                    $scope.voteFor = function () {
                        $scope.voteForIsWorking = true;
                        $scope.setAlertSuccess(null);
                        $scope.cryptosharesContract.methods.voteFor($scope.voting.proposalId)
                            .send({from: $rootScope.web3.eth.defaultAccount})
                            .then((receipt) => {

                                $log.debug("tx voteFor receipt:");
                                $log.debug(receipt);

                                // $scope.setAlertSuccess("Vote sent successfully");
                                $scope.createVotingForm.createVotingMessageSuccess = "Vote sent successfully";

                                $scope.loadVoting($scope.voting.proposalId);
                            })
                            .catch((error) => {
                                $log.error(error);
                                // $scope.setAlertDanger(error);
                                $scope.votingLoadForm.createVotingMessageError = error;
                            })
                            .finally(() => {
                                $scope.voteForIsWorking = false;
                            })
                    };

                    $scope.voteAgainstIsWorking = false;
                    $scope.voteAgainst = function () {
                        $scope.voteAgainstIsWorking = true;
                        $scope.setAlertSuccess(null);
                        $scope.cryptosharesContract.methods.voteAgainst($scope.voting.proposalId)
                            .send({from: $rootScope.web3.eth.defaultAccount})
                            .then((receipt) => {

                                $log.debug("tx voteAgainst receipt:");
                                $log.debug(receipt);

                                // $scope.setAlertSuccess("Vote sent successfully");
                                $scope.createVotingForm.createVotingMessageSuccess = "Vote sent successfully";

                                $scope.loadVoting($scope.voting.proposalId);
                            })
                            .catch((error) => {
                                $log.error(error);
                                // $scope.setAlertDanger(error);
                                $scope.votingLoadForm.createVotingMessageError = error;
                            })
                            .finally(() => {
                                $scope.voteAgainstIsWorking = false;
                            })

                    };

                    $scope.getRegisteredSharesForBlock = function () {
                        $scope.cryptosharesContract.methods.registeredShares()
                            .call({from: $rootScope.web3.eth.defaultAccount}, $scope.voting.resultsInBlock)
                            .then((result) => {
                                $scope.voting.registeredSharesForBlock = parseInt(result);
                            })
                    };

                    $scope.calculateVotingResults = function () {

                        $scope.getRegisteredSharesForBlock();

                        $scope.cryptosharesContract.methods.numberOfVotersFor($scope.voting.proposalId).call()
                            .then((result) => {
                                $scope.voting.numberOfVotersFor = parseInt(result);
                                countForVotes();
                                return $scope.cryptosharesContract.methods.numberOfVotersAgainst($scope.voting.proposalId).call();
                            })
                            .then((result) => {
                                $scope.voting.numberOfVotersAgainst = parseInt(result);
                                countAgainstVotes();
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                // $log.debug("$scope.voting:");
                                // $log.debug($scope.voting);
                                // TODO: remove here+
                                $scope.loadVotingIsWorking = false;
                            })
                    };

                    function countForVotes() {

                        $log.debug("Start counting 'for' votes. Number of voters 'for':", $scope.voting.numberOfVotersFor);

                        let votes = 0;
                        let votersCounter = 0;

                        if ($scope.voting.numberOfVotersFor > 0) {
                            // start with: i=1
                            for (let i = 1; i <= $scope.voting.numberOfVotersFor; i++) {

                                $scope.cryptosharesContract.methods.votedFor($scope.voting.proposalId, i).call()
                                    .then((address) => {
                                        return $scope.cryptosharesContract.methods.balanceOf(address)
                                            .call({from: $rootScope.web3.eth.defaultAccount}, $scope.voting.resultsInBlock);
                                    })
                                    .then((balanceInTokens) => {
                                        votes = votes + parseInt(balanceInTokens);
                                        votersCounter++;
                                    })
                                    .catch((error) => {
                                        $log.error(error);
                                        $scope.setAlertDanger(error);
                                    })
                                    .finally(() => {

                                        if (votersCounter === $scope.voting.numberOfVotersFor) {
                                            $scope.voting.votesFor = votes;
                                        }

                                        if ($scope.voting.votesFor >= 0 && $scope.voting.votesAgainst >= 0) {
                                            $scope.loadVotingIsWorking = false;

                                            $log.debug("results for voting with ID:", $scope.voting.proposalId);
                                            $log.debug($scope.voting);
                                        }

                                        $scope.$apply();
                                    })

                            }
                        } else {
                            $scope.voting.votesFor = 0;
                            if ($scope.voting.votesFor >= 0 && $scope.voting.votesAgainst >= 0) {
                                $scope.loadVotingIsWorking = false;

                                $log.debug("results for voting with ID:", $scope.voting.proposalId);
                                $log.debug($scope.voting);
                            }
                            $scope.$apply();
                        }


                    } // end of function countForVotes();

                    function countAgainstVotes() {

                        $log.debug("Start counting 'against' votes. Number of voters 'against':", $scope.voting.numberOfVotersAgainst);

                        let votes = 0;
                        let votersCounter = 0;

                        if ($scope.voting.numberOfVotersAgainst > 0) {
                            // start with: i=1
                            for (let i = 1; i <= $scope.voting.numberOfVotersAgainst; i++) {

                                $scope.cryptosharesContract.methods.votedAgainst($scope.voting.proposalId, i).call()
                                    .then((address) => {
                                        return $scope.cryptosharesContract.methods.balanceOf(address)
                                            .call({from: $rootScope.web3.eth.defaultAccount}, $scope.voting.resultsInBlock);
                                    })
                                    .then((balanceInTokens) => {
                                        votes = votes + parseInt(balanceInTokens);
                                        votersCounter++;
                                    })
                                    .catch((error) => {
                                        $log.error(error);
                                        $scope.setAlertDanger(error);
                                    })
                                    .finally(() => {

                                        if (votersCounter === $scope.voting.numberOfVotersAgainst) {
                                            $scope.voting.votesAgainst = votes;
                                        }

                                        if ($scope.voting.votesFor >= 0 && $scope.voting.votesAgainst >= 0) {
                                            $scope.loadVotingIsWorking = false;


                                            $log.debug("results for voting with ID:", $scope.voting.proposalId);
                                            $log.debug($scope.voting);
                                        }

                                        $scope.$apply();
                                    })

                            }
                        } else {
                            $scope.voting.votesAgainst = 0;

                            if ($scope.voting.votesFor >= 0 && $scope.voting.votesAgainst >= 0) {
                                $scope.loadVotingIsWorking = false;


                                $log.debug("results for voting with ID:", $scope.voting.proposalId);
                                $log.debug($scope.voting);
                            }
                            $scope.$apply();
                        }


                    } // end of function countAgainstVotes();

                    /* =========== TRANSFERS ==========================*/

                    $scope.transferValue = 0;
                    $scope.transferTo = $rootScope.web3.eth.defaultAccount;
                    $scope.transferData = null;

                    $scope.transfer = function () {
                        $scope.transferWorking = true;
                        $scope.transferError = null;
                        $scope.transferTxHash = null;
                        // $scope.refreshBalanceOfContract();
                        if ($scope.transferValue > $scope.cryptoSharesContractData.balanceOfDefaultAccount) {
                            $scope.transferError = "insufficient funds";
                            $scope.transferWorking = false;
                            return;
                        }

                        $log.debug(
                            "Transfer",
                            $scope.transferValue,
                            $scope.cryptoSharesContractData.symbol,
                            " from:",
                            $rootScope.web3.eth.defaultAccount,
                            "to:",
                            $scope.transferTo,
                            " bytes data:",
                            $scope.transferData,
                        );

                        if ($scope.transferData) {
                            $scope.cryptosharesContract.methods.transfer(
                                $scope.transferTo,
                                $scope.transferValue,
                                // $rootScope.web3.toHex($scope.transferData)
                                $scope.transferData
                            ).send({"from": $rootScope.web3.eth.defaultAccount})
                                .then(function (receipt) {

                                    $log.debug("Transfer tx receipt :");
                                    $log.debug(receipt);

                                    $scope.transferTxHash = receipt.transactionHash;
                                })
                                .catch(function (error) {
                                    $log.error("Transfer ERROR:");
                                    $log.error(error);
                                    $scope.transferError = error;
                                })
                                .finally(function () {
                                    $scope.transferWorking = false;
                                    $scope.refreshContractData();
                                });
                        } else {
                            $scope.cryptosharesContract.methods.transfer(
                                $scope.transferTo,
                                $scope.transferValue
                            ).send({"from": $rootScope.web3.eth.defaultAccount})
                                .then(function (receipt) {

                                    $log.debug("Transfer tx receipt :");
                                    $log.debug(receipt);

                                    $scope.transferTxHash = receipt.transactionHash;

                                })
                                .catch(function (error) {
                                    $log.debug("Transfer ERROR:");
                                    $log.debug(error);
                                    $scope.transferError = error;
                                })
                                .finally(function () {
                                    $scope.transferWorking = false;
                                    $scope.refreshContractData();
                                });
                        }


                    }; // end of transfer

                    /* ============== DIVIDENDS =================*/

                    $scope.getDividendsPeriod = function () {
                        $scope.cryptosharesContract.methods.dividendsPeriod().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.dividendsPeriod = parseInt(result);
                                $scope.cryptoSharesContractData.dividendsPeriodStr =
                                    $rootScope.secondsToDaysHoursMinutesSecondsStr(result);
                            })
                    };

                    $scope.calculateNextDividendsPeriodStart = function () {

                        $scope.cryptoSharesContractData.newDividendsRoundCanBeStarted = false;

                        let secondsAfterLastDividendsRound =
                            $rootScope.unixTimeFromDate(new Date) -
                            $scope.cryptoSharesContractData.dividendsRound.roundFinishedOnUnixTime;

                        $scope.cryptoSharesContractData.secondsLeftUntilNextRound =
                            $scope.cryptoSharesContractData.dividendsPeriod - secondsAfterLastDividendsRound;

                        if ($scope.cryptoSharesContractData.secondsLeftUntilNextRound < 0) {
                            $scope.cryptoSharesContractData.newDividendsRoundCanBeStarted = true;
                        } else {
                            $scope.cryptoSharesContractData.timeLeftUntilNextRoundStr =
                                $rootScope.secondsToDaysHoursMinutesSecondsStr($scope.cryptoSharesContractData.secondsLeftUntilNextRound)
                        }

                        $scope.startDividendsPaymentsIsWorking = false;
                        $scope.payDividendsToNextIsWorking = false;

                        $scope.$apply();

                        // $log.debug("$scope.cryptoSharesContractData:");
                        // $log.debug($scope.cryptoSharesContractData);

                    };

                    $scope.getDividendsRoundData = function () {

                        $scope.cryptosharesContract.methods.dividendsPeriod().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.dividendsPeriod = parseInt(result);
                                $scope.cryptoSharesContractData.dividendsPeriodStr =
                                    $rootScope.secondsToDaysHoursMinutesSecondsStr(result);
                                return $scope.cryptosharesContract.methods.dividendsRoundsCounter().call();
                            })
                            .then((result) => {
                                $scope.cryptoSharesContractData.dividendsRoundsCounter = parseInt(result);
                                return $scope.cryptosharesContract.methods.dividendsRound(
                                    $scope.cryptoSharesContractData.dividendsRoundsCounter
                                ).call();
                            })
                            .then((result) => {

                                // $log.debug("$scope.getDividendsRoundData result:");
                                // $log.debug(result);

                                $scope.cryptoSharesContractData.dividendsRound = result;

                                $scope.cryptoSharesContractData.dividendsRound.sumEthToPayForOneToken
                                    = $rootScope.web3.utils.fromWei(result.sumWeiToPayForOneToken, 'ether');

                                $scope.cryptoSharesContractData.dividendsRound.ethForTxFees
                                    = parseFloat($rootScope.web3.utils.fromWei(result.weiForTxFees, 'ether'));

                                $scope.cryptoSharesContractData.dividendsRound.shareholdersCounter
                                    = parseInt(result.shareholdersCounter);

                                $scope.cryptoSharesContractData.dividendsRound.roundFinishedOnUnixTime
                                    = parseInt(result.roundFinishedOnUnixTime);

                                $scope.cryptoSharesContractData.dividendsRound.roundStartedOnDate
                                    = $rootScope.dateFromUnixTime(result.roundStartedOnUnixTime);

                                $scope.cryptoSharesContractData.dividendsRound.roundFinishedOnDate
                                    = $rootScope.dateFromUnixTime(result.roundFinishedOnUnixTime);

                                // $log.debug("$scope.cryptoSharesContractData.dividendsRound");
                                // $log.debug($scope.cryptoSharesContractData.dividendsRound);

                                if (!$scope.cryptoSharesContractData.dividendsRound.roundIsRunning) {
                                    $scope.calculateNextDividendsPeriodStart();
                                } else {
                                    $scope.startDividendsPaymentsIsWorking = false;
                                    $scope.payDividendsToNextIsWorking = false;
                                }
                            })
                            .catch(function (error) {
                                $log.error("$scope.getDividendsRoundData ERROR:");
                                $log.error(error)
                            })
                            .finally(function () {
                                $scope.fundDividendsPayoutIsWorking = false;
                                $scope.$apply();
                            });
                    };

                    $scope.startDividendsPaymentsIsWorking = false;
                    $scope.dividendsError = null;
                    $scope.startDividendsPayments = function () {

                        $scope.dividendsError = null;
                        $scope.startDividendsPaymentsIsWorking = true;

                        $scope.cryptosharesContract.methods.registeredShares().call()
                            .then((result) => {
                                $scope.cryptoSharesContractData.registeredShares = parseInt(result);

                                return $rootScope.web3.eth.getBalance($scope.cryptosharesContract._address)
                            })
                            .then(function (result) {

                                // $log.debug("$rootScope.web3.eth.getBalance(" + $scope.cryptosharesContract._address + ") result:");
                                // $log.debug(result);

                                if (parseInt(result) > 0) {
                                    $scope.cryptoSharesContractData.weiBalanceOfContract = parseInt(result);
                                    $scope.cryptoSharesContractData.ethBalanceOfContract = $rootScope.web3.utils.fromWei(result, "ether");
                                } else {
                                    $scope.cryptoSharesContractData.weiBalanceOfContract = 0;
                                    $scope.cryptoSharesContractData.ethBalanceOfContract = 0;
                                }

                                return $scope.xEuroContract.methods.balanceOf($scope.cryptosharesContract._address).call();
                            })
                            .then(function (result) {
                                $scope.cryptoSharesContractData.xEuroBalanceOfContract = parseInt(result);

                                // >>>
                                const weiBalance = parseInt($scope.cryptoSharesContractData.weiBalanceOfContract);
                                const xEurBalance = parseInt($scope.cryptoSharesContractData.xEuroBalanceOfContract);
                                const registeredShares = parseInt($scope.cryptoSharesContractData.registeredShares);

                                const sumWeiToPayForOneToken = weiBalance / registeredShares;
                                const sumXEurToPayForOneToken = xEurBalance / registeredShares;
                                /*
                                $log.debug(
                                    "weiBalance:", weiBalance, "; xEurBalance:", xEurBalance, "; registeredShares:",
                                    registeredShares, "; sumWeiToPayForOneToken:", sumWeiToPayForOneToken,
                                    "; sumXEurToPayForOneToken:", sumXEurToPayForOneToken
                                );
                                */

                                if ((weiBalance === 0 && xEurBalance === 0) || (sumWeiToPayForOneToken < 1 && sumXEurToPayForOneToken < 1)) {
                                    $scope.dividendsError = "Insufficient funds in smart contract to pay dividends";
                                    $scope.startDividendsPaymentsIsWorking = false;
                                    $scope.$apply();
                                    return;
                                }

                                $scope.cryptosharesContract.methods.startDividendsPayments()
                                    .send({"from": $rootScope.web3.eth.defaultAccount})
                            })
                            .then((receipt) => {

                                // $log.debug("startDividendsPayments tx receipt:");
                                // $log.debug(receipt);

                                $scope.getDividendsRoundData();
                            })
                            .catch(function (error) {
                                $log.error("$scope.getDividendsRoundData ERROR:");
                                $log.error(error);
                                $scope.dividendsError = error;
                                $scope.startDividendsPaymentsIsWorking = false;
                                $scope.$apply();
                            })
                    };

                    $scope.fundDividendsPayoutIsWorking = false;
                    $scope.fundDividendsPayoutSumInEther = 0;
                    $scope.fundDividendsPayout = function () {
                        $scope.fundDividendsPayoutIsWorking = true;

                        const valueInWei = $rootScope.web3.utils.toWei(
                            $scope.fundDividendsPayoutSumInEther.toString(),
                            'ether'
                        );

                        $log.debug("value:", valueInWei);

                        $scope.cryptosharesContract.methods.fundDividendsPayout()
                            .send({"from": $rootScope.web3.eth.defaultAccount, "value": valueInWei})
                            .then((receipt) => {

                                $log.debug("fundDividendsPayout tx receipt:");
                                $log.debug(receipt);

                                $scope.getDividendsRoundData();
                            })
                            .catch(function (error) {
                                $log.error("$scope.getDividendsRoundData ERROR:");
                                $log.error(error)
                            })
                    };

                    $scope.payDividendsToNextIsWorking = false;
                    $scope.payDividendsToNext = function () {
                        $scope.payDividendsToNextIsWorking = true;
                        $scope.cryptosharesContract.methods.payDividendsToNext()
                            .send({"from": $rootScope.web3.eth.defaultAccount})
                            .then((receipt) => {

                                $log.debug("payDividendsToNext tx receipt:");
                                $log.debug(receipt);

                                $scope.getDividendsRoundData();

                            })
                            .catch(function (error) {
                                $log.error("$scope.payDividendsToNext ERROR:");
                                $log.error(error)
                            })

                    };

                    /* ============== EVENTS ===================  */

                    $scope.maxEventsNumber = 100;
                    $scope.watchEvents = function () {

                        // $log.debug("starting watchEvents()");
                        // $log.debug("[watchEvents()] $scope.contract:");
                        // $log.debug($scope.contract);

                        $scope.events = [];
                        // $scope.showEvents = false; // < collapse events

                        // https://web3js.readthedocs.io/en/1.0/web3-eth-contract.html#id38
                        const eventsOptions = {
                            "fromBlock": $scope.factoryContractDeployedOnBlock[$rootScope.currentNetwork.network_id]
                            // toBlock: 'latest'
                        };

                        $scope.cryptosharesContract.events.allEvents(
                            eventsOptions
                        )
                            .on('data', function (event) {

                                // $log.debug("event:");
                                // $log.debug(event);

                                // The unshift() method adds new items to the beginning of an array, and returns the new length.
                                $scope.events.unshift(event);
                                // $scope.events.push(event);

                                if ($scope.maxEventsNumber && $scope.events.length > $scope.maxEventsNumber) {
                                    // The pop() method removes the last element of an array, and returns that element.
                                    $scope.events.pop();
                                    // $scope.events.shift();
                                }

                                $scope.refreshContractData(); // <<< after each event refresh smart contract's data
                            })
                            .on('error', function (error) {
                                $log.error("contract.events.allEvents error:");
                                $log.error(error);
                            });

                    };
                    $scope.watchEvents(); // << run

                    /* =========== LOAD Contract Data ================ */

                    $scope.refreshContractData = function () {
                        $scope.refreshCurrentBlockNumber();
                        $scope.getVotingCounterForContract();
                        $scope.getShareholdersCounter();
                        $scope.getRegisteredShares();
                        $scope.getDisputeResolutionAgreementSignaturesCounter();
                        $scope.refreshTokenBalanceOfDefaultAccount();
                        $scope.refreshEthBalanceOfDefaultAccount();
                        $scope.refreshEthBalanceOfContract();
                        $scope.refreshXEuroBalanceOfContract();
                        $scope.refreshXEuroBalanceOfDefaultAccount();
                        $scope.getDividendsRoundData();
                        $scope.getMyShareholderData();
                        // $scope.getShareholdersLedger(); // (!) not here, to many requests for large data set
                    };

                    $scope.loadCryptoSharesContractData = function () {
                        $scope.getName();
                        $scope.getSymbol();
                        $scope.getTotalSupply();
                        $scope.getContractNumberInTheLedger();
                        $scope.getDisputeResolutionAgreement();
                        $scope.getDividendsPeriod();
                        $scope.getShareholdersLedger();
                        $scope.refreshContractData();
                    };
                    $scope.loadCryptoSharesContractData();

                    $scope.instantiateCryptoSharesContractIsWorking = false;
                    $scope.switchToCryptosharesContractNumberIsWorking = false;
                    $scope.$apply();

                }; // end of $scope.instantiateCryptoSharesContract()

                // $scope.cryptoSharesContractNumber = 0;
                $scope.switchToCryptosharesContractNumber = function (number) {

                    number = parseInt(number);
                    $log.debug("$scope.switchToCryptosharesContractNumber started, with number:", number);

                    if (isNaN(number) || number <= 0) {
                        $scope.setAlertDanger(
                            "There is no contract with number " + number + " in the ledger."
                        );
                        return;
                    }

                    $scope.switchToCryptosharesContractNumberIsWorking = true;

                    $scope.factoryContract.methods.cryptoSharesContractsLedger(number).call()
                    /*
                    uint contractId;
                    address contractAddress;
                    string name; // the same as token name
                    string symbol; // the same as token symbol
                    uint totalSupply; // the same as token totalSupply
                    uint dividendsPeriod;
                    */
                        .then((cryptoSharesContract) => {
                            $scope.cryptoSharesContractDataFromLedger = cryptoSharesContract;
                            $log.debug(
                                "$scope.cryptoSharesContractDataFromLedger :",
                                $scope.cryptoSharesContractDataFromLedger
                            );

                            if ($scope.cryptoSharesContractDataFromLedger[1] !== "0x0000000000000000000000000000000000000000") {
                                $scope.instantiateCryptoSharesContract(
                                    $scope.cryptoSharesContractDataFromLedger[1]
                                );
                            } else {
                                $scope.setAlertDanger(
                                    "There is no contract with number " + number + " in the ledger."
                                );
                                $scope.switchToCryptosharesContractNumberIsWorking = false;
                                $scope.$apply(); // (!) needed here
                            }
                        })
                        .catch((error) => {
                            $log.error("$scope.switchToCryptosharesContractNumber :");
                            $log.error(error);
                        })
                };

                /* >>> if number of contract provided in URL */
                if ($stateParams.contractNumber) {
                    $rootScope.progressbar.start();
                    let contractNumber = parseInt($stateParams.contractNumber);
                    $scope.switchToCryptosharesContractNumber(contractNumber);
                    $scope.changeTabToWork();
                }

            } // end of setUpContract

            $timeout($rootScope.progressbar.complete(), 1000); // <<< started in app.js

        }]); // end function homeCtl

})();