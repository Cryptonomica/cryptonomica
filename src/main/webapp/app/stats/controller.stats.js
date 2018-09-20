(function () { // < (!) use this pattern in all controllers
    'use strict';

    var controller_name = "cryptonomica.controller.stats";
    var controller = angular.module(controller_name, []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
    controller.config(function ($logProvider) {
            // $logProvider.debugEnabled(false);
            $logProvider.debugEnabled(true);
        }
    );

    controller.controller(controller_name, [
        '$scope',
        '$rootScope',
        // '$http',
        '$log',
        // '$sce',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        // '$stateParams',
        // '$cookies',
        '$timeout',
        function statsCtrl($scope,
                           $rootScope,
                           // $http,
                           $log,
                           // $sce,
                           GApi,
                           GAuth,
                           GData,
                           $state,
                           // $stateParams,
                           // $cookies,
                           $timeout) {

            $log.debug(controller_name, "started"); //
            $log.debug('$state');
            $log.debug($state);
            $timeout($rootScope.progressbar.complete(), 1000);
            //    ------------

            // prepaire:
            var counterUpOptionsObject = {
                delay: 10,
                time: 1000
            };
            $scope.getStatsAllTime = function () {
                // $rootScope.progressbar.start(); // <<<<<<<<<<<
                GApi.execute('statisticsAPI', 'getStatsAllTime')
                    .then(
                        function (result) {

                            $log.debug("$scope.getStatsAllTime result: ");
                            $log.debug(result);
                            $scope.statsAllTime = result;

                            var usersRegisteredAnimation = new CountUp("usersRegistered", 0, $scope.statsAllTime.usersRegistered);
                            var keysUploadedAnimation = new CountUp("keysUploaded", 0, $scope.statsAllTime.keysUploaded);
                            var keysVerifiedOnlineAnimation = new CountUp("keysVerifiedOnline", 0, $scope.statsAllTime.keysVerifiedOnline);
                            var keysVerifiedOfflineAnimation = new CountUp("keysVerifiedOffline", 0, $scope.statsAllTime.keysVerifiedOffline);

                            usersRegisteredAnimation.start();
                            keysUploadedAnimation.start();
                            keysVerifiedOnlineAnimation.start();
                            keysVerifiedOfflineAnimation.start();

                            // $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            console.log("$scope.getStatsAllTime error: ");
                            console.log(error);
                            // $scope.error = error.message; // resp.message or resp.error.message - java.lang.Exception:
                            // $rootScope.working = false;
                            // $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            };

            $scope.getOnlineVerificationsByCountry = function () {
                // $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.mapData = null;
                GApi.execute('statisticsAPI', 'onlineVerificationsByCountry')
                    .then(
                        function (result) {

                            $log.debug(" $scope.getOnlineVerificationsByCountry result: ");
                            $log.debug(result);
                            $scope.mapData = result; //

                            var mapOptionsObject = {
                                map: 'world_en',
                                backgroundColor: '#fff',
                                color: '#ffffff',
                                hoverColor: '#60c8fa',
                                hoverOpacity: 0.7,
                                // selectedColor: '#666666',
                                selectedColor: '#60c8fa',
                                // enableZoom: false,
                                enableZoom: true,
                                showTooltip: true,
                                // showTooltip: false,
                                values: $scope.mapData,
                                // scaleColors: [
                                //     '#C8EEFF',
                                //     '#006491'
                                // ],
                                scaleColors: [
                                    '#aaaaaa',
                                    '#444444'
                                ],
                                normalizeFunction: 'polynomial',
                                // see: https://stackoverflow.com/questions/14895048/jqvmap-how-to-show-data-values-onregionclick/15938450#15938450
                                onLabelShow: function (event, label, code) {
                                    if ($scope.mapData[code] > 0)
                                        label.append(': ' + $scope.mapData[code] + ' keys verified');
                                }

                                // onRegionOver: function (event, code, region) {
                                //     $log.debug(event);
                                //     $log.debug(code);
                                //     $log.debug(region);
                                // },
                                // onRegionClick: function (element, code, region) {
                                //     var message = region
                                //         + ' : '
                                //         + $scope.mapData[code]
                                //         + " keys verified";
                                //     alert(message);
                                // }

                            };

                            $('#vmap').vectorMap(mapOptionsObject);
                            // $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            console.log("$scope.getOnlineVerificationsByCountry error: ");
                            console.log(error);
                            // $scope.error = error.message; // resp.message or resp.error.message - java.lang.Exception:
                            // $rootScope.working = false;
                            // $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            };

            // run functions:
            $scope.getStatsAllTime();
            $scope.getOnlineVerificationsByCountry();

        } // end function statsCtl

    ]);

})();

