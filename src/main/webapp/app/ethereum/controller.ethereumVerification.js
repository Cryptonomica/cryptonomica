'use strict';

var controller_name = "cryptonomica.controller.ethereumVerification";

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
    // 'uiUploader',
    '$log',
    '$cookies',
    '$timeout',
    '$window',
    '$stateParams',
    function ethVerificationCtrl($scope,    // <- name for debugging
                                 $sce,
                                 $rootScope,
                                 $http,
                                 GApi,
                                 GAuth,
                                 GData,
                                 $state,
                                 // uiUploader,
                                 $log,
                                 $cookies,
                                 $timeout,
                                 $window,
                                 $stateParams) {

        /*
        * in the view add id with controller name to parent div:
        * <div id="ethVerificationCtrl">
        * use this id to access $scope in browser console:
        * angular.element(document.getElementById('ethVerificationCtrl')).scope();
        * for example: for example: $scope = angular.element(document.getElementById('ethVerificationCtrl')).scope();
        * */

        $log.info("ethVerificationCtrl started");

        GAuth.checkAuth().then(
            function () {
                $log.debug('[ethVerificationCtrl] GAuth.checkAuth(): success');
                $rootScope.getUserData(); // async
            }, // if error:
            function () {
                $log.debug('[ethVerificationCtrl] GAuth.checkAuth(): error');
                // see: https://github.com/maximepvrt/angular-google-gapi/tree/master#signup-with-google
                GAuth.login().then(function (user) {
                        // if pop-up in browser does not work we use a button with $rootScope.login();
                        // $log.debug('[ethVerificationCtrl]' + user.name + ' is logged in:');
                        $log.debug(user);
                        $rootScope.checkAuth(); // this includes $rootScope.getUserData() // if we use login button this will be triggered by $rootScope.login()
                        // your application can access their Google account
                    }, function () {
                        $log.debug('[ethereumVerificationCtrl} login failed');
                    }
                );
            }
        );

        /* Web Storage API , https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API/Using_the_Web_Storage_API */
        var storageType = 'localStorage'; // or 'sessionStorage'
        var storageAvailable =
            function () {
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

        if (storageAvailable) {
            $scope.signedString = localStorage.getItem('signedString');
        }

        $log.info("[ethVerification] $stateParams.fingerprint : " + $stateParams.fingerprint);
        // --- Alerts:
        $scope.alertDanger = null;  // red
        $scope.alertWarning = null; // yellow
        $scope.alertInfo = null;    // blue
        $scope.alertSuccess = null; // green
        //
        $scope.error = {};
        $scope.fingerprint = $stateParams.fingerprint;
        $scope.keyId = "0x" + $stateParams.fingerprint.substring(32, 40).toUpperCase();
        $scope.todayDate = new Date();
        $scope.pgpPublicKey = null;
        $scope.getPGPPublicKeyByFingerprintError = null;

        // example from:
        // https://github.com/trufflesuite/truffle-artifactor#artifactorgenerateoptions-networks
        const networks = {
            "1": {        // Main network
                "networkName": "Main Ethereum Network",
                "address": undefined, // contract address
                "contractAddress": undefined,
                "ownerAddress": undefined,
                "etherscanLinkPrefix": "https://ropsten.etherscan.io/"
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
                "networkName": undefined,
                "address": "0xb9ffed00f17De4CDA41bF30bBe0E11B78E3A2c57", // contract address
                "contractAddress": "0xb9ffed00f17De4CDA41bF30bBe0E11B78E3A2c57",
                "ownerAddress": "0x3fAB7ebe4B2c31a75Cf89210aeDEfc093928A87D",
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
                $scope.alertDanger = error;
            }
        }

        // >>>> Start execution:
        if ($rootScope.web3.isConnected()) {
            $log.debug('[ethVerificationCtrl] $rootScope.web3.isConnected() : true ');
            $rootScope.currentNetwork.connected = true;
            // $rootScope.$apply();

            $rootScope.web3.version.getNetwork(function (error, result) {
                if (error) {
                    $scope.alertDanger = error;
                    $scope.$apply();
                    $log.error(error);
                } else {

                    // (!!!) change in production ----------------------------------------------------------------!!!!!
                    if (result !== "4") { // not Rinkeby
                        $scope.alertDanger = "Service work on Rinkeby TestNet, but you are connected to another Ethereum Network";
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
                        'app/ethereum/CryptonomicaVerification.json', // see: https://github.com/trufflesuite/truffle-contract-schema
                        function (data) { // async

                            $log.debug('[ethVerificationCtrl] app/ethereum/CryptonomicaVerification.json :');
                            $log.debug(data);

                            var CryptonomicaVerificationContract = TruffleContract(data);
                            CryptonomicaVerificationContract.setProvider($rootScope.web3.currentProvider); // <- !

                            var contractAddress = networks[$rootScope.currentNetwork.network_id].contractAddress;
                            $log.debug('[ethVerificationCtrl] contractAddress: ', contractAddress);

                            // >>> instantiate smart contract
                            CryptonomicaVerificationContract.at(contractAddress).then(function (instance) {
                                    $scope.contract = instance;
                                    $scope.$apply();

                                    $log.debug('[ethVerificationCtrl] $scope.contract:');
                                    $log.debug($scope.contract);

                                    // -> send request to backend
                                    GApi.executeAuth('pgpPublicKeyAPI', 'getPGPPublicKeyByFingerprint',
                                        {"fingerprint": $stateParams.fingerprint}
                                    ).then(function (pgpPublicKeyGeneralView) {
                                        $scope.pgpPublicKey = pgpPublicKeyGeneralView;

                                        $log.debug("[ethVerificationCtrl] $scope.pgpPublicKey: ");
                                        $log.debug($scope.pgpPublicKey);

                                        if (!$scope.pgpPublicKey.verified) {
                                            $scope.alertDanger = 'This key is not verified';
                                            return;
                                        }

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
                                        ); //
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
                                            $scope.alertDanger =
                                                "Ethereum Account not recognized."
                                                + "Please connect your account in MetaMask or Mist and reload this page. "
                                        }

                                        // add functions to buttons:


                                        /* ------------ requestDataFromSmartContract --- */
                                        $scope.requestDataFromSmartContract = function () {
                                            $log.debug('$scope.requestDataFromSmartContract() started');
                                            $scope.requestDataFromSmartContractError = {};
                                            $scope.smartContractData = {};

                                            // (1) mapping(bytes20 => address) public addressAttached
                                            $scope.contract.addressAttached.call(
                                                // as a key we use fingerprint as bytes32, like 0x57A5FEE5A34D563B4B85ADF3CE369FD9E77173E5
                                                "0x" + $stateParams.fingerprint
                                            )
                                                .then(
                                                    function (ethAddressConnectedToFingerprint) {
                                                        $scope.smartContractData.ethAddressConnectedToFingerprint = ethAddressConnectedToFingerprint;
                                                        $scope.$apply();
                                                        $log.info('[$scope.requestDataFromSmartContract] $scope.smartContractData.ethAddressConnectedToFingerprint: ');
                                                        $log.info($scope.smartContractData.ethAddressConnectedToFingerprint);
                                                        // --------------------- with ethAddressConnectedToFingerprint:
                                                        // (0)
                                                        $scope.contract.verification.call(ethAddressConnectedToFingerprint)
                                                            .then(
                                                                function (verification) {
                                                                    $log.debug('[$scope.requestDataFromSmartContract] verification:');
                                                                    $log.debug(verification);
                                                                    $scope.smartContractData.fingerprint = verification[0]; // < verified
                                                                    $scope.smartContractData.keyCertificateValidUntilUnixTime = verification[1];
                                                                    $scope.smartContractData.keyCertificateValidUntilDate =
                                                                        $rootScope.dateFromUnixTime(
                                                                            $scope.smartContractData.keyCertificateValidUntilUnixTime
                                                                        );
                                                                    $scope.smartContractData.firstName = verification[2];
                                                                    $scope.smartContractData.lastName = verification[3];
                                                                    $scope.smartContractData.birthDateUnixTime = verification[4];
                                                                    $scope.smartContractData.birthDate =
                                                                        $rootScope.dateFromUnixTime(
                                                                            $scope.smartContractData.birthDateUnixTime
                                                                        );
                                                                    $scope.smartContractData.nationality = verification[5];
                                                                    $scope.smartContractData.verificationAddedOnUnixTime = verification[6];
                                                                    $scope.smartContractData.verificationAddedOnDate =
                                                                        $rootScope.dateFromUnixTime(
                                                                            $scope.smartContractData.verificationAddedOnUnixTime
                                                                        );
                                                                    $scope.smartContractData.revokedOnUnixTime = verification[7];
                                                                    $scope.smartContractData.revokedOnDate =
                                                                        $rootScope.dateFromUnixTime(
                                                                            $scope.smartContractData.revokedOnUnixTime
                                                                        );
                                                                    $scope.smartContractData.signedString = verification[8];

                                                                    $scope.$apply();
                                                                }
                                                            ).catch(function (error) {
                                                                $log.debug('$scope.contract.verification.call() error:');
                                                                $log.error(error);
                                                                $scope.requestDataFromSmartContractError.error = true;
                                                                $scope.requestDataFromSmartContractError.verification = error;
                                                                $scope.$apply();
                                                            }
                                                        );
                                                        // (1)
                                                        $scope.contract.fingerprint.call(ethAddressConnectedToFingerprint)
                                                            .then(
                                                                function (fingerprintAsBytes20) {
                                                                    $log.debug('[$scope.requestDataFromSmartContract] fingerprintAsBytes20:');
                                                                    $log.debug(fingerprintAsBytes20);
                                                                    $scope.smartContractData.fingerprintAsBytes20 = fingerprintAsBytes20;
                                                                    $scope.$apply();
                                                                }
                                                            ).catch(function (error) {
                                                                $log.debug('$scope.contract.fingerprint.call error:');
                                                                $log.error(error);
                                                                $scope.requestDataFromSmartContractError.error = true;
                                                                $scope.requestDataFromSmartContractError.fingerprint = error;
                                                                $scope.$apply();
                                                            }
                                                        );
                                                        // (2)
                                                        $scope.contract.unverifiedFingerprint.call(ethAddressConnectedToFingerprint)
                                                            .then(
                                                                function (unverifiedFingerprint) {
                                                                    $log.debug('[$scope.requestDataFromSmartContract] unverifiedFingerprint:');
                                                                    $log.debug(unverifiedFingerprint);
                                                                    $scope.smartContractData.unverifiedFingerprint = unverifiedFingerprint;
                                                                    $scope.$apply();
                                                                }
                                                            ).catch(function (error) {
                                                                $log.debug('$scope.contract.unverifiedFingerprint.call error:');
                                                                $log.error(error);
                                                                $scope.requestDataFromSmartContractError.error = true;
                                                                $scope.requestDataFromSmartContractError.unverifiedFingerprint = error;
                                                                $scope.$apply();
                                                            }
                                                        );
                                                        // (3)
                                                        // for verification request:
                                                        $scope.smartContractData.stringToSign =
                                                            "I hereby confirm that the address "
                                                            + $scope.ethAccount.toLowerCase()
                                                            + " is my Ethereum address";

                                                        // (4)
                                                        $scope.contract.signedStringUploadedOnUnixTime.call(ethAddressConnectedToFingerprint)
                                                            .then(
                                                                function (signedStringUploadedOnUnixTime) {
                                                                    $log.debug('[$scope.requestDataFromSmartContract] signedStringUploadedOnUnixTime:');
                                                                    $log.debug(signedStringUploadedOnUnixTime);
                                                                    $scope.smartContractData.signedStringUploadedOnUnixTime
                                                                        = signedStringUploadedOnUnixTime;
                                                                    $scope.smartContractData.signedStringUploadedOnDate
                                                                        = $rootScope.dateFromUnixTime(signedStringUploadedOnUnixTime);
                                                                    $scope.$apply();
                                                                }
                                                            ).catch(function (error) {
                                                                $log.debug('$scope.contract.stringToSignRequestedOnUnixTime.call error:');
                                                                $log.error(error);
                                                                $scope.requestDataFromSmartContractError.error = true;
                                                                $scope.requestDataFromSmartContractError.signedStringUploadedOnUnixTime = error;
                                                                $scope.$apply();
                                                            }
                                                        );
                                                        // (5)
                                                        $scope.contract.priceForVerificationInWei.call()
                                                            .then(
                                                                function (priceForVerificationInWei) {
                                                                    $log.debug('[$scope.requestDataFromSmartContract] priceForVerificationInWei:');
                                                                    $log.debug(priceForVerificationInWei);
                                                                    $scope.smartContractData.priceForVerificationInWei = priceForVerificationInWei;
                                                                    // https://github.com/ethereum/wiki/wiki/JavaScript-API#web3fromwei
                                                                    $scope.smartContractData.priceForVerificationInEth =
                                                                        $rootScope.web3.fromWei(
                                                                            priceForVerificationInWei,
                                                                            "ether"
                                                                        );
                                                                    $log.debug("$scope.smartContractData.priceForVerificationInEth:");
                                                                    $log.debug($scope.smartContractData.priceForVerificationInEth);
                                                                    $scope.$apply();
                                                                }
                                                            ).catch(function (error) {
                                                                $log.debug('$scope.contract.priceForVerificationInWei.call error:');
                                                                $log.error(error);
                                                                $scope.requestDataFromSmartContractError.error = true;
                                                                $scope.requestDataFromSmartContractError.priceForVerificationInWei = error;
                                                                $scope.$apply();
                                                            }
                                                        );
                                                        //----------------------
                                                    }
                                                ).catch(function (error) {
                                                    $log.debug('$scope.contract.getAddressConnectedToFingerprint.call error:');
                                                    $log.error(error);
                                                    $scope.requestDataFromSmartContractError.error = true;
                                                    $scope.requestDataFromSmartContractError.getAddressConnectedToFingerprint = error;
                                                    $scope.$apply();
                                                }
                                            );
                                        };
                                        // (!!!) run when controller starts >>>
                                        $scope.requestDataFromSmartContract();


                                        /* ----------- Upload Signed String ---------- */
                                        $scope.uploadSignedString = function () {
                                            $log.debug('$scope.uploadSignedString() started;');

                                            if ($stateParams.fingerprint !== $scope.pgpPublicKey.fingerprint) {
                                                $scope.uploadSignedStringError = "this is not your key";
                                                return;
                                            }

                                            $log.debug('signed string to upload:');
                                            $log.debug($scope.signedString);
                                            $scope.uploadSignedStringError = null;
                                            $scope.uploadSignedStringWorking = true;
                                            $log.debug('$scope.uploadSignedString starts');
                                            $log.debug('signed string: ');
                                            $log.debug($scope.signedString);
                                            if ($rootScope.stringIsNullUndefinedOrEmpty($scope.signedString)) {
                                                $scope.uploadSignedStringError = 'Signed string not provided';
                                                $scope.uploadSignedStringWorking = false;
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
                                                return;
                                            }
                                            $scope.uploadSignedStringTx = null;

                                            if (storageAvailable) {
                                                localStorage.setItem('signedString', $scope.signedString);
                                            }

                                            $scope.contract.priceForVerificationInWei.call()
                                                .then(
                                                    function (priceForVerificationInWei) {
                                                        $log.debug('[ethVerificationCtrl] priceForVerificationInWei:');
                                                        $log.debug(priceForVerificationInWei);
                                                        $scope.priceForVerificationInWei = priceForVerificationInWei;
                                                        var txParameters = {};
                                                        if ($scope.priceForVerificationInWei === null) {
                                                            $scope.uploadSignedStringError =
                                                                'Can not get price for verification from smart contract';
                                                            $scope.uploadSignedStringWorking = false;
                                                            $scope.$apply();
                                                            return;
                                                        }
                                                        txParameters.value = $scope.priceForVerificationInWei;
                                                        txParameters.from = $scope.ethAccount;
                                                        txParameters.gas = 110000; // 1010203
                                                        $log.debug('$scope.uploadSignedString txParameters: ');
                                                        $log.debug(txParameters);
                                                        $scope.contract.uploadSignedString(
                                                            $stateParams.fingerprint,
                                                            // as a key we use fingerprint as bytes32, like 0x57A5FEE5A34D563B4B85ADF3CE369FD9E77173E5
                                                            "0x" + $stateParams.fingerprint,
                                                            $scope.signedString,
                                                            txParameters
                                                        ).then(
                                                            function (tx) {
                                                                $log.debug('[ethVerificationCtrl] uploadSignedString result:');
                                                                $log.debug(tx);
                                                                $scope.uploadSignedStringTx = tx;
                                                                $scope.requestDataFromSmartContract();// < update data (async)
                                                                $scope.uploadSignedStringWorking = false;
                                                                $scope.$apply();
                                                            }
                                                        ).catch(function (error) {
                                                                $log.debug('$scope.contract.uploadSignedString error:');
                                                                $log.error(error);
                                                                $scope.uploadSignedStringError = error.toString();
                                                                $scope.uploadSignedStringWorking = false;
                                                                $scope.$apply();
                                                            }
                                                        );
                                                    }
                                                ).catch(function (error) {
                                                    // $scope.alertDanger = error.toString();
                                                    $scope.uploadSignedStringError = error.toString();
                                                    $log.debug('$scope.contract.priceForVerificationInWei.call() error:');
                                                    $log.error(error);
                                                    $scope.uploadSignedStringWorking = false;
                                                    $scope.$apply();
                                                }
                                            );
                                        };

                                        $scope.verify = function () {
                                            $rootScope.progressbar.start(); // <<<<<<<
                                            $scope.verifyWorking = true;
                                            GApi.executeAuth(
                                                'ethNodeAPI',
                                                'verifyEthAddress', //
                                                {"ethereumAcc": $scope.ethAccount}
                                            ).then(
                                                function (resp) {
                                                    $log.info("$scope.verify() resp: ");
                                                    $log.info(resp);
                                                    $scope.verificationResult = resp;
                                                    $scope.requestDataFromSmartContract();
                                                    $scope.verifyWorking = false;
                                                    // $scope.$apply(); // not needed here
                                                    $timeout($rootScope.progressbar.complete(), 1000);
                                                },
                                                function (error) {
                                                    $log.error("$scope.verify() error: ");
                                                    $log.error(error);
                                                    $scope.verificationResult = error.message;
                                                    $scope.verifyWorking = false;
                                                    // $scope.$apply(); // not needed here
                                                    $timeout($rootScope.progressbar.complete(), 1000);
                                                }
                                            );
                                        };

                                        // ---
                                    }, function (getPGPPublicKeyByFingerprintError) {
                                        $scope.getPGPPublicKeyByFingerprintError = getPGPPublicKeyByFingerprintError;
                                        $log.debug("$scope.getPGPPublicKeyByFingerprintError : ");
                                        $log.error($scope.getPGPPublicKeyByFingerprintError);
                                        // $scope.$apply(); // not needed here
                                    })
                                } // end of function (instance) {
                            )

                        } // end of function (data)
                    ); // end of $.getJSON(

                }
            });
        } else {
            $rootScope.web3isConnected = false;
            $scope.alertDanger = "Please use Google Chrome with MetaMask plugin or Mist Browser";
            $log.error('[ethereumVerificationCtrl] web3 is not connected to Ethereum node');
            return;
        }
    } // end of Ctrl
]);

