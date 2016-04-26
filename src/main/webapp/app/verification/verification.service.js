'use strict';

var app = angular.module('cryptonomica');
// Note: Using angular.module('app', []); sets a module, 
// whereas angular.module('app'); gets the module. 
// Only set once and get for all other instances 

app.service("VerificationService", function ($cookies,
                                             $log,
                                             GApi,
                                             GAuth) {

    /* --- GET verification info by verification ID */
    this.getVerificationById = function (verificationID) {

        $log.info("verification.service.js : getVerificationById");

        var result = {};
        result.error = null;

        GAuth.checkAuth().then(
            GApi.executeAuth('verificationAPI', 'getVerificationByID', {"verificationID": verificationID}).then(
                function (resp) {
                    $log.info("responce:");
                    $log.info(resp);
                    result = resp;
                    return result;
                },
                function (error) {
                    $log.error("error:");
                    $log.error(error);
                    result.error = error;
                    return result;
                }
            ),
            function () {
                $log.error("GAuth.checkAuth() - not successful");
                result.error = "user not logged in";
                return result;
            }
        );
        // return result;
    };

    /* --- GET verification info by verification WebSafeString */
    this.getVerificationByWebSafeString = function (verificationWebSafeString) {

        $log.info("verification.service.js : getVerificationByWebSafeString");

        var result = {};
        result.error = null;

        GAuth.checkAuth().then(
            GApi.executeAuth('verificationAPI', 'getVerificationByWebSafeString', {"verificationWebSafeString": verificationWebSafeString}).then(
                function (resp) {
                    $log.info("response:");
                    $log.info(resp);
                    result = resp;
                    // return result;
                },
                function (error) {
                    $log.error("error:");
                    $log.error(error);
                    result.error = error;
                    // return result;
                }
            ),
            function () {
                $log.error("GAuth.checkAuth() - not successful");
                result.error = "user not logged in";
                // return result;
            }
        );
        // return result;
    };

});