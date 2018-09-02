(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.openPGPOnline";

// add to:
// index.html +
// controller.js +
// router.js + ( url: '/openPGPOnline')

    var controller = angular.module(controller_name, []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
    controller.config(function ($logProvider) {
            // $logProvider.debugEnabled(false);
            $logProvider.debugEnabled(true);
        }
    );

    controller.controller(controller_name, [
        '$scope',
        '$sce',
        '$rootScope',
        '$http',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        // 'uiUploader',
        '$log',
        '$cookies',
        '$timeout',
        '$window',
        '$stateParams',
        function openPGPOnlineCtrl($scope,    // <- name for debugging
                                   $sce,
                                   $rootScope,
                                   $http,
                                   GApi,
                                   GAuth,
                                   GData,
                                   $state,
                                   // uiUploader,
                                   $log,
                                   $cookies,
                                   $timeout,
                                   $window,
                                   $stateParams) {

            /*
            * in the view add id with controller name to parent div:
            * <div id="openPGPOnlineCtrl">
            * use this id to access $scope in browser console:
            * angular.element(document.getElementById('openPGPOnlineCtrl')).scope();
            * for example: for example: $scope = angular.element(document.getElementById('openPGPOnlineCtrl')).scope();
            * */

            $log.debug(controller_name, "started"); //
            $timeout($rootScope.progressbar.complete(), 1000);

            // --- Alerts:
            $scope.alertDanger = null;  // red
            $scope.alertWarning = null; // yellow
            $scope.alertInfo = null;    // blue
            $scope.alertSuccess = null; // green
            //
            $scope.error = {};
            //

        } // end of Ctrl

    ]);


})();
