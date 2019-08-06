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

            // activate tabs:
            $('.menu .item').tab();

            // https://github.com/Semantic-Org/Semantic-UI/issues/3544
            $scope.changeTab = function (newTab) {
                $.tab('change tab', newTab);
                $('.menu .item').removeClass("active");
                $('.menu .item[data-tab="' + newTab + '"]').addClass("active");
            };

            $scope.changeTabToWork = function () {
                $scope.changeTab("work");
            };

            $scope.changeTabToHelp = function () {
                $scope.changeTab("helpTab");
            };


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

            (function setUpContractsData() {

                $scope.deployedOnNetworksArray = [
                    // 1, // MainNet
                    3 // Ropsten
                ];

                /* ---- Factory Contract ---- */
                $scope.factoryContractAbiPath = "contracts/compiled/cryptoshares/CryptoSharesFactory.abi";
                $scope.factoryContractAddress = {
                    3: "0x6be0318b570B2Ece035a5F2b2aa75a83A4ad96cA"
                };
                $scope.factoryContractDeployedOnBlock = {
                    3: 6135624
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

                $log.debug("'main' function started");

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

                        // $scope.xEuroContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                        //     $scope.xEuroContractABI,
                        //     $scope.xEuroContractAddress[$rootScope.currentNetwork.network_id]
                        // );

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

                        $scope.$apply();

                        setUpContractsFunctions();

                    })
                    .catch(function (error) {
                        $log.error("error:");
                        $log.error(error);
                    })

            } // end of main()

            function setUpContractsFunctions() {

                $scope.factoryContractData = {};
                $scope.cryptoSharesContractData = {};

                $scope.getXEurContractAddressIsWorking = false;
                $scope.getXEurContractAddress = function () {
                    $scope.getXEurContractAddressIsWorking = true;
                    $scope.factoryContract.methods.xEurContractAddress().call()
                        .then((xEurContractAddress) => {
                            $scope.factoryContractData.xEurContractAddress = xEurContractAddress;
                            $log.debug(
                                "$scope.factoryContractData.xEurContractAddress:",
                                $scope.factoryContractData.xEurContractAddress
                            );
                            $scope.xEuroContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                                $scope.xEuroContractABI,
                                xEurContractAddress
                            );
                        })
                        .catch((error) => {
                            $log.error("$scope.getXEurContractAddress :");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.getXEurContractAddressIsWorking = false;
                            $scope.$apply();
                        })
                };

                $scope.getCryptoSharesContractsCounterIsWorking = false;
                $scope.getCryptoSharesContractsCounter = function () {
                    $scope.getCryptoSharesContractsCounterIsWorking = true;
                    $scope.factoryContract.methods.cryptoSharesContractsCounter().call()
                        .then((cryptoSharesContractsCounter) => {
                            $scope.factoryContractData.cryptoSharesContractsCounter = cryptoSharesContractsCounter;
                            $log.debug(
                                "$scope.factoryContractData.cryptoSharesContractsCounter:",
                                $scope.factoryContractData.cryptoSharesContractsCounter
                            );
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
                            $log.debug(
                                "$scope.price:", $scope.priceWei, "wei,",
                                $scope.priceETH, "ETH"
                            );
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

                /* ----- refresh factory data ---- */
                $scope.refreshFactoryData = function () {
                    $scope.getPrice();
                    $scope.getCryptoSharesContractsCounter();
                    $scope.getXEurContractAddress();
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

                // see:
                // https://stackoverflow.com/a/36099084/1697878

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

                    $log.log(txOptions);

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

                            $log.debug("receipt.events:");
                            $log.debug(receipt.events);
                            $log.debug("receipt.events[0]:");
                            $log.debug(receipt.events[0]);
                            $log.debug(receipt.events[0].address);

                            $log.debug(
                                $rootScope.currentNetwork.etherscanLinkPrefix
                                + "address/"
                                + receipt.events[0].address
                            );

                            // $scope.instantiateCryptoSharesContract(receipt.events[0].address);

                            return $scope.factoryContract.methods.cryptoSharesContractsCounter().call();
                        })
                        .then((result) => {

                            $scope.factoryContractData.cryptoSharesContractsCounter = parseInt(result);
                            $scope.cryptoSharesContractNumber = parseInt(result);
                            // $log.debug("$scope.cryptoSharesContractNumber:", $scope.cryptoSharesContractNumber);
                            $scope.switchToCryptosharesContractNumber($scope.cryptoSharesContractNumber);

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
                            $scope.createBillsOfExchangeIsWorking = false;
                            $scope.$apply();
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                };

                $scope.instantiateCryptoSharesContractIsWorking = false;
                $scope.instantiateCryptoSharesContract = function (cryptoSharesContractAddress) {

                    $log.debug("$scope.instantiateCryptoSharesContract started");

                    $scope.cryptoSharesContractData = {};

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

                            $scope.cryptosharesContract.methods.disputeResolutionAgreementSignatures(signatureNumber).call()
                                .then((result) => {
                                    if (result && result.signatoryRepresentedBy !== "0x0000000000000000000000000000000000000000") {
                                        $scope.cryptoSharesContractData.signatures[signatureNumber] = result;
                                        $log.debug($scope.cryptoSharesContractData.signatures[signatureNumber]);
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
                            $scope.$apply();
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
                                    $log.debug("$scope.cryptoSharesContractData.balanceOfDefaultAccount :",
                                        $scope.cryptoSharesContractData.balanceOfDefaultAccount
                                    );
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

                    $scope.refreshEthBalanceOfDefaultAccountIsWorking = false;
                    $scope.refreshEthBalanceOfDefaultAccount = function () {

                        if ($rootScope.web3.eth.defaultAccount) {

                            $scope.refreshEthBalanceOfDefaultAccountIsWorking = true;

                            $rootScope.web3.eth.getBalance($rootScope.web3.eth.defaultAccount)
                                .then(function (result) {
                                    $scope.weiBalanceOfDefaultAccount = parseInt(result);
                                    $scope.ethBalanceOfDefaultAccount = $rootScope.web3.utils.fromWei(result, "ether");

                                    $log.debug("$scope.ethBalanceOfDefaultAccount :",
                                        $scope.ethBalanceOfDefaultAccount
                                    );
                                })
                                .catch(function (error) {
                                    $log.debug("$scope.ethbalanceOfDefaultAccount ERROR:");
                                    $log.debug(error);
                                })
                                .finally(function () {
                                    $scope.refreshEthBalanceOfDefaultAccountIsWorking = false;
                                    $scope.$apply();
                                })
                        }
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

                                $log.debug("$scope.registerAsShareholderAndSignArbitrationAgreementTxReceipt :");
                                $log.debug($scope.registerAsShareholderAndSignArbitrationAgreementTxReceipt);
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                $scope.registerAsShareholderAndSignArbitrationAgreementIsWorking = false;
                                $scope.$apply();
                                // $rootScope.progressbar.start();
                                $timeout($rootScope.progressbar.complete(), 1000);
                            })
                    };

                    $scope.getShareholdersLedgerIsWorking = false;
                    $scope.getShareholdersLedger = function () {
                        $scope.getShareholdersLedgerIsWorking = true;

                        $scope.cryptoSharesContractData.shareholdersLedger = [];
                        $scope.cryptoSharesContractData.shareholdersLedgerAll = [];

                        $scope.cryptosharesContract.methods.shareholdersCounter().call()
                            .then((result) => {
                                result = parseInt(result);
                                $scope.cryptoSharesContractData.shareholdersCounter = result;

                                for (let i = 1; i < result; i++) {
                                    $scope.cryptosharesContract.methods.shareholdersLedgerByIdNumber(i).call()
                                        .then((shareholder) => {
                                            let balanceOf = parseInt(shareholder.balanceOf);
                                            $scope.cryptoSharesContractData.shareholdersLedgerAll.push(shareholder);
                                            if (parseInt(shareholder.balanceOf) > 0) {
                                                $scope.cryptoSharesContractData.shareholdersLedger.push(shareholder);
                                            }
                                            if ($scope.cryptoSharesContractData.shareholdersLedgerAll.length === $scope.cryptoSharesContractData.shareholdersCounter) {
                                                $scope.getShareholdersLedgerIsWorking = false;
                                                $scope.$apply();
                                            }
                                        })
                                }
                            })
                            .catch(() => {
                                $log.debug("$scope.getShareholdersLedger  ERROR:");
                                $log.debug(error);
                            });
                    }; // end of $scope.getShareholdersLedger

                    $scope.loadCryptoSharesContractData = function () {
                        $scope.getName();
                        $scope.getSymbol();
                        $scope.getTotalSupply();
                        $scope.getContractNumberInTheLedger();
                        $scope.getShareholdersCounter();
                        $scope.getDisputeResolutionAgreementSignaturesCounter();
                        $scope.getDisputeResolutionAgreement();
                        $scope.refreshTokenBalanceOfDefaultAccount();
                        $scope.refreshEthBalanceOfDefaultAccount();
                    };
                    $scope.loadCryptoSharesContractData();

                    $scope.instantiateCryptoSharesContractIsWorking = false;
                    $scope.switchToCryptosharesContractNumberIsWorking = false;
                    $scope.$apply();

                }; // end of $scope.instantiateCryptoSharesContract()

                $scope.cryptoSharesContractNumber = 0;
                $scope.switchToCryptosharesContractNumber = function (number) {
                    // const number = $scope.cryptoSharesContractNumber;
                    $log.debug("$scope.switchToCryptosharesContractNumber started, with number:", number);
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

            } // end of setUpContractsFunctions()

            $timeout($rootScope.progressbar.complete(), 1000); // <<< started in app.js

        }]); // end function homeCtl

})();