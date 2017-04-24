'use strict';

/**
 * Home controller
 */

var controller_name = "cryptonomica.controller.home";

var controller = angular.module(controller_name, []);

controller.controller(controller_name, [
        '$scope',
        '$rootScope',
        '$http',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$cookies',
        '$timeout',
        'ngProgressFactory',
        function homeCtrl($scope,
                          $rootScope,
                          $http,
                          GApi,
                          GAuth,
                          GData,
                          $state,
                          $cookies,
                          $timeout,
                          ngProgressFactory) {

            GAuth.checkAuth().then(
                function () {
                    $rootScope.getUserData(); // async?
                    if ($rootScope.currentUser.registeredCryptonomicaUser) {
                        $state.go('registration'); // TODO: change to user profile page
                    } else {
                        $state.go('landing');
                    }
                },
                function () {
                    //$rootScope.getUserData();
                }
            );

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


        } // end function homeCtl
    ]
);