(function () { // < (!) use this pattern in all controllers

    'use strict';

    /**
     * Home controller
     */

    var controller_name = "cryptonomica.controller.home";
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
        // '$stateParams',
        // '$cookies',
        '$timeout',
        function homeCtrl($scope,
                          $rootScope,
                          // $http,
                          $log,
                          // $sce,
                          GApi,
                          GAuth,
                          GData,
                          $state,
                          // $stateParams,
                          // $cookies,
                          $timeout) {

            $log.debug(controller_name, "started"); //
            // $log.debug('$state');
            // $log.debug($state);

            // (!!!) for debug:
            // see:
            // https://stackoverflow.com/questions/13743058/how-do-i-access-the-scope-variable-in-browsers-console-using-angularjs
            // window.my$scope = $scope;

            // $timeout($rootScope.progressbar.complete(), 1000);

            // TODO: for test, remove
            // hljs.initHighlightingOnLoad();
            // > works better:
            // $('pre code').each(function (i, block) {
            //     hljs.highlightBlock(block);
            // });

            // if ($rootScope.currentUser) {
            //     // show menu for registered user
            // } else {
            //     // $rootScope.checkAuth();
            // }

            $timeout($rootScope.progressbar.complete(), 1000);

        } // end function homeCtl

    ]);

})();

