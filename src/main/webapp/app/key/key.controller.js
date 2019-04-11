(function () { // < (!) use this pattern in all controllers

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
        'GAuth',
        'GApi',
        '$scope',
        '$rootScope',
        // '$http',
        'GData',
        '$state',
        '$log',
        '$sce',
        '$timeout',
        '$stateParams',
        '$cookies',
        // 'Blob',
        '$location',
        '$anchorScroll',
        function showkeyCtrl(
            GAuth,
            GApi,
            $scope,
            $rootScope,
            // $http,
            GData,
            $state,
            $log,
            $sce,
            $timeout,
            $stateParams,
            $cookies,
            // Blob,
            $location,
            $anchorScroll) {


            $log.debug(controller_name, "started"); //
            // $timeout($rootScope.progressbar.complete(), 1000);

            (function setAlerts() {
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
            })();

            //
            $log.info("$stateParams.fingerprint : " + $stateParams.fingerprint);
            $scope.fingerprint = $stateParams.fingerprint;

            if ($rootScope.stringIsNullUndefinedOrEmpty($stateParams.fingerprint)) {
                $state.go('search'); // go to search page
            }

            /*
                        var gapiCheck = function () {

                            if (window.gapi && window.gapi.client) {
                                $log.debug("[key.controller.js] window.gapi.client:");
                                $log.debug(window.gapi.client);
                            } else {
                                $log.error("[key.controller.js] window.gapi.client is not loaded");
                                // setTimeout(gapiCheck, 1000); // check again in a second
                                setTimeout(gapiCheck, 1000); // check again in a second
                            }
                        };
                        gapiCheck();
            */

            GAuth.checkAuth().then(function (user) {

                console.log("GAuth.checkAuth() result:");
                console.log(user);
                //
                // if (!user) {
                //     $scope.setAlertWarning("User is not logged in. Click 'Login' or reload/refresh the page. Information is available only for registered users");
                // }

                $rootScope.googleUser = user;
                // $scope.alert = null;
                return GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData');
                // $rootScope.getUserData(); // async
            }).then(function (resp) {

                // (!) we always have response
                $rootScope.currentUser = resp;
                $log.info("[key.controller.js] $rootScope.currentUser: ");
                $log.info($rootScope.currentUser);

                if (!$rootScope.currentUser.loggedIn) { // user not logged in
                    $scope.setAlertDanger("User not logged in");
                    $log.debug("[key.controller.js] user not logged in");

                } else if (!$rootScope.currentUser.registeredCryptonomicaUser) { // user not registered
                    $scope.setAlertDanger("User is not registered. Please go to 'Registration' link on left menu");
                    $log.debug('[key.controller.js] user is not registered');
                } else {

                    $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink);
                    setUpFuctions();
                    $scope.showKey(); // TODO: check <<
                }

                // $rootScope.$apply(); // < not allowed here

                $rootScope.getMyBookmarks(); // < in background
                $timeout($rootScope.progressbar.complete(), 1000);

            }).catch(function (error) {
                $log.error("[key.controller.js] > error:");
                $log.error(error);
                $scope.setAlertDanger(error);
                $timeout($rootScope.progressbar.complete(), 1000);
            }).finally(function () {
                $timeout($rootScope.progressbar.complete(), 1000);
            });

            function setUpFuctions() {

                //
                $scope.dateOptions = {
                    changeYear: true,
                    changeMonth: true,
                    yearRange: '1900:-0'
                };

                //
                $scope.verifyOnline = function () {
                    $state.go('onlineVerification', {'fingerprint': $stateParams.fingerprint});
                };

                /* TODO: >>> */
                $scope.showEthereumVerificationData = function () {
                    $state.go('ethereumVerification', {'fingerprint': $stateParams.fingerprint});
                };

                $scope.goToEthereumAccountVerification = function () {
                    $state.go('ethereumVerification', {'fingerprint': $stateParams.fingerprint});
                };


                // =============== Bookmarks :
                $scope.addKeyToMyBookmarks = function () {
                    $rootScope.progressbar.start();
                    GApi.executeAuth('cryptonomicaUserAPI', 'addKeyToMyBookmarks', {"fingerprint": $stateParams.fingerprint}) // async
                        .then(function (resp) {
                                $rootScope.myBookmarks = resp;
                                $log.info("$rootScope.myBookmarks:");
                                $log.info($rootScope.myBookmarks);
                                // $rootScope.$apply(); // not needed here
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<
                            }, function (error) {
                                $log.error(error);
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<<
                            }
                        );
                };

                $scope.removeKeyFromMyBookmarks = function () {
                    $rootScope.progressbar.start();
                    GApi.executeAuth('cryptonomicaUserAPI', 'removeKeyFromMyBookmarks', {"fingerprint": $stateParams.fingerprint}) // async
                        .then(function (resp) {
                                $rootScope.myBookmarks = resp;
                                $log.info("$rootScope.myBookmarks:");
                                $log.info($rootScope.myBookmarks);
                                // $rootScope.$apply(); // not needed here
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<
                            }, function (error) {
                                $log.error(error);
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<<
                            }
                        );
                };

                // main function:
                $scope.showKey = function () {
                    $rootScope.progressbar.start(); // <<<<<<<
                    $log.debug("request for key:", $stateParams.fingerprint);
                    // $scope.key = {empty: true};
                    GApi.executeAuth(
                        'pgpPublicKeyAPI',
                        'getPGPPublicKeyByFingerprint', //
                        {"fingerprint": $stateParams.fingerprint}
                    ).then(
                        function (resp) {

                            $log.info("[key.controller.js] showKey() - resp: ");
                            $log.info(resp);
                            $scope.key = resp;

                            // use $rootScope.googleUser can be different than $rootScope.currentUser
                            // TODO: why?
                            // if ($rootScope.googleUser) {
                            //     $log.debug('$rootScope.googleUser.id : ', $rootScope.googleUser.id);
                            // }
                            // if ($scope.key) {
                            //     $log.debug('$scope.key.cryptonomicaUserId : ', $scope.key.cryptonomicaUserId);
                            // }
                            // $log.debug('$rootScope.currentUser : ', $rootScope.currentUser);

                            if ($scope.key.exp) {
                                if (new Date($scope.key.exp) < new Date()) {
                                    $scope.key.expired = true;
                                    $log.debug('key ' + $scope.key.keyID + 'expired:');
                                    $log.debug(new Date($scope.key.exp));
                                }
                            }

                            // $scope.$apply(); // not needed here
                            $timeout($rootScope.progressbar.complete(), 1000);
                        },
                        function (error) {

                            $log.error("[key.controller.js] showKey() - error: ");
                            $log.error(error);

                            // $scope.alert = error.message;
                            $scope.setAlertDanger(error.message);
                            // $scope.key.error = error;
                            // $scope.$apply(); // not needed here
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
                };

                // // not used:
                // $scope.download = function (text) {
                //     var data = new Blob([text], {type: 'text/plain;charset=utf-8'});
                //     var keyFileName = 'publicKey.asc';
                //     if ($scope.key && $scope.key.keyID) {
                //         keyFileName = $scope.key.keyID + keyFileName;
                //     }
                //     FileSaver.saveAs(data, keyFileName);
                // };

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

                /* (!!!) > to make verification in key field 'paid' should be 'true'*/
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
                };

                /* --- Change user name (admin) */
                $scope.newFirstName = null;
                $scope.newLastName = null;
                $scope.changeName = function () {
                    $log.debug('$scope.changeName started: ' + $scope.newFirstName + ' ' + $scope.newLastName);
                    if ($rootScope.stringIsNullUndefinedOrEmpty($scope.newFirstName) || $rootScope.stringIsNullUndefinedOrEmpty($scope.newFirstName)) {
                        $scope.setAlertDanger("name and first name can not be null or empty");
                        return;
                    }
                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                    GApi.executeAuth('onlineVerificationAPI',
                        'changeName', {
                            "fingerprint": $stateParams.fingerprint, // < different from online verification view
                            "userID": $scope.key.cryptonomicaUserId, // < different from online verification view
                            "firstName": $scope.newFirstName,
                            "lastName": $scope.newLastName
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
                }; // end of $scope.changeName() ;

            } // end of main()

        } // end of function showkeyCtrl()
    ]);
})();
