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
    d.directive('testDirective1', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <test-directive-1></test-directive-1>
            // replace: 'true', // >> error
            template: "<div>" +
            "<h2>Test Directive #1</h2>" +
            "<div>Name:{{customer.name}}<br>" +
            "Street:{{customer.street}}" +
            "</div>" +
            "</div>"
            // template: '<div>html of Directive 1</div>'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('testDirective2', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <test-directive-2></test-directive-2>
            // replace: 'true', // >> error
            templateUrl: 'app/directives/testDirective2.html'
        };
    });
    // ------------------------------------------------------------------------------

})();