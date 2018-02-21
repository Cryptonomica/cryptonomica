'use strict';

var controller_name = "cryptonomica.controller.ethereumVerification";
// add to: 
// index.html
// controller.js
// router.js

var controller = angular.module(controller_name, []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
controller.config(function ($logProvider) {
        // $logProvider.debugEnabled(false);
        $logProvider.debugEnabled(true);
    }
);

controller.controller(controller_name, [
    '$scope',
    '$sce',
    '$rootScope',
    '$http',
    'GApi',
    'GAuth',
    'GData',
    '$state',
    'uiUploader',
    '$log',
    '$cookies',
    '$timeout',
    '$window',
    '$stateParams',
    function ethereumVerificationCtrl($scope,    // <- name for debugging
                                      $sce,
                                      $rootScope,
                                      $http,
                                      GApi,
                                      GAuth,
                                      GData,
                                      $state,
                                      uiUploader,
                                      $log,
                                      $cookies,
                                      $timeout,
                                      $window,
                                      $stateParams) {

        $log.info("cryptonomica.controller.ethereumVerification ver. 01 started");

        if (!$rootScope.currentUser) {
            GAuth.checkAuth().then(
                function () {
                    $rootScope.getUserData(); // async?
                },
                function () {
                    $log.debug('User not logged in');
                    $state.go('landing');
                }
            );
        }

        //
        $log.info("[ethereumVerification] $stateParams.fingerprint : " + $stateParams.fingerprint);
        // --- Alerts:
        $scope.alertDanger = null;  // red
        $scope.alertWarning = null; // yellow
        $scope.alertInfo = null;    // blue
        $scope.alertSuccess = null; // green
        //
        $scope.error = {};
        $scope.fingerprint = $stateParams.fingerprint;
        $scope.keyId = "0x" + $stateParams.fingerprint.substring(32, 40).toUpperCase();
        $scope.todayDate = new Date();
        $scope.pgpPublicKey = null;
        $scope.getPGPPublicKeyByFingerprintError = null;
        //

        const nullUndefinedEmptySting = function (str) {
            return (str == null || str === undefined || str === "" || str.length === 0);
        };

        $scope.contractDeployed = {
            '0': {
                'network': 'Ethereum network not connected',
                'address': 'no contract deployed on this network',
                // 'onBlock': 0,
                'etherscanLinkPrefix': 'https://etherscan.io/'
            },
            '1': {
                'network': 'MainNet',
                'address': '',
                // 'onBlock': 0,
                'etherscanLinkPrefix': 'https://etherscan.io/'
            },
            '3': {
                'network': 'Ropsten',
                'address': '',
                // 'onBlock': 0,
                'etherscanLinkPrefix': 'https://ropsten.etherscan.io/'
            },
            '4': {
                'network': 'Rinkeby',
                'address': '0x001672c61A3cE56d0599A81f2F28103A891a1A39', // <<<<<
                // 'onBlock': 0,
                'etherscanLinkPrefix': 'https://rinkeby.etherscan.io/'
            }
        };

        $scope.transactions = [];
        $scope.currentNetwork = {};

        $scope.getPGPPublicKeyByFingerprint = function () {
            $log.debug('$scope.getPGPPublicKeyByFingerprint started');
            GApi.executeAuth(
                'pgpPublicKeyAPI',
                'getPGPPublicKeyByFingerprint',
                {"fingerprint": $stateParams.fingerprint}
            ).then(
                function (pgpPublicKeyGeneralView) {
                    $scope.pgpPublicKey = pgpPublicKeyGeneralView;
                    $log.debug("$scope.pgpPublicKey: ");
                    $log.debug($scope.pgpPublicKey);

                    /* --------- START truffle */

                    /* web3 instantiation */
                    // see 'NOTE FOR DAPP DEVELOPERS' on https://github.com/ethereum/mist/releases/tag/v0.9.0
                    // To instantiate your (self-included) web3.js lib you can use:
                    if (typeof window.web3 !== 'undefined') {

                        $log.debug('[app.js] web3 object presented by provider:');
                        $log.debug(window.web3.currentProvider);

                        $scope.web3 = new Web3(window.web3.currentProvider);

                        $log.debug('and will be used in $scope.web3:');
                        $log.debug($scope.web3);

                    } else {

                        $log.debug('web3 object is not provided by client app');
                        $log.debug('and will be instantiated from web3.js lib');

                        $scope.web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));

                        $log.debug('$scope.web3: ');
                        $log.debug($scope.web3);
                    }

                    // to access web3 instance in browser console:
                    // angular.element('body').scope().$root.web3 (?)

                    if ($scope.web3.isConnected()) {

                        $scope.currentNetwork.connected = true;
                        if (!$scope.web3.eth.defaultAccount) {
                            if ($scope.web3.eth.accounts.length > 0) {
                                $scope.web3.eth.defaultAccount = $scope.web3.eth.accounts[0];
                            } else {
                                $scope.alertDanger = "no accounts detected";
                            }
                        }
                        // $scope.$apply(); // << not needed here

                        $scope.web3.version.getNetwork(function (error, result) {
                            if (error) {
                                $scope.alertDanger = error;
                                $scope.$apply();
                                $log.error(error);
                            } else {
                                $scope.currentNetwork.network_id = result; // "3" for Ropsten, "1" for MainNet etc.
                                $scope.etherscanLinkPrefix = $scope.contractDeployed[$scope.currentNetwork.network_id].etherscanLinkPrefix;
                                $scope.$apply(); // needed here
                                $log.debug(
                                    'web3.version.network: '
                                    + $scope.currentNetwork.network_id
                                );

                                /* create contract: */
                                $.getJSON('app/ethereum/CryptonomicaVerification.json', function (data) { // async

                                    var CryptonomicaVerification = TruffleContract(data);

                                    CryptonomicaVerification.network_id = $scope.currentNetwork.network_id;
                                    CryptonomicaVerification.setProvider($scope.web3.currentProvider);

                                    CryptonomicaVerification.at($scope.contractDeployed[$scope.currentNetwork.network_id].address).then(function (instance) {

                                            $scope.contract = instance;
                                            $scope.$apply();
                                            $log.debug("$scope.contract.address: ", $scope.contract.address);
                                            $log.debug("contract:");
                                            $log.debug($scope.contract);

                                            $scope.requestStringToSign = function () {
                                                $scope.requestStringToSignWorking = true;
                                                var txParameters = {};
                                                txParameters.from = $scope.web3.eth.defaultAccount;
                                                $scope.contract.requestStringToSignWithKey($scope.fingerprint, txParameters)
                                                    .then(function (tx) {
                                                            $log.info(tx);
                                                            $scope.lastTx = tx;
                                                            $scope.transactions.push(tx);
                                                            $scope.contract.stringToSign(txParameters.from).then(function (stringToSign) {
                                                                    $scope.stringToSign = stringToSign; //
                                                                    $scope.requestStringToSignWorking = false;
                                                                    $scope.$apply(); // <<< needed
                                                                    $log.info(' $scope.stringToSign: ');
                                                                    $log.info($scope.stringToSign);
                                                                }
                                                            ).catch(function (error) {
                                                                    $scope.alertDanger = error.toString();
                                                                    $log.error(error);
                                                                    $scope.requestStringToSignWorking = false;
                                                                    $scope.$apply();
                                                                }
                                                            );
                                                        }
                                                    ).catch(function (error) {
                                                        $scope.alertDanger = error.toString();
                                                        $log.error(error);
                                                        $scope.requestStringToSignWorking = false;
                                                        $scope.$apply();
                                                    }
                                                );
                                            }
                                            // ------

                                            /// TODO: >>>>

                                        }
                                    ); // end of function (instance)

                                }); // end of $.getJSON('contracts/DEEX.json'

                            }
                        });

                    } else {

                        $log.debug('web3 is not connected to Ethereum node');

                    }

                    /* ---------- END truffle */

                }, function (getPGPPublicKeyByFingerprintError) {
                    $scope.getPGPPublicKeyByFingerprintError = getPGPPublicKeyByFingerprintError;
                    $log.debug("$scope.getPGPPublicKeyByFingerprintError : ");
                    $log.error($scope.getPGPPublicKeyByFingerprintError);
                    $scope.$apply();
                }
            )
        };

        // RUN main: <<<<<<<<<< TODO: rewrite this
        if (!$rootScope.currentUser) {
            $log.debug(' if (!$rootScope.currentUser) started:');
            $rootScope.progressbar.start();
            GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData')
                .then(
                    function (resp) {
                        $rootScope.currentUser = resp;
                        $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink); //;
                        $log.info("$rootScope.currentUser: ");
                        $log.info($rootScope.currentUser);
                        $scope.getPGPPublicKeyByFingerprint();
                        // $timeout($rootScope.progressbar.complete(), 1000); //
                    }, function (error) {
                        // console.log("$rootScope.getUserData: error: ");
                        $log.error(error);
                        $timeout($rootScope.progressbar.complete(), 1000); //
                    }
                );
        } else {
            $log.debug('checkpoint # 04 ');
            $scope.getPGPPublicKeyByFingerprint(); // >>> this is also 'next step'
        }

    } // end of Ctrl
]);

