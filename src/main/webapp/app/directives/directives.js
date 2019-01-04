(function () {

    'use strict';

    var d = angular.module('cryptonomica.directives', []);

    // -----------------------------------------------------------------------------------------
    /* see:
    // https://stackoverflow.com/questions/17470790/how-to-use-a-keypress-event-in-angularjs */
    d.directive('myEnter', function () {
        return function (scope, element, attrs) {
            element.bind("keydown keypress", function (event) {
                if (event.which === 13) {
                    scope.$apply(function () {
                        scope.$eval(attrs.myEnter);
                    });
                    // see: https://developer.mozilla.org/en-US/docs/Web/API/Event/preventDefault
                    event.preventDefault();
                }
            });
        };
    });

    // -----------------------------------------------------------------------------------------
    d.directive('leftSidebar', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <left-sidebar></left-sidebar>
            templateUrl: 'app/directives/leftSidebar.html'
        };
    });
    // -----------------------------------------------------------------------------------------
    d.directive('rightSidebar', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <right-sidebar></right-sidebar>
            templateUrl: 'app/directives/rightSidebar.html'
        };
    });
    // ----------------------------------------------------------------------------------------
    d.directive('topHeaderMenu', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <top-header-menu></top-header-menu>
            templateUrl: 'app/directives/topHeaderMenu.html'
        };
    });

    // ------------------------------------------------------------------------------
    d.directive('alerts', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <alerts></alerts>
            templateUrl: 'app/directives/alerts.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('footerMain', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <footer-main></footer-main>
            // replace: 'true', // >> error
            templateUrl: 'app/directives/footerMain.html'
        };
    });

    /* ---- */

    // ------------------------------------------------------------------------------
    d.directive('navigationMenu', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <navigation-menu></navigation-menu>
            templateUrl: 'app/directives/navigationMenu.html'
        };
    });

    // ------------------------------------------------------------------------------
    d.directive('onlineVerificationTerms', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <online-verification-terms></online-verification-terms>
            templateUrl: 'app/directives/onlineVerificationTerms.html'
        };
    });
    // ------------------------------------------------------------------------------
    // d.directive('recordVideoJs', function () {
    //     return {
    //         restrict: 'EA', //E = element, A = attribute, C = class, M = comment
    //         // <record-video-js></record-video-js>
    //         templateUrl: 'app/directives/recordVideoJs.html'
    //     };
    // });
    // ------------------------------------------------------------------------------
    d.directive('textForCamera', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <text-for-camera></text-for-camera>
            templateUrl: 'app/directives/textForCamera.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('onlineVerificationVideo', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <online-verification-video></online-verification-video>
            templateUrl: 'app/directives/onlineVerificationVideo.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('smsCheck', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <sms-check></sms-check>
            templateUrl: 'app/directives/smsCheck.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('stripePaymentForOnlineKeyVerification', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <stripe-payment-for-online-key-verification></stripe-payment-for-online-key-verification>
            templateUrl: 'app/directives/stripePaymentForOnlineKeyVerification.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('stripePaymentVerificationCode', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <stripe-payment-verification-code></stripe-payment-verification-code>
            templateUrl: 'app/directives/stripePaymentVerificationCode.html'
        };
    });
    // ------------------------------------------------------------------------------
    /* see:
     https://veamospues.wordpress.com/2014/01/27/reading-files-with-angularjs/
     http://jsfiddle.net/alexsuch/6aG4x/535/
     * Copyright (c) 2015 Alejandro Such Berenguer
     *
     * Permission is hereby granted, free of charge, to any person obtaining
     * a copy of this software and associated documentation files (the
     * "Software"), to deal in the Software without restriction, including
     * without limitation the rights to use, copy, modify, merge, publish,
     * distribute, sublicense, and/or sell copies of the Software, and to
     * permit persons to whom the Software is furnished to do so, subject to
     * the following conditions:

     * The above copyright notice and this permission notice shall be
     * included in all copies or substantial portions of the Software.

     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
     * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
     * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
     * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
     * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
     * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
     * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
     * */
    d.directive('onReadFile', function ($parse) {
        return {
            restrict: 'A',
            scope: false,
            link: function (scope, element, attrs) {
                var fn = $parse(attrs.onReadFile);

                element.on('change', function (onChangeEvent) {
                    var reader = new FileReader();

                    reader.onload = function (onLoadEvent) {
                        scope.$apply(function () {
                            fn(scope, {$fileContent: onLoadEvent.target.result});
                        });
                    };

                    reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
                });
            }
        };
    });
    // ------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------

})();
