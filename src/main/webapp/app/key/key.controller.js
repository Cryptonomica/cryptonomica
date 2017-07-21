'use strict';

/**
 *  get and shows  key data by key fingerprint
 *
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
        '$location',
        '$anchorScroll',
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
                             Blob,
                             $location,
                             $anchorScroll
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
                if ($stateParams.fingerprint != null && $stateParams.fingerprint.length > 0) {

                    verifyKey();

                } else {
                    $log.error("$scope.verifyPGPPublicKeyFormSubmit ERROR: fingerprint not valid");
                    $scope.alertDanger = "fingerprint not valid";
                    $location.hash('alertDanger');
                    $anchorScroll();
                    return;
                }
            };

            var verifyKey = function () {
                $rootScope.progressbar.start(); // <<<<<<
                $scope.VerifyPGPPublicKeyForm.fingerprint = $stateParams.fingerprint;

                // $scope.VerifyPGPPublicKeyForm.verificationInfo - from the form
                // $scope.VerifyPGPPublicKeyForm.basedOnDocument  - from the form
                // $scope.VerifyPGPPublicKeyForm.nationality  - from the form
                if ($scope.VerifyPGPPublicKeyForm == null
                    || $scope.VerifyPGPPublicKeyForm.verificationInfo == null
                    || $scope.VerifyPGPPublicKeyForm.verificationInfo.length == 0
                    || $scope.VerifyPGPPublicKeyForm.basedOnDocument == null
                    || $scope.VerifyPGPPublicKeyForm.basedOnDocument.length == 0
                    || $scope.VerifyPGPPublicKeyForm.nationality == null
                    || $scope.VerifyPGPPublicKeyForm.nationality.length == 0
                ) {
                    $scope.alertDanger = "Please fill all required data in the form";
                    $location.hash('alertDanger');
                    $anchorScroll();
                    $timeout($rootScope.progressbar.complete(), 1000);
                    return;
                }
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
