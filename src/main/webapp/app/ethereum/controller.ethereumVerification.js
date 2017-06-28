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
        $scope.notAllowed = true;
        // $scope.isKeyOwner = false; // <<<

        $log.debug('checkpoint # 01 ');

        var nullUndefinedEmptySting = function (str) {
            return (str == null || str == undefined || str == "" || str.length == 0);
        };

        $log.debug('checkpoint # 02 ');
        
        // TODO: rewrite this
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
                }, function (getPGPPublicKeyByFingerprintError) {
                    $scope.getPGPPublicKeyByFingerprintError = getPGPPublicKeyByFingerprintError;
                    $log.debug("$scope.getPGPPublicKeyByFingerprintError : ");
                    $log.error($scope.getPGPPublicKeyByFingerprintError);
                    $scope.$apply();
                }
            )
        };

        // RUN main: <<<<<<<<<< TODO: rewrite this
        $log.debug('checkpoint # 03 ');
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

