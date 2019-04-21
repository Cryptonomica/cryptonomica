(function () {

    'use strict';

// console.log("app.js");

    var app = angular.module('app', [
            'ui.router',
            // 'yaru22.md', // https://github.com/yaru22/angular-md
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
        '$anchorScroll',
        '$location',
        '$log',
        function (
            $state,
            $rootScope,
            $window,
            $sce,
            $anchorScroll,
            $location,
            $log) {

            // console.log("application started");
            $rootScope.webAppVersion = "0.3.2.dev";
            $rootScope.webAppLastChange = "2019-04-21";
            console.log("eth-sign webapp, version", $rootScope.webAppVersion, "of", $rootScope.webAppLastChange);

            /* === Utility functions === */

            $rootScope.goTo = function (id) {
                // set the location.hash to the id of the element you wish to scroll to.
                $location.hash(id);
                $anchorScroll();
            };

            $rootScope.unixTimeFromDate = function (date) {
                return Math.round(date.getTime() / 1000);
            };

            $rootScope.dateFromUnixTime = function (unixTime) {
                return new Date(unixTime * 1000);
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