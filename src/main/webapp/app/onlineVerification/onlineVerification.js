(function () {

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
        function onlineVerCrtr($scope,    // <- name for debugging
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

// ------------------------------------------------------------------------------
            
// ------------------------------------------------------------------------------

        }]);

})();
