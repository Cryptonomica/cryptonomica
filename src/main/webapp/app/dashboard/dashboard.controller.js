'use strict';

var controller_name = "cryptonomica.controller.dashboard";

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
        '$log',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$cookies',
        '$timeout',
        function dashbCtrl($scope,
                           $rootScope,
                           $http,
                           $log,
                           GApi,
                           GAuth,
                           GData,
                           $state,
                           $cookies,
                           $timeout) {
            //// check if user is registered Cryptonomica member
            ////GAuth.checkAuth();
            //$rootScope.getUserData();
            //if (!$rootScope.currentUser || !$rootScope.currentUser.loggedIn) {
            //    $state.go('home');
            //}
            if (!$rootScope.currentUser) {
                GAuth.checkAuth().then(
                    function () {
                        $rootScope.getUserData(); // async?
                    },
                    function () {
                        $log.debug('User not logged in');
                        $state.go('landing');
                    }
                );
            }


            console.log("dashboard controller test");
        } // -- end dashbCtrl
    ]
);