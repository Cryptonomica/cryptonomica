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

                // --- **** get user data
                //$rootScope.getUserData(); // ? async -->
                //if (!$rootScope.currentUser || !$rootScope.currentUser.loggedIn) {
                //    $state.go('home');
                //}
                /*                GAuth.checkAuth().then(
                 function () {
                 $rootScope.getUserData(); // async?
                 },
                 function () {
                 //$rootScope.getUserData();
                 $log.error("[controller.registration.js] GAuth.checkAuth() - unsuccessful");
                 $scope.alert = "User not logged in";
                 }
                 );*/
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
                $scope.dateOptions = {changeYear: true, changeMonth: true, yearRange: '1900:-0'};
                // --------------------------------------------------------------
                $scope.regForm = {};
                $scope.pgpPublicKeyUploadForm = {};
                $scope.readFileContent = function ($fileContent) {
                    // $scope.content = $fileContent;
                    $scope.regForm.armoredPublicPGPkeyBlock = $fileContent;
                    $scope.pgpPublicKeyUploadForm.asciiArmored = $fileContent;
                    $log.debug('$scope.regForm.armoredPublicPGPkeyBlock: '
                        + $scope.regForm.armoredPublicPGPkeyBlock
                    );
                    $log.debug(' $scope.pgpPublicKeyUploadForm.asciiArmored: '
                        + $scope.pgpPublicKeyUploadForm.asciiArmored
                    );
                };

                // --------------------------------------------------------------
                $scope.getCsUploadURL = function () {
                    GApi.executeAuth('uploadAPI', 'getCsUploadURL')
                        .then(
                            function (resp) {
                                $scope.imageUploadUrl = $sce.trustAsResourceUrl(resp.imageUploadUrl);
                                $scope.imageUploadKey = resp.imageUploadKey;
                                console.log("$scope.getCsUploadURL resp:");
                                console.log(resp);
                            }, function (error) {
                                console.log("$scope.getCsUploadURL error:");
                                console.log(error);
                            }
                        )
                }; // TODO: do we need this?

                /*    https://github.com/angular-ui/ui-uploader     */
                $scope.btn_remove = function (file) {
                    $log.info('deleting=' + file);
                    uiUploader.removeFile(file);
                };
                $scope.btn_clean = function () {
                    uiUploader.removeAll();
                };

                $scope.uploadPic = function () {
                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                    $scope.resp = {};
                    $scope.resp.error = null;
                    GApi.executeAuth('uploadAPI', 'getCsUploadURL')
                        .then(
                            function (resp) {
                                $scope.imageUploadUrl = $sce.trustAsResourceUrl(resp.imageUploadUrl);
                                $scope.imageUploadKey = resp.imageUploadKey;
                                console.log("$scope.getCsUploadURL resp:");
                                console.log(resp);
                                $scope.btn_upload(); // <<<
                            }, function (error) {
                                console.log("$scope.getCsUploadURL error:");
                                console.log(error);
                            }
                        );
                    $rootScope.checkAuth();
                    //$rootScope.$apply(); // - no
                    $timeout($rootScope.progressbar.complete(), 1000);
                };

                $scope.btn_upload = function () {
                    $log.info('uploading...');
                    uiUploader.startUpload({
                        //url: 'http://realtica.org/ng-uploader/demo.html',
                        url: $scope.imageUploadUrl,
                        //concurrency: 1, //
                        options: {
                            withCredentials: true
                        },
                        headers: {
                            //'Accept': 'application/json'
                            'imageUploadKey': $scope.imageUploadKey,
                            // see: https://stackoverflow.com/questions/40516226/access-control-allow-origin-issue-with-api
                            'Access-Control-Allow-Origin': '*'
                        },
                        onProgress: function (file) {
                            $log.info(file.name + '=' + file.humanSize);
                            $scope.$apply();
                        },
                        onCompleted: function (file, response) {
                            $log.info(file + 'response' + response);
                            $rootScope.checkAuth();
                            //$rootScope.userCurrentImageLink = $sce.trustAsResourceUrl($rootScope.userCurrentImageLink);
                            //
                        }
                    });
                }; // end btn_upload

                $scope.files = [];
                var element = document.getElementById('file1');
                element.addEventListener('change', function (e) {
                    var files = e.target.files;
                    uiUploader.addFiles(files);
                    $scope.files = uiUploader.getFiles();
                    $scope.$apply();
                });

                /* -------------------------------------------------*/

                $scope.refreshPhoto = function () {
                    $rootScope.checkAuth();
                    $state.go('registration');
                };

                $scope.registerNewUser = function () {
                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                    if ($scope.resp && $scope.resp.error) {
                        $scope.resp.error = null;
                    }
                    $log.info($scope.regForm);
                    GApi.executeAuth('newUserRegistrationAPI', 'registerNewUser', $scope.regForm)
                        .then(
                            function (resp) {
                                $scope.resp = resp; //
                                $rootScope.currentUser = resp.userProfileGeneralView;
                                console.log("resp: ");
                                $log.info(resp);
                                $timeout($rootScope.progressbar.complete(), 1000);
                                $rootScope.checkAuth();
                            }, function (resp) {
                                console.log("error: ");
                                $log.info(resp);
                                $scope.resp = resp; // resp.message or resp.error.message - java.lang.Exception:
                                $timeout($rootScope.progressbar.complete(), 1000);
                                $rootScope.checkAuth();
                            }
                        );
                    // $rootScope.checkAuth();
                    //$scope.getCsUploadURL();
                }; // end registerNewUser

                /* ---- NEW KEY UPLOAD */
                $scope.keyUploadFn = function () {
                    $rootScope.progressbar.start(); // <<<<<<<<<<<
                    if ($scope.resp && $scope.resp.error) {
                        $scope.resp.error = null;
                    }
                    $scope.asciiArmoredKeyError = null;

                    $log.info($scope.pgpPublicKeyUploadForm);

                    // check if provided text is valid OpenPGP key:
                    if ($rootScope.stringIsNullUndefinedOrEmpty($scope.pgpPublicKeyUploadForm.asciiArmored)) {
                        $scope.asciiArmoredKeyError = 'Key form is empty';
                        $timeout($rootScope.progressbar.complete(), 1000);
                        // $scope.$apply();
                        return;
                    }
                    try {
                        var readArmored = openpgp.key.readArmored(
                            $scope.pgpPublicKeyUploadForm.asciiArmored
                        );
                        $log.debug(readArmored);
                        $log.debug('readArmored.keys.length : ', readArmored.keys.length);
                        if (readArmored.keys.length === 0) {
                            $log.debug('readArmored.keys.length === 0');
                            $scope.asciiArmoredKeyError = "This is not valid OpenPGP key";
                            $timeout($rootScope.progressbar.complete(), 1000);
                            // $scope.$apply();
                            return;
                        }
                    } catch (error) {
                        $log.debug(error.message);
                        $scope.asciiArmoredKeyError =
                            "This is not valid OpenPGP key";
                        $timeout($rootScope.progressbar.complete(), 1000);
                        // $scope.$apply();
                        return;
                    }

                    GApi.executeAuth('pgpPublicKeyAPI', 'uploadNewPGPPublicKey',
                        $scope.pgpPublicKeyUploadForm // -> net.cryptonomica.forms.PGPPublicKeyUploadForm
                    )
                        .then(
                            function (resp) {
                                $scope.resp = resp;
                                // net.cryptonomica.returns.PGPPublicKeyUploadReturn :
                                // String messageToUser; // -> resp.messageToUser in alert
                                // PGPPublicKeyGeneralView pgpPublicKeyGeneralView;
                                console.log("resp: ");
                                $log.info(resp);
                                $timeout($rootScope.progressbar.complete(), 1000);
                            }, function (resp) {
                                console.log("error: ");
                                $log.info(resp);
                                $scope.resp = resp; // resp.message or resp.error.message - java.lang.Exception:
                                $timeout($rootScope.progressbar.complete(), 1000);
                            }
                        );

                }; // end of $scope.keyUpload

            }
        ]
    );
})();
