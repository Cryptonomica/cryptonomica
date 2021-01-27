(function () { // < (!) use this pattern in all controllers

    'use strict';

    var app = angular.module('cryptonomica', [
        'ngCookies', // (1.4.9) https://code.angularjs.org/1.4.9/docs/api/ngCookies
        'ui.router', // (0.2.18 ) https://github.com/angular-ui/ui-router/tree/legacy
        'ui.uploader', // () https://github.com/angular-ui/ui-uploader
        'angular-google-gapi', // (1.0.1) https://github.com/maximepvrt/angular-google-gapi/
        'yaru22.md', // https://github.com/yaru22/angular-md
        'ngProgress', // https://github.com/VictorBjelkholm/ngProgress
        // 'ngclipboard', // https://sachinchoolur.github.io/ngclipboard/
        'ngFileSaver', // http://alferov.github.io/angular-file-saver/#demo
        'ui.date', // https://github.com/angular-ui/ui-date
        'puigcerber.countryPicker', // angular-country-picker: https://github.com/Puigcerber/angular-country-picker (pvp-country-picker)
        // ---- my:
        'cryptonomica.ui.router',
        'cryptonomica.controller',
        'cryptonomica.directives',
        'cryptonomica.controller.test' // http://www.w3schools.com/angular/angular_directives.asp
        // https://weblogs.asp.net/dwahlin/creating-custom-angularjs-directives-part-i-the-fundamentals
    ]);

    app.config(function ($sceDelegateProvider) {
        $sceDelegateProvider.resourceUrlWhitelist([
            'self', // Allow same origin resource loads
            'https://cryptonomica-server.appspot.com/**',
            'https://sandbox-cryptonomica.appspot.com/**', // < for sandbox
            'https://lh3.googleusercontent.com/**', // files (photos)  from blob storage service
            'https://raw.githubusercontent.com/Cryptonomica/arbitration-rules/**', // works!
            'https://raw.githubusercontent.com/Cryptonomica/arbitration-rules/master/README.md'
        ]);
    });

// see:
// https://stackoverflow.com/questions/24039226/angularjs-format-text-return-from-json-to-title-case
    app.filter('titleCase', function () {
        return function (input) {
            input = input || '';
            return input.replace(/\w\S*/g, function (txt) {
                return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
            });
        };
    });

    app.run([
            'GAuth',
            'GApi',
            'GData',
            '$state',
            '$rootScope',
            '$window',
            '$document',
            '$sce',
            'ngProgressFactory',
            '$timeout',
            '$cookies',
            // '$http',
            '$anchorScroll',
            '$location',
            '$log',
            function (GAuth,
                      GApi,
                      GData,
                      $state,
                      $rootScope,
                      $window,
                      $document,
                      $sce,
                      ngProgressFactory,
                      $timeout,
                      $cookies,
                      // $http,
                      $anchorScroll,
                      $location,
                      $log) {

                $rootScope.appVersion = '3.5.1 (2021-01-27)';

                // (!!!) for debug:
                // see:
                // https://stackoverflow.com/questions/13743058/how-do-i-access-the-scope-variable-in-browsers-console-using-angularjs
                // window.my$rootScope = $rootScope;

                /* --- UI */
                $rootScope.sidebarVisible = true;

                // =============== Function calls:
                $rootScope.progressbar = ngProgressFactory.createInstance();
                $rootScope.progressbar.setHeight('6px'); // any valid CSS value Eg '10px', '1em' or '1%'
                // $rootScope.progressbar.setColor('orangered');
                // $rootScope.progressbar.setColor('purple');
                // $rootScope.progressbar.setColor('#C800C8');
                $rootScope.progressbar.setColor('#60c8fa');

                // >>>>
                $rootScope.progressbar.start();
                // $timeout($rootScope.progressbar.complete(), 1000);

                /* === angular-google-gapi */
                /* see: https://github.com/maximepvrt/angular-google-gapi */

                // (!!!) this prevents errors
                var gapiCheck = function () {
                    if ($window.gapi && $window.gapi.client) {
                        $log.info("[app.js] $window.gapi.client:");
                        $log.info(window.gapi.client);
                    } else {
                        $log.error("[app.js] $window.gapi.client is not loaded");
                        // setTimeout(gapiCheck, 1000); // check again in a second
                        setTimeout(gapiCheck, 100); //
                    }
                };
                gapiCheck();
                // while (true) {
                //     if ($window.gapi && $window.gapi.client) {
                //         $log.debug("[" + $scope.controllerName + "] $window.gapi.client:");
                //         $log.debug($window.gapi.client);
                //         break;
                //     }
                // }

                $rootScope.gdata = GData;
                $rootScope.supportEmail = "support@cryptonomica.net";

            var SED_START;
            $rootScope.PRODUCTION = true;
            var SED_END;

                var CLIENT;
                if ($rootScope.PRODUCTION) {
                    $rootScope.gaeProjectDomain = "cryptonomica-server.appspot.com";
                    CLIENT = "762021407984-9ab8gugumsg30rrqgvma9htfkqd3uid5.apps.googleusercontent.com";
                } else {
                    $document.title = "[SANDBOX] " + $document.title;
                    $rootScope.gaeProjectDomain = "sandbox-cryptonomica.appspot.com";
                    CLIENT = "517360814873-7pn1cta2addmcvug6rb1jr2u20vv2qnt.apps.googleusercontent.com";
                }

                var BASE = "https://" + $rootScope.gaeProjectDomain + "/_ah/api";
                // $log.debug("API BASE:", BASE);

                GApi.load('notaryAPI', 'v1', BASE);             // 1
                GApi.load('pgpPublicKeyAPI', 'v1', BASE);       // 2
                GApi.load('uploadAPI', 'v1', BASE);             // 3
                GApi.load('userSearchAndViewAPI', 'v1', BASE);  // 4
                GApi.load('visitorAPI', 'v1', BASE);            // 5
                GApi.load('newUserRegistrationAPI', 'v1', BASE);// 6
                GApi.load('cryptonomicaUserAPI', 'v1', BASE);   // 7
                GApi.load('verificationAPI', 'v1', BASE);       // 8
                GApi.load('arbitratorsAPI', 'v1', BASE);        // 9
                GApi.load('ethNodeAPI', 'v1', BASE);            // 10
                GApi.load('onlineVerificationAPI', 'v1', BASE); // 11
                GApi.load('stripePaymentsAPI', 'v1', BASE);     // 12
                GApi.load('statisticsAPI', 'v1', BASE);         // 13
                //
                GAuth.setClient(CLIENT);
                GAuth.setScope(
                    'https://www.googleapis.com/auth/userinfo.email'
                );

                $log.info('webapp started,  version: ', $rootScope.appVersion);
                $log.info('production: ', $rootScope.PRODUCTION);

                $rootScope.checkAuth = function () {
                    GAuth.checkAuth()
                        .then(
                            function (result) {
                                //
                                // $log.debug('[app.js] user is logged in:');
                                // $log.debug(result); // undefined

                                $log.debug('$rootScope.gapi.user:');
                                $log.debug($rootScope.gapi.user);
                                // used in 'registration.html' etc.:
                                $rootScope.googleUser = $rootScope.gapi.user;
                                if ($rootScope.gapi && $rootScope.gapi.user && $rootScope.gapi.user.email) {
                                    $log.debug("$rootScope.gapi.user.email: ", $rootScope.gapi.user.email);
                                    $cookies.put('userEmail', $rootScope.gapi.user.email);
                                }
                                //
                                // does not work:
                                // $log.debug("$rootScope.gapi.auth.getToken():");
                                // $log.debug($rootScope.gapi.auth.getToken());
                                //
                                // this works:
                                // $rootScope.accessToken = $window.gapi.auth.getToken().access_token;
                                // $log.debug("$rootScope.accessToken:");
                                // $log.debug($rootScope.accessToken);
                                // // this works:
                                // $log.debug("$window.gapi.auth.getToken()");
                                // $log.debug($window.gapi.auth.getToken());
                                //
                                $rootScope.getUserData();
                            })
                        .catch(function (error) {
                            $log.debug("[app.js] user is not logged in:");
                            $log.debug("[app.js] error:", error);
                            //
                            // $state.go('login'); // an example of action if it's impossible to
                            // authenticate user at startup of the application
                        });
                };
                $rootScope.checkAuth(); // <<< run

                $rootScope.login = function () { // < shows auth window from Google
                    $rootScope.progressbar.start();
                    GAuth.login()
                        .then(function (result) {
                            $log.debug('[app.js] google login:');
                            $log.debug(result);
                            $log.debug("$rootScope.gapi.user : ");
                            $log.debug($rootScope.gapi.user);
                            // >
                            $rootScope.googleUser = $rootScope.gapi.user;
                            if ($rootScope.gapi && $rootScope.gapi.user && $rootScope.gapi.user.email) {
                                $log.debug("$rootScope.gapi.user.email: ", $rootScope.gapi.user.email);
                                $cookies.put('userEmail', $rootScope.gapi.user.email);
                            }
                            $('#userAvatar')
                                .popup({
                                    // position : 'right center',
                                    target: '#userAvatar',
                                    title: 'Google account:',
                                    content: $rootScope.gapi.user.email
                                });
                            $rootScope.getUserData();
                        })
                        .catch(function (error) {
                            $log.debug("[app.js] login failed:");
                            $log.debug(error);
                            //
                            // $state.go('login'); // an example of action if it's impossible to
                            // authenticate user at startup of the application
                        });
                }; // end: $rootScope.login

                $rootScope.myBookmarks = {}; //
                $rootScope.getMyBookmarks = function () {
                    // $rootScope.progressbar.start();
                    GApi.executeAuth('cryptonomicaUserAPI', 'getMyBookmarks') // async
                        .then(function (resp) {
                                $rootScope.myBookmarks = resp;
                                $log.info("[app.js] $rootScope.myBookmarks: ");
                                $log.info($rootScope.myBookmarks);
                                // $log.debug('$rootScope.myBookmarks.keys');
                                // $log.debug(Object.keys($rootScope.myBookmarks));
                                // $timeout($rootScope.progressbar.complete(), 1000); // <<<
                            }, function (error) {
                                $log.error(error);
                                $timeout($rootScope.progressbar.complete(), 1000); // <<<<
                            }
                        );
                };

                $rootScope.currentUser = {}; //

                $rootScope.getUserData = function () {  // we use this in $rootScope.checkAuth
                    $rootScope.progressbar.start();

                    GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData') // async
                        .then(function (resp) {
                            // (!) we always have response
                            $rootScope.currentUser = resp;
                            $log.info("[app.js] $rootScope.currentUser: ");
                            $log.info($rootScope.currentUser);

                            if (!$rootScope.currentUser.loggedIn) { // user not logged in
                                // show error: user not logged in
                                $log.debug("user not logged in");

                            } else if (!$rootScope.currentUser.registeredCryptonomicaUser) { // user not registered
                                $log.debug('user is not registered');
                                // $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<

                                if (!($state.includes('openPGPOnline') || $state.includes('openPGPSoftware'))) {
                                    $state.go('registration');
                                }
                            }

                            $rootScope.userCurrentImageLink = $sce.trustAsResourceUrl(resp.userCurrentImageLink);

                            $rootScope.getMyBookmarks(); // < in background

                            $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<

                        })
                        .catch(function (error) {
                            $log.debug("GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData') ERROR:");
                            $log.error(error);
                            $timeout($rootScope.progressbar.complete(), 1000); // <<<<<
                        })
                };

                //// --- define other functions:

                // for tests
                $rootScope.consoleLog = function (arg) {
                    $log.info(arg);
                };

                $rootScope.stringIsNullUndefinedOrEmpty = function (str) {
                    return typeof str === 'undefined' || str === null || str.length === 0;
                };

                $rootScope.validateFingerprint = function (fingerprint) {
                    if ($rootScope.stringIsNullUndefinedOrEmpty(fingerprint) || (fingerprint.length !== 40)) {
                        return false;
                    }
                    // https://stackoverflow.com/a/49732936
                    // and https://stackoverflow.com/questions/9011524/regex-to-check-whether-a-string-contains-only-numbers
                    var reg = new RegExp('^([0-9]+[a-zA-Z]+|[a-zA-Z]+[0-9]+)[0-9a-zA-Z]*$');
                    return reg.test(fingerprint)

                };

                $rootScope.instantFindFingerprint = function (fingerprint) {

                    $rootScope.instantFindFingerprintError = null;
                    // see: https://github.com/Semantic-Org/Semantic-UI/issues/760
                    $("#instantFindFingerprint").popup('destroy');

                    if (!$rootScope.stringIsNullUndefinedOrEmpty(fingerprint)) {

                        $rootScope.instantFindFingerprintIsWorking = true;

                        if ($rootScope.currentUser && $rootScope.currentUser.registeredCryptonomicaUser) {

                            if ($rootScope.validateFingerprint(fingerprint)) {
                                $log.debug('requesting data for fingerprint:', fingerprint);
                                // GApi.executeAuth('pgpPublicKeyAPI', 'requestPGPPublicKeyByFingerprint', fingerprint)
                                GApi.executeAuth('pgpPublicKeyAPI', 'getPGPPublicKeyByFingerprint', {"fingerprint": fingerprint})
                                    .then(
                                        function (resp) {
                                            $rootScope.instantFindFingerprintError = null;
                                            $rootScope.instantPGPPublicKeyGeneralView = resp; // PGPPublicKeyGeneralView

                                            console.log("$rootScope.instantPGPPublicKeyGeneralView:");
                                            console.log($rootScope.instantPGPPublicKeyGeneralView);

                                            $rootScope.instantFindFingerprintIsWorking = false;
                                            // $rootScope.$apply(); // not needed here
                                            $("#instantFindFingerprint").popup('destroy');
                                            $state.go("key", {"fingerprint": fingerprint});
                                        },
                                        // if key not found in DB:
                                        // java.lang.IllegalArgumentException: Public PGP key with fingerprint ... not found in DataBase
                                        function (error) {
                                            console.log("error: ");
                                            console.log(error);
                                            $rootScope.instantFindFingerprintError = error.message;

                                            $("#instantFindFingerprint").popup({
                                                position: 'bottom center',
                                                target: '#instantFindFingerprint',
                                                title: 'Error:',
                                                content: $rootScope.instantFindFingerprintError
                                            }).popup('show');

                                            $rootScope.instantFindFingerprintIsWorking = false;
                                            // $rootScope.$apply(); // not needed here
                                        }
                                    );
                            } else {

                                $rootScope.instantFindFingerprintError = "Fingerprint not valid";
                                $rootScope.instantFindFingerprintIsWorking = false;
                                $log.debug($rootScope.instantFindFingerprintError);

                                $("#instantFindFingerprint").popup({
                                    position: 'bottom center',
                                    target: '#instantFindFingerprint',
                                    title: 'Error:',
                                    content: $rootScope.instantFindFingerprintError
                                }).popup('show');

                                $rootScope.instantFindFingerprintIsWorking = false;
                                // $rootScope.$apply(); // not needed here
                            }

                        } else {
                            $rootScope.instantFindFingerprintError = "To use search you have to login/register";
                            $log.debug($rootScope.instantFindFingerprintError);
                            $rootScope.instantFindFingerprintIsWorking = false;

                            $("#instantFindFingerprint").popup({
                                position: 'bottom center',
                                target: '#instantFindFingerprint',
                                title: 'Error:',
                                content: $rootScope.instantFindFingerprintError
                            }).popup('show');

                            $rootScope.instantFindFingerprintIsWorking = false;
                            // $rootScope.$apply(); // not needed here
                        }

                    }

                };

                $rootScope.goTo = function (id) {
                    // set the location.hash to the id of
                    // the element you wish to scroll to.
                    // $location.hash('about');
                    $location.hash(id);
                    // call $anchorScroll()
                    $anchorScroll();
                };

                $rootScope.stateGo = function (state, parameter, parameterValue) {
                    if (parameter && parameterValue) {
                        $state.go(state, {parameter: parameterValue});
                    } else {
                        $state.go(state);
                    }
                };

                $rootScope.unixTimeFromDate = function (date) {
                    return Math.round(date.getTime() / 1000);
                };

                $rootScope.dateFromUnixTime = function (unixTime) {
                    return new Date(unixTime * 1000);
                };

                /* https://codepen.io/shaikmaqsood/pen/XmydxJ/ */
                $rootScope.copyToClipboard = function (element) {
                    // var $temp = $("<input>");
                    var $temp = $("<textarea></textarea>");
                    $("body").append($temp);
                    console.log('copy to clipboard: $(' + element + ').val() :');
                    console.log($(element).text());
                    $temp.val(
                        $(element).text()
                        // $(element).val()
                    ).select();
                    document.execCommand("copy");
                    $temp.remove();
                };

                $rootScope.saveKeyAsFile = function (element, fileName) {
                    // var textToSave = $(element).val();
                    var textToSave = $(element).text();
                    console.log("textToSave as file:");
                    console.log(textToSave);
                    var blob = new Blob([textToSave], {type: "text/plain;charset=utf-8"});
                    $log.debug(blob);
                    // uses https://eligrey.com/demos/FileSaver.js/
                    if ($rootScope.stringIsNullUndefinedOrEmpty(fileName)) {
                        fileName = 'key.asc'
                    }
                    saveAs(blob, fileName);
                };

                $rootScope.readPublicKeyData = function (stringPublicKeyArmored) {

                    var publicKeyOpenPGPjs = openpgp.key.readArmored(stringPublicKeyArmored);
                    $log.debug("publicKeyOpenPGPjs:");
                    $log.debug(publicKeyOpenPGPjs);

                    var publicKey = {};
                    publicKey.publicKeyOpenPGPjs = publicKeyOpenPGPjs;

                    publicKey.keyId = '[' + publicKeyOpenPGPjs.keys[0].primaryKey.keyid.toHex().toUpperCase() + ']';
                    publicKey.fingerprint = publicKeyOpenPGPjs.keys[0].primaryKey.fingerprint.toUpperCase();
                    publicKey.userId = publicKeyOpenPGPjs.keys[0].users[0].userId.userid;
                    publicKey.created = publicKeyOpenPGPjs.keys[0].primaryKey.created;
                    publicKey.bitsSize = publicKeyOpenPGPjs.keys[0].primaryKey.getBitSize();

                    /**
                     * see: https://github.com/openpgpjs/openpgpjs/blob/master/src/key.js#L472
                     * .getExpirationTime() returns the expiration time of the primary key or null if key does not expire
                     * @return {Date|null}
                     */
                    publicKey.exp = publicKeyOpenPGPjs.keys[0].getExpirationTime(); // <<< ---- use this

                    $log.debug("$rootScope.readPublicKeyData result:");
                    $log.debug(publicKey);

                    return publicKey;
                };

                // returs promise
                // see: https://stackoverflow.com/a/43383990/1697878
                $rootScope.getHash = function (str, algo = "SHA-256") {
                    let strBuf = new TextEncoder('utf-8').encode(str);
                    return crypto.subtle.digest(algo, strBuf)
                        .then(hash => {
                            window.hash = hash;
                            // here hash is an arrayBuffer,
                            // so we'll connvert it to its hex version
                            let result = '';
                            const view = new DataView(hash);
                            for (let i = 0; i < hash.byteLength; i += 4) {
                                result += ('00000000' + view.getUint32(i).toString(16)).slice(-8);
                            }
                            return result;
                        });
                };

                if (!$rootScope.stringIsNullUndefinedOrEmpty($cookies.get('promocode'))) {
                    // https://docs.angularjs.org/api/ngCookies/service/$cookies
                    $rootScope.promoCodeMessage = $cookies.get('promocode');
                }

                //    ---

                $rootScope.iso3166codes = {
                    "AD": "Andorra",
                    "AE": "United Arab Emirates",
                    "AF": "Afghanistan",
                    "AG": "Antigua and Barbuda",
                    "AI": "Anguilla",
                    "AL": "Albania",
                    "AM": "Armenia",
                    "AN": "Netherlands Antilles",
                    "AO": "Angola",
                    "AQ": "Antarctica",
                    "AR": "Argentina",
                    "AS": "American Samoa",
                    "AT": "Austria",
                    "AU": "Australia",
                    "AW": "Aruba",
                    "AX": "Åland Islands",
                    "AZ": "Azerbaijan",
                    "BA": "Bosnia and Herzegovina",
                    "BB": "Barbados",
                    "BD": "Bangladesh",
                    "BE": "Belgium",
                    "BF": "Burkina Faso",
                    "BG": "Bulgaria",
                    "BH": "Bahrain",
                    "BI": "Burundi",
                    "BJ": "Benin",
                    "BL": "Saint Barthélemy",
                    "BM": "Bermuda",
                    "BN": "Brunei",
                    "BO": "Bolivia",
                    "BQ": "Bonaire, Sint Eustatius and Saba",
                    "BR": "Brazil",
                    "BS": "Bahamas",
                    "BT": "Bhutan",
                    "BV": "Bouvet Island",
                    "BW": "Botswana",
                    "BY": "Belarus",
                    "BZ": "Belize",
                    "CA": "Canada",
                    "CC": "Cocos Islands",
                    "CD": "The Democratic Republic Of Congo",
                    "CF": "Central African Republic",
                    "CG": "Congo",
                    "CH": "Switzerland",
                    "CI": "Côte d'Ivoire",
                    "CK": "Cook Islands",
                    "CL": "Chile",
                    "CM": "Cameroon",
                    "CN": "China",
                    "CO": "Colombia",
                    "CR": "Costa Rica",
                    "CU": "Cuba",
                    "CV": "Cape Verde",
                    "CW": "Curaçao",
                    "CX": "Christmas Island",
                    "CY": "Cyprus",
                    "CZ": "Czech Republic",
                    "DE": "Germany",
                    "DJ": "Djibouti",
                    "DK": "Denmark",
                    "DM": "Dominica",
                    "DO": "Dominican Republic",
                    "DZ": "Algeria",
                    "EC": "Ecuador",
                    "EE": "Estonia",
                    "EG": "Egypt",
                    "EH": "Western Sahara",
                    "ER": "Eritrea",
                    "ES": "Spain",
                    "ET": "Ethiopia",
                    "FI": "Finland",
                    "FJ": "Fiji",
                    "FK": "Falkland Islands",
                    "FM": "Micronesia",
                    "FO": "Faroe Islands",
                    "FR": "France",
                    "GA": "Gabon",
                    "GB": "United Kingdom",
                    "GD": "Grenada",
                    "GE": "Georgia",
                    "GF": "French Guiana",
                    "GG": "Guernsey",
                    "GH": "Ghana",
                    "GI": "Gibraltar",
                    "GL": "Greenland",
                    "GM": "Gambia",
                    "GN": "Guinea",
                    "GP": "Guadeloupe",
                    "GQ": "Equatorial Guinea",
                    "GR": "Greece",
                    "GS": "South Georgia And The South Sandwich Islands",
                    "GT": "Guatemala",
                    "GU": "Guam",
                    "GW": "Guinea-Bissau",
                    "GY": "Guyana",
                    "HK": "Hong Kong",
                    "HM": "Heard Island And McDonald Islands",
                    "HN": "Honduras",
                    "HR": "Croatia",
                    "HT": "Haiti",
                    "HU": "Hungary",
                    "ID": "Indonesia",
                    "IE": "Ireland",
                    "IL": "Israel",
                    "IM": "Isle Of Man",
                    "IN": "India",
                    "IO": "British Indian Ocean Territory",
                    "IQ": "Iraq",
                    "IR": "Iran",
                    "IS": "Iceland",
                    "IT": "Italy",
                    "JE": "Jersey",
                    "JM": "Jamaica",
                    "JO": "Jordan",
                    "JP": "Japan",
                    "KE": "Kenya",
                    "KG": "Kyrgyzstan",
                    "KH": "Cambodia",
                    "KI": "Kiribati",
                    "KM": "Comoros",
                    "KN": "Saint Kitts And Nevis",
                    "KP": "North Korea",
                    "KR": "South Korea",
                    "KW": "Kuwait",
                    "KY": "Cayman Islands",
                    "KZ": "Kazakhstan",
                    "LA": "Laos",
                    "LB": "Lebanon",
                    "LC": "Saint Lucia",
                    "LI": "Liechtenstein",
                    "LK": "Sri Lanka",
                    "LR": "Liberia",
                    "LS": "Lesotho",
                    "LT": "Lithuania",
                    "LU": "Luxembourg",
                    "LV": "Latvia",
                    "LY": "Libya",
                    "MA": "Morocco",
                    "MC": "Monaco",
                    "MD": "Moldova",
                    "ME": "Montenegro",
                    "MF": "Saint Martin",
                    "MG": "Madagascar",
                    "MH": "Marshall Islands",
                    "MK": "Macedonia",
                    "ML": "Mali",
                    "MM": "Myanmar",
                    "MN": "Mongolia",
                    "MO": "Macao",
                    "MP": "Northern Mariana Islands",
                    "MQ": "Martinique",
                    "MR": "Mauritania",
                    "MS": "Montserrat",
                    "MT": "Malta",
                    "MU": "Mauritius",
                    "MV": "Maldives",
                    "MW": "Malawi",
                    "MX": "Mexico",
                    "MY": "Malaysia",
                    "MZ": "Mozambique",
                    "NA": "Namibia",
                    "NC": "New Caledonia",
                    "NE": "Niger",
                    "NF": "Norfolk Island",
                    "NG": "Nigeria",
                    "NI": "Nicaragua",
                    "NL": "Netherlands",
                    "NO": "Norway",
                    "NP": "Nepal",
                    "NR": "Nauru",
                    "NU": "Niue",
                    "NZ": "New Zealand",
                    "OM": "Oman",
                    "PA": "Panama",
                    "PE": "Peru",
                    "PF": "French Polynesia",
                    "PG": "Papua New Guinea",
                    "PH": "Philippines",
                    "PK": "Pakistan",
                    "PL": "Poland",
                    "PM": "Saint Pierre And Miquelon",
                    "PN": "Pitcairn",
                    "PR": "Puerto Rico",
                    "PS": "Palestine",
                    "PT": "Portugal",
                    "PW": "Palau",
                    "PY": "Paraguay",
                    "QA": "Qatar",
                    "RE": "Reunion",
                    "RO": "Romania",
                    "RS": "Serbia",
                    "RU": "Russia",
                    "RW": "Rwanda",
                    "SA": "Saudi Arabia",
                    "SB": "Solomon Islands",
                    "SC": "Seychelles",
                    "SD": "Sudan",
                    "SE": "Sweden",
                    "SG": "Singapore",
                    "SH": "Saint Helena",
                    "SI": "Slovenia",
                    "SJ": "Svalbard And Jan Mayen",
                    "SK": "Slovakia",
                    "SL": "Sierra Leone",
                    "SM": "San Marino",
                    "SN": "Senegal",
                    "SO": "Somalia",
                    "SR": "Suriname",
                    "SS": "South Sudan",
                    "ST": "Sao Tome And Principe",
                    "SV": "El Salvador",
                    "SX": "Sint Maarten (Dutch part)",
                    "SY": "Syria",
                    "SZ": "Swaziland",
                    "TC": "Turks And Caicos Islands",
                    "TD": "Chad",
                    "TF": "French Southern Territories",
                    "TG": "Togo",
                    "TH": "Thailand",
                    "TJ": "Tajikistan",
                    "TK": "Tokelau",
                    "TL": "Timor-Leste",
                    "TM": "Turkmenistan",
                    "TN": "Tunisia",
                    "TO": "Tonga",
                    "TR": "Turkey",
                    "TT": "Trinidad and Tobago",
                    "TV": "Tuvalu",
                    "TW": "Taiwan",
                    "TZ": "Tanzania",
                    "UA": "Ukraine",
                    "UG": "Uganda",
                    "UM": "United States Minor Outlying Islands",
                    "US": "United States",
                    "UY": "Uruguay",
                    "UZ": "Uzbekistan",
                    "VA": "Vatican",
                    "VC": "Saint Vincent And The Grenadines",
                    "VE": "Venezuela",
                    "VG": "British Virgin Islands",
                    "VI": "U.S. Virgin Islands",
                    "VN": "Vietnam",
                    "VU": "Vanuatu",
                    "WF": "Wallis And Futuna",
                    "WS": "Samoa",
                    "YE": "Yemen",
                    "YT": "Mayotte",
                    "ZA": "South Africa",
                    "ZM": "Zambia",
                    "ZW": "Zimbabwe"
                };

            } // end main function
        ]
    );
})();