'use strict';

/**
 *  get and shows verification info by verification id
 *
 */

// register in /app/controller.js
// register in /app/router.js
// add to index.html
var controller_name = "cryptonomica.controller.verification";

var controller = angular.module(controller_name, []);

controller.controller(controller_name, [
        'VerificationService',
        '$scope',
        '$rootScope',
        '$http',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$log',
        '$timeout',
        '$stateParams',
        '$cookies',
        // 'UserService',
        function verificationCtrl(VerificationService,
                                  $scope,
                                  $rootScope,
                                  $http,
                                  GApi,
                                  GAuth,
                                  GData,
                                  $state,
                                  $log,
                                  $timeout,
                                  $stateParams,
                                  $cookies
                                  // UserService
        ) {
            //
            GAuth.checkAuth().then(
                function () {
                    $scope.alert = null;
                    $rootScope.getUserData(); // async?
                },
                function () {
                    //$rootScope.getUserData();
                    $log.error("[verification.controller.js] GAuth.checkAuth() - unsuccessful");
                    $scope.alert = "User not logged in";
                }
            );
            //

            $log.info("$stateParams.verificationWebSafeString : " + $stateParams.verificationWebSafeString);
            // $scope.result = VerificationService.getVerificationByWebSafeString($stateParams.verificationWebSafeString);
            var verificationWebSafeString = $stateParams.verificationWebSafeString;
            var getVerification = function (verificationWebSafeString) {

                $log.info("verification.service.js : getVerificationByWebSafeString");

                var result = {};
                result.error = null;

                GAuth.checkAuth().then(
                    GApi.executeAuth('verificationAPI', 'getVerificationByWebSafeString', {"verificationWebSafeString": verificationWebSafeString}).then(
                        function (resp) {
                            $log.info("response:");
                            $log.info(resp);
                            $scope.result = resp;
                            // return result;
                        },
                        function (error) {
                            $log.error("error:");
                            $log.error(error);
                            $scope.result.error = error;
                            // return result;
                        }
                    ),
                    function () {
                        $log.error("GAuth.checkAuth() - not successful");
                        $scope.result.error = "user not logged in";
                        // return result;
                    }
                );
                // return result;
            }; //
            getVerification(verificationWebSafeString); // << -- call 
            $log.info("$scope.result: ");
            $log.info($scope.result);
        }
    ]
);