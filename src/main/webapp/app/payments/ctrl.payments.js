(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.payments"; // add to controller.js

    const debugEnabled = true;

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
        '$http',
        'GApi',
        // 'GAuth',
        // 'GData',
        '$state',
        '$stateParams',
        '$log',
        // '$cookies',
        '$timeout',
        function testCtrl($scope,    // <- name for debugging
                          $rootScope,
                          $http,
                          GApi,
                          // GAuth,
                          // GData,
                          $state,
                          $stateParams,
                          $log,
                          // $cookies,
                          $timeout) {


            (function forDebugOnly() {
                if (debugEnabled) {
                    $log.debug(controller_name, "started");
                    window.scope = $scope;
                    window.rootScope = $rootScope;
                    $log.debug("paymentTypeCode : " + $stateParams.paymentTypeCode);
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

                $scope.setPaymentError = function (message) {
                    $scope.paymentError.message = message;
                    $scope.$apply();
                }

            })();

            $scope.priceChangeDisabled = false;
            $scope.paymentForChangeDisabled = false;
            $scope.paymentResponse = {};
            $scope.paymentError = {};

            $scope.stripePaymentFormGeneral = {};

            function applyPaymentTypeData(paymentType) {
                if (paymentType) {
                    // if (paymentType.paymentTypeCode) {
                    //     $scope.stripePaymentFormGeneral.paymentTypeCode = paymentType.paymentTypeCode;
                    // }
                    if (paymentType.priceInCents) {
                        $scope.stripePaymentFormGeneral.priceInEuro = paymentType.priceInCents / 100;
                        $scope.priceChangeDisabled = true;
                    }
                    if (paymentType.paymentFor) {
                        $scope.stripePaymentFormGeneral.paymentFor = paymentType.paymentFor;
                        $scope.paymentForChangeDisabled = true;
                    }
                }
            }

            if ($stateParams.paymentTypeCode) {
                $scope.stripePaymentFormGeneral.paymentCode = $stateParams.paymentCode;
                $scope.submitPaymentBtnDisabled = true;
                GApi.executeAuth(
                    'stripePaymentsAPI',
                    'getPaymentType',
                    {"paymentTypeCode": $stateParams.paymentTypeCode}
                )
                    .then(function (resp) {
                        $log.debug("paymentType:");
                        $log.debug(resp);
                        if (resp) {
                            applyPaymentTypeData(resp);
                        }
                    })
                    .catch(function (error) {
                        $log.error("error: ");
                        $log.error(error);
                        if (debugEnabled) {
                            $scope.alertDanger = error;
                        } else {
                            $scope.alertDanger = error.message.replace('java.lang.Exception: ', '');
                        }
                    })
                    .finally(function () {
                        // $scope.$apply();
                        $scope.submitPaymentBtnDisabled = false;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
            }

            /* ---- Stripe payment  */
            $scope.submitPayment = function () {

                $scope.paymentError = {};

                if (
                    $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.nameOnCard)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.cardNumber)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.cardCVC)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.cardExpMonth)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.cardExpYear)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.paymentFor)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.cardHolderEmail)
                    || $rootScope.stringIsNullUndefinedOrEmpty($scope.stripePaymentFormGeneral.confirmEmail)
                    || !$scope.stripePaymentFormGeneral.priceInEuro
                ) {
                    $scope.setPaymentError("Please fill all required fields");
                    // $scope.setAlertDanger("Please fill all required fields");
                    return;
                }

                if ($scope.stripePaymentFormGeneral.cardHolderEmail !== $scope.stripePaymentFormGeneral.confirmEmail) {
                    $scope.setPaymentError("Emails don't much:  " + $scope.stripePaymentFormGeneral.cardHolderEmail + "  " + $scope.stripePaymentFormGeneral.confirmEmail);
                    // $scope.setAlertDanger("Emails don't much: " + $scope.stripePaymentFormGeneral.cardHolderEmail + " " + $scope.stripePaymentFormGeneral.confirmEmail);
                    return;
                }

                if (!$scope.stripePaymentFormGeneral.priceInCents) {
                    $scope.stripePaymentFormGeneral.priceInCents = $scope.stripePaymentFormGeneral.priceInEuro * 100;
                }

                $rootScope.progressbar.start(); // <<<<<<<<<<<
                $scope.submitPaymentWorking = true;
                $log.debug("$scope.stripePaymentFormGeneral:");
                $log.debug($scope.stripePaymentFormGeneral);
                GApi.executeAuth(
                    'stripePaymentsAPI',
                    'processStripePaymentGeneral',
                    $scope.stripePaymentFormGeneral
                )
                    .then(function (paymentResponse) {
                        $scope.paymentResponse = paymentResponse;
                        $log.debug("$scope.paymentResponse:");
                        $log.debug($scope.paymentResponse);
                    })
                    .catch(function (paymentError) {
                        $scope.paymentError = paymentError;
                        console.log("$scope.paymentError: ");
                        $log.info($scope.paymentError);
                    })
                    .finally(function () {
                        $scope.submitPaymentWorking = false;
                        $timeout($rootScope.progressbar.complete(), 1000);
                    })
            }; // end of $scope.submitPayment()

        }]);
})();
