(function () {

    'use strict';

    var d = angular.module('cryptonomica.directives', []);

    // ------------------------------------------------------------------------------
    d.directive('headerMainMenu', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <header-main-menu></header-main-menu>
            // replace: 'true', // >> error
            templateUrl: 'app/directives/headerMainMenu.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('navigationMenu', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <navigation-menu></navigation-menu>
            templateUrl: 'app/directives/navigationMenu.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('headerLandingPage', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <header-landing-page></header-landing-page>
            // replace: 'true', // >> error
            templateUrl: 'app/directives/headerLandingPage.html'
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
    // ------------------------------------------------------------------------------
    d.directive('mediaStreamRecorder', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <media-stream-recorder></media-stream-recorder>
            templateUrl: 'app/directives/mediaStreamRecorder.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('recordVideoJs', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <record-video-js></record-video-js>
            templateUrl: 'app/directives/recordVideoJs.html'
        };
    });
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

})();