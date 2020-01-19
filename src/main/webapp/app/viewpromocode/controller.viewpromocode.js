(function () { // < (!) use this pattern in all controllers

    'use strict';

    /**
     *  shows promo code info, for admins only
     */

    const controller_name = "cryptonomica.controller.viewpromocode";

    const debugEnabled = true;

    const controller = angular.module(controller_name, []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
    controller.config(function ($logProvider) {
            $logProvider.debugEnabled(debugEnabled);
        }
    );

    controller.controller(controller_name, [
        '$scope',
        '$rootScope',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$stateParams',
        '$log',
        '$timeout',
        function viewPromoCodeCtrl(
            $scope,
            $rootScope,
            GApi,
            GAuth,
            GData,
            $state,
            $stateParams,
            $log,
            $timeout
        ) {

            (function forDebugOnly() {
                if (debugEnabled) {
                    $log.debug(controller_name, "started");
                    window.scope = $scope;
                    window.rootScope = $rootScope;
                }
            })();

            (function setAlerts() {
                $scope.alertDanger = null;  // red
                $scope.alertWarning = null; // yellow
                $scope.alertInfo = null;    // blue
                $scope.alertSuccess = null; // green
                $scope.alertMessage = {}; // grey

                $scope.setAlertDanger = function (message) {
                    $scope.alertDanger = message;
                    $log.error("$scope.alertDanger:", $scope.alertDanger);
                    // $scope.$apply(); not here
                    $rootScope.goTo("alertDanger");
                };

                $scope.setAlertWarning = function (message) {
                    $scope.alertWarning = message;
                    $log.debug("$scope.alertWarning:", $scope.alertWarning);
                    // $scope.$apply();
                    $rootScope.goTo("alertWarning");
                };

                $scope.setAlertInfo = function (message) {
                    $scope.alertInfo = message;
                    $log.debug("$scope.alertInfo:", $scope.alertInfo);
                    // $scope.$apply();
                    $rootScope.goTo("alertInfo");
                };

                $scope.setAlertSuccess = function (message) {
                    $scope.alertSuccess = message;
                    $log.debug("$scope.alertSuccess:", $scope.alertSuccess);
                    // $scope.$apply();
                    $rootScope.goTo("alertSuccess");
                };

                $scope.setAlertMessage = function (message, header) {
                    $scope.alertMessage = {};
                    $scope.alertMessage.header = header;
                    $scope.alertMessage.message = message;
                    $log.debug("$scope.alertMessage:", $scope.alertMessage);
                    // $scope.$apply();
                    $rootScope.goTo("alertMessage");
                };
            })();

            $scope.promoCodeStr = $stateParams.promoCodeStr;
            $log.info("$stateParams.promoCodeStr : " + $stateParams.promoCodeStr);

            $scope.getPromoCode = function () {

                $rootScope.progressbar.start(); // <<<<<<<
                $scope.promoCode = null;
                $scope.error = null;

                GApi.executeAuth('stripePaymentsAPI', 'getPromoCodeInfo', {"promoCodeStr": $stateParams.promoCodeStr})
                    .then(function (resp) {
                            $scope.promoCode = resp; // net.cryptonomica.entities.PromoCode
                            $log.info("$scope.promoCode:");
                            $log.info($scope.promoCode);
                        }
                    )
                    .catch(function (error) {
                        $log.error("error: ");
                        $log.error(error);
                        $scope.alertDanger = error.message.replace('java.lang.Exception: ', '');
                    })
                    .finally(function () {
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
            };

            if ($rootScope.currentUser && $rootScope.currentUser.cryptonomicaOfficer) {
                $scope.getPromoCode(); // << main
            } else if ($rootScope.googleUser) {
                GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData')
                    .then(function (resp) {
                        // (!) we always have response
                        $rootScope.currentUser = resp;
                        if ($rootScope.currentUser && $rootScope.currentUser.cryptonomicaOfficer) {
                            $scope.getPromoCode(); // < main
                        } else {
                            $scope.setAlertDanger("This section is for admins only");
                        }
                    })
                    .catch(function (error) {
                        $scope.setAlertDanger(error);
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
            } else {
                GAuth.checkAuth()
                    .then(
                        function (result) {

                            return GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData');
                        })
                    .then(function (resp) {
                        // (!) we always have response
                        $rootScope.currentUser = resp;
                        if ($rootScope.currentUser && $rootScope.currentUser.cryptonomicaOfficer) {
                            $scope.getPromoCode(); // < main
                        } else {
                            $scope.setAlertDanger("This section is for admins only");
                        }
                    })
                    .catch(function (error) {
                        if (typeof error === "undefined") {
                            error = "User is not logged in";
                        }
                        $scope.setAlertDanger(error);
                        $timeout($rootScope.progressbar.complete(), 1000);
                    });
            }

        }]);
})();
