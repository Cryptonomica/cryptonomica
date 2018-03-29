(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.test";

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

            $log.info("cryptonomica.controller.test started");

            $scope.customer = {
                name: null,
                street: null
            };
            $scope.customer.name = "Jonh";
            $scope.customer.street = "Doe";

        }]);

})();
