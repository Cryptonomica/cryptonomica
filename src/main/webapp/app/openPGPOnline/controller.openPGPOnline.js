(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.openPGPOnline";

// add to:
// index.html +
// controller.js +
// router.js + ( url: '/openPGPOnline')

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
        // 'uiUploader',
        '$log',
        '$cookies',
        '$timeout',
        '$window',
        '$stateParams',
        function openPGPOnlineCtrl($scope,    // <- name for debugging
                                   $sce,
                                   $rootScope,
                                   $http,
                                   GApi,
                                   GAuth,
                                   GData,
                                   $state,
                                   // uiUploader,
                                   $log,
                                   $cookies,
                                   $timeout,
                                   $window,
                                   $stateParams) {

            /*
            * in the view add id with controller name to parent div:
            * <div id="openPGPOnlineCtrl">
            * use this id to access $scope in browser console:
            * angular.element(document.getElementById('openPGPOnlineCtrl')).scope();
            * for example: for example: $scope = angular.element(document.getElementById('openPGPOnlineCtrl')).scope();
            * */

            $log.debug("openPGPOnline controller started"); //
            // $timeout($rootScope.progressbar.complete(), 1000);

            // --- Alerts:
            $scope.alertDanger = null;  // red
            $scope.alertWarning = null; // yellow
            $scope.alertInfo = null;    // blue
            $scope.alertSuccess = null; // green
            //
            $scope.error = {};
            //

            $scope.prefillCreateKeyForm = function () {

                $scope.createKeyForm = {};
                $scope.createKeyForm.firstName;
                $scope.createKeyForm.lastName;
                $scope.createKeyForm.userEmail;

                if ($rootScope.gapi && $rootScope.gapi.user && $rootScope.gapi.user.email) {
                    $scope.createKeyForm.userEmail = $rootScope.gapi.user.email;
                }

                if ($rootScope.currentUser) {

                    if ($rootScope.currentUser.firstName) {
                        $scope.createKeyForm.firstName = $rootScope.currentUser.firstName;
                    }

                    if ($rootScope.currentUser.lastName) {
                        $scope.createKeyForm.lastName = $rootScope.currentUser.lastName;
                    }

                }

                // $scope.$apply();
            };
            $scope.prefillCreateKeyForm();

            $scope.publicKey;
            $scope.verifyPublicKeyData = function () {

                $scope.keyUploadErrorMessage = null;
                $scope.keyUploadSuccessMessage = null;

                // $scope.pgpPublicKeyDataError = null; // use this:
                if ($rootScope.stringIsNullUndefinedOrEmpty($scope.pgpPublicKeyUploadForm.asciiArmored)) {
                    $scope.keyUploadErrorMessage = "Key string is null or empty";
                    return false;
                }

                if ($scope.pgpPublicKeyUploadForm.asciiArmored.includes("PGP PRIVATE KEY BLOCK")) {
                    $scope.keyUploadErrorMessage = "(!) THIS IS PRIVATE KEY, DO NOT UPLOAD (!)";
                    // $scope.$apply(); not here
                    return false;
                }

                try {
                    $scope.pgpPublicKeyData = $rootScope.readPublicKeyData($scope.pgpPublicKeyUploadForm.asciiArmored);
                } catch (error) {
                    $log.debug(error);
                    $scope.keyUploadErrorMessage = "This is not a valid OpenPGP public key";
                    return false;
                }

                if ($scope.stringIsNullUndefinedOrEmpty($scope.pgpPublicKeyData.userId)) {
                    $scope.keyUploadErrorMessage = "User ID is empty";
                    return false;
                }

                if ($scope.pgpPublicKeyData.bitsSize < 2048) {
                    $scope.keyUploadErrorMessage = "Key size is " + $scope.pgpPublicKeyData.bitsSize + "but min. 2048 required";
                    return false;
                }
                return true;
            };

            $scope.addPublicKeyToProfile = function () {

                $scope.pgpPublicKeyUploadForm = {};
                $scope.pgpPublicKeyUploadForm.asciiArmored = $("#pubkeyShow").val();
                $log.info($scope.pgpPublicKeyUploadForm);

                if ($scope.verifyPublicKeyData()) {
                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                    GApi.executeAuth('pgpPublicKeyAPI', 'uploadNewPGPPublicKey',
                        $scope.pgpPublicKeyUploadForm // -> net.cryptonomica.forms.PGPPublicKeyUploadForm
                    )
                        .then(function (pgpPublicKeyUploadReturn) {
                            console.log("pgpPublicKeyUploadReturn: ");
                            $log.info(pgpPublicKeyUploadReturn);
                            $scope.keyUploadSuccessMessage = pgpPublicKeyUploadReturn.messageToUser;
                        })
                        .catch(function (error) {
                            console.log("error: ");
                            $log.info(error);
                            $scope.keyUploadErrorMessage = error.message.replace("java.lang.Exception: ", ""); // resp.message or resp.error.message - java.lang.Exception:
                        })
                        .finally(function () {
                            // $scope.$apply();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        });
                } else {
                    $log.error("public key verification failed");
                }


            }; // end of $scope.keyUpload


            $timeout($rootScope.progressbar.complete(), 1000);
        } // end of Ctrl

    ]);


})();
