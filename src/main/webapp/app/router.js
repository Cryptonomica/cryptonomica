'use strict';

/**
 * see:
 * https://github.com/maximepvrt/angular-google-gapi/blob/gh-pages/app/router.js
 */

var router = angular.module('cryptonomica.ui.router', []);

router
    .config(['$urlRouterProvider',
            function ($urlRouterProvider) {
                $urlRouterProvider.otherwise("/");
            }
        ]
    );
router
    .config(['$stateProvider',
        function ($stateProvider) {
            $stateProvider

                .state('home', {
                    url: '/',
                    controller: 'cryptonomica.controller.home',
                    templateUrl: 'app/home/home.html',
                    // resolve: {
                    //     goat: function (GoatService) {
                    //         return GoatService.getGoat();
                    //     }
                    // }
                })

                .state('registration', {
                    url: '/registration',
                    controller: 'cryptonomica.controller.registration',
                    templateUrl: 'app/registration/registration.html'
                })

                .state('search', {
                    url: '/search',
                    controller: 'cryptonomica.controller.search',
                    templateUrl: 'app/search/search.html'
                })

                .state('viewprofile', {
                    url: '/viewprofile/{userId}',
                    controller: 'cryptonomica.controller.viewprofile',
                    templateUrl: 'app/viewprofile/viewprofile.html'
                })

                .state('showkey', {
                    url: '/showkey/{websafestring}',
                    controller: 'cryptonomica.controller.showkey',
                    templateUrl: 'app/showkey/showkey.html'
                })

                .state('verification', {
                    url: '/verification/{verificationWebSafeString}',
                    controller: 'cryptonomica.controller.verification',
                    templateUrl: 'app/verification/verification.html'
                })

                .state('arbitrationrules', {
                    url: '/arbitrationrules',
                    // controller: '',
                    templateUrl: 'app/arbitrationrules/arbitration-rules.html',
                    // resolve: {
                    //     arbitrationRulesURL:
                    //         'https://raw.githubusercontent.com/Cryptonomica/arbitration-rules/master/Arbitration_Rules/IACC/IACC-Arbitration-Rules.EN.signed.md'
                    // }
                })

                .state('dashboard', {
                    url: '/dashboard',
                    controller: 'cryptonomica.controller.dashboard',
                    templateUrl: 'app/dashboard/dashboard.html'
                })

        } // end of function ($stateProvider)..
    ]); // end of .config
