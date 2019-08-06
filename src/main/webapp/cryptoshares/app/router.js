(function () {

    'use strict';

// console.log("router.js");

    /**
     * see example:
     * https://github.com/maximepvrt/angular-google-gapi/blob/gh-pages/app/router.js
     */

    let router = angular.module('app.ui.router', []);

    router.config(['$urlRouterProvider',
        function ($urlRouterProvider) {
            $urlRouterProvider.otherwise("/");
        }
    ]);

    router.config(['$stateProvider', function ($stateProvider) {

        $stateProvider
            .state('home', {
                url: '/{contractNumber}', //  = $stateParams.contractNumber;
                controller: 'app.home',
                templateUrl: 'app/home/home.html'
            })

    } // end of function ($stateProvider)..

    ]);

})();