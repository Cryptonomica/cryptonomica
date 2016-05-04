(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.registration";

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
            function regCtrl($scope,    // TODO: do i need a name for this function?
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
                GAuth.checkAuth().then(
                    function () {
                        $rootScope.getUserData(); // async?
                    },
                    function () {
                        //$rootScope.getUserData();
                        $log.error("[controller.registration.js] GAuth.checkAuth() - unsuccessful");
                        $scope.alert = "User not logged in";
                    }
                );
                //
                $scope.dateOptions = {changeYear: true, changeMonth: true, yearRange: '1900:-0'};

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
                };

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
                    GApi.executeAuth('uploadAPI', 'getCsUploadURL')
                        .then(
                            function (resp) {
                                $scope.imageUploadUrl = $sce.trustAsResourceUrl(resp.imageUploadUrl);
                                $scope.imageUploadKey = resp.imageUploadKey;
                                console.log("$scope.getCsUploadURL resp:");
                                console.log(resp);
                                $scope.btn_upload();
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
                            'imageUploadKey': $scope.imageUploadKey
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
                    $log.info($scope.regForm);
                    GApi.executeAuth('newUserRegistrationAPI', 'registerNewUser', $scope.regForm)
                        .then(
                            function (resp) {
                                $scope.resp = resp; // net.cryptonomica.returns.NotaryGeneralView array in resp.items
                                $rootScope.currentUser = resp.userProfileGeneralView;
                                console.log("resp: ");
                                $log.info(resp);
                            }, function (resp) {
                                console.log("error: ");
                                $log.info(resp);
                                $scope.resp = resp; // resp.message or resp.error.message - java.lang.Exception:
                            }
                        );
                    $rootScope.checkAuth();
                    //$scope.getCsUploadURL();
                }; // end registerNewUser
            }
        ]
    );
})();