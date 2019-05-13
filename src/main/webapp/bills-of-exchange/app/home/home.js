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
        '$state',
        '$stateParams',
        function homeCtrl($scope,
                          $rootScope,
                          $http,
                          $timeout, // for ngProgressFactory
                          $log,
                          $state,
                          $stateParams
        ) {

            // $log.debug("homeCtrl started");

            // TODO: for test only:
            window.myScope = $scope;

            // activate tabs:
            $('.menu .item').tab();

            (function setUpContractData() {

                // TODO: after deployment compile smart contract and change data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                $scope.deployedOnNetworkId = 3;
                // TODO: check paths on web
                $scope.contractAbiPath = "contracts/compiled/bills-of-exchange-factory/BillsOfExchangeFactory.abi";
                $scope.contractAddress = "0x94443372325aB9EB6Dd033f8fEA5821c69a4Fd05";
                $scope.contractDeployedOnBlock = "5586322";

                $scope.verificationContractAbiPath = "contracts/compiled/CryptonomicaVerificationMockUp/CryptonomicaVerificationMockUp.abi";
                $scope.verificationContractAddress = "0x7f05e9807f509281a8db8f8b5230b89a96650087";

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

                $log.debug("$stateParams :");
                $log.debug($stateParams);
                $log.debug("$stateParams.billsOfExchangeAddress :");
                $log.debug($stateParams.billsOfExchangeAddress);

                if ($stateParams.billsOfExchangeAddress) {
                    $scope.changeTabToWork();
                }

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

                $scope.billsOfExchange.timeOfPaymentDate = new Date();
                $scope.billsOfExchange.timeOfPaymentUnixTime = $rootScope.unixTimeFromDate($scope.billsOfExchange.timeOfPaymentDate);
                $scope.billsOfExchange.placeWhereTheBillIsIssued = "London, U.K.";
                $scope.billsOfExchange.placeWherePaymentIsToBeMade = $scope.billsOfExchange.placeWhereTheBillIsIssued;

                $scope.clearForm = function () {
                    $scope.billsOfExchange = {};
                };

                $scope.atSightCheckbox = false;
                $scope.draweeIsTheSameAsDrawerCheckbox = false;
                $scope.placesAreTheSameCheckbox = false;

                $scope.createBillsOfExchangeIsWorking = false;
                $scope.createBillsOfExchange = function () {
                    $rootScope.progressbar.start();
                    // $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.createBillsOfExchangeIsWorking = true;

                    if ($scope.atSightCheckbox) {
                        $scope.billsOfExchange.timeOfPaymentUnixTime = 0;
                    }
                    if ($scope.draweeIsTheSameAsDrawerCheckbox) {
                        $scope.billsOfExchange.drawee = $scope.billsOfExchange.drawerName;
                    }
                    if ($scope.placesAreTheSameCheckbox) {
                        $scope.billsOfExchange.placeWherePaymentIsToBeMade = $scope.billsOfExchange.placeWhereTheBillIsIssued;
                    }
                    if ($scope.billsOfExchange.timeOfPaymentDate && $scope.billsOfExchange.timeOfPaymentDate instanceof Date) {
                        $scope.billsOfExchange.timeOfPaymentUnixTime = $rootScope.unixTimeFromDate($scope.billsOfExchange.timeOfPaymentDate)
                    }

                    /*
                    $log.debug(
                        "$scope.billsOfExchange.totalSupply :", $scope.billsOfExchange.totalSupply, "\n",
                        "$scope.billsOfExchange.currency :", $scope.billsOfExchange.currency, "\n",
                        "$scope.billsOfExchange.sumToBePaidForEveryToken :", $scope.billsOfExchange.sumToBePaidForEveryToken, "\n",
                        "$scope.billsOfExchange.drawerName :", $scope.billsOfExchange.drawerName, "\n",
                        "$scope.billsOfExchange.linkToSignersAuthorityToRepresentTheDrawer, :", $scope.billsOfExchange.linkToSignersAuthorityToRepresentTheDrawer, "\n",
                        "$scope.billsOfExchange.drawee :", $scope.billsOfExchange.drawee, "\n",
                        "$scope.billsOfExchange.draweeSignerAddress :", $scope.billsOfExchange.draweeSignerAddress, "\n",
                        "$scope.billsOfExchange.timeOfPaymentUnixTime :", $scope.billsOfExchange.timeOfPaymentUnixTime, "\n",
                        "$scope.billsOfExchange.placeWhereTheBillIsIssued :", $scope.billsOfExchange.placeWhereTheBillIsIssued, "\n",
                        "$scope.billsOfExchange.placeWherePaymentIsToBeMade :", $scope.billsOfExchange.placeWherePaymentIsToBeMade, "\n",
                    );*/

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
                        $scope.billsOfExchange.timeOfPaymentUnixTime,
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

                            $scope.billsOfExchangeContract = new $rootScope.web3.eth.Contract( // <<<<<<<<<<<<<<<<<< contract
                                value.data, // ABI
                                $scope.newBillsOfExchangeContracCreationReceipt.events[0].address
                            );

                            $scope.getBillsOfExchangeContractData();

                            // TODO: debug
                            $log.debug("$scope.billsOfExchangeContract ", $scope.billsOfExchangeContract._address);
                            $log.debug($scope.billsOfExchangeContract);

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

                $scope.billsOfExchangeContractData = {};
                $scope.getBillsOfExchangeContractData = function () {
                    // $rootScope.progressbar.start();
                    $timeout($rootScope.progressbar.complete(), 1000);
                    $scope.billsOfExchangeContract.methods.name().call()
                        .then((name) => {
                            $scope.billsOfExchangeContractData.name = name;
                            return $scope.billsOfExchangeContract.methods.symbol().call();
                        })
                        .then((symbol) => {
                            $scope.billsOfExchangeContractData.symbol = symbol;
                            return $scope.billsOfExchangeContract.methods.totalSupply().call();
                        })
                        .then((totalSupply) => {
                            $scope.billsOfExchangeContractData.totalSupply = totalSupply;
                            return $scope.billsOfExchangeContract.methods.balanceOf($rootScope.web3.eth.defaultAccount).call();
                        })
                        .then((balanceOf) => {
                            $scope.billsOfExchangeContractData.balanceOf = balanceOf;
                            return $scope.billsOfExchangeContract.methods.billsOfExchangeContractNumber().call();
                        })
                        .then((billsOfExchangeContractNumber) => {
                            $scope.billsOfExchangeContractData.billsOfExchangeContractNumber = billsOfExchangeContractNumber;
                            return $scope.billsOfExchangeContract.methods.drawerName().call();
                        })
                        .then((drawerName) => {
                            $scope.billsOfExchangeContractData.drawerName = drawerName;
                            // return $scope.billsOfExchangeContract.methods.drawerName().call(); TODO: continue
                        })
                        .catch((error) => {
                            $log.error("$scope.getBillsOfExchangeContractData Error:");
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.$apply();
                            $log.debug(" $scope.billsOfExchangeContractData :");
                            $log.debug($scope.billsOfExchangeContractData);
                            // $rootScope.progressbar.start();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        })
                }

            }

            $timeout($rootScope.progressbar.complete(), 1000); // <<< started in app.js
        }]); // end function homeCtl
})();