(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.proofOfExistence";

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
        '$log',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$cookies',
        '$timeout',
        function PoECtrl($scope,    // <- name for debugging
                         $rootScope,
                         $http,
                         $log,
                         GApi,
                         GAuth,
                         GData,
                         $state,
                         $cookies,
                         $timeout) {

            $log.info("cryptonomica.controller.proofOfExistence started");

            // --- get user data
            GAuth.checkAuth().then(
                function () {
                    $rootScope.getUserData(); // async?
                },
                function () {
                    //$rootScope.getUserData();
                    $log.error("[proofOfExistence.controller.js] GAuth.checkAuth() - unsuccessful");
                    $scope.alert = "User not logged in";
                }
            );

            $scope.ethAddDoc = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<

                $scope.fullResponse1 = null;
                $scope.resp1 = null;
                $scope.error1 = null;

                console.log("ethAddDocForm: " + JSON.stringify($scope.ethAddDocForm));

                GApi.executeAuth('ethNodeAPI', 'addDoc', $scope.ethAddDocForm)
                    .then(
                        function (resp) {
                            $scope.fullResponse1 = resp; //
                            $log.info("[ethAddDoc] $scope.fullResponse: ");
                            $log.info($scope.fullResponse1);
                            $scope.alert = null;
                            $scope.resp1 = {};
                            $scope.resp1.txHash = resp.txHash;
                            $scope.resp1.sha256 = resp.storedTextSha256;

                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            $log.error("error:");
                            $log.error(error);

                            // $scope.error1 = error; // resp.message or resp.error.message - java.lang.Exception:
                            $scope.alert = "Error:" + error.message;

                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end ethAddDoc

            $scope.ethGetDocBySha256 = function () {
                $rootScope.progressbar.start(); // <<<<<<<<<<<

                $scope.fullResponse2 = null;
                $scope.resp2 = null;
                $scope.error2 = null;

                $log.info("ethGetDocBySha256Form: " + JSON.stringify($scope.ethGetDocBySha256Form));

                GApi.executeAuth('ethNodeAPI', 'getDocBySha256', $scope.ethGetDocBySha256Form)
                    .then(
                        function (resp) {
                            $scope.fullResponse2 = resp; //
                            $log.info("[ethGetDocBySha256] $scope.fullResponse2: ");
                            $log.info($scope.fullResponse2);

                            $scope.resp2 = {};
                            $scope.alert = null;
                            if (resp.storedDoc[4] == "") {
                                $scope.resp2.docText = "no documents found"
                            } else {
                                $scope.resp2.docText = resp.storedDoc[4];
                                $scope.resp2.unixTime = resp.storedDoc[2];
                            }
                            if ($scope.resp2.unixTime) {
                                $scope.resp2.publishedDate = new Date($scope.resp2.unixTime * 1000).toString();
                            }
                            $scope.resp2.publishedBy = resp.storedDoc[1];
                            $scope.resp2.publishedInBlockNumber = resp.storedDoc[3];

                            $timeout($rootScope.progressbar.complete(), 1000);
                        }, function (error) {
                            $log.error("error: ");
                            $log.error(error);
                            $scope.alert = error.message;
                            $timeout($rootScope.progressbar.complete(), 1000);
                        }
                    );
            }; // end ethAddDoc

        }]);

})();
