(function () {

    'use strict';

    var controller_name = "cryptonomica.controller.verifyEthAddress";

// add to: 
// index.html +
// controller.js +
// router.js + ( url: '/ethereumVerification/{fingerprint}')

    var controller = angular.module(controller_name, []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
    controller.config(function ($logProvider) {
            // $logProvider.debugEnabled(false);
            $logProvider.debugEnabled(true);
        }
    );

    /*
    * This controller has 3 main functions:
    * 1) request and show the data from smart contract
    * 2) upload signed string
    * 3) request verification from backend server
    * */

    controller.controller(controller_name, [
        '$scope',
        '$sce',
        '$rootScope',
        '$http',
        'GApi',
        'GAuth',
        'GData',
        '$state',
        '$log',
        '$cookies',
        '$timeout',
        '$window',
        '$stateParams',
        function verifyEthAddressCtrl($scope,    // <- name for debugging
                                      $sce,
                                      $rootScope,
                                      $http,
                                      GApi,
                                      GAuth,
                                      GData,
                                      $state,
                                      $log,
                                      $cookies,
                                      $timeout,
                                      $window,
                                      $stateParams) {

            /*
            * in the view add id with controller name to parent div:
            * <div id="verifyEthAddressCtrl">
            * use this id to access $scope in browser console:
            * angular.element(document.getElementById('verifyEthAddressCtrl')).scope();
            * for example: for example: $scope = angular.element(document.getElementById('verifyEthAddressCtrl')).scope();
            * */

            $log.debug(controller_name, "started"); //
            $log.debug('$state');
            $log.debug($state);
            $timeout($rootScope.progressbar.complete(), 1000);

            $scope.smartContractData = {};
            $scope.showDescription = true;

            /* --- Alerts */
            $scope.alertDanger = null;  // red
            $scope.alertWarning = null; // yellow
            $scope.alertInfo = null;    // blue
            $scope.alertSuccess = null; // green
            $scope.alertMessage = {}; // grey

            $scope.setAlertDanger = function (message) {
                $scope.alertDanger = message;
                $log.debug("$scope.alertDanger:", $scope.alertDanger);
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
            /* ---- */
            // example from:
            // https://github.com/trufflesuite/truffle-artifactor#artifactorgenerateoptions-networks
            var networks = {
                "1": {        // Main network
                    "networkName": "Main Ethereum Network",
                    "address": "0x846942953c3b2A898F10DF1e32763A823bf6b27f", // contract address
                    "contractAddress": "0x846942953c3b2A898F10DF1e32763A823bf6b27f",
                    "ownerAddress": "0xDADfa63d05D01f536930F1150238283Fe917D28c",
                    "etherscanLinkPrefix": "https://etherscan.io/"
                },
                "2": {        // Morden network
                    "networkName": undefined,
                    "address": undefined, // contract address
                    "contractAddress": undefined,
                    "ownerAddress": undefined,
                    "etherscanLinkPrefix": undefined
                },
                "3": {        // Ropsten network
                    "networkName": "Ropsten Test Network",
                    "address": undefined, // contract address
                    "contractAddress": undefined,
                    "ownerAddress": undefined,
                    "etherscanLinkPrefix": "https://ropsten.etherscan.io/"
                },
                "4": {        // Rinkeby network
                    "networkName": "Rinkeby",
                    "address": undefined, // contract address
                    "contractAddress": undefined,
                    "ownerAddress": undefined,
                    "etherscanLinkPrefix": "https://rinkeby.etherscan.io/"
                },
                "1337": {     // private network
                    "networkName": undefined,
                    "address": undefined, // contract address
                    "contractAddress": undefined,
                    "ownerAddress": undefined,
                    "etherscanLinkPrefix": undefined
                }
            };

            $rootScope.currentNetwork = {
                'network_id': '', // integer
                'networkName': '',
                'node': '',
                'ethereumProtocolVersion': '',
                'connected': false
            };

            /* web3 instantiation */
            // to access web3 instance in browser console:
            // angular.element('body').scope().$root.web3
            // see 'NOTE FOR DAPP DEVELOPERS' on https://github.com/ethereum/mist/releases/tag/v0.9.0
            // To instantiate your (self-included) web3.js lib you can use:
            if (typeof window.web3 !== 'undefined') {
                $log.debug('[ethVerificationCtrl] web3 object presented by provider:');
                $log.debug(window.web3.currentProvider);
                $rootScope.web3 = new Web3(window.web3.currentProvider);
                $log.debug('[ethVerificationCtrl] and will be used in $rootScope.web3:');
                $log.debug($rootScope.web3);
            } else {
                $log.debug('[ethVerificationCtrl] web3 object is not provided by client app');
                $log.debug('[ethVerificationCtrl] and will be instantiated from web3.js lib');
                try {
                    $rootScope.web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
                    $log.debug('[ethVerificationCtrl] $rootScope.web3: ');
                    $log.debug($rootScope.web3);
                } catch (error) {
                    $log.error(error);
                    $scope.setAlertDanger(error);
                    return;
                }
            }

            /* Web Storage API , https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API/Using_the_Web_Storage_API */
            var storageType = 'localStorage'; // or 'sessionStorage'
            var storageAvailable = function () {
                try {
                    var storage = window[storageType],
                        x = '__storage_test__';
                    storage.setItem(x, x);
                    storage.removeItem(x);
                    return true;
                }
                catch (e) {
                    return e instanceof DOMException && (
                            // everything except Firefox
                        e.code === 22 ||
                        // Firefox
                        e.code === 1014 ||
                        // test name field too, because code might not be present
                        // everything except Firefox
                        e.name === 'QuotaExceededError' ||
                        // Firefox
                        e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
                        // acknowledge QuotaExceededError only if there's something already stored
                        storage.length !== 0;
                }
            }();
            $log.debug("storageAvailable: " + storageAvailable);

            // > don't do this, users upload false (old) strings
            if (storageAvailable) {
                // > don't do this, users upload false (old) strings
                // $scope.signedString = localStorage.getItem('signedString');
                $scope.privateKeyArmored = localStorage.getItem("myPrivateKey");
                if ($scope.privateKeyArmored) {
                    $scope.privateKeyInLocalStorage = openpgp.key.readArmored($scope.privateKeyArmored).keys[0];
                    $scope.privateKeyInLocalStorageFingerprint = $scope.privateKeyInLocalStorage.primaryKey.fingerprint.toUpperCase();
                    $scope.signButtonEnabled = true;
                    if ($stateParams.fingerprint && $scope.privateKeyInLocalStorageFingerprint && $stateParams.fingerprint !== $scope.privateKeyInLocalStorageFingerprint) {
                        $scope.signButtonEnabled = false;
                    }
                }
            }

            $scope.userProfileGeneralView = null;
            $scope.userKeys = [];
            $scope.fingerprint = null; // used in <select>
            //
            $rootScope.progressbar.start(); // <<<<<<<<<<<
            GAuth.checkAuth()
                .then(function () {
                    return GApi.executeAuth('cryptonomicaUserAPI', 'getMyUserData'); // async
                })
                .then(function (resp) {
                    // (!) we always have response
                    if (!$rootScope.currentUser || $rootScope.currentUser === {}) {
                        $rootScope.currentUser = resp;
                        $log.info("[" + controller_name + "]" + " $rootScope.currentUser: ");
                        $log.info($rootScope.currentUser);
                    }

                    if (!$rootScope.currentUser.loggedIn) { // user not logged in
                        return Promise.reject("User is not logged in. Click 'Login' and/or refresh the page");
                    } else if (!$rootScope.currentUser.registeredCryptonomicaUser) { // user not registered
                        return Promise.reject("user is not registered, please go to 'Registration' page");
                    }

                    return GApi.executeAuth('userSearchAndViewAPI', 'getUserProfileById', {"id": $rootScope.currentUser.userId});
                })
                .then(function (resp) {

                    $scope.userProfileGeneralView = resp; // net.cryptonomica.returns.UserProfileGeneralView
                    $log.info("$scope.userProfileGeneralView:");
                    $log.info($scope.userProfileGeneralView);

                    $log.debug("$scope.userProfileGeneralView.pgpPublicKeyGeneralViews :");
                    $log.debug($scope.userProfileGeneralView.pgpPublicKeyGeneralViews);

                    if ($scope.userProfileGeneralView.hasOwnProperty("pgpPublicKeyGeneralViews")
                        && $scope.userProfileGeneralView.pgpPublicKeyGeneralViews
                        && $scope.userProfileGeneralView.pgpPublicKeyGeneralViews.length > 0) {
                        for (var i = 0; i < $scope.userProfileGeneralView.pgpPublicKeyGeneralViews.length; i++) {

                            var keyEntityFromArray = $scope.userProfileGeneralView.pgpPublicKeyGeneralViews[i];

                            if (keyEntityFromArray.exp) {
                                if (new Date(keyEntityFromArray.exp) < new Date()) {
                                    keyEntityFromArray.expired = true;
                                    $log.debug('key ' + keyEntityFromArray.keyID + 'expired:' + new Date(keyEntityFromArray.exp));
                                }
                            }

                            if ((!keyEntityFromArray.expired) && (keyEntityFromArray.verifiedOnline || keyEntityFromArray.verifiedOffline) && (!keyEntityFromArray.revoked)) {
                                $scope.userKeys.push(keyEntityFromArray.fingerprint);
                            }
                        }

                        // $scope.$apply(); // not needed here
                        $log.debug("$scope.userKeys:");
                        $log.debug($scope.userKeys);

                        if ($scope.userKeys.indexOf($scope.privateKeyInLocalStorageFingerprint) > -1) {
                            $scope.fingerprint = $scope.privateKeyInLocalStorageFingerprint;
                        }

                        if ($scope.userKeys.indexOf($stateParams.fingerprint) > -1) {
                            $scope.fingerprint = $stateParams.fingerprint;
                        }

                    } //

                    $scope.sign = function () {

                        if (!$scope.privateKeyInLocalStorage) { // openpgpjs obj
                            return;
                        } else {
                            $log.debug("$scope.privateKeyInLocalStorage");
                            $log.debug($scope.privateKeyInLocalStorage);
                        }

                        var passphrase = prompt("Please enter password for private key "
                            + $scope.privateKeyInLocalStorageFingerprint
                            + " (check if you use the key you really intended to use)"
                        );
                        if ($rootScope.stringIsNullUndefinedOrEmpty(passphrase)) {
                            $scope.setAlertDanger("Password is empty");
                            return;
                        }

                        var privateKeyEncrypted;

                        try {
                            privateKeyEncrypted = openpgp.key.readArmored($scope.privateKeyArmored).keys[0];
                            console.log("typeof privateKeyEncrypted: ", typeof privateKeyEncrypted);

                            var privateKeyDecryptionResult = privateKeyEncrypted.decrypt(passphrase); // boolean

                            if (!privateKeyDecryptionResult) {
                                $scope.setAlertDanger("Can not decrypt private key with fingerprint ", $scope.privateKeyInLocalStorageFingerprint);
                                return;
                            }

                        } catch (error) {
                            $scope.setAlertDanger(error);
                            console.log(error);
                            return;
                        }

                        var privateKeyDecrypted = privateKeyEncrypted;
                        var messageToSign = "I hereby confirm that the address "
                            + $scope.ethAccount.toLowerCase()
                            + " is my Ethereum address";
                        var signObj = {
                            data: messageToSign, // cleartext input to be signed
                            privateKeys: privateKeyDecrypted, // array of keys or single key with decrypted secret key data to sign cleartext
                            armor: true // (optional) if the return value should be ascii armored or the message object
                        };
                        var signedMessageObj = {};
                        // see: https://openpgpjs.org/openpgpjs/doc/openpgp.js.html#line285
                        // https://openpgpjs.org/openpgpjs/doc/module-openpgp.html
                        openpgp.sign(signObj)
                            .then(function (res) { //
                                // @return {Promise<String|CleartextMessage>} ASCII armored message or the message of type CleartextMessage
                                signedMessageObj = res;
                                console.log(JSON.stringify(signedMessageObj));
                                console.log(signedMessageObj.data);
                                $scope.signedString = signedMessageObj.data;
                                privateKeyDecrypted = null;
                                passphrase = null;
                                $scope.$apply();
                                $rootScope.goTo("signedStringInput");
                            })
                            .catch(function (error) {
                                console.log("sign message error:");
                                console.log(error);
                                $scope.setAlertDanger(error);
                                privateKeyDecrypted = null;
                                passphrase = null;
                            });
                    };

                    // ---->>>>

                    if ($rootScope.web3.isConnected()) {
                        $log.debug('[ethVerificationCtrl] $rootScope.web3.isConnected() : true ');
                        $rootScope.currentNetwork.connected = true;
                        // $rootScope.$apply();

                        // get network info:
                        $rootScope.web3.version.getNode(function (error, result) {
                                if (error) {
                                    $log.debug(error);
                                } else {
                                    $rootScope.currentNetwork.node = result;
                                    $rootScope.$apply();
                                    $log.debug('[ethVerificationCtrl] web3.version.node: ' + $rootScope.currentNetwork.node);
                                    // "Geth/v1.7.2-stable-1db4ecdc/linux-amd64/go1.9"
                                }
                            }
                        );

                        $rootScope.web3.version.getEthereum(function (error, result) {
                                if (error) {
                                    $log.debug(error);
                                } else {
                                    $rootScope.currentNetwork.ethereumProtocolVersion = result;
                                    $rootScope.$apply();
                                    $log.debug('[ethVerificationCtrl] web3.version.ethereum: ' + $rootScope.currentNetwork.ethereumProtocolVersion);
                                    // the Ethereum protocol version (like: web3.version.ethereum: 0x3f )
                                }
                            }
                        );//
                        // https://github.com/ethereum/wiki/wiki/JavaScript-API#web3ethblocknumber
                        $rootScope.web3.eth.getBlockNumber(function (error, result) {
                                if (error) {
                                    $log.debug(error);
                                } else {
                                    $rootScope.currentNetwork.lastKnownBlock = result;
                                    $rootScope.$apply();
                                    $log.debug('[ethVerificationCtrl] $rootScope.currentNetwork.lastKnownBlock: ' + $rootScope.currentNetwork.lastKnownBlock);
                                }
                            }
                        );//
                        $rootScope.currentNetwork.web3Version = $rootScope.web3.version.api;

                        if ($rootScope.web3.eth.defaultAccount) {
                            $scope.ethAccount = $rootScope.web3.eth.defaultAccount;
                        } else if ($rootScope.web3.eth.accounts[0]) {
                            $scope.ethAccount = $rootScope.web3.eth.accounts[0];
                            $rootScope.web3.eth.defaultAccount = $rootScope.web3.eth.accounts[0];
                        } else {

                            $scope.setAlertDanger(
                                "Ethereum Account not recognized."
                                + "Please connect your account in MetaMask or Mist and reload this page. "
                            );
                            return;
                        }

                        $rootScope.web3.version.getNetwork(function (error, result) {

                            if (error) {
                                $scope.alertDanger = error;
                                $scope.$apply();
                                $log.error(error);
                            } else {
                                if (result !== "1") { // not MainNet
                                    $scope.setAlertDanger(
                                        "Service works on Ethereum MainNet, but you are connected to another Ethereum Network"
                                    );
                                }

                                $rootScope.currentNetwork.network_id = result; // "3" for Ropsten, "1" for MainNet etc.
                                $rootScope.currentNetwork.networkName = networks[result].networkName;
                                $rootScope.etherscanLinkPrefix = networks[result].etherscanLinkPrefix;

                                $rootScope.$apply(); // needed here
                                $scope.$apply(); // needed here

                                $log.debug(
                                    '[ethVerificationCtrl] web3.version.network: '
                                    + $rootScope.currentNetwork.network_id
                                );

                                // >>> start creating contract instance
                                $.getJSON(
                                    '/app/ethereum/CryptonomicaVerification.json', // see: https://github.com/trufflesuite/truffle-contract-schema
                                    function (data) { // async

                                        $log.info('[ethVerificationCtrl] /app/ethereum/CryptonomicaVerification.json :');
                                        $log.info(data);

                                        var CryptonomicaVerificationContract = TruffleContract(data);
                                        CryptonomicaVerificationContract.setProvider($rootScope.web3.currentProvider); // <- !

                                        var contractAddress = networks[$rootScope.currentNetwork.network_id].contractAddress;
                                        $log.debug('[ethVerificationCtrl] contractAddress: ', contractAddress);

                                        // >>> instantiate smart contract
                                        CryptonomicaVerificationContract.at(contractAddress).then(function (instance) {

                                                $scope.contract = instance;
                                                $scope.$apply();

                                                $log.debug('$scope.contract:');
                                                $log.debug($scope.contract);

                                                $scope.smartContractData = {};

                                                // get price for verification
                                                $scope.contract.priceForVerificationInWei.call()
                                                    .then(function (priceForVerificationInWei) {
                                                        $log.debug('[$scope.requestDataFromSmartContract] priceForVerificationInWei:');
                                                        $log.debug(priceForVerificationInWei.toNumber());
                                                        $scope.smartContractData.priceForVerificationInWei = priceForVerificationInWei.toNumber();
                                                        // https://github.com/ethereum/wiki/wiki/JavaScript-API#web3fromwei
                                                        $scope.smartContractData.priceForVerificationInEth =
                                                            $rootScope.web3.fromWei(
                                                                priceForVerificationInWei.toNumber(),
                                                                "ether"
                                                            );
                                                        $log.debug("$scope.smartContractData.priceForVerificationInEth:");
                                                        $log.debug($scope.smartContractData.priceForVerificationInEth);
                                                        $scope.$apply();
                                                        // $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                    })
                                                    .catch(function (error) {
                                                            $log.debug('$scope.contract.priceForVerificationInWei.call error:');
                                                            $log.error(error);
                                                            $scope.requestDataFromSmartContractError = {};
                                                            $scope.requestDataFromSmartContractError.error = true;
                                                            $scope.requestDataFromSmartContractError.priceForVerificationInWei = error;
                                                            $scope.$apply();
                                                            // $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                        }
                                                    );

                                                /* ------ functions for buttons */

                                                if ($scope.ethAccount) {
                                                    $scope.smartContractData.stringToSign =
                                                        "I hereby confirm that the address "
                                                        + $scope.ethAccount.toLowerCase()
                                                        + " is my Ethereum address";
                                                } else {
                                                    $scope.smartContractData.stringToSign = "Can not provide string to sign: your current ETH account not detected"
                                                }

                                                $scope.requestDataFromSmartContract = function () {
                                                    $rootScope.progressbar.start(); // <<<<<<<
                                                    $scope.requestDataFromSmartContractIsWorking = true;

                                                    $scope.checkSignatureError = null;

                                                    // if ($scope.ethAccount) {
                                                    //     $scope.smartContractData.stringToSign =
                                                    //         "I hereby confirm that the address "
                                                    //         + $scope.ethAccount.toLowerCase()
                                                    //         + " is my Ethereum address";
                                                    // } else {
                                                    //     $scope.smartContractData.stringToSign = "Can not provide string to sign: your current ETH account not detected"
                                                    // }

                                                    if ($scope.fingerprint) {
                                                        $log.debug('$scope.requestDataFromSmartContract() started');
                                                        $scope.requestDataFromSmartContractError = {};
                                                        // $scope.smartContractData = {}; // <<< not here because of $scope.smartContractData.stringToSign
                                                        // (1) mapping(bytes20 => address) public addressAttached
                                                        $scope.contract.addressAttached.call("0x" + $scope.fingerprint)
                                                            .then(function (ethAddressConnectedToFingerprint) {
                                                                    $scope.smartContractData.ethAddressConnectedToFingerprint = ethAddressConnectedToFingerprint;
                                                                    $scope.$apply();
                                                                    $log.info('[$scope.requestDataFromSmartContract] $scope.smartContractData.ethAddressConnectedToFingerprint: ');
                                                                    $log.info($scope.smartContractData.ethAddressConnectedToFingerprint);
                                                                    // --------------------- with ethAddressConnectedToFingerprint:
                                                                    // (0)
                                                                    // TODO: think about this:
                                                                    var acc = ethAddressConnectedToFingerprint;
                                                                    if (acc === "0x0000000000000000000000000000000000000000") {
                                                                        acc = $scope.ethAccount;
                                                                    }
                                                                    $scope.contract.verification.call(acc)
                                                                        .then(
                                                                            function (verification) {

                                                                                $log.debug('[$scope.requestDataFromSmartContract] verification:');
                                                                                $log.debug(verification);

                                                                                $scope.smartContractData.fingerprint = verification[0]; // < verified
                                                                                $scope.smartContractData.keyCertificateValidUntilUnixTime = verification[1].toNumber();
                                                                                $scope.smartContractData.keyCertificateValidUntilDate =
                                                                                    $rootScope.dateFromUnixTime(
                                                                                        $scope.smartContractData.keyCertificateValidUntilUnixTime
                                                                                    );
                                                                                $scope.smartContractData.firstName = verification[2];
                                                                                $scope.smartContractData.lastName = verification[3];
                                                                                $scope.smartContractData.birthDateUnixTime = verification[4].toNumber();
                                                                                $scope.smartContractData.birthDate =
                                                                                    $rootScope.dateFromUnixTime(
                                                                                        $scope.smartContractData.birthDateUnixTime
                                                                                    );
                                                                                $scope.smartContractData.nationality = verification[5];
                                                                                $scope.smartContractData.verificationAddedOnUnixTime = verification[6].toNumber();
                                                                                $scope.smartContractData.verificationAddedOnDate =
                                                                                    $rootScope.dateFromUnixTime(
                                                                                        $scope.smartContractData.verificationAddedOnUnixTime
                                                                                    );
                                                                                $scope.smartContractData.revokedOnUnixTime = verification[7].toNumber();
                                                                                $scope.smartContractData.revokedOnDate =
                                                                                    $rootScope.dateFromUnixTime(
                                                                                        $scope.smartContractData.revokedOnUnixTime
                                                                                    );
                                                                                $scope.smartContractData.signedString = verification[8];

                                                                                $scope.$apply();
                                                                                return $scope.contract.signedStringUploadedOnUnixTime.call(acc);
                                                                            }
                                                                        )
                                                                        // TODO: duplicate:
                                                                        .then(function (signedStringUploadedOnUnixTime) {
                                                                            $log.debug("signedStringUploadedOnUnixTime:", signedStringUploadedOnUnixTime);
                                                                            $log.debug("signedStringUploadedOnUnixTime.toNumber():", signedStringUploadedOnUnixTime.toNumber());
                                                                            $scope.smartContractData.signedStringUploadedOnUnixTime = signedStringUploadedOnUnixTime.toNumber();
                                                                            $scope.smartContractData.signedStringUploadedOnDate =
                                                                                $rootScope.dateFromUnixTime(
                                                                                    $scope.smartContractData.signedStringUploadedOnUnixTime
                                                                                );
                                                                            $scope.requestDataFromSmartContractIsWorking = false;
                                                                            $scope.$apply();
                                                                            $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                                        })
                                                                        .catch(function (error) {
                                                                            $log.debug('$scope.contract.verification.call() error:');
                                                                            $log.error(error);
                                                                            $scope.requestDataFromSmartContractError.error = true;
                                                                            $scope.requestDataFromSmartContractError.verification = error;
                                                                            $scope.requestDataFromSmartContractIsWorking = false;
                                                                            $scope.$apply();
                                                                            $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                                        });
                                                                    //----------------------
                                                                }
                                                            ).catch(function (error) {
                                                            $log.debug('$scope.contract.getAddressConnectedToFingerprint.call error:');
                                                            $log.error(error);
                                                            $scope.requestDataFromSmartContractError.error = true;
                                                            $scope.requestDataFromSmartContractError.getAddressConnectedToFingerprint = error;
                                                            $scope.$apply();
                                                        });
                                                    } else {
                                                        $scope.setAlertDanger("fingerprint not provided, can not request data from smart contract");
                                                        $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                    }

                                                };

                                                // if $scope.fingerprint defined, run:
                                                if ($scope.fingerprint) {
                                                    $scope.requestDataFromSmartContract();
                                                }

                                                /* ---- UPLOAD SIGNED STRING */
                                                $scope.uploadSignedStringButtonDisabled = true;
                                                $scope.uploadSignedString = function () {
                                                    $rootScope.progressbar.start(); // <<<<<<<
                                                    $log.debug('$scope.uploadSignedString() started;');

                                                    $log.debug('signed string to upload:');
                                                    $log.debug($scope.signedString);
                                                    $scope.uploadSignedStringError = null;
                                                    $scope.uploadSignedStringWorking = true;

                                                    if ($rootScope.stringIsNullUndefinedOrEmpty($scope.signedString)) {
                                                        $scope.uploadSignedStringError = 'Signed string not provided';
                                                        $scope.uploadSignedStringWorking = false;
                                                        $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                        return;
                                                    }
                                                    // check if provided text is valid OpenPGP signed message:
                                                    try {
                                                        openpgp.cleartext.readArmored($scope.signedString);
                                                    } catch (error) {
                                                        console.log(error.message);
                                                        $scope.uploadSignedStringError =
                                                            "This is not valid OpenPGP signed text";
                                                        $scope.uploadSignedStringWorking = false;
                                                        $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                        return;
                                                    }
                                                    $scope.uploadSignedStringTx = null;
                                                    $scope.contract.priceForVerificationInWei.call()
                                                        .then(function (priceForVerificationInWei) {

                                                                $scope.priceForVerificationInWei = priceForVerificationInWei.toNumber();
                                                                $log.debug('[ethVerificationCtrl] priceForVerificationInWei:');
                                                                $log.debug(priceForVerificationInWei);
                                                                // https://github.com/ethereum/wiki/wiki/JavaScript-API#parameters-25
                                                                var txParameters = {};
                                                                if ($scope.priceForVerificationInWei === null) {
                                                                    $scope.uploadSignedStringError =
                                                                        'Can not get price for verification from smart contract';
                                                                    $scope.uploadSignedStringWorking = false;
                                                                    $scope.$apply();
                                                                    $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                                    return;
                                                                }

                                                                txParameters.value = $scope.priceForVerificationInWei;
                                                                txParameters.gasPrice = $scope.gasPrice = 19 * 1000000000; // < this will be price proposed by MetaMask
                                                                txParameters.from = $scope.ethAccount;
                                                                // txParameters.gas = ; //

                                                                $log.debug('$scope.uploadSignedString txParameters: ');
                                                                $log.debug(txParameters);

                                                                // CryptonomicaVerification.sol :
                                                                // function uploadSignedString(string _fingerprint, bytes20 _fingerprintBytes20, string _signedString) public payable {
                                                                $scope.contract.uploadSignedString(
                                                                    $scope.fingerprint,
                                                                    // $stateParams.fingerprint,
                                                                    // as a key we use fingerprint as bytes32, like 0x57A5FEE5A34D563B4B85ADF3CE369FD9E77173E5
                                                                    "0x" + $scope.fingerprint,
                                                                    // "0x" + $stateParams.fingerprint,
                                                                    $scope.signedString,
                                                                    txParameters
                                                                ).then(
                                                                    function (tx) {
                                                                        $log.debug('[ethVerificationCtrl] uploadSignedString result:');
                                                                        $log.debug(tx);
                                                                        $scope.uploadSignedStringTx = tx;
                                                                        // $scope.requestDataFromSmartContract();// < update data (async)
                                                                        $scope.uploadSignedStringWorking = false;
                                                                        $scope.$apply();
                                                                        $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                                    }
                                                                ).catch(function (error) {
                                                                        $log.debug('$scope.contract.uploadSignedString error:');
                                                                        $log.error(error);
                                                                        $scope.uploadSignedStringError = error.toString();
                                                                        if ($scope.uploadSignedStringError.indexOf("wasn't processed in 240 seconds") >= 0) {
                                                                            $scope.uploadSignedStringError = $scope.uploadSignedStringError +
                                                                                " (but it can be mined, please, update/refresh data from smart contract)"
                                                                        }
                                                                        $scope.uploadSignedStringWorking = false;
                                                                        $scope.$apply();
                                                                        $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                                    }
                                                                );
                                                            }
                                                        ).catch(function (error) {
                                                            $scope.uploadSignedStringError = error.toString();
                                                            $log.debug('$scope.contract.priceForVerificationInWei.call() error:');
                                                            $log.error(error);
                                                            $scope.uploadSignedStringWorking = false;
                                                            $scope.$apply();
                                                            $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                                                        }
                                                    );
                                                };

                                                /* --------- CHECK SIGNATURE */

                                                $scope.checkSignature = function () {
                                                    $rootScope.progressbar.start(); // <<<<<<<
                                                    $scope.checkSignatureIsWorking = true;
                                                    $scope.checkSignatureError = null;

                                                    if (!$scope.fingerprint) {
                                                        $scope.checkSignatureError = "OpenPGP key not selected";
                                                        $scope.checkSignatureIsWorking = false;
                                                        $timeout($rootScope.progressbar.complete(), 1000);
                                                        return;
                                                    } else if (!$scope.signedString) {
                                                        $scope.checkSignatureError = "No signed string";
                                                        $scope.checkSignatureIsWorking = false;
                                                        $timeout($rootScope.progressbar.complete(), 1000);
                                                        return;
                                                    }

                                                    GApi.executeAuth(
                                                        'pgpPublicKeyAPI',
                                                        'getPGPPublicKeyByFingerprint',
                                                        {"fingerprint": $scope.fingerprint}
                                                    ).then(function (resp) {
                                                            $log.info("PGPPublicKeyGeneralView: ");
                                                            $log.info(resp);
                                                            $scope.pgpPublicKeyGeneralView = resp;

                                                            var options = {};
                                                            options.message = openpgp.cleartext.readArmored($scope.signedString);
                                                            options.publicKeys = openpgp.key.readArmored(resp.asciiArmored).keys;
                                                            return openpgp.verify(options);
                                                        }
                                                    ).then(function (verified) {
                                                        // console.log("verified:");
                                                        // console.log(verified);
                                                        console.log("verified.signatures:");
                                                        console.log(verified.signatures);
                                                        $scope.signatureValid = verified.signatures[0].valid; // true/false
                                                        if ($scope.signatureValid) {
                                                            $scope.checkSignatureResult = "Signature is valid for OpenPGP key"
                                                                + $scope.pgpPublicKeyGeneralView.fingerprint;
                                                            $scope.uploadSignedStringButtonDisabled = false;
                                                        } else {
                                                            $scope.checkSignatureError = "Signature is NOT valid for OpenPGP key"
                                                                + $scope.pgpPublicKeyGeneralView.fingerprint;
                                                            $scope.uploadSignedStringButtonDisabled = true;
                                                        }
                                                        $scope.checkSignatureIsWorking = false;
                                                        // $scope.$apply(); // not needed here
                                                        $timeout($rootScope.progressbar.complete(), 1000);
                                                    }).catch(function (error) {
                                                        $log.error(" $scope.checkSignature error: ");
                                                        $log.error(error);
                                                        $scope.checkSignatureError = error.message;
                                                        $scope.checkSignatureIsWorking = false;
                                                        // $scope.$apply(); // not needed here
                                                        $timeout($rootScope.progressbar.complete(), 1000);
                                                    });
                                                };

                                                /* --------- VERIFY */
                                                $scope.verify = function () {
                                                    $rootScope.progressbar.start(); // <<<<<<<
                                                    $scope.verifyWorking = true;
                                                    GApi.executeAuth(
                                                        'ethNodeAPI',
                                                        // 'verifyEthAddress', //
                                                        'verifyEthAddressInfura',
                                                        {"ethereumAcc": $scope.ethAccount}
                                                    ).then(
                                                        function (resp) {
                                                            $log.info("$scope.verify() resp: ");
                                                            $log.info(resp);
                                                            $scope.verificationResult = "EHT address verified, please refresh smart contract data";
                                                            $scope.requestDataFromSmartContract();
                                                            $scope.verifyWorking = false;
                                                            // $scope.$apply(); // not needed here
                                                            $timeout($rootScope.progressbar.complete(), 1000);
                                                        },
                                                        function (error) {
                                                            $log.error("$scope.verify() error: ");
                                                            $log.error(error);
                                                            $scope.verificationError = error.message;
                                                            $scope.verifyWorking = false;
                                                            // $scope.$apply(); // not needed here
                                                            $timeout($rootScope.progressbar.complete(), 1000);
                                                        }
                                                    );
                                                };

                                            } // end of function (instance) {
                                        )

                                    } // end of function (data)
                                ); // end of $.getJSON(

                            }
                        });

                        $timeout($rootScope.progressbar.complete(), 1000); // <<<<<< end of creating UI (assign functions to buttons, get data etc.)
                    } else {
                        $rootScope.web3isConnected = false;
                        $log.error('[ethereumVerificationCtrl] web3 is not connected to Ethereum node');
                        // return Promise.reject("No connection to Ethereum network. Please use a browser with MetaMask plugin or Mist Browser");
                    }
                })
                .catch(function (error) {
                    $log.debug(error);
                    $scope.setAlertDanger(error);
                    $timeout($rootScope.progressbar.complete(), 1000); // <<<<<<
                });

        } // end of Ctrl
    ]);

})();
