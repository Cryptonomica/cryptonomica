'use strict';

/**
 *  get and showes  by key fingerprint
 *  TODO: if user is a notary or cryptonomica officer - offer to add verification
 */

var controller_name = "cryptonomica.controller.key";

var controller = angular.module(controller_name, []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
controller.config(function ($logProvider) {
        // $logProvider.debugEnabled(false);
        $logProvider.debugEnabled(true);
    }
);

controller.controller(controller_name, [
        '$scope',
        '$rootScope',
        '$http',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$log',
        '$timeout',
        '$stateParams',
        '$cookies',
        'FileSaver',
        'Blob',
        // 'UserService',
        function showkeyCtrl($scope,
                             $rootScope,
                             $http,
                             GApi,
                             GAuth,
                             GData,
                             $state,
                             $log,
                             $timeout,
                             $stateParams,
                             $cookies,
                             FileSaver,
                             Blob
                             // UserService
        ) {
            //
            $log.info("$stateParams.fingerprint : " + $stateParams.fingerprint);
            if ($stateParams.fingerprint == ""
                || $stateParams.fingerprint == null
                || $stateParams.fingerprint == undefined) {
                $state.go('search'); // go to search page
            }
            //
            $scope.verifyOnline = function () {
                $state.go('onlineVerification', {'fingerprint': $stateParams.fingerprint});
            };
            //
            GAuth.checkAuth().then(
                function () {
                    $scope.alert = null;
                    $rootScope.getUserData(); // async?
                },
                function () {
                    //$rootScope.getUserData();
                    $log.error("[showkey.controller.js] GAuth.checkAuth() - unsuccessful");
                    $scope.alert = "User not logged in";
                }
            );

            //
            $scope.dateOptions = {
                changeYear: true,
                changeMonth: true,
                yearRange: '1900:-0'
            };
            //
            var showKey = function () {
                $rootScope.progressbar.start(); // <<<<<<<
                $scope.key = {empty: true};
                GApi.executeAuth(
                    'pgpPublicKeyAPI',
                    'getPGPPublicKeyByFingerprint', //
                    {"fingerprint": $stateParams.fingerprint}
                ).then(
                    function (resp) {
                        $log.info("[key.controller.js] showKey() - resp: ");
                        $log.info(resp);
                        $scope.key = resp;
                        if ($scope.key.exp) {
                            if (new Date($scope.key.exp) < new Date()) {
                                $scope.key.expired = true;
                                $log.debug('key ' + $scope.key.keyID + 'expired:');
                                $log.debug(new Date($scope.key.exp));
                            }
                        }

                        $scope.alert = null;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    },
                    function (error) {
                        $log.error("[key.controller.js] showKey() - error: ");
                        $log.error(error);
                        $scope.alert = error.message;
                        $scope.key.error = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    }
                );
            };
            showKey();

            $log.info("[key.controller.js] $scope.key: ");
            $log.info($scope.key);
            //
            $scope.download = function (text) {
                var data = new Blob([text], {type: 'text/plain;charset=utf-8'});
                FileSaver.saveAs(data, 'key.asc');
            };
            // key verification:
            $scope.KeyVerificationFormShow = false;
            $scope.KeyVerificationFormShowToggle = function () {
                $scope.KeyVerificationFormShow = !$scope.KeyVerificationFormShow;
            };
            // net.cryptonomica.forms.VerifyPGPPublicKeyForm:
            // private String webSafeString; // ..............1
            // private String verificationInfo; // ...........2
            // private String basedOnDocument; // ............3
            /*------------------------*/
            // net.cryptonomica.returns.VerifyPGPPublicKeyReturn
            // String MessageToUser;
            // VerificationGeneralView verificationGeneralView;
            // PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
            // UserProfileGeneralView userProfileGeneralView;
            /*-------------------------*/
            $scope.VerifyPGPPublicKeyForm = {};
            $scope.verifyPGPPublicKeyFormSubmit = function () {
                if ($scope.KeyVerificationFORM.$valid && $scope.key.cryptonomicaUserId) {
                    verifyKey();
                } else if ($scope.KeyVerificationFORM.$invalid) {
                    $log.error("$scope.KeyVerificationFORM.$valid :"
                        + $scope.KeyVerificationFORM.$valid);
                } else {
                    $log.error("$scope.key.cryptonomicaUserId: " + $scope.key.cryptonomicaUserId);
                }
            };
            var verifyKey = function () {
                $rootScope.progressbar.start(); // <<<<<<
                //
                // $scope.VerifyPGPPublicKeyForm.webSafeString = $stateParams.websafestring;  // old
                $scope.VerifyPGPPublicKeyForm.fingerprint = $stateParams.fingerprint;  //
                // $scope.VerifyPGPPublicKeyForm.verificationInfo - from the form
                // $scope.VerifyPGPPublicKeyForm.basedOnDocument  - from the form
                $log.info("$scope.VerifyPGPPublicKeyForm : ");
                $log.info($scope.VerifyPGPPublicKeyForm);
                //
                GApi.executeAuth(
                    'notaryAPI',
                    'verifyPGPPublicKey',
                    $scope.VerifyPGPPublicKeyForm // net.cryptonomica.forms.VerifyPGPPublicKeyForm
                ).then(
                    function (resp) { // net.cryptonomica.returns.VerifyPGPPublicKeyReturn 
                        // String MessageToUser;
                        // VerificationGeneralView verificationGeneralView;
                        // PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
                        // UserProfileGeneralView userProfileGeneralView;                        
                        $log.info("[key.controller.js] $scope.verifyKey() - resp: ");
                        $log.info(resp);
                        $scope.VerifyPGPPublicKeyForm = {}; // clear form
                        $scope.VerifyPGPPublicKeyReturn = resp;
                        $scope.key = resp.pgpPublicKeyGeneralView;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    },
                    function (error) {
                        $log.error("[key.controller.js] $scope.verifyKey() - error: ");
                        $log.error(error);
                        $scope.verifyKeyError = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    }
                );

            }

        }
    ]
);
