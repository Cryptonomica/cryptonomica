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

            $scope.setNationalityFlag = function () {
                // for using in ng-class in semantic ui flag class
                // see: https://semantic-ui.com/elements/flag.html
                if ($scope.onlineVerification
                    && $scope.onlineVerification.nationality
                ) {
                    $scope.nationalityFlag = [$scope.onlineVerification.nationality.toLowerCase(), 'flag']
                }
            };

            $scope.setNewNationalityFlag = function () {
                // for using in ng-class in semantic ui flag class
                // see: https://semantic-ui.com/elements/flag.html
                // if ($scope.newNationalityFlag) {
                $scope.newNationalityFlag = [$scope.newNationality.toLowerCase(), 'flag']
                // }
            };

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

                        if ($rootScope.PRODUCTION) { // TODO: move urls to app constants
                            $scope.videoUrl = "https://cryptonomica-server.appspot.com/gcs?verificationVideoId=" + $scope.onlineVerification.verificationVideoId;
                        } else {
                            $scope.videoUrl = "https://sandbox-cryptonomica.appspot.com/gcs?verificationVideoId=" + $scope.onlineVerification.verificationVideoId;
                        }

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

                        /* set flag for nationality */
                        $scope.setNationalityFlag();

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
                $scope.getPGPPublicKeyByFingerprint(); // >>> this is also 'next step'
            }

            /* ---- for Cryptonomica compliance officer : */

            $scope.images = {};
            $scope.rotateImageRight = function (imageId) {
                // $log.debug("rotateImageRight clicked for image id:", imageId);
                if (!$rootScope.stringIsNullUndefinedOrEmpty(imageId)) {

                    const img = document.getElementById(imageId);
                    const imgModal = document.getElementById(imageId + "modalImage");
                    // $log.debug(img);
                    // $log.debug("img.width:", img.width, "img.height:", img.height);

                    if (img) {

                        if (!$scope.images[imageId]) {
                            $scope.images[imageId] = {};
                            $scope.images[imageId].rotationsCounter = 0;
                        }

                        if ($scope.images[imageId].rotationsCounter === 0) {
                            img.style.transform = "rotate(90deg)";
                            imgModal.style.transform = "rotate(90deg)";
                            $scope.images[imageId].rotationsCounter++;
                        } else if ($scope.images[imageId].rotationsCounter === 1) {
                            img.style.transform = "rotate(180deg)";
                            imgModal.style.transform = "rotate(180deg)";
                            $scope.images[imageId].rotationsCounter++;
                        } else if ($scope.images[imageId].rotationsCounter === 2) {
                            img.style.transform = "rotate(270deg)";
                            imgModal.style.transform = "rotate(270deg)";
                            $scope.images[imageId].rotationsCounter++;
                        } else if ($scope.images[imageId].rotationsCounter === 3) {
                            img.style.transform = "rotate(360deg)";
                            imgModal.style.transform = "rotate(360deg)";
                            $scope.images[imageId].rotationsCounter = 0;
                        }
                    }
                }
            }; // end of $scope.rotateImageRight

            $scope.showModal = function (modalId) {
                // see: https://semantic-ui.com/modules/modal.html#/definition
                $('#' + modalId).modal('show');
            };

            $scope.hideModal = function (modalId) {
                // see: https://semantic-ui.com/modules/modal.html#/definition
                $('#' + modalId).modal('hide');
            };

            /* --- remove video and send message to user : */
            $scope.messageToUser = null;
            $scope.removeVideoWithMessage = function () {
                $log.debug('$scope.removeVideoWithMessage started');
                $rootScope.progressbar.start(); // <<<<<<<<<<<

                GApi.executeAuth('onlineVerificationAPI',
                    'removeVideoWithMessage', {
                        "fingerprint": $stateParams.fingerprint,
                        "messageToUser": $scope.messageToUser,
                    })
                    .then(function (response) { // BooleanWrapperObject.java
                        $log.debug(response);
                        $scope.approveResponse = response.message;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.debug(error);
                        $scope.approveResponseError = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.removeVideoWithMessage() ;

            /* --- remove documents send message to user : */
            $scope.removeDocumentsMessageToUser = null;
            $scope.removeDocumentsWithMessage = function () {
                $log.debug('$scope.removeDocumentsWithMessage started');
                $rootScope.progressbar.start(); // <<<<<<<<<<<removeVideoWithMes

                GApi.executeAuth('onlineVerificationAPI',
                    'removeDocumentsWithMessage', {
                        "fingerprint": $stateParams.fingerprint,
                        "messageToUser": $scope.removeDocumentsMessageToUser,
                    })
                    .then(function (response) { // BooleanWrapperObject.java
                        $log.debug(response);
                        $scope.approveResponse = response.message;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.debug(error);
                        $scope.approveResponseError = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.removeDocumentsMessageToUser() ;

            /* --- add name on card : */
            $scope.nameOnCard = null;
            $scope.addNameOnCard = function () {
                $log.debug('$scope.removeVideoWithMessage started,  $scope.nameOnCard: ' + $scope.nameOnCard);
                if ($rootScope.stringIsNullUndefinedOrEmpty($scope.nameOnCard)) {
                    $scope.setAlertDanger("please provide name on card");
                    return
                }
                $rootScope.progressbar.start(); // <<<<<<<<<<<

                GApi.executeAuth('onlineVerificationAPI',
                    'addNameOnCard', {
                        "fingerprint": $stateParams.fingerprint,
                        "nameOnCard": $scope.nameOnCard,
                    })
                    .then(function (response) { // BooleanWrapperObject.java
                        $log.debug(response);
                        $scope.setAlertSuccess(response.message);
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.debug(error);
                        $scope.setAlertDanger(error);
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.addNameOnCard() ;

            /* --- Change user name (admin) */
            $scope.newFirstName = null;
            $scope.newLastName = null;
            $scope.changeName = function () {
                $log.debug('$scope.changeName started: ' + $scope.newFirstName + ' ' + $scope.newLastName + " for " + $scope.onlineVerification.cryptonomicaUserId);
                if ($rootScope.stringIsNullUndefinedOrEmpty($scope.newFirstName) || $rootScope.stringIsNullUndefinedOrEmpty($scope.newFirstName)) {
                    $scope.approveResponseError = "name and first name can not be null or empty";
                    return;
                }
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                GApi.executeAuth('onlineVerificationAPI',
                    'changeName', {
                        "fingerprint": $stateParams.fingerprint,
                        "userID": $scope.onlineVerification.cryptonomicaUserId,
                        "firstName": $scope.newFirstName,
                        "lastName": $scope.newLastName
                    })
                    .then(function (response) { // BooleanWrapperObject.java
                        $log.debug(response);
                        $scope.approveResponse = response.message; // <<< TODO: change mame
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.debug(error);
                        $scope.approveResponseError = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.changeName() ;

            /* --- Change user nationality (admin) */
            $scope.newNationality = null;
            $scope.changeNationality = function () {

                $log.debug(
                    '$scope.changeNationality started: '
                    + $scope.newNationality + ' ' + $scope.newLastName
                    + " for " + $scope.onlineVerification.cryptonomicaUserId
                );

                if ($rootScope.stringIsNullUndefinedOrEmpty($scope.newNationality)) {
                    $scope.approveResponseError = "New nationality can not be null or empty";
                    return;
                }

                $rootScope.progressbar.start(); // <<<<<<<<<<<

                GApi.executeAuth('onlineVerificationAPI',
                    'changeNationality', {
                        "fingerprint": $stateParams.fingerprint,
                        "newNationality": $scope.newNationality,
                    })
                    .then(function (response) { // BooleanWrapperObject.java
                        $log.debug(response);
                        $scope.approveResponse = response.message; // <<< TODO: change mame
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.debug(error);
                        $scope.approveResponseError = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.changeName() ;

            /* --- Change user Birthdate (admin) */
            $scope.newDay = null;
            $scope.newMonth = null;
            $scope.newYear = null;
            $scope.changeBirthdate = function () {
                $log.debug('$scope.changeBirthdate started: ' + $scope.newYear + '-' + $scope.newMonth + "-" + $scope.newDay);
                if (!$scope.newDay || !$scope.newMonth || !$scope.newYear
                    || $scope.newDay <= 0 || $scope.newDay > 31
                    || $scope.newMonth <= 0 || $scope.newMonth > 12
                    || $scope.newYear < 1900) {
                    $scope.approveResponseError = "Entered birthdate " + $scope.newYear + '-' + $scope.newMonth + "-" + $scope.newDay + " is invalid";
                    return;
                }
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                GApi.executeAuth('onlineVerificationAPI',
                    'changeBirthdate', {
                        "userID": $scope.onlineVerification.cryptonomicaUserId,
                        // "fingerprint": $stateParams.fingerprint,
                        "day": $scope.newDay,
                        "month": $scope.newMonth,
                        "year": $scope.newYear
                    })
                    .then(function (response) { // BooleanWrapperObject.java
                        $log.debug(response);
                        $scope.approveResponse = response.message; // <<< TODO: change name
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.debug(error);
                        $scope.approveResponseError = error;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.changeBirthdate ;

            $scope.onlineVerificationApproved = null;
            $scope.approve = function () {
                $log.debug('$scope.approve started');
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.approveSuccess = null;
                $scope.approveError = null;
                $scope.approveResponse = null;
                /// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                GApi.executeAuth('onlineVerificationAPI',
                    'approve', {
                        "fingerprint": $stateParams.fingerprint,
                        "verificationNotes": $scope.verificationNotes,
                        "onlineVerificationApproved": $scope.onlineVerificationApproved
                    })
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

            $scope.confirmDeleteOnlineVerificationEntity = null;
            $scope.reasonForDeletingOnlineVerificationEntity = null;
            $scope.deleteOnlineVerificationEntity = function () {
                $log.debug('$scope.deleteOnlineVerificationEntity started');
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.approveSuccess = null;
                $scope.approveError = null;
                $scope.approveResponse = null;
                /// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                GApi.executeAuth('onlineVerificationAPI',
                    'deleteOnlineVerificationEntity', {
                        "fingerprint": $stateParams.fingerprint,
                        "reason": $scope.reasonForDeletingOnlineVerificationEntity
                    })
                    .then(
                        function (result) { // BooleanWrapperObject.java
                            $log.debug(result);
                            $scope.setAlertSuccess(result.message);
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            $log.debug(error);
                            $scope.setAlertDanger(error);
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end of  $scope.deleteOnlineVerificationEntity();

            $timeout($rootScope.progressbar.complete(), 1000);
        } // end of onlineVerCtrl
    ]);
})();
