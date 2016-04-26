'use strict';

var app = angular.module('cryptonomica', [
    'ngCookies', // (1.4.9) https://code.angularjs.org/1.4.9/docs/api/ngCookies
    'ui.router', // (0.2.18 ) https://github.com/angular-ui/ui-router/tree/legacy
    'ui.uploader', // () https://github.com/angular-ui/ui-uploader
    'angular-google-gapi', // (1.0.0-beta.1) https://github.com/maximepvrt/angular-google-gapi/
    'yaru22.md', // https://github.com/yaru22/angular-md
    'ngProgress', // https://github.com/VictorBjelkholm/ngProgress
    'ngclipboard', // https://sachinchoolur.github.io/ngclipboard/
    'ngFileSaver', // http://alferov.github.io/angular-file-saver/#demo
    // 'angular-country-select', // http://alexcheng1982.github.io/angular-country-select/
    // 'ui.date', // https://github.com/angular-ui/ui-date 
    'puigcerber.countryPicker',
    // my:
    'cryptonomica.ui.router',
    'cryptonomica.controller'
]);


app.config(function ($sceDelegateProvider) {
    $sceDelegateProvider.resourceUrlWhitelist([
        'self', // Allow same origin resource loads
        'https://cryptonomica-test.appspot.com/**',
        'https://raw.githubusercontent.com/Cryptonomica/arbitration-rules/**' // works!
    ]);
});


app.run([
        'GAuth',
        'GApi',
        'GData',
        '$state',
        '$rootScope',
        '$window',
        '$sce',
        'ngProgressFactory',
        '$timeout',
        '$cookies',
        '$log',
        function (GAuth,
                  GApi,
                  GData,
                  $state,
                  $rootScope,
                  $window,
                  $sce,
                  ngProgressFactory,
                  $timeout,
                  $cookies,
                  $log) {

            $rootScope.gdata = GData;

            var CLIENT = '602780521094-jim3gi59m9d2clhsi2kvuvad59c9m57l.apps.googleusercontent.com';
            var BASE = 'https://cryptonomica-test.appspot.com/_ah/api';
            $rootScope.gaeProjectDomain = "cryptonomica-test.appspot.com";
            //
            GApi.load('notaryAPI', 'v1', BASE);             // 1
            GApi.load('pgpPublicKeyAPI', 'v1', BASE);       // 2
            GApi.load('uploadAPI', 'v1', BASE);             // 3
            GApi.load('userSearchAndViewAPI', 'v1', BASE);  // 4
            GApi.load('visitorAPI', 'v1', BASE);            // 5
            GApi.load('newUserRegistrationAPI', 'v1', BASE);// 6
            GApi.load('cryptonomicaUserAPI', 'v1', BASE);   // 7
            GApi.load('verificationAPI', 'v1', BASE);       // 8

            //
            GAuth.setClient(CLIENT);
            GAuth.setScope(
                'https://www.googleapis.com/auth/userinfo.email'
            );

            $rootScope.getUserData = function () {  // we use this in $rootScope.checkAuth
                $rootScope.progressbar.start();
                GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData') // async????
                    .then(
                        function (resp) {
                            $rootScope.currentUser = resp;
                            $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink); //;
                            $log.info("[app.js] $rootScope.currentUser: ");
                            $log.info($rootScope.currentUser);
                            // put data to cookies:
                            $cookies.put('userId', GData.getUserId());
                            $cookies.put('arbitrator', resp.arbitrator);
                            $cookies.put('cryptonomicaOfficer', resp.cryptonomicaOfficer);
                            $cookies.put('lawyer', resp.lawyer);
                            $cookies.put('notary', resp.notary);
                            $cookies.put('registeredCryptonomicaUser', resp.registeredCryptonomicaUser);
                            $cookies.put('userCurrentImageLink', $sce.trustAsResourceUrl(resp.userCurrentImageLink));

                            //
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (resp) {
                            // console.log("$rootScope.getUserData: error: ");
                            $log.error(resp);
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            };

            $rootScope.checkAuth = function () {  // functions to call if Auth successful or not

                GAuth.checkAuth().then(
                    function () {
                        $rootScope.getUserData(); // async?
                    },
                    function () {
                        //$rootScope.getUserData();
                    }
                );
            };

            //// --- define other functions:

            $rootScope.login = function () { // shows auth window from Google
                GAuth.login().then(
                    function () {
                        $rootScope.checkAuth();
                    });
            };

            $rootScope.logout = function () {
                GAuth.logout().then(
                    function () {
                        $rootScope.currentUser = null;
                        $state.go('home'); // go to home page
                    });
            };

            $rootScope.register = function () { // shows auth window from Google
                GAuth.login().then(
                    function () {
                        //$rootScope.getUserData();
                        $state.go('registration');
                    });

            };
            // =============== Function calls:

            $rootScope.progressbar = ngProgressFactory.createInstance();
            $rootScope.progressbar.setHeight('5px'); // any valid CSS value Eg '10px', '1em' or '1%'
            $rootScope.progressbar.setColor('orangered');
            //$rootScope.checkAuth(); // ? - call this in home controller ect.

        } // and main function
    ]
);
