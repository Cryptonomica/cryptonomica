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
                    // templateUrl: 'app/landing/landing.html',
                    // resolve: {
                    //     goat: function (GoatService) {
                    //         return GoatService.getGoat();
                    //     }
                    // }
                })
                .state('promocode', {
                    url: '/promocode/{promocode}',
                    controller: 'cryptonomica.controller.promocode',
                    // templateUrl: 'app/home/home.html',
                    //
                    // templateUrl: 'app/landing/landing.html',
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
                .state('key', {
                    url: '/key/{fingerprint}',
                    controller: 'cryptonomica.controller.key',
                    templateUrl: 'app/key/key.html'
                })
                .state('verification', {
                    url: '/verification/{verificationWebSafeString}',
                    controller: 'cryptonomica.controller.verification',
                    templateUrl: 'app/verification/verification.html'
                })
                .state('termsOfService', {
                    url: '/termsOfService',
                    // controller: '',
                    templateUrl: 'app/termsOfService/termsOfService.html'
                })
                .state('privacyPolicy', {
                    url: '/privacyPolicy',
                    // controller: '',
                    templateUrl: 'app/termsOfService/privacyPolicy.html'
                })
                .state('showallarbitrators', {
                    url: '/showallarbitrators',
                    controller: 'cryptonomica.controller.showallarbitrators',
                    templateUrl: 'app/arbitrators/showallarbitrators.html'
                })
                .state('arbitration', {
                    url: '/arbitration',
                    controller: 'cryptonomica.controller.arbitration',
                    templateUrl: 'app/arbitration/arbitration.html'
                })
                .state('notaries', {
                    url: '/notaries',
                    controller: 'cryptonomica.controller.showAllNotaries',
                    templateUrl: 'app/notaries/showAllNotaries.html'
                })
                .state('proofOfExistence', {
                    url: '/proofOfExistence',
                    controller: 'cryptonomica.controller.proofOfExistence',
                    templateUrl: 'app/proofOfExistence/proofOfExistence.html'
                })
                .state('onlineVerification', {
                    url: '/onlineVerification/{fingerprint}',
                    controller: 'cryptonomica.controller.onlineVerification',
                    templateUrl: 'app/onlineVerification/onlineVerification.html'
                })
                .state('onlineVerificationView', {
                    url: '/onlineVerificationView/{fingerprint}',
                    controller: 'cryptonomica.controller.onlineVerificationView',
                    templateUrl: 'app/onlineVerification/onlineVerificationView.html'
                })
                .state('ethereumVerification', {
                    url: '/ethereumVerification/{fingerprint}',
                    controller: 'cryptonomica.controller.ethereumVerification',
                    templateUrl: 'app/ethereum/ethereumVerification.html'
                })
                .state('verifyEthAddress', {
                    url: '/verifyEthAddress/{fingerprint}',
                    controller: 'cryptonomica.controller.verifyEthAddress',
                    templateUrl: 'app/ethereum/verifyEthAddress.html'
                })
                .state('openPGPOnline', {
                    url: '/openPGPOnline',
                    controller: 'cryptonomica.controller.openPGPOnline',
                    templateUrl: 'app/openPGPOnline/openPGPOnline.html'
                })
                .state('openPGPSoftware', {
                    url: '/openPGPSoftware',
                    // controller: '',
                    templateUrl: 'app/openPGPSoftware/openPGPSoftware.html'
                })
                // .state('onlineVerificationVideo', {
                //     url: '/onlineVerificationVideo/{fingerprint}',
                //     controller: 'cryptonomica.controller.onlineVerification',
                //     templateUrl: 'app/onlineVerification/onlineVerificationVideo.html'
                // })
                .state('test', {
                    url: '/test',
                    controller: 'cryptonomica.controller.test',
                    templateUrl: 'app/test/test.html'
                })
                .state('dashboard', {
                    url: '/dashboard',
                    controller: 'cryptonomica.controller.dashboard',
                    templateUrl: 'app/dashboard/dashboard.html'
                })
                .state('stats', {
                    url: '/stats',
                    controller: 'cryptonomica.controller.stats',
                    templateUrl: 'app/stats/stats.html'
                })
        } // end of function ($stateProvider)..
    ]); // end of .config
