'use strict';

var controller_name = "cryptonomica.controller.onlineVerification";

var controller = angular.module(controller_name, []);

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
    function onlineVerCtrl($scope,    // <- name for debugging
                           $rootScope,
                           $http,
                           GApi,
                           GAuth,
                           GData,
                           $state,
                           $log,
                           $cookies,
                           $timeout) {

        $log.info("cryptonomica.controller.onlineVerification started");

        // --- get user data
        GAuth.checkAuth().then(
            function () {
                $rootScope.getUserData(); // async?
            },
            function () {
                //$rootScope.getUserData();
                $log.error("[cryptonomica.controller.onlineVerification] GAuth.checkAuth() - unsuccessful");
                $scope.alert = "User not logged in";
            }
        );
        // -----------------
        $scope.todayDate = new Date();
        $scope.videoUploadKey = null;
        $scope.getVideoUploadKey = function () {
            GApi.executeAuth('onlineVerificationAPI', 'getVideoUploadKey')
                .then(
                    function (resp) {
                        console.log("$scope.getVideoUploadKey resp:");
                        console.log(resp);

                        $scope.videoUploadKey = resp.message;

                    }, function (error) {
                        console.log("$scope.etVideoUploadKey error:");
                        console.log(error);
                        $scope.error = error.message;
                    }
                )
        };
        $scope.getVideoUploadKey();
// ------------------------------------------------------------------------------

    }
]);

