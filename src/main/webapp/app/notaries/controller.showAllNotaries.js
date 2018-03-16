'use strict';

/**
 * showAllNotaries controller
 */

var controller_name = "cryptonomica.controller.showAllNotaries";

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
        '$log',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$cookies',
        // 'ngProgressFactory',
        '$timeout',
        function showAllNotariesCtrl($scope,
                                     $rootScope,
                                     $http,
                                     $log,
                                     GApi,
                                     GAuth,
                                     GData,
                                     $state,
                                     $cookies,
                                     // ngProgressFactory,
                                     $timeout) {
            // define functions:
            $scope.searchForNotaries = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                GApi.execute('visitorAPI', 'searchForNotaries', $scope.form)
                    .then(
                        function (resp) {
                            $scope.resp = resp; // net.cryptonomica.returns.NotaryGeneralView array in resp.items
                            console.log("resp: ");
                            console.log(resp);
                            // $rootScope.working = false;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            console.log("error: ");
                            console.log(error);
                            $scope.error = error; // resp.message or resp.error.message - java.lang.Exception:
                            // $rootScope.working = false;
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