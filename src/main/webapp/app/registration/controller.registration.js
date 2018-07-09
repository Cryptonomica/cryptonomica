(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.registration";

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
        function regCtrl($scope,
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
                         $timeout) {

            $log.debug(controller_name, "started"); //
            $timeout($rootScope.progressbar.complete(), 1000);

            // ======== Terms of Use: start  =======
            if (!$rootScope.currentUser.registeredCryptonomicaUser) {
                // if ($rootScope.googleUser && !$rootScope.currentUser.registeredCryptonomicaUser) {
                // if ($rootScope.currentUser.loggedIn && !$rootScope.currentUser.registeredCryptonomicaUser) {
                $log.debug("[regCtrl] new user registration");
                // see:
                // https://stackoverflow.com/questions/27776174/type-error-cannot-read-property-childnodes-of-undefined
                // https://stackoverflow.com/a/28630404/1697878

                setTimeout(function () {
                    $log.debug("show modal:");
                    $log.debug($("#modalAcceptTerms"));
                    $("#modalAcceptTerms").modal("show");
                }, 0);

            } else if (!$rootScope.currentUser.loggedIn) {
                $log.debug("$rootScope.currentUser.loggedIn:", $rootScope.currentUser.loggedIn);
            }

            $scope.termsAccepted = false;
            $scope.rejectTerms = function () {

                $state.go('home');

                // GAuth.logout()
                //     .then(function (logoutResult) {
                //         $log.debug('logoutResult:');
                //         $log.debug(logoutResult);
                //     })
                //     .catch(function (error) {
                //         $log.debug(error);
                //     })
                //     /* A finally callback will not receive any argument,
                //     since there's no reliable means of determining if the promise was fulfilled or rejected.
                //     This use case is for precisely when you do not care about the rejection reason,
                //     or the fulfillment value, and so there's no need to provide it.*/
                //     .finally(function () {
                //         $rootScope.googleUser = null;
                //         $rootScope.currentUser = null;
                //         $state.go('home');
                //     })

            }; // end of $scope.rejectTerms

            $scope.acceptTerms = function () {
                $log.debug("terms accepted");
                $scope.termsAccepted = true;
            };

            // ======== Terms of Use: end =======

            /* ========= KEY upload start: */
            $scope.regForm = {};
            $scope.privateKeyPasted = false;

            $scope.verifyPublicKeyData = function () {

                // debug:
                $log.debug('$scope.regForm.armoredPublicPGPkeyBlock: '
                    + $scope.regForm.armoredPublicPGPkeyBlock
                );

                $scope.pgpPublicKeyData = null;
                $scope.pgpPublicKeyDataError = null;
                $scope.privateKeyPasted = false;

                // $scope.pgpPublicKeyDataError = null; // use this:
                if ($rootScope.stringIsNullUndefinedOrEmpty($scope.regForm.armoredPublicPGPkeyBlock)) {
                    $scope.pgpPublicKeyDataError = null;
                    return;
                }

                if ($scope.regForm.armoredPublicPGPkeyBlock.includes("PGP PRIVATE KEY BLOCK")) {
                    $scope.pgpPublicKeyDataError = "(!) THIS IS PRIVATE KEY, DO NOT UPLOAD (!)";
                    // $scope.$apply(); not here
                    return;
                }

                try {

                    $scope.pgpPublicKeyData = $rootScope.readPublicKeyData($scope.regForm.armoredPublicPGPkeyBlock);

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

            $scope.readFileContent = function ($fileContent) {
                $scope.regForm.armoredPublicPGPkeyBlock = $fileContent;
                $scope.verifyPublicKeyData();
            };

            $scope.dateOptions = {changeYear: true, changeMonth: true, yearRange: '1900:-0'};

            $scope.registerNewUser = function () {

                $log.debug("$scope.regForm:");
                $log.debug($scope.regForm);

                if ($rootScope.stringIsNullUndefinedOrEmpty($scope.regForm.armoredPublicPGPkeyBlock)) {
                    $rootScope.setAlertDanger("Please provide your public key");
                    return;
                }

                if ($scope.verifyPublicKeyData) {

                    if ($scope.privateKeyPasted) {
                        $rootScope.setAlertDanger("Do not upload private key");
                        return;
                    }

                    if (!$scope.regForm.birthday) {
                        $rootScope.setAlertDanger("Birthdate can not be empty");
                        return;
                    }

                    $rootScope.progressbar.start(); // <<<<<<<<<<<

                    if ($rootScope.alertWarning) {
                        $rootScope.alertWarning = null;
                    }


                    GApi.executeAuth('newUserRegistrationAPI', 'registerNewUser', $scope.regForm)
                        .then(
                            function (resp) {
                                $scope.resp = resp; //
                                console.log("resp: ");
                                $log.info(resp);
                                $rootScope.currentUser = resp.userProfileGeneralView;
                                $rootScope.checkAuth();
                                $timeout($rootScope.progressbar.complete(), 1000);
                                $state.go('home'); // TODO: <<<<

                            })
                        .catch(function (error) {
                            console.log("error: ");
                            $log.info(error);
                            $rootScope.setAlertDanger(error.message);
                            $rootScope.checkAuth();
                            $timeout($rootScope.progressbar.complete(), 1000);

                        });
                } else {
                    $rootScope.setAlertDanger("Key can not be uploaded, please check key data and provide correct public key");
                }

            }; // end registerNewUser


        }]);
})();
