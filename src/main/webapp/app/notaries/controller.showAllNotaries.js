'use strict';

/**
 * showAllNotaries controller
 */

var controller_name = "cryptonomica.controller.showAllNotaries";

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
        function showAllNotariesCtrl($scope,
                                     $rootScope,
                                     $http,
                                     GApi,
                                     GAuth,
                                     GData,
                                     $state,
                                     $cookies,
                                     $timeout,
                                     ngProgressFactory) {
            // define functions:
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

            // --- user data
            GAuth.checkAuth().then(
                function () {
                    $rootScope.getUserData(); // async?
                    $scope.searchForNotaries();
                },
                function () {
                    //$rootScope.getUserData();
                }
            );

            $scope.clearSearchForNotaries = function () {
                $scope.resp = null;
            }; // clearSearchForNotaries

            $scope.refreshNotaries = function () {
                $scope.clearSearchForNotaries();
                $scope.searchForNotaries();
            };
            
        } // end function showAllNotariesCtrl
    ]
);