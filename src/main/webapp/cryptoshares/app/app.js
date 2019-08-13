(function () {

    'use strict';

// console.log("app.js");

    var app = angular.module('app', [
            'ui.router',
            // 'ui.date', // https://github.com/angular-ui/ui-date
            'ngProgress', // https://github.com/VictorBjelkholm/ngProgress
            'ngClipboard', // https://github.com/nico-val/ngClipboard
            // ---- App :
            'app.ui.router',
            'app.controllers',
            'app.directives'
        ]
    );

    app.config(function ($sceDelegateProvider) {
        $sceDelegateProvider.resourceUrlWhitelist([
            'self' // Allow same origin resource loads
        ]);
    });

// // see:
// // https://stackoverflow.com/questions/41460772/angularjs-how-to-remove-bang-prefix-from-url/41461312
//     app.config(['$locationProvider', function ($locationProvider) {
//         $locationProvider.hashPrefix('');
//     }]);

    app.run([
        '$state',
        '$rootScope',
        '$window',
        '$sce',
        'ngProgressFactory',
        '$timeout', // for ngProgressFactory
        '$anchorScroll',
        '$location',
        '$log',
        function (
            $state,
            $rootScope,
            $window,
            $sce,
            ngProgressFactory,
            $timeout, // for ngProgressFactory
            $anchorScroll,
            $location,
            $log) {

            // console.log("application started");
            $rootScope.webAppVersion = "0.1.0";
            $rootScope.webAppLastChange = "2019-08-13";
            $rootScope.production = false;
            $rootScope.devModeOn = false;
            // $rootScope.devModeOn = true;

            if ($rootScope.devModeOn) {
                window.myRootScope = $rootScope;
            }

            console.log("CryptoShares webapp, version", $rootScope.webAppVersion, "of", $rootScope.webAppLastChange);

            // $rootScope.labelText = "Ropsten only,  ver. " + $rootScope.webAppVersion + "  "; //  + $rootScope.webAppLastChange;
            // $rootScope.labelText = $rootScope.currentNetwork.networkName;

            $rootScope.showLabel = true;

            $rootScope.progressbar = ngProgressFactory.createInstance();
            $rootScope.progressbar.setHeight('7px'); // any valid CSS value Eg '10px', '1em' or '1%'
            // $rootScope.progressbar.setColor('#60c8fa');
            $rootScope.progressbar.setColor('black');
            // $rootScope.progressbar.setColor('red');
            // >>>>
            $rootScope.progressbar.start();
            // $timeout($rootScope.progressbar.complete(), 1000);

            /* === Utility functions === */

            $rootScope.goTo = function (id) {
                // set the location.hash to the id of the element you wish to scroll to.
                $location.hash(id);
                $anchorScroll();
            };

            $rootScope.unixTimeFromDate = function (date) {
                let result = null;
                if (date instanceof Date) {
                    result = Math.round(date.getTime() / 1000);
                    // $log.debug("unix time calculated:", result);
                } else {
                    $log.error(date, "is not a date");
                }
                return result;
            };

            $rootScope.dateFromUnixTime = function (unixTime) {
                unixTime = parseInt(unixTime);
                return new Date(unixTime * 1000);
            };

            $rootScope.dateToDateStr = function (date) {
                let result = null;
                if (date instanceof Date) {
                    result =
                        date.getFullYear() + "-"
                        + date.getMonth() + "-"
                        + date.getDate();
                } else {
                    $log.error(date, "is not a date");
                }
                return result;
            };

            /*
            * see:
            * https://stackoverflow.com/a/36099084/1697878
            * */
            $rootScope.secondsToDaysHoursMinutesSecondsStr = function (seconds) {

                seconds = parseInt(seconds);

                let days = Math.floor(seconds / (3600 * 24));
                seconds -= days * 3600 * 24;
                let hrs = Math.floor(seconds / 3600);
                seconds -= hrs * 3600;
                let mnts = Math.floor(seconds / 60);
                seconds -= mnts * 60;
                let result = days + " days, " + hrs + " hrs, " + mnts + " min, " + seconds + " sec";
                // console.log(days+" days, "+hrs+" Hrs, "+mnts+" Minutes, "+seconds+" Seconds");
                return result;
            };


            /* === Ethereum  === */
            // example from:
            // https://github.com/trufflesuite/truffle-artifactor#artifactorgenerateoptions-networks
            $rootScope.networks = {
                1: {
                    "networkName": "Main Ethereum Network",
                    "etherscanLinkPrefix": "https://etherscan.io/",
                    "etherscanApiLink": "https://api.etherscan.io/"
                },
                3: {
                    "networkName": "Ropsten TestNet",
                    "etherscanLinkPrefix": "https://ropsten.etherscan.io/",
                    "etherscanApiLink": "https://api-ropsten.etherscan.io/"
                },
                4: {        //
                    "networkName": "Rinkeby TestNet",
                    "etherscanLinkPrefix": "https://rinkeby.etherscan.io/",
                    "etherscanApiLink": "https://api-rinkeby.etherscan.io/"
                },
                42: {        //
                    "networkName": "Kovan TestNet",
                    "etherscanLinkPrefix": "https://kovan.etherscan.io/",
                    "etherscanApiLink": "https://api-kovan.etherscan.io/"
                }
            }; // end of $rootScope.networks

        }]);

})();