(function () {

    'use strict';

    /**
     * showallarbitrators controller
     */

    var controller_name = "cryptonomica.controller.showallarbitrators";

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
            '$cookies',
            '$timeout',
            '$log',
            function showallarbitratorsCtrl($scope,
                                            $rootScope,
                                            $http,
                                            GApi,
                                            GAuth,
                                            GData,
                                            $state,
                                            $cookies,
                                            $timeout,
                                            $log) {
                $log.debug(controller_name, "started"); //
                $timeout($rootScope.progressbar.complete(), 1000);

                /* --- Alerts */
                $scope.alertDanger = null;  // red
                $scope.alertWarning = null; // yellow
                $scope.alertInfo = null;    // blue
                $scope.alertSuccess = null; // green
                $scope.alertMessage = {}; // grey

                $scope.setAlertDanger = function (message) {
                    $scope.alertDanger = message;
                    $log.debug("$scope.alertDanger:", $scope.alertDanger);
                    // $scope.$apply(); not here
                    $scope.goTo("alertDanger");
                };

                $scope.setAlertWarning = function (message) {
                    $scope.alertWarning = message;
                    $log.debug("$scope.alertWarning:", $scope.alertWarning);
                    // $scope.$apply();
                    $scope.goTo("alertWarning");
                };

                $scope.setAlertInfo = function (message) {
                    $scope.alertInfo = message;
                    $log.debug("$scope.alertInfo:", $scope.alertInfo);
                    // $scope.$apply();
                    $scope.goTo("alertInfo");
                };

                $scope.setAlertSuccess = function (message) {
                    $scope.alertSuccess = message;
                    $log.debug("$scope.alertSuccess:", $scope.alertSuccess);
                    // $scope.$apply();
                    $scope.goTo("alertSuccess");
                };

                $scope.setAlertMessage = function (message, header) {
                    $scope.alertMessage = {};
                    $scope.alertMessage.header = header;
                    $scope.alertMessage.message = message;
                    $log.debug("$scope.alertMessage:", $scope.alertMessage);
                    // $scope.$apply();
                    $scope.goTo("alertMessage");
                };
                /* ---- */

                //
                GAuth.checkAuth().then(
                    function () {
                        $rootScope.getUserData(); // async?
                    },
                    function () {
                        //$rootScope.getUserData();
                        $scope.setAlertDanger("Your have to be registered and logged in to use this service")
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
                            }, function (error) {
                                console.log("error: ");
                                // console.log(resp);
                                // $scope.resp = resp; // resp.message or resp.error.message - java.lang.Exception:
                                $scope.setAlertDanger(error);
                                $rootScope.working = false;
                                $timeout($rootScope.progressbar.complete(), 1000);
                            }
                        );
                }; // end showAllArbitrators

                $scope.showAllArbitrators(); // <<<<<< get data

            } // end function showallarbitratorsCtrl
        ]
    );

})();
