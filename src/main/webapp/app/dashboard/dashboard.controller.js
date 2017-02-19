'use strict';

var controller_name = "cryptonomica.controller.dashboard";

var controller = angular.module(controller_name, []);

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
        function dashbCtrl($scope,
                           $rootScope,
                           $http,
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

            GAuth.checkAuth().then(
                function () {
                    $rootScope.getUserData(); // async?
                },
                function () {
                    //$rootScope.getUserData();
                }
            );

            console.log("dashboard controller test");
        } // -- end dashbCtrl
    ]
);