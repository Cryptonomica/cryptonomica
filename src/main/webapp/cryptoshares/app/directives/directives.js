(function () {

    'use strict';

    var d = angular.module('app.directives', []);

    // ------------------------------------------------------------------------------
    d.directive('alerts', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <alerts></alerts>
            templateUrl: 'app/directives/alerts.html'
        };
    });

    // ------------------------------------------------------------------------------
    d.directive('network-label', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <network-label></network-label>
            templateUrl: 'app/directives/label.html'
        };
    });
    // ------------------------------------------------------------------------------
    d.directive('noConnectionToNodeError', function () {
        return {
            restrict: 'EA', //E = element, A = attribute, C = class, M = comment
            // <no-connection-to-node-error></no-connection-to-node-error>
            // replace: 'true', // >> error
            templateUrl: 'app/directives/noConnectionToNodeError.html'
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
                let fn = $parse(attrs.onReadFile);

                element.on('change', function (onChangeEvent) {
                    let reader = new FileReader();

                    reader.onload = function (onLoadEvent) {
                        scope.$apply(function () {
                            fn(scope, {$fileContent: onLoadEvent.target.result});
                        });
                    };

                    reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
                });
            }
        };
        /* see also:
        * https://jsfiddle.net/alexsuch/6aG4x/
        * */
    });

    // ------------------------------------------------------------------------------

})();