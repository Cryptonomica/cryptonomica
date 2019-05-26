(function () {

    'use strict';

// console.log("home.js");

    var controller = angular.module("app.home", []);

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

            // TODO: for test only:
            window.myScope = $scope;

            if ($location.host() === "localhost") {
                $scope.thisUrl = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/bills-of-exchange/#!/";
            } else {
                $scope.thisUrl = $location.protocol() + "://" + $location.host() + "/bills-of-exchange/#!/"
            }

            $log.debug($scope.thisUrl);
            // $log.debug($location.absUrl());

            // activate tabs:
            $('.menu .item').tab();

            (function setUpContractData() {

                // TODO: after deployment compile smart contract and change data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                $scope.deployedOnNetworkId = 3; // ROPSTEN
                // TODO: check paths on web
                $scope.contractAbiPath = "contracts/compiled/bills-of-exchange-factory/BillsOfExchangeFactory.abi";
                $scope.contractAddress = "0x74eB4DBD3124D41B6775701FD1821571EAd5cf9A"; // ROPSTEN
                $scope.contractDeployedOnBlock = "5674792"; // ROPSTEN

                $scope.verificationContractAbiPath = "contracts/compiled/CryptonomicaVerificationMockUp/CryptonomicaVerificationMockUp.abi";
                $scope.verificationContractAddress = "0xE48BC3dB5b512d4A3e3Cd388bE541Be7202285B5";

                $scope.billsOfExchangeAbiPath = "contracts/compiled/bills-of-exchange-factory/BillsOfExchange.abi"
            })();

            /* --- Alerts */

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

            function getNetworkInfo() {

                $rootScope.noConnectionToNodeError = true;
                $rootScope.currentNetwork = {};

                // https://web3js.readthedocs.io/en/1.0/web3-eth-net.html#getid
                // Promise returns Number: The network ID
                $rootScope.web3.eth.net.getId()
                    .then(function (result) {
                        $rootScope.noConnectionToNodeError = false;
                        $rootScope.currentNetwork.network_id = result; //
                        if (result === 1 || result === 3 || result === 4 || result === 42) {
                            $rootScope.currentNetwork.networkName = $rootScope.networks[result].networkName;
                            $rootScope.currentNetwork.etherscanLinkPrefix = $rootScope.networks[result].etherscanLinkPrefix;
                            $rootScope.currentNetwork.etherscanApiLink = $rootScope.networks[result].etherscanApiLink;
                        } else {
                            $rootScope.currentNetwork.networkName = "unknown network";
                            $rootScope.currentNetwork.etherscanLinkPrefix = "blockchain_explorer";
                            $rootScope.currentNetwork.etherscanApiLink = "blockchainExplorerAPI";
                            $scope.setAlertWarning("Unknown Ethereum network. Network ID: " + result);
                        }

                        $log.debug("$rootScope.currentNetwork.networkName:", $rootScope.currentNetwork.networkName);
                        $rootScope.$apply(); // needed here

                    })
                    .catch(function (error) {
                        $log.error("$rootScope.web3.eth.net.getId() error:");
                        $log.error($rootScope.web3.eth.net.getId());
                    });

                $rootScope.currentBlockNumber = null;
                $rootScope.refreshCurrentBlockNumber = function () {
                    $rootScope.web3.eth.getBlockNumber(
                        function (error, result) {
                            if (!error) {
                                $rootScope.currentBlockNumber = result;
                                $log.debug("$rootScope.currentBlockNumber:", $rootScope.currentBlockNumber);
                                $rootScope.$apply();
                                // $log.debug("$rootScope.currentBlockNumber: ", $rootScope.currentBlockNumber);
                            } else {
                                $log.error(error);
                            }
                        }
                    )
                };
                $rootScope.refreshCurrentBlockNumber();
            }

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
            }
            // Non-dapp browsers:
            else {

                $log.error('Non-Ethereum browser detected. You should consider trying MetaMask!');
                // $scope.setAlertDanger("This web application requires Ethereum connection. Please install MetaMask.io browser plugin");
                // > or use <no-connection-to-node-error></no-connection-to-node-error> directive
            }

            function main() {

                getNetworkInfo();

                $rootScope.web3.eth.net.getId()
                    .then((result) => {

                        $rootScope.currentNetwork.network_id = result; //
                        if (result === 1 || result === 3 || result === 4 || result === 42) {
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
                        return $http.get($scope.contractAbiPath);
                    })
                    .then((value) => {
                        $scope.contractABI = value.data; // ABI
                        return $http.get($scope.verificationContractAbiPath);
                    })
                    .then((value) => {
                        $scope.verificationContractABI = value.data; // ABI

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

                        return $rootScope.web3.eth.getAccounts();
                    })
                    .then(function (accounts) {

                        if (!$rootScope.web3.eth.defaultAccount && accounts && accounts.length && accounts.length > 0) {
                            // https://web3js.readthedocs.io/en/1.0/web3-eth.html#id20
                            $rootScope.web3.eth.defaultAccount = accounts[0];
                            // $log.debug("$rootScope.web3.eth.defaultAccount:", $rootScope.web3.eth.defaultAccount);
                            $scope.$apply();
                        }
                        if (!$rootScope.web3.eth.defaultAccount) {
                            $scope.setAlertDanger("Ethereum account not recognized. If you use MetaMask please login to MetaMask and connect to this site");
                        }

                        setUpContractFunctions();

                    })
                    .catch(function (error) {
                        $log.error("error:");
                        $log.error(error);
                    })

            } // end of main()

            function setUpContractFunctions() {

                $log.debug("$scope.contract:");
                $log.debug($scope.contract);

                $scope.contractData = {};
                // $scope.contractData.billsOfExchangeContractsCounter = 0;

                $scope.getBillsOfExchangeContractsCounterIsWorking = false;
                $scope.getBillsOfExchangeContractsCounter = function () {
                    $scope.getBillsOfExchangeContractsCounterIsWorking = true;
                    $scope.contract.methods.billsOfExchangeContractsCounter().call()
                        .then((billsOfExchangeContractsCounter) => {
                            $scope.contractData.billsOfExchangeContractsCounter = billsOfExchangeContractsCounter;
                            $log.debug("$scope.contractData.billsOfExchangeContractsCounter:", $scope.contractData.billsOfExchangeContractsCounter);
                        })
                        .catch((error) => {
                            $log.error("$scope.getContractData Error:");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.getBillsOfExchangeContractsCounterIsWorking = false;
                            $scope.$apply();
                        })
                };

                $scope.getContractData = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.contract.methods.description().call()
                        .then((description) => {
                            $scope.contractData.description = description;
                            return $scope.contract.methods.order().call();
                        })
                        .then((order) => {
                            $scope.contractData.order = order;
                            return $scope.contract.methods.disputeResolutionAgreement().call();
                        })
                        .then((disputeResolutionAgreement) => {
                            $scope.contractData.disputeResolutionAgreement = disputeResolutionAgreement;
                            return $scope.contract.methods.price().call();
                        })
                        .then((price) => {
                            $scope.price = price;
                            $log.debug("$scope.price: ", $scope.price);
                            $scope.priceETH = $rootScope.web3.utils.fromWei(price);
                            $log.debug("$scope.priceETH : ", $scope.priceETH);
                            return $scope.contract.methods.billsOfExchangeContractsCounter().call();
                        })
                        .then((billsOfExchangeContractsCounter) => {
                            $scope.contractData.billsOfExchangeContractsCounter = billsOfExchangeContractsCounter;
                            $log.debug("$scope.contractData.billsOfExchangeContractsCounter:", $scope.contractData.billsOfExchangeContractsCounter);
                        })
                        .catch((error) => {
                            $log.error("$scope.getContractData Error:");
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.$apply();
                            $log.debug("$scope.contractData:");
                            $log.debug($scope.contractData);
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                };
                $scope.getContractData();

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

                $scope.dateOptions = {changeYear: true, changeMonth: true, yearRange: '1900:-0'};

                $scope.billsOfExchange = {};
                $scope.billsOfExchange.name = "Test Company Ltd. bills of exchange, Series TST01";
                $scope.billsOfExchange.symbol = "TST01";

                $scope.billsOfExchange.totalSupply = 1000;
                $scope.billsOfExchange.currency = "EUR";
                $scope.billsOfExchange.sumToBePaidForEveryToken = 1;

                $scope.billsOfExchange.drawerName = "Test Company Ltd, 3 Main Street, London, XY1Z  1XZ, U.K.; company # 12345678"; // person who issues the bill (drawer)
                $scope.billsOfExchange.drawerSignerAddress = $rootScope.web3.eth.defaultAccount; // who has to sign in the name of the drawer
                $scope.billsOfExchange.linkToSignersAuthorityToRepresentTheDrawer = "https://beta.companieshouse.gov.uk/company/12345678/officers";

                $scope.billsOfExchange.drawee = $scope.billsOfExchange.drawerName; // the name of the person who is to pay
                $scope.billsOfExchange.draweeSignerAddress = $rootScope.web3.eth.defaultAccount; // who has to sign in the name of the drawee
                $scope.billsOfExchange.linkToSignersAuthorityToRepresentTheDrawee = "https://beta.companieshouse.gov.uk/company/12345678/officers";

                $scope.billsOfExchange.timeOfPayment = "at sight but not before 01 Sep 2021";

                $scope.billsOfExchange.placeWhereTheBillIsIssued = "London, U.K.";
                $scope.billsOfExchange.placeWherePaymentIsToBeMade = $scope.billsOfExchange.placeWhereTheBillIsIssued;

                $scope.clearForm = function () {
                    $scope.billsOfExchange = {};
                };

                $scope.draweeIsTheSameAsDrawerCheckbox = false;
                $scope.placesAreTheSameCheckbox = false;

                $scope.createBillsOfExchangeIsWorking = false;
                $scope.createBillsOfExchange = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.createBillsOfExchangeIsWorking = true;

                    if ($scope.draweeIsTheSameAsDrawerCheckbox) {
                        $scope.billsOfExchange.drawee = $scope.billsOfExchange.drawerName;
                    }
                    if ($scope.placesAreTheSameCheckbox) {
                        $scope.billsOfExchange.placeWherePaymentIsToBeMade = $scope.billsOfExchange.placeWhereTheBillIsIssued;
                    }

                    $log.debug($scope.billsOfExchange);

                    $scope.newBillsOfExchangeContracCreationReceipt = null;
                    $scope.contract.methods.createBillsOfExchange(
                        $scope.billsOfExchange.name,
                        $scope.billsOfExchange.symbol,
                        $scope.billsOfExchange.totalSupply,
                        $scope.billsOfExchange.currency,
                        $scope.billsOfExchange.sumToBePaidForEveryToken,
                        $scope.billsOfExchange.drawerName,
                        $scope.billsOfExchange.linkToSignersAuthorityToRepresentTheDrawer,
                        $scope.billsOfExchange.drawee,
                        $scope.billsOfExchange.draweeSignerAddress,
                        $scope.billsOfExchange.timeOfPayment,
                        $scope.billsOfExchange.placeWhereTheBillIsIssued,
                        $scope.billsOfExchange.placeWherePaymentIsToBeMade
                    )
                        .send({from: $rootScope.web3.eth.defaultAccount, value: $scope.price})
                        .then((receipt) => {

                            $scope.newBillsOfExchangeContracCreationReceipt = receipt;
                            $scope.setAlertSuccess(
                                "New bills of exchange created at Ethereum address:  "
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

                            return $http.get($scope.billsOfExchangeAbiPath);

                        })
                        .then((value) => {

                            $scope.instantiateBillsOfExchangeContract(
                                $scope.newBillsOfExchangeContracCreationReceipt.events[0].address
                            );

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

                $scope.createAndAcceptBillsOfExchange = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.createBillsOfExchangeIsWorking = true;

                    if ($scope.draweeIsTheSameAsDrawerCheckbox) {
                        $scope.billsOfExchange.drawee = $scope.billsOfExchange.drawerName;
                    }
                    if ($scope.placesAreTheSameCheckbox) {
                        $scope.billsOfExchange.placeWherePaymentIsToBeMade = $scope.billsOfExchange.placeWhereTheBillIsIssued;
                    }

                    // $log.debug("$scope.atSightCheckbox :", $scope.atSightCheckbox);
                    $log.debug($scope.billsOfExchange);

                    $scope.newBillsOfExchangeContracCreationReceipt = null;
                    $scope.contract.methods.createAndAcceptBillsOfExchange(
                        $scope.billsOfExchange.name,
                        $scope.billsOfExchange.symbol,
                        $scope.billsOfExchange.totalSupply,
                        $scope.billsOfExchange.currency,
                        $scope.billsOfExchange.sumToBePaidForEveryToken,
                        $scope.billsOfExchange.drawerName,
                        $scope.billsOfExchange.linkToSignersAuthorityToRepresentTheDrawer,
                        // $scope.billsOfExchange.drawee, // > the same as drawer
                        // $scope.billsOfExchange.draweeSignerAddress, // > the same as msg.sender
                        $scope.billsOfExchange.timeOfPayment,
                        $scope.billsOfExchange.placeWhereTheBillIsIssued,
                        $scope.billsOfExchange.placeWherePaymentIsToBeMade
                    )
                        .send({from: $rootScope.web3.eth.defaultAccount, value: $scope.price})
                        .then((receipt) => {

                            $scope.newBillsOfExchangeContracCreationReceipt = receipt;
                            $scope.setAlertSuccess(
                                "New bills of exchange created at Ethereum address:  "
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

                            return $http.get($scope.billsOfExchangeAbiPath);

                        })
                        .then((value) => {

                            $scope.instantiateBillsOfExchangeContract(
                                $scope.newBillsOfExchangeContracCreationReceipt.events[0].address
                            );

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

                $scope.getSignatoryData = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.verificationContract.methods.verification($rootScope.web3.eth.defaultAccount).call()
                        .then((result) => {
                            $scope.signatoryVerificationData = result;
                            $log.debug("$scope.signatoryVerificationData:");
                            $log.debug($scope.signatoryVerificationData);
                            if (result.firstName && result.lastName && result.birthDate && result.nationality) {
                                let birthDate = $rootScope.dateFromUnixTime(result.birthDate);

                                $scope.signatoryVerificationDataStr =
                                    result.firstName + " " + result.lastName + ", "
                                    + "birthdate: " + birthDate.toLocaleDateString()
                                    // + birthDate.getFullYear() + "-" + birthDate.getMonth() + "-" + birthDate.getDay()
                                    + ", nationality: " + result.nationality;
                            }
                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.$apply();
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                };
                $scope.getSignatoryData();

                $scope.instantiateBillsOfExchangeContract = function (contractAddress) {
                    $rootScope.progressbar.start();
                    $scope.billsOfExchangeContract = null;
                    $scope.instantiateBillsOfExchangeContractIsWorking = true;
                    $http.get($scope.billsOfExchangeAbiPath)
                        .then((value) => {
                            $scope.billsOfExchangeContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                                value.data, // ABI
                                contractAddress
                            );
                            $scope.getBillsOfExchangeContractData();
                            $scope.getBillsOfExchangeContractsCounter();
                        })
                        .catch((error) => {
                            $log.error("$scope.instantiateBillsOfExchangeContract Error");
                            $log.error(error);
                            $scope.setAlertDanger(error);
                            $scope.createBillsOfExchangeIsWorking = false;
                            // $scope.$apply(); // < not needed here
                            $scope.instantiateBillsOfExchangeContractIsWorking = false;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })

                };

                $scope.billsOfExchangeContractData = {};
                $scope.getBillsOfExchangeContractDataIsWorking = false;
                $scope.getBillsOfExchangeContractData = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.getBillsOfExchangeContractDataIsWorking = true;
                    $scope.billsOfExchangeContract.methods.name().call()
                        .then((name) => {
                            $scope.billsOfExchangeContractData.name = name;
                            return $scope.billsOfExchangeContract.methods.symbol().call();
                        })
                        .then((symbol) => {
                            $scope.billsOfExchangeContractData.symbol = symbol;
                            return $scope.billsOfExchangeContract.methods.currency().call();
                        })
                        .then((currency) => {
                            $scope.billsOfExchangeContractData.currency = currency;
                            return $scope.billsOfExchangeContract.methods.totalSupply().call();
                        })
                        .then((totalSupply) => {
                            $scope.billsOfExchangeContractData.totalSupply = parseInt(totalSupply);
                            return $scope.billsOfExchangeContract.methods.balanceOf($rootScope.web3.eth.defaultAccount).call();
                        })
                        .then((balanceOf) => {
                            $scope.billsOfExchangeContractData.balanceOf = parseInt(balanceOf);
                            return $scope.billsOfExchangeContract.methods.billsOfExchangeContractNumber().call();
                        })
                        .then((billsOfExchangeContractNumber) => {
                            $scope.billsOfExchangeContractData.billsOfExchangeContractNumber = parseInt(billsOfExchangeContractNumber);
                            $scope.billsOfExchangeContractNumber = $scope.billsOfExchangeContractData.billsOfExchangeContractNumber;
                            return $scope.billsOfExchangeContract.methods.drawerName().call();
                        })
                        .then((drawerName) => {
                            $scope.billsOfExchangeContractData.drawerName = drawerName;

                            return $scope.billsOfExchangeContract.methods.drawerRepresentedBy().call();
                        })
                        .then((drawerRepresentedBy) => {
                            $scope.billsOfExchangeContractData.drawerRepresentedBy = drawerRepresentedBy;
                            return $scope.verificationContract.methods.verification(drawerRepresentedBy).call()
                        })
                        .then((result) => {
                            $scope.billsOfExchangeContractData.drawerSignerAddressData = result;
                            $scope.billsOfExchangeContractData.drawerSignerAddressDataStr =
                                result.firstName + " "
                                + result.lastName + ", date of birth "
                                + $rootScope.dateToDateStr(
                                $rootScope.dateFromUnixTime(parseInt(result.birthDate))
                                ) + ", "
                                + result.nationality;

                            return $scope.billsOfExchangeContract.methods.linkToSignersAuthorityToRepresentTheDrawer().call();
                        })
                        .then((linkToSignersAuthorityToRepresentTheDrawer) => {
                            $scope.billsOfExchangeContractData.linkToSignersAuthorityToRepresentTheDrawer = linkToSignersAuthorityToRepresentTheDrawer;
                            return $scope.billsOfExchangeContract.methods.drawee().call();
                        })
                        .then((drawee) => {
                            $scope.billsOfExchangeContractData.drawee = drawee;
                            return $scope.billsOfExchangeContract.methods.draweeSignerAddress().call();
                        })
                        .then((draweeSignerAddress) => {
                            $scope.billsOfExchangeContractData.draweeSignerAddress = draweeSignerAddress;
                            return $scope.verificationContract.methods.verification(draweeSignerAddress).call()
                        })
                        .then((result) => {
                            $scope.billsOfExchangeContractData.draweeSignerAddressVerificationData = result;
                            $scope.billsOfExchangeContractData.draweeSignerAddressVerificationDataStr =
                                result.firstName + " "
                                + result.lastName + ", date of birth "
                                + $rootScope.dateToDateStr(
                                $rootScope.dateFromUnixTime(parseInt(result.birthDate))
                                ) + ", "
                                + result.nationality;

                            return $scope.billsOfExchangeContract.methods.linkToSignersAuthorityToRepresentTheDrawee().call();
                        })
                        .then((linkToSignersAuthorityToRepresentTheDrawee) => {
                            $scope.billsOfExchangeContractData.linkToSignersAuthorityToRepresentTheDrawee = linkToSignersAuthorityToRepresentTheDrawee;
                            return $scope.billsOfExchangeContract.methods.timeOfPayment().call();
                        })
                        .then((timeOfPayment) => {
                            $scope.billsOfExchangeContractData.timeOfPayment = timeOfPayment;
                            return $scope.billsOfExchangeContract.methods.issuedOnUnixTime().call();
                        })
                        .then((issuedOnUnixTime) => {
                            $scope.billsOfExchangeContractData.issuedOnUnixTime = parseInt(issuedOnUnixTime);
                            $scope.billsOfExchangeContractData.issuedOnDate = $rootScope.dateFromUnixTime(issuedOnUnixTime);
                            return $scope.billsOfExchangeContract.methods.placeWhereTheBillIsIssued().call();
                        })
                        .then((placeWhereTheBillIsIssued) => {
                            $scope.billsOfExchangeContractData.placeWhereTheBillIsIssued = placeWhereTheBillIsIssued;
                            return $scope.billsOfExchangeContract.methods.placeWherePaymentIsToBeMade().call();
                        })
                        .then((placeWherePaymentIsToBeMade) => {
                            $scope.billsOfExchangeContractData.placeWherePaymentIsToBeMade = placeWherePaymentIsToBeMade;
                            return $scope.billsOfExchangeContract.methods.sumToBePaidForEveryToken().call();
                        })
                        .then((sumToBePaidForEveryToken) => {
                            $scope.billsOfExchangeContractData.sumToBePaidForEveryToken = parseInt(sumToBePaidForEveryToken);
                            return $scope.billsOfExchangeContract.methods.disputeResolutionAgreementSignaturesCounter().call();
                        })
                        .then((disputeResolutionAgreementSignaturesCounter) => {
                            $scope.billsOfExchangeContractData.disputeResolutionAgreementSignaturesCounter = parseInt(disputeResolutionAgreementSignaturesCounter);
                            $scope.billsOfExchangeContractData.disputeResolutionAgreementSignatures = [];

                            for (let i = 1; i <= disputeResolutionAgreementSignaturesCounter; i++) {
                                $scope.billsOfExchangeContract.methods.disputeResolutionAgreementSignatures(i).call().then(
                                    (result) => {
                                        // Solidity code:
                                        // struct Signature {
                                        //     address signatoryAddress;
                                        //     string signatoryName;
                                        // }

                                        // The push() method adds one or more elements to the end of an array and returns the new length of the array.
                                        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/push
                                        let signaturesArrayLength = $scope.billsOfExchangeContractData.disputeResolutionAgreementSignatures.push(result);
                                        let signatureNumber = signaturesArrayLength - 1;
                                        let signatory = result[0];
                                        $scope.verificationContract.methods.verification(signatory).call()
                                            .then((result) => {
                                                $scope.billsOfExchangeContractData.disputeResolutionAgreementSignatures[signatureNumber][2] = result;
                                                $scope.billsOfExchangeContractData.disputeResolutionAgreementSignatures[signatureNumber][3] =
                                                    result.firstName + " "
                                                    + result.lastName + ", date of birth "
                                                    + $rootScope.dateToDateStr(
                                                    $rootScope.dateFromUnixTime(parseInt(result.birthDate))
                                                    ) + ", "
                                                    + result.nationality;
                                            })
                                            .catch((error) => {
                                                $log.error("$scope.verificationContract.methods.verification(signatory).call() Error:");
                                                $log.error(error);
                                            })
                                    }
                                )
                            }
                            return $scope.billsOfExchangeContract.methods.acceptedOnUnixTime().call();
                        })
                        .then((acceptedOnUnixTime) => {
                            $scope.billsOfExchangeContractData.acceptedOnUnixTime = parseInt(acceptedOnUnixTime);
                            $scope.billsOfExchangeContractData.acceptedOnDate = $rootScope.dateFromUnixTime(acceptedOnUnixTime);
                            $scope.billsOfExchangeContractData.acceptedOnDateStr =
                                $rootScope.dateToDateStr($scope.billsOfExchangeContractData.acceptedOnDate);
                            return $scope.billsOfExchangeContract.methods.order().call();
                        })
                        .then((order) => {
                            $scope.billsOfExchangeContractData.order = order;
                            return $scope.billsOfExchangeContract.methods.disputeResolutionAgreement().call();
                        })
                        .then((disputeResolutionAgreement) => {
                            $scope.billsOfExchangeContractData.disputeResolutionAgreement = disputeResolutionAgreement;
                            return $scope.billsOfExchangeContract.methods.linkToSignersAuthorityToRepresentTheDrawee().call();
                        })
                        .then((linkToSignersAuthorityToRepresentTheDrawee) => {
                            $scope.billsOfExchangeContractData.linkToSignersAuthorityToRepresentTheDrawee = linkToSignersAuthorityToRepresentTheDrawee;
                        })
                        .catch((error) => {
                            $log.error("$scope.getBillsOfExchangeContractData Error:");
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            // $scope.$apply(); // < not here
                            $log.debug(" $scope.billsOfExchangeContractData :");
                            $log.debug($scope.billsOfExchangeContractData);

                            /* --- for transfer */
                            $scope.billsOfExchangeContractData.transferValue = 0;
                            $scope.billsOfExchangeContractData.transferIsWorking = false;
                            $scope.billsOfExchangeContractData.transferTo = $rootScope.web3.eth.defaultAccount;
                            /* -----------------*/

                            /* --- for burn */
                            $scope.billsOfExchangeContractData.burnValue = 0;
                            $scope.billsOfExchangeContractData.burnTokensIsWorking = false;
                            /* -----------------*/

                            $scope.createBillsOfExchangeIsWorking = false;
                            $scope.instantiateBillsOfExchangeContractIsWorking = false;
                            $scope.acceptIsWorking = false;
                            $scope.getBillsOfExchangeContractDataIsWorking = false;

                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                };

                $scope.refreshAccountBalanceIsWorking = false;
                $scope.refreshAccountBalance = function () {
                    if ($rootScope.web3.eth.defaultAccount) {
                        $scope.refreshAccountBalanceIsWorking = true;
                        $scope.billsOfExchangeContract.methods.balanceOf($rootScope.web3.eth.defaultAccount).call()
                            .then((balanceOf) => {
                                $scope.billsOfExchangeContractData.balanceOf = parseInt(balanceOf);
                                $scope.refreshAccountBalanceIsWorking = false;
                            })
                            .catch((error) => {
                                $log.error("$scope.refreshAccountBalance Error:");
                                $log.error(error);
                            })
                            .finally(() => {
                                $scope.refreshAccountBalanceIsWorking = false;
                                $scope.$apply();
                            })
                    }
                };

                $scope.transfer = function () {
                    $scope.billsOfExchangeContractData.transferIsWorking = true;

                    if ($scope.billsOfExchangeContractData && $scope.billsOfExchangeContractData.transferValue && $scope.billsOfExchangeContractData.transferValue > $scope.billsOfExchangeContractData.balanceOf) {
                        $scope.billsOfExchangeContractData.transferError = "Error: insufficient funds";
                        $scope.billsOfExchangeContractData.transferIsWorking = false;
                        return;
                    }

                    if (!$rootScope.web3.utils.isAddress($scope.billsOfExchangeContractData.transferTo)) {
                        $scope.billsOfExchangeContractData.transferError = "Error: invalid address";
                        $scope.billsOfExchangeContractData.transferIsWorking = false;
                        return;
                    }

                    $scope.billsOfExchangeContract.methods.transfer(
                        $scope.billsOfExchangeContractData.transferTo,
                        $scope.billsOfExchangeContractData.transferValue
                    ).send({"from": $rootScope.web3.eth.defaultAccount})
                        .then(function (result) {

                            $log.debug("$scope.billsOfExchangeContract.methods.transfer result:");
                            $log.debug(result);

                            $scope.billsOfExchangeContractData.transferTxHash = result.transactionHash;
                            $scope.refreshAccountBalance();
                        })
                        .catch(function (error) {
                            $log.debug("$scope.billsOfExchangeContract.methods.transfer ERROR:");
                            $log.debug(error);
                            $scope.billsOfExchangeContractData.transferError = error;
                        })
                        .finally(function () {
                            $scope.billsOfExchangeContractData.transferIsWorking = false;
                            $scope.$apply();
                        });
                }; //

                $scope.burnTokens = function () {
                    $scope.billsOfExchangeContractData.burnTokensIsWorking = true;

                    if ($scope.billsOfExchangeContractData
                        && $scope.billsOfExchangeContractData.burnValue
                        && $scope.billsOfExchangeContractData.burnValue > $scope.billsOfExchangeContractData.balanceOf
                    ) {
                        $scope.billsOfExchangeContractData.burnError = "Error: insufficient funds";
                        $scope.billsOfExchangeContractData.burnTokensIsWorking = false;
                        return;
                    }

                    $scope.billsOfExchangeContract.methods.burnTokens(
                        $scope.billsOfExchangeContractData.burnValue
                    ).send({"from": $rootScope.web3.eth.defaultAccount})
                        .then(function (result) {

                            $log.debug("$scope.billsOfExchangeContract.methods.burnTokens result:");
                            $log.debug(result);

                            $scope.billsOfExchangeContractData.burnTxHash = result.transactionHash;
                            $scope.refreshAccountBalance();
                        })
                        .catch(function (error) {
                            $log.debug("$scope.billsOfExchangeContract.methods.burnTokens ERROR:");
                            $log.debug(error);
                            $scope.billsOfExchangeContractData.burnError = error;
                        })
                        .finally(function () {
                            $scope.billsOfExchangeContractData.burnTokensIsWorking = false;
                            $scope.$apply();
                        });
                }; // end of $scope.burnTokens

                $scope.linkToSignersAuthorityToRepresentTheDrawee = "https://beta.companieshouse.gov.uk/company/12345678/officers";
                $scope.acceptIsWorking = false;
                $scope.accept = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.acceptIsWorking = true;
                    $scope.billsOfExchangeContract.methods.accept($scope.linkToSignersAuthorityToRepresentTheDrawee)
                        .send({from: $rootScope.web3.eth.defaultAccount})
                        .then((receipt) => {
                            $log.debug("accept tx receipt:");
                            $log.debug(receipt);
                            $scope.getBillsOfExchangeContractData();
                            $scope.acceptSuccess = true;
                        })
                        .catch((error) => {
                            $log.error("$scope.accept  Error:");
                            $log.error(error);
                            $scope.acceptIsWorking = false;
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                            $scope.setAlertDanger(error);

                        })
                };

                $scope.signatoryName = "Test Company Ltd, 3 Main Street, London, XY1Z  1XZ, U.K.; company # 12345678";
                $scope.signDisputeResolutionAgreementIsWorking = false;
                $scope.signDisputeResolutionAgreementSuccess = null;
                $scope.signDisputeResolutionAgreementError = null;
                $scope.signDisputeResolutionAgreement = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.signDisputeResolutionAgreementIsWorking = true;
                    $scope.billsOfExchangeContract.methods.signDisputeResolutionAgreement($scope.signatoryName)
                        .send({from: $rootScope.web3.eth.defaultAccount})
                        // .send()
                        .then((receipt) => {
                            $log.debug("signDisputeResolutionAgreement tx receipt:");
                            $log.debug(receipt);
                            $scope.signDisputeResolutionAgreementTxHash = receipt.transactionHash;
                            $scope.signDisputeResolutionAgreementSuccess = true;
                            $scope.signDisputeResolutionAgreementError = null;
                            $scope.$apply(); // < needed here
                            $scope.getBillsOfExchangeContractData();
                        })
                        .catch((error) => {
                            $log.error("$scope.signDisputeResolutionAgreement Error:");
                            $log.error(error);
                            $scope.signDisputeResolutionAgreementSuccess = null;
                            $scope.signDisputeResolutionAgreementError = error;
                        })
                        .finally(() => {
                            $scope.signDisputeResolutionAgreementIsWorking = false;
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                };

                $scope.billsOfExchangeContractNumber = 0;
                $scope.switchToBillsOfExchangeContractNumber = function (number) {
                    $rootScope.progressbar.start();
                    $scope.billsOfExchangeContractNumber = number;
                    $scope.contract.methods.billsOfExchangeContractsLedger(number).call()
                        .then((contractAddress) => {
                            $log.debug("contract address for # ", number);
                            $log.debug(contractAddress);
                            if (contractAddress === "0x0000000000000000000000000000000000000000") {

                                $scope.setAlertDanger("There is no smart contract with # " + number);
                                $scope.billsOfExchangeContractData = {};

                                // $rootScope.progressbar.start();
                                $timeout($rootScope.progressbar.complete(), 1000);
                                return;
                            }
                            $scope.instantiateBillsOfExchangeContract(contractAddress);
                            $scope.changeTabToWork();
                        })
                };

                /* >>> if number of contract provided in URL */
                if ($stateParams.billsOfExchangeNumber) {
                    $rootScope.progressbar.start();
                    $scope.billsOfExchangeContractNumber = parseInt($stateParams.billsOfExchangeNumber);
                    $scope.switchToBillsOfExchangeContractNumber($scope.billsOfExchangeContractNumber);
                    $scope.changeTabToWork();
                }

            }

            $timeout($rootScope.progressbar.complete(), 1000); // <<< started in app.js
        }]); // end function homeCtl
})();