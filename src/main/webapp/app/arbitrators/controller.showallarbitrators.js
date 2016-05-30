'use strict';

/**
 * showallarbitrators controller
 */

var controller_name = "cryptonomica.controller.showallarbitrators";

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
        //'ngProgressFactory',
        function showallarbitratorsCtrl($scope,
                                        $rootScope,
                                        $http,
                                        GApi,
                                        GAuth,
                                        GData,
                                        $state,
                                        $cookies,
                                        $timeout
                                        //ngProgressFactory
        ) {

            GAuth.checkAuth().then(
                function () {
                    $rootScope.getUserData(); // async?
                },
                function () {
                    //$rootScope.getUserData();
                }
            );

            $scope.showAllArbitrators = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<
                GApi.executeAuth('arbitratorsAPI', 'showAllArbitrators')
                    .then(
                        function (resp) {
                            $scope.resp = resp; // ArrayList<ArbitratorGeneralView>
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
            }; // end showAllArbitrators

            $scope.showAllArbitrators(); // <<<<<< get data 

        } // end function showallarbitratorsCtrl
    ]
);