(function () { // < (!) use this pattern in all controllers

    'use strict';

    /**
     * Promocode controller
     */

    var controller_name = "cryptonomica.controller.promocode";
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
        // '$http',
        '$log',
        // '$sce',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$stateParams',
        '$cookies',
        // '$timeout',
        function promocodeCtrl($scope,
                               $rootScope,
                               // $http,
                               $log,
                               // $sce,
                               GApi,
                               GAuth,
                               GData,
                               $state,
                               $stateParams,
                               $cookies
                               // $timeout
        ) {

            $log.debug(controller_name, "started"); //
            // $log.debug('$state');
            // $log.debug($state);
            // $timeout($rootScope.progressbar.complete(), 1000);

            if (!$rootScope.stringIsNullUndefinedOrEmpty($stateParams.promocode)) {
                // https://docs.angularjs.org/api/ngCookies/service/$cookies
                $cookies.put('promocode', $stateParams.promocode);
                $rootScope.promoCodeMessage = $cookies.get('promocode');
                // $rootScope.$apply(); // < not needed here
            }

            // if error, see:
            // https://stackoverflow.com/questions/28470222/state-go-is-not-working-from-separate-controller
            $state.go('home');

        } // end function  promocodeCtrl

    ]);

})();

