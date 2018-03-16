(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.search";

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
            function searchCtrl($scope,
                                $rootScope,
                                $http,
                                $log,
                                GApi,
                                GAuth,
                                GData,
                                $state,
                                $cookies) {

                // --- **** get user data
                GAuth.checkAuth().then(
                    function () {
                        $rootScope.getUserData(); // async?
                    },
                    function () {
                        //$rootScope.getUserData();
                    }
                );

                $scope.doSearch = function () {
                    $rootScope.working = true;
                    $scope.resp = null;
                    console.log("search for: " + JSON.stringify($scope.form)); // search for: [object Object] without JSON.stringify
                    GApi.executeAuth('userSearchAndViewAPI', 'generalSearchUserProfiles', $scope.form)
                        .then(
                            function (resp) {
                                $scope.resp = resp; //
                                console.log("controller.search: doSearch(): resp: ");
                                console.log(resp);
                                $rootScope.working = false;
                            }, function (resp) {
                                console.log("error: ");
                                console.log(resp);
                                $scope.resp = resp; // resp.message or resp.error.message - java.lang.Exception:
                                $rootScope.working = false;
                            }
                        );
                }; // end $scope.doSearch
            }
        ]
    );
})();
