(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.onlineVerification";

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
        '$location',
        '$anchorScroll',
        '$stateParams',
        function onlineVerCtrl($scope,    // <- name for debugging
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
                               $location,
                               $anchorScroll,
                               $stateParams) {

            $log.info(controller_name, " ver. 01 started");

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
            // --- Alerts:
            $scope.alertDanger = null;  // red
            $scope.alertWarning = null; // yellow
            $scope.alertInfo = null;    // blue
            $scope.alertSuccess = null; // green
            //
            $scope.error = {};
            $scope.getOnlineVerificationError = false;
            $scope.fingerprint = $stateParams.fingerprint;
            $scope.keyId = "0x" + $stateParams.fingerprint.substring(32, 40).toUpperCase();
            $scope.todayDate = new Date();
            $scope.nationality = null;
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

            // $log.debug('checkpoint # 01 '); // TODO: why does not work?
            console.log('checkpoint # 01 '); // works

            var nullUndefinedEmptySting = function (str) {
                return (str == null || str == undefined || str == "" || str.length == 0);
            };

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

                        // $log.debug('$scope.onlineVerification:');
                        // $log.debug($scope.onlineVerification);
                        console.log('$scope.onlineVerification:');
                        console.log($scope.onlineVerification);

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
                        if (!$scope.isKeyOwner) {
                            $log.debug('You are not the owner of this key');
                            $scope.alertDanger = 'You are not the owner of this key!';
                            $location.hash('alertDanger');
                            $anchorScroll();
                        } else if ($scope.onlineVerification.onlineVerificationDataVerified) {
                            $scope.alertSuccess = 'Online verification for key '
                                + $scope.fingerprint
                                + ' completed!';
                            $location.hash('alertSuccess');
                            $anchorScroll();
                        } else if ($scope.onlineVerification.onlineVerificationFinished) {
                            $scope.alertInfo = 'You entered all required data.'
                                + ' Please wait for data verification by our compliance officer.';
                            $location.hash('alertInfo');
                            $anchorScroll();
                        } else {
                            // check current verification step:
                            // 0
                            $scope.showTerms = false;
                            // 1
                            $scope.verificationDocument = false;
                            // 2
                            $scope.verificationVideo = false;
                            // 3
                            $scope.phoneVerification = false;
                            // 4
                            $scope.stripePaymentForOnlineKeyVerification = false;
                            // 5
                            $scope.stripePaymentVerificationCode = false;
                            //
                            if ($scope.onlineVerification.termsAccepted === false) {
                                $scope.showTerms = true;
                            } else if ($scope.onlineVerification.verificationDocumentsArray === undefined
                                || $scope.onlineVerification.verificationDocumentsArray === null
                                || $scope.onlineVerification.verificationDocumentsArray.length < 2) {

                                $scope.verificationDocument = true;

                                // ------------
                                /*    https://github.com/angular-ui/ui-uploader     */

                                $scope.btn_remove = function (file) {
                                    $log.info('deleting=' + file);
                                    uiUploader.removeFile(file);
                                };

                                $scope.btn_clean = function () {
                                    console.log('$scope.btn_clean');
                                    uiUploader.removeAll();
                                };

                                $scope.uploadDocs = function () {
                                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                                    $log.info('$scope.uploadDocs ...');
                                    $scope.resp = {};
                                    $scope.resp.error = null;
                                    if ($scope.nationality == null
                                        || $scope.nationality.length == 0) {
                                        $scope.alertDanger = "Please provide nationality";
                                        // see: https://docs.angularjs.org/api/ng/service/$anchorScroll
                                        // set the location.hash to the id of
                                        // the element you wish to scroll to.
                                        $location.hash('alertDanger');
                                        $anchorScroll();
                                        $timeout($rootScope.progressbar.complete(), 1000);
                                        return;
                                    }
                                    GApi.executeAuth('onlineVerificationAPI', 'getDocumentsUploadKey')
                                        .then(
                                            function (resp) {
                                                $scope.verificationDocumentsUploadKey = resp.message; // net.cryptonomica.returns.StringWrapperObject
                                                console.log('onlineVerificationAPI > getDocumentsUploadKey:');
                                                console.log(resp);
                                                $scope.btn_upload(); // <<<< set headers here
                                            }, function (error) {
                                                console.log('[Error] onlineVerificationAPI > getDocumentsUploadKey:');
                                                console.log(error);
                                                $scope.alertDanger = error;
                                                // see: https://docs.angularjs.org/api/ng/service/$anchorScroll
                                                // set the location.hash to the id of
                                                // the element you wish to scroll to.
                                                $location.hash('alertDanger');
                                                $anchorScroll();
                                                $timeout($rootScope.progressbar.complete(), 1000);
                                            }
                                        );
                                };

                                $scope.btn_upload = function () {
                                    $log.info('uploading...');
                                    uiUploader.startUpload({
                                        //url: 'http://realtica.org/ng-uploader/demo.html',
                                        url: $sce.trustAsResourceUrl('/docs'),
                                        //concurrency: 1, //
                                        options: {
                                            withCredentials: true
                                        },
                                        headers: {
                                            //'Accept': 'application/json'
                                            'imageUploadKey': $scope.imageUploadKey,
                                            'userId': $rootScope.currentUser.userId,
                                            'userEmail': $rootScope.currentUser.email,
                                            'fingerprint': $stateParams.fingerprint,
                                            'nationality': $scope.nationality,
                                            'verificationDocumentsUploadKey': $scope.verificationDocumentsUploadKey
                                        },
                                        onProgress: function (file) {
                                            $log.info(file.name + '=' + file.humanSize);
                                            $scope.$apply();
                                        },
                                        onCompleted: function (file, response) {
                                            console.log("File:");
                                            $log.info(file);
                                            console.log("Response:");
                                            $log.info(response);
                                            var responseObj;
                                            try {
                                                responseObj = JSON.parse(response);
                                                $log.debug(responseObj);
                                                if (responseObj.uploadedDocumentId) {
                                                    var numb
                                                        = $scope.verificationDocsUrls.push(
                                                        "https://cryptonomica.net/docs?verificationDocumentId="
                                                        + responseObj.uploadedDocumentId);
                                                    $log.debug("(" + numb - 1 + ") "
                                                        + $scope.verificationDocsUrls[numb - 1]
                                                    );
                                                }
                                            }
                                            catch (error) {
                                                $log.error(error);
                                                $scope.alertDanger = error;
                                                $location.hash('alertDanger');
                                                $anchorScroll();
                                            }
                                            $scope.$apply(); // ?
                                            $timeout($rootScope.progressbar.complete(), 1000);
                                        }
                                    });
                                }; // end btn_upload

                                $scope.files = [];
                                var elementOne = $window.document.getElementById('fileOne');
                                console.log("elementOne: ");
                                console.log(elementOne);
                                elementOne.addEventListener('change', function (e) {
                                    var files = e.target.files;
                                    uiUploader.addFiles(files);
                                    $scope.files = uiUploader.getFiles();
                                    $scope.$apply();
                                });
                                var elementTwo = $window.document.getElementById('fileTwo');
                                console.log("elementTwo: ");
                                console.log(elementTwo);
                                elementTwo.addEventListener('change', function (e) {
                                    var files = e.target.files;
                                    uiUploader.addFiles(files);
                                    $scope.files = uiUploader.getFiles();
                                    $scope.$apply();
                                });


                                $timeout($rootScope.progressbar.complete(), 1000);
                                // $scope.$apply(); // not needed here

                            } else if (nullUndefinedEmptySting($scope.onlineVerification.verificationVideoId)) {
                                // $state.go('onlineVerificationVideo', {'fingerprint': $stateParams.fingerprint});
                                $scope.verificationVideo = true;
                                $scope.getVideoUploadKey();

                            } else if (nullUndefinedEmptySting($scope.onlineVerification.phoneNumber)) {

                                $scope.phoneVerification = true;
                                $timeout($rootScope.progressbar.complete(), 1000);
                                // $scope.$apply(); // not here

                            } else if (nullUndefinedEmptySting($scope.onlineVerification.paymentMade)
                                || !$scope.onlineVerification.paymentMade) {

                                $scope.stripePaymentForOnlineKeyVerification = true;
                                $scope.getPriceForKeyVerification();

                            } else if (nullUndefinedEmptySting($scope.onlineVerification.paymentVerified)
                                || !$scope.onlineVerification.paymentVerified) {

                                $scope.stripePaymentVerificationCode = true;
                                $timeout($rootScope.progressbar.complete(), 1000);
                                // $scope.$apply(); not here

                            } else {
                                $timeout($rootScope.progressbar.complete(), 1000);
                                // $log.error("define verification step error");
                                // $scope.alertDanger = "define verification step error";
                                // $timeout($rootScope.progressbar.complete(), 1000);
                                // $scope.$apply();
                            }
                        }

                    }, function (error) {
                        $log.error("$scope.getOnlineVerification error:");
                        console.log(error);
                        if (error.message.indexOf("you are not allowed to get online verification data for key") !== -1) {
                            $scope.keyNotOwned = true;
                        }
                        $log.info("$scope.keyNotOwned : " + $scope.keyNotOwned);
                        $scope.error.message = "[server error] " + error.message;
                        $scope.getOnlineVerificationError = true;
                        $timeout($rootScope.progressbar.complete(), 1000);
                        // $scope.$apply(); // not needed here
                    }
                )
            };

            $log.debug('checkpoint # 02 ');

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
            $log.debug('checkpoint # 03 ');
            if (!$rootScope.currentUser) {
                $log.debug(' if (!$rootScope.currentUser) started:');
                $rootScope.progressbar.start();
                GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData')
                    .then(
                        function (resp) {
                            $rootScope.currentUser = resp;
                            $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink); //;
                            $log.info("[onlineVerification.js] $rootScope.currentUser: ");
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

            /* Functions for steps, that should be called when step loaded */

            $scope.getPriceForKeyVerification = function () {
                GApi.executeAuth(
                    'stripePaymentsAPI',
                    'getPriceForKeyVerification',
                    {"fingerprint": $stateParams.fingerprint}
                ).then(
                    function (IntegerWrapperObject) {
                        $scope.priceForKeyVerification = IntegerWrapperObject.number / 100; // from cents to dollars
                        console.log("$scope.priceForKeyVerification: ");
                        console.log($scope.priceForKeyVerification);
                        $timeout($rootScope.progressbar.complete(), 1000);
                        // $scope.$apply(); // not here

                    }, function (getPriceForKeyVerificationError) {
                        $scope.getPriceForKeyVerificationError = getPriceForKeyVerificationError;
                        console.log("$scope.getPriceForKeyVerificationError : ");
                        $log.error($scope.getPriceForKeyVerificationError);
                        $timeout($rootScope.progressbar.complete(), 1000);
                        // $scope.$apply(); // not here
                    }
                )
            };

            $scope.getVideoUploadKey = function () {
                GApi.executeAuth('onlineVerificationAPI', 'getVideoUploadKey')
                    .then(
                        function (resp) {
                            console.log("$scope.getVideoUploadKey resp:");
                            console.log(resp);

                            $scope.videoUploadKey = resp.message;

                            $timeout($rootScope.progressbar.complete(), 1000);
                            // $scope.$apply(); // <<< not here

                        }, function (error) {
                            console.log("$scope.etVideoUploadKey error:");
                            console.log(error);
                            $scope.error.message = "[server error]" + error.message;
                            $timeout($rootScope.progressbar.complete(), 1000);
                            // $scope.$apply(); //
                        }
                    )
            };

// --- get user data and execute calls :
//         GAuth.checkAuth().then(
//             function () {
//                 $rootScope.getUserData(); // async?
//                 $scope.getOnlineVerification();
//                 $scope.getVideoUploadKey();
//                 $scope.getPGPPublicKeyByFingerprint();
//                 $scope.getPriceForKeyVerification();
//             },
//             function () {
//                 $log.error("[cryptonomica.controller.onlineVerification] GAuth.checkAuth() - unsuccessful");
//             }
//         );


// ---------- SMS
            $scope.sendSms = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $log.info('$scope.sendSms ...');
                GApi.executeAuth('onlineVerificationAPI',
                    'sendSms',
                    {
                        "fingerprint": $stateParams.fingerprint,
                        "phoneNumber": $scope.phoneNumber
                    }
                )
                    .then(
                        function (resp) {
                            console.log('onlineVerificationAPI > sendSms:');
                            console.log(resp); // new StringWrapperObject("SMS message send successfully")
                            $scope.alertSuccess = resp.message;
                            $location.hash('alertSuccess');
                            $anchorScroll();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            console.log('[Error] onlineVerificationAPI > sendSms');
                            console.log(error);
                            $scope.alertDanger = error.message;
                            $location.hash('alertDanger');
                            $anchorScroll();
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            };

            $scope.checkSms = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $log.info('$scope.checkSms ...');
                GApi.executeAuth('onlineVerificationAPI',
                    'checkSms',
                    {
                        "fingerprint": $stateParams.fingerprint,
                        "smsMessage": $scope.smsMessage
                    }
                )
                    .then(
                        function (resp) {
                            console.log('onlineVerificationAPI > checkSms:');
                            console.log(resp); //  new StringWrapperObject();
                            $scope.smsResult = resp.message;
                            $timeout($rootScope.progressbar.complete(), 1000);

                        }, function (error) {
                            console.log('[Error] onlineVerificationAPI > checkSms');
                            console.log(error);
                            // for example:
                            // Object {code: 400, data: Array(1), message: "Required parameter: smsMessage", error: Object}
                            $scope.smsResult = error.message;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end of $scope.checkSms

            /* ---- Stripe payment  */
            $scope.submitPayment = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.submitPaymentWorking = true;
                $log.info($scope.stripePaymentForm);
                GApi.executeAuth('stripePaymentsAPI', 'processStripePayment', $scope.stripePaymentForm)
                    .then(
                        function (paymentResponse) {
                            $scope.paymentResponse = paymentResponse;
                            console.log("$scope.paymentResponse: ");
                            $log.info($scope.paymentResponse);
                            $scope.submitPaymentWorking = false;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (paymentError) {
                            $scope.paymentError = paymentError;
                            console.log("$scope.paymentError: ");
                            $log.info($scope.paymentError);
                            $scope.submitPaymentWorking = false;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end of $scope.submitPayment()

            /* ---- Stripe payment verification : */
            $scope.checkPaymentVerificationCode = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $log.info($scope.stripePaymentForm);
                GApi.executeAuth('stripePaymentsAPI',
                    'checkPaymentVerificationCode',
                    {
                        "fingerprint": $stateParams.fingerprint,
                        "paymentVerificationCode": $scope.paymentVerificationCode
                    }
                )
                    .then(
                        function (paymentVerificationResponse) { // StringWrapperObject
                            $scope.paymentVerificationResponse = paymentVerificationResponse;
                            // use $scope.paymentVerificationResponse.message
                            console.log("$scope.paymentVerificationResponse: ");
                            $log.info($scope.paymentVerificationResponse);
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (paymentVerificationError) {
                            $scope.paymentVerificationError = paymentVerificationError;
                            console.log("$scope.paymentVerificationError: ");
                            $log.info($scope.paymentVerificationError);
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end of $scope.checkPaymentVerificationCode()

            /* ---- Accept terms : */
            $scope.acceptTermsAnswer = false;
            $scope.acceptTerms = function () {
                $log.debug('$scope.acceptTerms strarted');
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.acceptTermsSuccess = null;
                $scope.acceptTermsError = null;
                GApi.executeAuth('onlineVerificationAPI',
                    'acceptTerms',
                    {
                        "fingerprint": $stateParams.fingerprint,
                        "termsAccepted": $scope.acceptTermsAnswer
                    }
                )
                    .then(
                        function (acceptTermsResponse) { // BooleanWrapperObject.java
                            $log.debug(acceptTermsResponse);
                            $scope.acceptTermsResponce = acceptTermsResponse;
                            if (acceptTermsResponse.result) {
                                $scope.acceptTermsSuccess = "Terms accepted!"
                            } else {
                                $scope.acceptTermsError
                                    = "Terms not accepted! We can not proceed with online verification"
                            }
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (acceptTermsError) {
                            $log.debug(acceptTermsError);
                            $scope.acceptTermsError = acceptTermsError.message;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end of

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
                            $scope.approveResponse = approveResponse;
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