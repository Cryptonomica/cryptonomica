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

            // $log.debug("openPGPOnline controller started"); //
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

                if ($rootScope.gapi && $rootScope.gapi.user) {
                    $log.debug($rootScope.gapi.user);
                }

                $scope.createKeyForm = {};
                $scope.createKeyForm.name;
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

                    $scope.name = $scope.createKeyForm.firstName + " " + $scope.createKeyForm.lastName;

                } else if ($rootScope.gapi && $rootScope.gapi.user && $rootScope.gapi.user.name) {
                    $scope.name = $rootScope.gapi.user.name
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

            $scope.toggleShowPassword = function (passwordElementId, iconId) {
                const passphrase = document.getElementById(passwordElementId);
                const showPasswordIcon = document.getElementById(iconId);
                if (passphrase.type === "password") {
                    passphrase.type = "text";
                    showPasswordIcon.className = "eye icon";
                    showPasswordIcon.title = "click to hide password";
                } else {
                    passphrase.type = "password";
                    showPasswordIcon.className = "eye slash icon";
                    showPasswordIcon.title = "click to show password";
                }
            }

            // ----- SIGN
            $scope.textToSign = "This is a test message we want to sign.";
            $scope.signText = function () {

                $('#signMessage').click(function (event) {

                    $("#signOrEncryptMessageError").text("");

                    $scope.textToSign = $scope.textToSign.trim();

                    if ($scope.messageToSign.toString().length === 0) {
                        $("#signOrEncryptMessageError").text("Message to sign is empty").css('color', 'red');
                        return;
                    }

                    var privateKeyArmored = $("#privkeyShow").val();

                    if (privateKeyArmored.toString().length === 0) {
                        $("#signOrEncryptMessageError").text("Private key is empty").css('color', 'red');
                        return;
                    }

                    var passphrase = $("#passphrase").val();
                    if (passphrase.toString().length === 0) {
                        $("#signOrEncryptMessageError").text("Password for private key is empty").css('color', 'red');
                        return;
                    }

                    var privateKeyEncrypted;
                    try {
                        privateKeyEncrypted = openpgp.key.readArmored(privateKeyArmored).keys[0];
                        if (typeof privateKeyEncrypted === 'undefined' || privateKeyEncrypted === null) {
                            $("#signOrEncryptMessageError").text("Private key is invalid").css('color', 'red');
                            return;
                        }
                        privateKeyEncrypted.decrypt(passphrase); // boolean
                    } catch (error) {
                        console.log("sign message error:");
                        console.log(error);
                        $("#signOrEncryptMessageError").text(error).css('color', 'red');
                    }

                    var privateKeyDecrypted = privateKeyEncrypted;

                    var signObj = {
                        data: messageToSign, // cleartext input to be signed
                        privateKeys: privateKeyDecrypted, // array of keys or single key with decrypted secret key data to sign cleartext
                        armor: true // (optional) if the return value should be ascii armored or the message object
                    };
                    var signedMessageObj = {};
                    // see: https://openpgpjs.org/openpgpjs/doc/openpgp.js.html#line285
                    // https://openpgpjs.org/openpgpjs/doc/module-openpgp.html
                    openpgp.sign(signObj)
                        .then(function (res) { //
                            // @return {Promise<String|CleartextMessage>} ASCII armored message or the message of type CleartextMessage
                            signedMessageObj = res;

                            console.log(JSON.stringify(signedMessageObj));

                            console.log(signedMessageObj.data);

                            $("#signedMessage").val(signedMessageObj.data);
                            console.log("signedMessageObj.data.length: ", signedMessageObj.data.length);

                            onSignedMessageChange();

                        })
                        .catch(function (error) {
                            console.log("sign message error:");
                            console.log(error);
                            ("#signOrEncryptMessageError").text(error).css('color', 'red');
                        });
                });


            }


            $timeout($rootScope.progressbar.complete(), 1000);
        } // end of Ctrl

    ]);


})();
