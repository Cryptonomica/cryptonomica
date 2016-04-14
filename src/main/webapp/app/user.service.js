'use strict';

var app = angular.module('cryptonomica');
// Note: Using angular.module('app', []); sets a module, 
// whereas angular.module('app'); gets the module. 
// Only set once and get for all other instances 

app.service("UserService", function ($cookies, $log, GApi, GAuth) {

    this.readUserCookies = function () {
        var user = {}; // create empty obj, to avoid: "TypeError: Cannot set property 'userId' of undefined"
        user.userId = $cookies.get('userId');
        user.arbitrator = $cookies.get('arbitrator');
        user.cryptonomicaOfficer = $cookies.get('cryptonomicaOfficer');
        user.lawyer = $cookies.get('lawyer');
        user.notary = $cookies.get('notary');
        user.registeredCryptonomicaUser = $cookies.get('registeredCryptonomicaUser');        
        return user;
    };

    this.getKeyById = function (fingerprint) {
        $log.info("user.service.js : getKeyById");
        var result = {};
        GAuth.checkAuth().then(
            GApi.executeAuth('pgpPublicKeyAPI', 'getKeyById', {"fingerprint": fingerprint}).then(
                function (resp) {
                    $log.info("[showkey.controller.js]");
                    $log.info(resp);
                    result = resp;
                },
                function (error) {
                    $log.error("[showkey.controller.js]");
                    $log.error(error);
                    result.error = error;
                }
            ),
            function () {
                $log.error("GAuth.checkAuth() - not successful");
                result.error = "user not logged in";
            }
        );
        return result;
    };
});