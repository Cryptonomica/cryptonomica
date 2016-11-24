(function () {

    'use strict';

    var d = angular.module('cryptonomica.directives', []);

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

    d.directive('testDirective2', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <test-directive-2></test-directive-2>
            // replace: 'true', // >> error
            templateUrl: 'app/directives/testDirective2.html'
        };
    });

})();