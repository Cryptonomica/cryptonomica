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

            /* GAuth.checkAuth().then(
             function () {
             $rootScope.getUserData(); // async?
             $log.info("[controller.viewprofile.js] $rootScope.getUserData() - success");
             },
             function () {
             //$rootScope.getUserData();
             $log.error("[controller.viewprofile.js] $rootScope.getUserData() - error");
             }
             );*/
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

            var viewprofile = function () {
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
                                }

                            } // end formatting asciiArmored
                            $scope.userProfileGeneralView = resp; // net.cryptonomica.returns.UserProfileGeneralView
                            $log.info("controller.viewprofile: viewprofile(): resp: ");
                            $log.info(resp);
                        },
                        function (resp) {
                            $log.error("error: ");
                            $log.error(resp);
                            $scope.error = resp; // resp.message or resp.error.message - java.lang.Exception:
                            $scope.alert = null;
                        }
                    );
            }; // end $scope.viewprofile
            viewprofile();
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
                }
                else {
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
        } // end: viewCtrl
    ]
);