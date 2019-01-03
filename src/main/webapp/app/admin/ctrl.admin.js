(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.admin"; // add to controller.js

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
        '$log',
        '$cookies',
        '$timeout',
        function testCtrl($scope,    // <- name for debugging
                          $rootScope,
                          $http,
                          GApi,
                          GAuth,
                          GData,
                          $state,
                          $log,
                          $cookies,
                          $timeout) {

            $log.debug(controller_name, "started"); //
            $log.debug('$state');
            $log.debug($state);
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

            /* ---- main functions --- */

            $scope.onlineVerifications = null;
            $scope.getWaitingForVerification = function () {
                $log.debug('$scope.getWaitingForVerification started');
                $rootScope.progressbar.start(); // <<<<<<<<<<<

                GApi.executeAuth('onlineVerificationAPI', 'getWaitingForVerification')
                    .then(function (result) { // OnlineVerificationsList.java
                        $scope.onlineVerifications = result;
                        $log.debug("$scope.onlineVerifications:");
                        $log.debug($scope.onlineVerifications);
                        // $scope.$apply(); // <<< not needed here
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
                    .catch(function (error) {
                        $log.error(error);
                        $scope.setAlertMessage("Error", error.message);
                        // $scope.$apply(); // <<< not needed here
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }; // end of $scope.getWaitingForVerification() ;

            // RUN:
            $scope.getWaitingForVerification();


        }]);

})();
