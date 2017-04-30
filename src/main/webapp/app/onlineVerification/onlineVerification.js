'use strict';

var controller_name = "cryptonomica.controller.onlineVerification";

var controller = angular.module(controller_name, []);

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
                           $stateParams) {

        $log.info("cryptonomica.controller.onlineVerification started");
        $log.info("$stateParams.fingerprint : " + $stateParams.fingerprint);
        $scope.error = {};
        $scope.getOnlineVerificationError = false;
        $scope.fingerprint = $stateParams.fingerprint;
        $scope.keyId = "0x" + $stateParams.fingerprint.substring(32, 40).toUpperCase();
        $scope.todayDate = new Date();
        $scope.videoUploadKey = null;
        $scope.verificationDocsUrls = [];

        // --- define functions:
        $scope.getOnlineVerification = function () {
            GApi.executeAuth(
                'onlineVerificationAPI',
                'getOnlineVerificationByFingerprint',
                {"fingerprint": $stateParams.fingerprint}
            ).then(
                function (resp) {
                    console.log("$scope.getOnlineVerification resp:");
                    console.log(resp);

                    $scope.onlineVerification = resp;
                    $scope.getOnlineVerificationError = false;

                }, function (error) {
                    $log.error("$scope.getOnlineVerification error:");
                    console.log(error);
                    if (error.message.indexOf("you are not allowed to get online verification data for key") !== -1) {
                        $scope.keyNotOwned = true;
                    }
                    $log.info("$scope.keyNotOwned : " + $scope.keyNotOwned);
                    $scope.error.message = "[server error] " + error.message;
                    $scope.getOnlineVerificationError = true;
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

                    }, function (error) {
                        console.log("$scope.etVideoUploadKey error:");
                        console.log(error);
                        $scope.error.message = "[server error]" + error.message;
                    }
                )
        };

        // --- get user data
        GAuth.checkAuth().then(
            function () {
                $rootScope.getUserData(); // async?
                $scope.getOnlineVerification();
                $scope.getVideoUploadKey();
            },
            function () {
                $log.error("[cryptonomica.controller.onlineVerification] GAuth.checkAuth() - unsuccessful");
            }
        );

        // -------------------
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
            GApi.executeAuth('onlineVerificationAPI', 'getDocumentsUploadKey')
                .then(
                    function (resp) {
                        $scope.verificationDocumentsUploadKey = resp.message; // net.cryptonomica.returns.StringWrapperObject
                        console.log('onlineVerificationAPI > getDocumentsUploadKey:');
                        console.log(resp);
                        $scope.btn_upload(); // <<<
                    }, function (error) {
                        console.log('[Error] onlineVerificationAPI > getDocumentsUploadKey:');
                        console.log(error);
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
                    var responseObj = JSON.parse(response);
                    $log.info(responseObj);
                    if (responseObj.uploadedDocumentId) {
                        var numb
                            = $scope.verificationDocsUrls.push("https://cryptonomica.net/docs?verificationDocumentId="
                            + responseObj.uploadedDocumentId);
                        $log.info("(" + numb - 1 + ") " + $scope.verificationDocsUrls[numb - 1]);
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
                        $timeout($rootScope.progressbar.complete(), 1000);

                    }, function (error) {
                        console.log('[Error] onlineVerificationAPI > sendSms');
                        console.log(error);
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
        }
    }
]);

