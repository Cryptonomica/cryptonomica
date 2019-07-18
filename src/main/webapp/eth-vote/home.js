'use strict';

const controller = angular.module("app.home", []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
controller.config(function ($logProvider) {
    // TODO: change in production
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

        $log.debug("app.home started");

        // TODO: for test only:
        window.myScope = $scope;

        (function semanticUiSetup() {

            // activate tabs
            // see: https://semantic-ui.com/modules/tab.html#/examples
            $('.menu .item').tab();

            // https://github.com/Semantic-Org/Semantic-UI/issues/3544
            $scope.changeTab = function (newTab) {
                $.tab('change tab', newTab);
                $('.menu .item').removeClass("active");
                $('.menu .item[data-tab="' + newTab + '"]').addClass("active");
            };

        })();

        (function uiSetup() {
            $scope.showDescription = false;
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

        (function contractData() {

            $scope.contractAbiPath = "contracts/VotingForERC20.abi";
            $scope.erc20ContractAbiPath = "contracts/ERC20TokensContract.abi";

            $scope.deployedOnNetworksArray = [
                // 3, // Ropsten
                42 // Kovan
            ];
            $scope.contractsAddresses = {
                // 3: "", // Ropsten
                42: "0x67acd0dd43dC9D074884cb2D0e7631dF9C00e5B7" // Kovan
            };
            $scope.contractsDeployedOnBlocks = {
                // 3: "", // Ropsten
                42: "12277863" // Kovan
            };
            $scope.mockUpERC20ContractsAddresses = {
                3: "0x37d2a83c587b64358dB4FA9afAD0b6CfC48A7371", // Ropsten
                42: "0x8e681aB751641745bD00fe21F31C4253645f5C50" // Kovan
            };

        })();

        (function ethereum() {
            if (window.ethereum) {

                // we need this to reference 'web3' and 'ethereum' in html
                // like:
                // {{ethereum.selectedAddress}} or {{web3.eth.defaultAccount}} or ng-show="web3"  or ng-show="ethereum.isMetaMask" etc.
                $rootScope.web3 = new Web3(ethereum);
                $rootScope.ethereum = ethereum;

                if (typeof window.ethereum.selectedAddress === 'undefined') { // privacy mode on
                    (async function () {
                        try {
                            // Request account access if needed
                            // This method returns a Promise that resolves to an array of user accounts once access is approved for a given dapp.
                            await window.ethereum.enable();
                            // Accounts now exposed
                            // $rootScope.web3.eth.accounts[0] > does not work
                            // but you have already: web3.eth.defaultAccount
                            $rootScope.noConnectionToNodeError = false;
                            $log.debug("MetaMask connection added manually");
                            main();

                        } catch (error) {
                            // if user denied account access:
                            // {"message":"User denied account authorization","code":4001}
                            $log.error(error);
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
                    $log.debug("MetaMask privacy mode off or already connected");
                    main();
                }

            } else if (window.web3) {
                $log.debug("old web3 browser detected");
                $rootScope.noConnectionToNodeError = false;
                // $rootScope.web3 = new Web3(web3.currentProvider, null, web3Options);
                $rootScope.web3 = new Web3(web3.currentProvider);
                // Accounts always exposed
                if (!$rootScope.web3.eth.defaultAccount && $rootScope.web3.eth.accounts && $rootScope.web3.eth.accounts[0]) {
                    $rootScope.web3.eth.defaultAccount = $rootScope.web3.eth.accounts[0];
                    $log.debug("$rootScope.web3.eth.defaultAccount : ");
                    $log.debug($rootScope.web3.eth.defaultAccount);
                }
                main();
            } else {  // Non-dapp browsers:
                $log.error('Non-Ethereum browser detected. You should consider trying MetaMask!');
                $rootScope.noConnectionToNodeError = true;
            }
        })();

        function main() {
            $log.debug("main() started");
            $http.get($scope.contractAbiPath)
                .then((value) => {
                    $scope.contractABI = value.data; // ABI

                    $log.debug("contract ABI:");
                    $log.debug($scope.contractABI);

                    // const truffleContract = TruffleContract($scope.contractABI);
                    // truffleContract.network_id = ethereum.networkVersion;
                })

        }

    }]);