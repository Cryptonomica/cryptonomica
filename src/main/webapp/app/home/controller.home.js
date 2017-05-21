'use strict';

/**
 * Home controller
 */

var controller_name = "cryptonomica.controller.home";

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
        // '$http',
        '$log',
        '$sce',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        // '$cookies',
        '$timeout',
        function homeCtrl($scope,
                          $rootScope,
                          // $http,
                          $log,
                          $sce,
                          GApi,
                          GAuth,
                          GData,
                          $state,
                          // $cookies,
                          $timeout) {

            if (!$rootScope.currentUser) {
                $rootScope.progressbar.start();
                GAuth.checkAuth().then(
                    function () {
                        GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData')
                            .then(
                                function (resp) {
                                    $rootScope.currentUser = resp;
                                    $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink);
                                    $log.debug("[controller.home.js] $rootScope.currentUser: ");
                                    $log.debug($rootScope.currentUser);
                                    if ($rootScope.currentUser) {
                                        if ($rootScope.currentUser.registeredCryptonomicaUser) {
                                            $timeout($rootScope.progressbar.complete(), 1000);
                                            $state.go('viewprofile', {userId: $rootScope.currentUser.userId});
                                        } else {
                                            $timeout($rootScope.progressbar.complete(), 1000);
                                            $state.go('registration');
                                        }
                                    } else {
                                        $log.debug('User not logged in');
                                        $timeout($rootScope.progressbar.complete(), 1000);
                                        $state.go('landing');
                                    }
                                }, function (error) {
                                    $log.error(error);
                                    $timeout($rootScope.progressbar.complete(), 1000);
                                }
                            );
                    },
                    function () {
                        $timeout($rootScope.progressbar.complete(), 1000);
                        $log.debug('GAuth.checkAuth() get error:')
                    }
                );
            } else {
                if ($rootScope.currentUser.registeredCryptonomicaUser) {
                    $timeout($rootScope.progressbar.complete(), 1000);
                    $state.go('viewprofile', {userId: $rootScope.currentUser.userId});
                } else {
                    $timeout($rootScope.progressbar.complete(), 1000);
                    $state.go('registration');
                }
            }

            /*
             GAuth.checkAuth().then(
             function () {
             $rootScope.getUserData();
             if ($rootScope.currentUser && $rootScope.currentUser.registeredCryptonomicaUser) {
             $state.go('registration'); // TODO: change to user profile page
             } else {
             $state.go('landing');
             }
             },
             function () {
             //$rootScope.getUserData();
             }
             );*/

            /*
             $scope.searchForNotaries = function () {
             $rootScope.progressbar.start(); // <<<<<<<<<<<
             GApi.execute('visitorAPI', 'searchForNotaries', $scope.form)
             .then(
             function (resp) {
             $scope.resp = resp; // net.cryptonomica.returns.NotaryGeneralView array in resp.items
             console.log("resp: ");
             console.log(resp);
             $rootScope.working = false;
             $timeout($rootScope.progressbar.complete(), 1000);
             }, function (resp) {
             console.log("error: ");
             console.log(resp);
             $scope.resp = resp; // resp.message or resp.error.message - java.lang.Exception:
             $rootScope.working = false;
             $timeout($rootScope.progressbar.complete(), 1000);
             }
             );
             }; // end searchForNotaries

             $scope.clearSearchForNotaries = function () {
             $scope.resp = null;
             }; // clearSearchForNotaries
             */

        } // end function homeCtl
    ]
)
;