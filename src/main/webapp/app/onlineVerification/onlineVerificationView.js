(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.onlineVerificationView";

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
        function onlineVerViewCtrl($scope,    // <- name for debugging
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

            $log.info("cryptonomica.controller.onlineVerificationView ver. 01 started");

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
            $log.info("[onlineVerification.js] $stateParams.fingerprint : " + $stateParams.fingerprint);
            // // --- Alerts:
            // $scope.alertDanger = null;  // red
            // $scope.alertWarning = null; // yellow
            // $scope.alertInfo = null;    // blue
            // $scope.alertSuccess = null; // green

            /* --- Alerts */
            $scope.alertDanger = null;  // red
            $scope.alertWarning = null; // yellow
            $scope.alertInfo = null;    // blue
            $scope.alertSuccess = null; // green
            $scope.alertMessage = {}; // grey

            $scope.setAlertDanger = function (message) {
                $scope.alertDanger = message;
                $log.debug("$scope.alertDanger:", $scope.alertDanger);
                // $scope.$apply(); not here
                $scope.goTo("alertDanger");
            };

            $scope.setAlertWarning = function (message) {
                $scope.alertWarning = message;
                $log.debug("$scope.alertWarning:", $scope.alertWarning);
                // $scope.$apply();
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
            /* ---- */

            //
            $scope.error = {};
            $scope.getOnlineVerificationError = false;
            $scope.fingerprint = $stateParams.fingerprint;
            $scope.keyId = "0x" + $stateParams.fingerprint.substring(32, 40).toUpperCase();
            $scope.todayDate = new Date();
            $scope.videoUploadKey = null;
            $scope.verificationDocsUrls = [];
            $scope.pgpPublicKey = null;
            $scope.getPGPPublicKeyByFingerprintError = null;
            //
            $scope.notAllowed = true;
            // $scope.isKeyOwner = false; // <<<
            //
            $scope.priceForKeyVerification = null;
            $scope.getPriceForKeyVerificationError = null;
            //
            // for stripe payment:
            $scope.stripePaymentForm = {};
            $scope.stripePaymentForm.fingerprint = $stateParams.fingerprint;
            $scope.paymentResponse = null;
            $scope.paymentError = null;
            $scope.paymentVerificationCode = null;
            $scope.paymentVerificationResponse = null;
            $scope.paymentVerificationError = null;
            //
            $scope.acceptTermsAnswer = null;

            $log.debug('checkpoint # 01 ');

            //>>> Main Function:
            $scope.getOnlineVerification = function () {
                $log.debug('$scope.getOnlineVerification() started');
                GApi.executeAuth(
                    'onlineVerificationAPI',
                    'getOnlineVerificationByFingerprint',
                    {"fingerprint": $stateParams.fingerprint}
                ).then(
                    function (resp) {
                        // $log.debug("$scope.getOnlineVerification resp:");
                        // $log.debug(resp);
                        $scope.onlineVerification = resp; // OnlineVerificationView.java
                        $log.debug('$scope.onlineVerification:');
                        $log.debug($scope.onlineVerification);
                        $scope.videoUrl = "https://cryptonomica-server.appspot.com/gcs?verificationVideoId=" + $scope.onlineVerification.verificationVideoId;
                        $log.debug('$scope.videoUrl: ' + $scope.videoUrl);
                        $scope.notAllowed = false;
                        $scope.getOnlineVerificationError = false;

                        // ---- check if user is keyOwner
                        $log.debug('$scope.pgpPublicKey.cryptonomicaUserId: ' + $scope.pgpPublicKey.cryptonomicaUserId);
                        $log.debug('$rootScope.currentUser.userId: ' + $rootScope.currentUser.userId);
                        if ($scope.pgpPublicKey && $rootScope.currentUser) {
                            $log.debug('$scope.pgpPublicKey && $rootScope.currentUser : true');
                            if ($scope.pgpPublicKey.cryptonomicaUserId && $rootScope.currentUser.userId) {
                                // see: http://stackoverflow.com/a/3586788/1697878
                                $scope.isKeyOwner =
                                    $scope.pgpPublicKey.cryptonomicaUserId.valueOf() === $rootScope.currentUser.userId.valueOf();
                            }
                        }
                        // only key owner, cryptonomica officer, or notary can view key data
                        // TODO: allow user to grant access to verification data to other users
                        $log.debug('$scope.isKeyOwner: ' + $scope.isKeyOwner);
                        if (!$scope.isKeyOwner && !$rootScope.currentUser.cryptonomicaOfficer && !notary

                        ) {
                            $log.debug('You are not authorized to view verification data for this key');
                            $scope.alertDanger = 'You are not authorized to view verification data for this key';
                        }

                    }, function (error) {
                        $log.error("$scope.getOnlineVerification error:");
                        console.log(error);
                        if (error.message.indexOf("you are not allowed to get online verification data for key") !== -1) {
                            $scope.keyNotOwned = true;
                        }
                        $log.info("$scope.keyNotOwned : " + $scope.keyNotOwned);

                        $scope.error.message = "[server error] " + error.message;
                        $scope.setAlertDanger(error.message);

                        $scope.getOnlineVerificationError = true;
                        $timeout($rootScope.progressbar.complete(), 1000);
                        // $scope.$apply(); // not needed here
                    }
                )
            };

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
                        $scope.getOnlineVerification(); // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    }, function (getPGPPublicKeyByFingerprintError) {
                        $scope.getPGPPublicKeyByFingerprintError = getPGPPublicKeyByFingerprintError;
                        $log.debug("$scope.getPGPPublicKeyByFingerprintError : ");
                        $log.error($scope.getPGPPublicKeyByFingerprintError);
                        $scope.$apply();
                    }
                )
            };

            // RUN main: <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            if (!$rootScope.currentUser) {
                $log.debug(' if (!$rootScope.currentUser) started:');
                $rootScope.progressbar.start();
                GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData')
                    .then(
                        function (resp) {
                            $rootScope.currentUser = resp;
                            $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink); //;
                            $log.info("[onlineVerificationView.js] $rootScope.currentUser: ");
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

            /* ---- Approve OnlineVerification (for Cryptonomica complience officer) : */
            $scope.onlineVerificationApproved = null;
            $scope.approve = function () {
                $log.debug('$$scope.approve strarted');
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.approveSuccess = null;
                $scope.approveError = null;
                $scope.approveResponse = null;
                /// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                GApi.executeAuth('onlineVerificationAPI',
                    'approve',
                    {
                        "fingerprint": $stateParams.fingerprint,
                        "verificationNotes": $scope.verificationNotes,
                        "onlineVerificationApproved": $scope.onlineVerificationApproved
                    }
                )
                    .then(
                        function (approveResponse) { // BooleanWrapperObject.java
                            $log.debug(approveResponse);
                            $scope.approveResponse = approveResponse.message;
                            $scope.getPGPPublicKeyByFingerprint();
                            // >>>>>>
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (approveResponseError) {
                            $log.debug(approveResponseError);
                            $scope.approveResponseError = approveResponseError;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end of $scope.approve();

        } // end of onlineVerCtrl
    ]);
})();
