(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.webinars"; // add to controller.js

    const debugEnabled = true;
    // const debugEnabled = false;

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
        // 'GAuth',
        // 'GData',
        '$state',
        '$stateParams',
        '$log',
        // '$cookies',
        '$timeout',
        function testCtrl($scope,    // <- name for debugging
                          $rootScope,
                          $http,
                          GApi,
                          // GAuth,
                          // GData,
                          $state,
                          $stateParams,
                          $log,
                          // $cookies,
                          $timeout) {


            (function forDebugOnly() {
                if (debugEnabled) {
                    $log.debug(controller_name, "started");
                    window.scope = $scope;
                    window.rootScope = $rootScope;
                    if ($stateParams) {
                        $log.debug("$stateParams:");
                        $log.debug($stateParams);
                    }
                }
            })();

            (function setAlerts() {
                $scope.alertDanger = null;  // red
                $scope.alertWarning = null; // yellow
                $scope.alertInfo = null;    // blue
                $scope.alertSuccess = null; // green
                $scope.alertMessage = {}; // grey

                $scope.setAlertDanger = function (message) {
                    $scope.alertDanger = message;
                    $log.error("$scope.alertDanger:", $scope.alertDanger);
                    // $scope.$apply(); not here
                    $rootScope.goTo("alertDanger");
                };

                $scope.setAlertWarning = function (message) {
                    $scope.alertWarning = message;
                    $log.debug("$scope.alertWarning:", $scope.alertWarning);
                    // $scope.$apply();
                    $rootScope.goTo("alertWarning");
                };

                $scope.setAlertInfo = function (message) {
                    $scope.alertInfo = message;
                    $log.debug("$scope.alertInfo:", $scope.alertInfo);
                    // $scope.$apply();
                    $rootScope.goTo("alertInfo");
                };

                $scope.setAlertSuccess = function (message) {
                    $scope.alertSuccess = message;
                    $log.debug("$scope.alertSuccess:", $scope.alertSuccess);
                    // $scope.$apply();
                    $rootScope.goTo("alertSuccess");
                };

                $scope.setAlertMessage = function (message, header) {
                    $scope.alertMessage = {};
                    $scope.alertMessage.header = header;
                    $scope.alertMessage.message = message;
                    $log.debug("$scope.alertMessage:", $scope.alertMessage);
                    // $scope.$apply();
                    $rootScope.goTo("alertMessage");
                };
            })();

            if ($stateParams && $stateParams.webinarCode) {
                $scope.webinarCode = $stateParams.webinarCode;
            }

            $timeout($rootScope.progressbar.complete(), 1000);
        }]);
})();
