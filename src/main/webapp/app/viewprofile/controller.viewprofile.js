(function () { // < (!) use this pattern in all controllers

    'use strict';

    /**
     *  shows profile of another user
     *  we can use this for view of user, arbitrator, notary, lawyer
     */

    var controller_name = "cryptonomica.controller.viewprofile";

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
            '$stateParams',
            '$cookies',
            'FileSaver',
            'Blob',
            '$log',
            '$timeout',
            function viewCtrl($scope,
                              $rootScope,
                              $http,
                              GApi,
                              GAuth,
                              GData,
                              $state,
                              $stateParams,
                              $cookies,
                              FileSaver,
                              Blob,
                              $log,
                              $timeout) {

                $log.debug(controller_name, "started"); //
                $timeout($rootScope.progressbar.complete(), 1000);

                if (!$rootScope.currentUser) {
                    GAuth.checkAuth().then(
                        function () {
                            $rootScope.getUserData(); //
                        },
                        function () {
                            $log.debug('User not logged in');
                            $state.go('landing');
                        }
                    );
                }

                /* -----  Alerts START  */
                $scope.alertDanger = null;  // red
                $scope.alertWarning = null; // yellow
                $scope.alertInfo = null;    // blue
                $scope.alertSuccess = null; // green
                $scope.alertMessage = {}; // grey

                $scope.setAlertDanger = function (message) {
                    $scope.alertDanger = message;
                    $log.debug("$scope.alertDanger:", $scope.alertDanger);
                    // $scope.$apply(); not here
                    $rootScope.goTo("alertDanger");
                };

                $scope.setAlertWarning = function (message) {
                    $scope.alertWarning = message;
                    $log.debug("$scope.alertWarning:", $scope.alertWarning);
                    // $scope.$apply();
                    $rootScope.goTo("alertWarning");
                };

                $scope.setAlertInfo = function (message) {
                    $scope.alertInfo = message;
                    $log.debug("$scope.alertInfo:", $scope.alertInfo);
                    // $scope.$apply();
                    $rootScope.goTo("alertInfo");
                };

                $scope.setAlertSuccess = function (message) {
                    $scope.alertSuccess = message;
                    $log.debug("$scope.alertSuccess:", $scope.alertSuccess);
                    // $scope.$apply();
                    $rootScope.goTo("alertSuccess");
                };

                $scope.setAlertMessage = function (message, header) {
                    $scope.alertMessage = {};
                    $scope.alertMessage.header = header;
                    $scope.alertMessage.message = message;
                    $log.debug("$scope.alertMessage:", $scope.alertMessage);
                    // $scope.$apply();
                    $rootScope.goTo("alertMessage");
                };
                /* -----  Alerts END  */

                //
                $scope.dateOptions = {changeYear: true, changeMonth: true, yearRange: '1900:-0'};
                //
                $scope.download = function (text) {
                    var data = new Blob([text], {type: 'text/plain;charset=utf-8'});
                    FileSaver.saveAs(data, 'key.asc');
                };

                //
                var userId = $stateParams.userId;
                $log.info("$stateParams.userId : " + userId);

                // =============== Bookmarks :
                $scope.addUserToMyBookmarks = function () {
                    $rootScope.progressbar.start();
                    GApi.executeAuth('cryptonomicaUserAPI', 'addUserToMyBookmarks', {"userID": $stateParams.userId}) // async
                        .then(function (resp) {
                                $rootScope.myBookmarks = resp;
                                $log.info("$rootScope.myBookmarks: ");
                                $log.info($rootScope.myBookmarks);
                                // $rootScope.$apply(); // not needed here
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<
                            }, function (error) {
                                $log.error(error);
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<<
                            }
                        );
                };

                $scope.removeUserFromMyBookmarks = function () {
                    $rootScope.progressbar.start();
                    GApi.executeAuth('cryptonomicaUserAPI', 'removeUserFromMyBookmarks', {"userID": $stateParams.userId}) // async
                        .then(function (resp) {
                                $rootScope.myBookmarks = resp;
                                $log.info("$rootScope.myBookmarks: ");
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
                $scope.viewprofile = function () {
                    $rootScope.progressbar.start(); // <<<<<<<
                    $scope.userProfileGeneralView = null;
                    $scope.error = null;
                    GApi.executeAuth('userSearchAndViewAPI', 'getUserProfileById', {"id": userId})
                        .then(
                            function (resp) {
                                if (resp.hasOwnProperty("userProfileGeneralView")
                                    && resp.userProfileGeneralView
                                    && resp.userProfileGeneralView.hasOwnProperty(pgpPublicKeyGeneralViews)
                                    && resp.userProfileGeneralView.pgpPublicKeyGeneralViews
                                    && resp.userProfileGeneralView.pgpPublicKeyGeneralViews.length > 0) {

                                    for (var pgpPublicKeyGeneralView in resp.userProfileGeneralView.pgpPublicKeyGeneralViews) {

                                        if (pgpPublicKeyGeneralView.hasOwnProperty("asciiArmored") && pgpPublicKeyGeneralView.asciiArmored) {
                                            // 1
                                            pgpPublicKeyGeneralView.asciiArmored = pgpPublicKeyGeneralView.asciiArmored
                                                .replace("BEGIN PGP PUBLIC KEY BLOCK-----", "BEGIN PGP PUBLIC KEY BLOCK-----\n");
                                            // 2
                                            pgpPublicKeyGeneralView.asciiArmored = pgpPublicKeyGeneralView.asciiArmored
                                                .replace(")", ")\n\n");
                                            // 3
                                            pgpPublicKeyGeneralView.asciiArmored = pgpPublicKeyGeneralView.asciiArmored
                                                .replace("-----END", "\n-----END");
                                        }
                                        if (pgpPublicKeyGeneralView.exp) {
                                            if (new Date(pgpPublicKeyGeneralView.exp) < new Date()) {
                                                pgpPublicKeyGeneralView.expired = true;
                                                $log.debug('key ' + pgpPublicKeyGeneralView.keyID + 'expired:');
                                                $log.debug(new Date(pgpPublicKeyGeneralView.exp));
                                            }
                                        }
                                    }

                                } // end formatting asciiArmored
                                $scope.userProfileGeneralView = resp; // net.cryptonomica.returns.UserProfileGeneralView
                                $log.info("controller.viewprofile: viewprofile(): resp: ");
                                $log.info(resp);
                                $timeout($rootScope.progressbar.complete(), 1000);
                            },
                            function (error) {
                                $log.error("error: ");
                                $log.error(error);
                                $scope.alertDanger = error.message.replace('java.lang.Exception: ', '');
                                $timeout($rootScope.progressbar.complete(), 1000);
                            }
                        );
                }; // end $scope.viewprofile
                $scope.viewprofile();
                //
                // addOrRewriteNotary:

                $scope.userStatus = null;
                if ($rootScope.currentUser) {
                    if ($rootScope.currentUser.cryptonomicaOfficer == true) {
                        $scope.userStatus = "IACC Officer";
                    } else if ($rootScope.currentUser.notary == true) {
                        $scope.userStatus = "Notary";
                    }
                }

                $log.info("$scope.userStatus (current user): " + $scope.userStatus);
                $scope.AddNotaryForm = {}; // net.cryptonomica.forms.AddNotaryForm
                $scope.AddNotaryFormShow = false; // net.cryptonomica.forms.AddNotaryForm
                $scope.AddNotaryFormShowToogle = function () {
                    $scope.AddNotaryFormShow = !$scope.AddNotaryFormShow;
                };
                // String notaryCryptonomicaUserID; // id of a notary (licenceOwner in licence)
                // String notaryInfo; // info about a notary
                // String licenceIssuedBy;
                // String licenceCountry;
                // Date licenceIssuedOn;
                // Date licenceValidUntil;
                // String licenceInfo;
                // String verificationInfo;
                $scope.AddNotarySubmit = function () {
                    if ($scope.AddNotaryFORM.$valid) {
                        addNotary();
                    } else if ($scope.AddNotaryFORM.licenceCountry.$invalid) {
                        $scope.licenceCountryInvalid = true;
                        $log.error("$scope.AddNotaryFORM.licenceCountry.$invalid: " + $scope.AddNotaryFORM.licenceCountry.$invalid)
                    } else {
                        $log.error("$scope.AddNotaryFORM.$valid: " + $scope.AddNotaryFORM.$valid);
                    }
                };
                var addNotary = function () {
                    $rootScope.progressbar.start();
                    $scope.AddNotaryForm.notaryCryptonomicaUserID = $stateParams.userId;
                    $log.info("AddNoraryForm to send to server:");
                    $log.info($scope.AddNotaryForm);
                    GApi.executeAuth(
                        'notaryAPI',
                        'addOrRewriteNotary',
                        $scope.AddNotaryForm // net.cryptonomica.forms.AddNotaryForm
                    ).then(
                        function (resp) {
                            $log.info("[controller.viewprofile.js] $scope.AddNotary() - resp: ");
                            $log.info(resp);
                            $scope.AddNotaryForm = {};
                            $scope.AddNotaryForm.resp = resp;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        },
                        function (error) {
                            $log.error("[controller.viewprofile.js] $scope.AddNotary() - error: ");
                            $log.error(error);
                            $scope.AddNotaryForm.error = error;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
                }; // end: $scope.AddNotary


                /* ---- NEW KEY UPLOAD */

                $scope.pgpPublicKeyUploadForm = {};
                $scope.privateKeyPasted = false;

                $scope.verifyPublicKeyData = function () {

                    // debug:
                    $log.debug('$scope.pgpPublicKeyUploadForm.asciiArmored: '
                        + $scope.pgpPublicKeyUploadForm.asciiArmored
                    );

                    // $scope.pgpPublicKeyData = null;
                    $scope.pgpPublicKeyDataError = null;
                    $scope.privateKeyPasted = false;

                    // $scope.pgpPublicKeyDataError = null; // use this:
                    if ($rootScope.stringIsNullUndefinedOrEmpty($scope.pgpPublicKeyUploadForm.asciiArmored)) {
                        $scope.pgpPublicKeyDataError = null;
                        return;
                    }

                    if ($scope.pgpPublicKeyUploadForm.asciiArmored.includes("PGP PRIVATE KEY BLOCK")) {
                        $scope.pgpPublicKeyDataError = "(!) THIS IS PRIVATE KEY, DO NOT UPLOAD (!)";
                        // $scope.$apply(); not here
                        return;
                    }

                    try {
                        $scope.pgpPublicKeyData = $rootScope.readPublicKeyData($scope.pgpPublicKeyUploadForm.asciiArmored);
                    } catch (error) {
                        $log.debug(error);
                        $scope.pgpPublicKeyDataError = "This is not a valid OpenPGP public key";
                        // $rootScope.pgpPublicKeyDataError = "This is not a valid OpenPGP public key (" + error + ")";
                        return;
                    }

                    if ($scope.stringIsNullUndefinedOrEmpty($scope.pgpPublicKeyData.userId)) {
                        $scope.pgpPublicKeyDataError = "User ID is empty";
                        return;
                    }

                    if ($scope.pgpPublicKeyData.bitsSize < 2048) {
                        $scope.pgpPublicKeyDataError = "Key size is " + $scope.pgpPublicKeyData.bitsSize + "but min. 2048 required";
                        return;
                    }
                    return true;
                };

                // > first try to load key from browser storage
                var storageType = 'localStorage'; // or 'sessionStorage'
                var storageAvailable = function () {
                    try {
                        var storage = window[storageType],
                            x = '__storage_test__';
                        storage.setItem(x, x);
                        storage.removeItem(x);
                        return true;
                    } catch (e) {
                        return e instanceof DOMException && (
                                // everything except Firefox
                            e.code === 22 ||
                            // Firefox
                            e.code === 1014 ||
                            // test name field too, because code might not be present
                            // everything except Firefox
                            e.name === 'QuotaExceededError' ||
                            // Firefox
                            e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
                            // acknowledge QuotaExceededError only if there's something already stored
                            storage.length !== 0;
                    }
                }();

                $log.debug("storageAvailable: " + storageAvailable);
                // > don't do this, users upload false (old) strings
                if (storageAvailable) {
                    // > don't do this, users upload false (old) strings
                    // $scope.signedString = localStorage.getItem('signedString');
                    $scope.publicKeyArmoredFromLocalStorage = localStorage.getItem("myPublicKey");
                    $log.debug("$scope.publicKeyArmoredFromLocalStorage:", $scope.publicKeyArmoredFromLocalStorage);
                    if ($scope.publicKeyArmoredFromLocalStorage) {
                        $scope.pgpPublicKeyUploadForm.asciiArmored = $scope.publicKeyArmoredFromLocalStorage;
                        try {
                            $scope.pgpPublicKeyData = $rootScope.readPublicKeyData($scope.pgpPublicKeyUploadForm.asciiArmored);
                            $scope.verifyPublicKeyData();
                        } catch (error) {
                            $log.debug(error);
                            $scope.pgpPublicKeyDataError =
                                "Public PGP key stored in local browser storage is not a valid OpenPGP public key";
                        }
                    }

                }

                $scope.readFileContent = function ($fileContent) {
                    $scope.pgpPublicKeyUploadForm.asciiArmored = $fileContent;
                    $scope.verifyPublicKeyData();
                };

                $scope.keyUploadFn = function () {

                    $rootScope.progressbar.start(); // <<<<<<<<<<<

                    $log.info($scope.pgpPublicKeyUploadForm);

                    GApi.executeAuth('pgpPublicKeyAPI', 'uploadNewPGPPublicKey',
                        $scope.pgpPublicKeyUploadForm // -> net.cryptonomica.forms.PGPPublicKeyUploadForm
                    )
                        .then(
                            function (pgpPublicKeyUploadReturn) {
                                // net.cryptonomica.returns.PGPPublicKeyUploadReturn :
                                // String messageToUser; // -> resp.messageToUser in alert
                                // PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
                                $scope.resp = pgpPublicKeyUploadReturn;
                                console.log("pgpPublicKeyUploadReturn: ");
                                $log.info(pgpPublicKeyUploadReturn);
                                $scope.keyUploadSuccessMessage = pgpPublicKeyUploadReturn.messageToUser;
                                // reload page >
                                $scope.viewprofile();
                                // $timeout($rootScope.progressbar.complete(), 1000); // < in $scope.viewprofile();
                            }, function (error) {
                                console.log("error: ");
                                $log.info(error);
                                $scope.keyUploadErrorMessage = error.message; // resp.message or resp.error.message - java.lang.Exception:
                                $timeout($rootScope.progressbar.complete(), 1000);
                            }
                        );

                }; // end of $scope.keyUpload


                /* ----- Delete Profile (Admin) */
                $scope.deleteUserProfileCheckbox = null;
                $scope.reason = null;
                $scope.deleteProfile = function () {
                    $log.debug('$scope.deleteProfile started: '
                        + $scope.deleteUserProfileCheckbox + ' '
                        + $scope.reason + " for "
                        + $scope.userProfileGeneralView.email + " " + $stateParams.userId
                    );
                    if ($rootScope.stringIsNullUndefinedOrEmpty($scope.reason)) {
                        $scope.setAlertDanger("please provide reason for deleting this profile (will be sent to user)");
                        return;
                    }
                    if ($scope.deleteUserProfileCheckbox !== true) {
                        $scope.setAlertDanger("Please confirm deleting a profile in checkbox");
                        return;
                    }
                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                    GApi.executeAuth('onlineVerificationAPI',
                        'deleteProfile', {
                            "userID": $stateParams.userId,
                            "reason": $scope.reason
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
                }; // end of $scope.deleteProfile () ;

            } // end: viewCtrl
        ]
    );

})();
