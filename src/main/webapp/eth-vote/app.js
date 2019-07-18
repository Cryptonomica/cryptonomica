'use strict';

// const controllers = angular.module('app.controllers', [
//     'app.home'
// ]);

const app = angular.module('app', [
        'ui.router',
        'ngProgress', // https://github.com/VictorBjelkholm/ngProgress
        'ngClipboard', // https://github.com/nico-val/ngClipboard
        // ---- App :
        'app.ui.router', // https://ui-router.github.io/ng1/docs/0.4.2/#/api/ui.router
        'app.home'
    ]
);

// === ui-router (https://ui-router.github.io/ng1/docs/0.4.2/#/api/ui.router)

const router = angular.module('app.ui.router', []);

router.config(['$urlRouterProvider',
    function ($urlRouterProvider) {
        $urlRouterProvider.otherwise("/");
    }
]);

router.config(['$stateProvider', function ($stateProvider) {
    $stateProvider
        .state('home', {
            // can be used as:
            // ../?contract=0x9fd1983776E2317FC4901E21daeD00225E4A998C&proposal=1
            url: '/?contract&proposal',
            // url: '/:contract/:proposal',
            // url: '/',
            controller: 'app.home',
            templateUrl: 'home.html'
        })
}]);

app.run([
    '$state',
    '$rootScope',
    '$window',
    '$sce',
    'ngProgressFactory',
    '$timeout', // for ngProgressFactory
    '$anchorScroll',
    '$location',
    '$log',
    function (
        $state,
        $rootScope,
        $window,
        $sce,
        ngProgressFactory,
        $timeout, // for ngProgressFactory
        $anchorScroll,
        $location,
        $log) {

        // TODO: for tests only
        window.myRootScope = $rootScope;

        // console.log("application started");
        $rootScope.webAppVersion = "0.2.0";
        $rootScope.webAppLastChange = "2019-07-18";
        $rootScope.production = false;
        console.log("Webapp version", $rootScope.webAppVersion, "of", $rootScope.webAppLastChange);

        $rootScope.labelText = "In test mode, ver. " + $rootScope.webAppVersion + "  ";
        $rootScope.showLabel = true;

        $rootScope.progressbar = ngProgressFactory.createInstance();
        $rootScope.progressbar.setHeight('7px'); // any valid CSS value Eg '10px', '1em' or '1%'
        // $rootScope.progressbar.setColor('#60c8fa');
        $rootScope.progressbar.setColor('black');
        // >>>>
        $rootScope.progressbar.start();
        // $timeout($rootScope.progressbar.complete(), 1000);

        /* === Utility functions === */

        $rootScope.goTo = function (id) {
            // set the location.hash to the id of the element you wish to scroll to.
            $location.hash(id);
            $anchorScroll();
        };

        $rootScope.unixTimeFromDate = function (date) {
            let result = null;
            if (date instanceof Date) {
                result = Math.round(date.getTime() / 1000);
                // $log.debug("unix time calculated:", result);
            } else {
                $log.error(date, "is not a date");
            }
            return result;
        };

        $rootScope.dateFromUnixTime = function (unixTime) {
            return new Date(unixTime * 1000);
        };

        $rootScope.dateToDateStr = function (date) {
            let result = null;
            if (date instanceof Date) {
                result =
                    date.getFullYear() + "-"
                    + date.getMonth() + "-"
                    + date.getDate();
            } else {
                $log.error(date, "is not a date");
            }
            return result;
        };

        /* === Ethereum  === */

        // example from:
        // https://github.com/trufflesuite/truffle-artifactor#artifactorgenerateoptions-networks
        $rootScope.networks = {
            1: {
                "networkName": "Main Ethereum Network",
                "etherscanLinkPrefix": "https://etherscan.io/",
                "etherscanApiLink": "https://api.etherscan.io/"
            },
            3: {
                "networkName": "Ropsten TestNet",
                "etherscanLinkPrefix": "https://ropsten.etherscan.io/",
                "etherscanApiLink": "https://api-ropsten.etherscan.io/"
            },
            4: {        //
                "networkName": "Rinkeby TestNet",
                "etherscanLinkPrefix": "https://rinkeby.etherscan.io/",
                "etherscanApiLink": "https://api-rinkeby.etherscan.io/"
            },
            42: {        //
                "networkName": "Kovan TestNet",
                "etherscanLinkPrefix": "https://kovan.etherscan.io/",
                "etherscanApiLink": "https://api-kovan.etherscan.io/"
            }
        }; // end of $rootScope.networks

        $log.debug("app.run end");

    }]);

const controller = angular.module("app.home", []);

// https://docs.angularjs.org/api/ng/provider/$logProvider
controller.config(function ($logProvider) {
    // TODO: change in production
    // $logProvider.debugEnabled(false);
    $logProvider.debugEnabled(true);
});

controller.controller("app.home", [
        '$scope',
        '$rootScope',
        '$http',
        '$timeout', // for ngProgressFactory
        'ngClipboard',
        '$log',
        '$location',
        '$state',
        '$stateParams',
        function homeCtrl($scope,
                          $rootScope,
                          $http,
                          $timeout, // for ngProgressFactory
                          ngClipboard,
                          $log,
                          $location,
                          $state,
                          $stateParams
        ) {

            $log.debug("app.home started");

            // TODO: for test only:
            window.myScope = $scope;

            // activate tabs
            // see: https://semantic-ui.com/modules/tab.html#/examples
            $('.menu .item').tab();

            // https://github.com/Semantic-Org/Semantic-UI/issues/3544
            $scope.changeTab = function (newTab) {
                $.tab('change tab', newTab);
                $('.menu .item').removeClass("active");
                $('.menu .item[data-tab="' + newTab + '"]').addClass("active");
            };

            // $scope.showDescription = false;
            // $scope.showEvents = false;

            (function setUpContractData() {

                $scope.contractAbiPath = "contracts/VotingForERC20.abi";
                $scope.erc20ContractAbiPath = "contracts/ERC20TokensContract.abi";

                $scope.deployedOnNetworksArray = [
                    1, // MainNet
                    3, // Ropsten
                    4, // Rinkeby
                    42 // Kovan
                ];
                $scope.contractsAddresses = {
                    1: "0xfd2E0BbFBa42CF6Df125578B732b0e0CBd5F723F", // MaiNet
                    3: "0xB33C86E9a03128B42296a578273337323fAA626b", // Ropsten
                    4: "0x2EedffD05935fF3148bBAc9e0DBACB03C48Edd0c", // Rinkeby
                    42: "0x67acd0dd43dC9D074884cb2D0e7631dF9C00e5B7" // Kovan
                };
                $scope.contractsDeployedOnBlocks = {
                    1: 8173358, // MainNet
                    3: 6011131, // Ropsten
                    4: 4753788, // Rinkeby
                    42: 12277863 // Kovan
                };
                $scope.mockUpERC20ContractsAddresses = {
                    1: "0x2EedffD05935fF3148bBAc9e0DBACB03C48Edd0c", // MainNet
                    3: "0x37d2a83c587b64358dB4FA9afAD0b6CfC48A7371", // Ropsten
                    4: "0xfd2E0BbFBa42CF6Df125578B732b0e0CBd5F723F", // Rinkeby
                    42: "0x8e681aB751641745bD00fe21F31C4253645f5C50" // Kovan
                };

            })();

            (function alerts() {
                $scope.alertDanger = null;  // red
                $scope.alertWarning = null; // yellow
                $scope.alertInfo = null;    // blue
                $scope.alertSuccess = null; // green
                $scope.alertMessage = null; // grey

                $scope.setAlertDanger = function (message) {
                    $scope.alertDanger = message;
                    $log.debug("$scope.alertDanger:", $scope.alertDanger);
                    // $scope.$apply(); // < needed
                    $scope.goTo("alertDanger");
                };

                $scope.setAlertWarning = function (message) {
                    $scope.alertWarning = message;
                    $log.debug("$scope.alertWarning:", $scope.alertWarning);
                    // $scope.$apply(); //
                    $scope.goTo("alertWarning");
                };

                $scope.setAlertInfo = function (message) {
                    $scope.alertInfo = message;
                    $log.debug("$scope.alertInfo:", $scope.alertInfo);
                    // $scope.$apply();
                    $scope.goTo("alertInfo");
                };

                $scope.setAlertSuccess = function (message) {
                    $scope.alertSuccess = message;
                    $log.debug("$scope.alertSuccess:", $scope.alertSuccess);
                    // $scope.$apply();
                    $scope.goTo("alertSuccess");
                };

                $scope.setAlertMessage = function (message, header) {
                    $scope.alertMessage = {};
                    $scope.alertMessage.header = header;
                    $scope.alertMessage.message = message;
                    $log.debug("$scope.alertMessage:", $scope.alertMessage);
                    // $scope.$apply();
                    $scope.goTo("alertMessage");
                };

            })();

            // https://github.com/MetaMask/metamask-extension/issues/5699#issuecomment-445480857
            if (window.ethereum) {

                // we need this to reference 'web3' and 'ethereum' in html
                // like:
                // {{ethereum.selectedAddress}} or {{web3.eth.defaultAccount}} or ng-show="web3"  or ng-show="ethereum.isMetaMask" etc.
                // $rootScope.web3 = new Web3(ethereum, null, web3Options);
                $rootScope.web3 = new Web3(ethereum);
                $rootScope.ethereum = ethereum;

                if (typeof window.ethereum.selectedAddress === 'undefined') { // privacy mode on
                    (async function () {
                        try {
                            // Request account access if needed
                            // This method returns a Promise that resolves to an array of user accounts once access is approved for a given dapp.
                            await window.ethereum.enable();
                            // Accounts now exposed
                            // $rootScope.web3.eth.accounts[0] > does not work
                            // but you have already: web3.eth.defaultAccount
                            // $log.debug("$rootScope.web3.eth.defaultAccount : ");
                            // $log.debug($rootScope.web3.eth.defaultAccount);
                            $rootScope.noConnectionToNodeError = false;
                            $log.debug("MetaMask connection added manually");
                            main();

                        } catch (error) {
                            // if user denied account access:
                            // {"message":"User denied account authorization","code":4001}
                            $log.error(error);
                            if (error.message && error.code === 4001) {
                                error = "User denied MetaMask connect. Web application can not be started."
                                    + " If you wish to use this webapp, please, reload the page and connect MetaMask";
                            }
                            $scope.setAlertDanger(error);
                            $scope.$apply(); // <<< needed here!
                        }

                    })();
                } else { // privacy mode off or already connected
                    $rootScope.noConnectionToNodeError = false;
                    $log.debug("MetaMask privacy mode off or already connected");
                    main();
                }

            } else if (window.web3) {
                $log.debug("old web3 browser detected");
                $rootScope.noConnectionToNodeError = false;
                // $rootScope.web3 = new Web3(web3.currentProvider, null, web3Options);
                $rootScope.web3 = new Web3(web3.currentProvider);
                // Accounts always exposed
                if (!$rootScope.web3.eth.defaultAccount && $rootScope.web3.eth.accounts && $rootScope.web3.eth.accounts[0]) {
                    $rootScope.web3.eth.defaultAccount = $rootScope.web3.eth.accounts[0];
                    $log.debug("$rootScope.web3.eth.defaultAccount : ");
                    $log.debug($rootScope.web3.eth.defaultAccount);
                }
                main();
            } else {  // Non-dapp browsers:
                $log.error('Non-Ethereum browser detected. You should consider trying MetaMask!');
                // $scope.setAlertDanger("This web application requires Ethereum connection. Please install MetaMask.io browser plugin");
                // > or use <no-connection-to-node-error></no-connection-to-node-error> directive
                $rootScope.noConnectionToNodeError = true;
            }


            function main() {

                $rootScope.currentBlockNumber = null;
                $rootScope.refreshCurrentBlockNumberIsWorking = false;
                $rootScope.refreshCurrentBlockNumber = function () {
                    $rootScope.refreshCurrentBlockNumberIsWorking = true;
                    $rootScope.web3.eth.getBlockNumber()
                        .then((blockNumber) => {
                            $rootScope.currentBlockNumber = blockNumber;
                            // $log.debug("last block number:", $rootScope.currentBlockNumber);
                        })
                        .catch((error) => {
                            $log.error(error);
                        })
                        .finally(() => {
                            $rootScope.refreshCurrentBlockNumberIsWorking = false;
                            $scope.$apply();
                        });
                };

                $scope.ethBalanceOfDefaultAccount = null;
                $scope.getEthBalanceOfDefaultAccountIsWorking = false;
                $scope.getEthBalanceOfDefaultAccount = function () {
                    $scope.getEthBalanceOfDefaultAccountIsWorking = true;
                    $rootScope.web3.eth.getBalance($rootScope.web3.eth.defaultAccount)
                        .then((result) => {
                            $scope.ethBalanceOfDefaultAccount = $rootScope.web3.utils.fromWei(result, 'ether');
                            // $log.debug($rootScope.web3.eth.defaultAccount, "balance: ETH", $scope.ethBalanceOfDefaultAccount)
                        })
                        .catch((error) => {
                            $log.error("Error reading balance of default account:");
                            $log.error(error);
                        })
                        .finally(() => {
                            $scope.getEthBalanceOfDefaultAccountIsWorking = false;
                            $scope.$apply();
                        })
                };

                $rootScope.currentNetwork = {};

                $rootScope.web3.eth.net.getId()
                    .then((result) => {

                        $rootScope.currentNetwork.network_id = result; //
                        if (result === 1 || result === 3 || result === 4 || result === 42) {
                            $rootScope.currentNetwork.networkName = $rootScope.networks[result].networkName;
                            $rootScope.currentNetwork.etherscanLinkPrefix = $rootScope.networks[result].etherscanLinkPrefix;
                            $rootScope.currentNetwork.etherscanApiLink = $rootScope.networks[result].etherscanApiLink;
                        } else {
                            $rootScope.currentNetwork.networkName = "unknown network";
                            $rootScope.currentNetwork.etherscanLinkPrefix = "blockchain_explorer";
                            $rootScope.currentNetwork.etherscanApiLink = "blockchainExplorerAPI";
                            $scope.setAlertWarning("Unknown Ethereum network. Network ID: " + result);
                        }

                        // $log.debug("$rootScope.currentNetwork.networkName:", $rootScope.currentNetwork.networkName);
                        $rootScope.$apply(); // needed here
                        return $rootScope.web3.eth.getNodeInfo();
                    })
                    .then((result) => {
                        $rootScope.currentNetwork.nodeInfo = result;
                        // $log.debug("Node info: ", $rootScope.currentNetwork.nodeInfo);
                        return $http.get($scope.contractAbiPath);
                    })
                    .then((value) => {
                        $scope.contractABI = value.data; // ABI
                        return $http.get($scope.erc20ContractAbiPath);
                    })
                    .then((value) => {
                        $scope.erc20ContractAbi = value.data; // ABI

                        if ($scope.deployedOnNetworksArray.includes($rootScope.currentNetwork.network_id)) {

                            // create voting contract instance:
                            $scope.contract = new $rootScope.web3.eth.Contract(
                                $scope.contractABI, // < the same for all networks
                                $scope.contractsAddresses[$rootScope.currentNetwork.network_id]
                            );

                            if (window.ethereum && window.ethereum.selectedAddress) {
                                $scope.contract.from = window.ethereum.selectedAddress;
                            } else if ($rootScope.web3 && $rootScope.web3.eth && $rootScope.web3.eth.defaultAccount) {
                                $scope.contract.from = $rootScope.web3.eth.defaultAccount;
                            }

                            $scope.$apply();

                            $log.debug("contract address: ", $scope.contract._address);

                            $log.debug("$scope.contract:");
                            $log.debug($scope.contract);

                        } else {
                            $scope.setAlertDanger(
                                "This smart contract does not work on "
                                + $rootScope.currentNetwork.networkName
                                + ", please, change to another network"
                            );
                            $scope.notSupportedNetwork = true;
                        }

                        return $rootScope.web3.eth.getAccounts();
                    })
                    .then(function (accounts) {

                        if (!$rootScope.web3.eth.defaultAccount && accounts && accounts.length && accounts.length > 0) {
                            // https://web3js.readthedocs.io/en/1.0/web3-eth.html#id20
                            $rootScope.web3.eth.defaultAccount = accounts[0];
                            // $log.debug("$rootScope.web3.eth.defaultAccount:", $rootScope.web3.eth.defaultAccount);
                            $scope.$apply();
                        }
                        if (!$rootScope.web3.eth.defaultAccount) {
                            $scope.setAlertDanger("Ethereum account not recognized. If you use MetaMask please login to MetaMask and connect to this site");
                        }

                        $rootScope.refreshCurrentBlockNumber();
                        $scope.getEthBalanceOfDefaultAccount();
                        setUpContractFunctions();
                    })
                    .catch(function (error) {
                        $log.error("error:");
                        $log.error(error);
                        $scope.setAlertDanger(error);
                    })
            }

            function setUpContractFunctions() {

                if ($scope.notSupportedNetwork) {
                    return;
                }

                $scope.createVotingForm = {};
                $scope.createVotingForm.erc20ContractAddress = $scope.mockUpERC20ContractsAddresses[$rootScope.currentNetwork.network_id];
                $scope.createVotingForm.proposalText = "This is a mock up proposal to test voting functionality.";

                $scope.votingLoadForm = {};
                $scope.votingLoadForm.forContract = $scope.mockUpERC20ContractsAddresses[$rootScope.currentNetwork.network_id];
                $scope.votingLoadForm.proposalId = 1;

                $rootScope.web3.eth.getBlockNumber()
                    .then((blockNumber) => {
                        $scope.createVotingForm.resultsInBlock = parseInt(blockNumber) + 200; // +200 blocks
                    })
                    .catch((error) => {
                        $log.error(error);
                    })
                    .finally(() => {
                        $scope.$apply();
                    });

                $scope.createIsWorking = false;
                $scope.create = function () {

                    $scope.createIsWorking = true;
                    $scope.voting = {};
                    $scope.lastCreateTxHash = null;

                    if (!$rootScope.web3.utils.isAddress($scope.createVotingForm.erc20ContractAddress)) {
                        $scope.setAlertDanger(
                            $scope.createVotingForm.erc20ContractAddress + " is not a valid Ethereum address"
                        );
                        $scope.createIsWorking = false;
                        return;
                    }

                    try {
                        $scope.erc20Contract = new $rootScope.web3.eth.Contract(
                            $scope.erc20ContractAbi, // < the same for all networks
                            $scope.createVotingForm.erc20ContractAddress
                        )
                    } catch (e) {
                        $log.error(e);
                        $scope.setAlertDanger(
                            "ERROR:" + $scope.createVotingForm.erc20ContractAddress + " is probably not a valid ERC20 contract"
                        );
                        return;
                    }

                    $scope.erc20Contract.methods.balanceOf($rootScope.web3.eth.defaultAccount)
                        .call({from: $rootScope.web3.eth.defaultAccount})
                        .then((result) => {

                            result = parseInt(result);

                            /*
                                $log.debug(
                                "Creating voting for " + $scope.createVotingForm.erc20ContractAddress
                                + ". Balance of " + $rootScope.web3.eth.defaultAccount + " in contract: "
                                + result + " tokens."
                            );*/

                            if (result === 0) {
                                throw "You need at least one token in contract " + $scope.createVotingForm.erc20ContractAddress + ".";
                            }
                            return $scope.contract.methods.create(
                                $scope.createVotingForm.erc20ContractAddress,
                                $scope.createVotingForm.proposalText,
                                $scope.createVotingForm.resultsInBlock,
                            )
                                .send({from: $rootScope.web3.eth.defaultAccount});
                        })
                        .then((receipt) => {
                            $log.debug("'create' tx receipt:");
                            $log.debug(receipt);
                            if (receipt && receipt.events && receipt.events.Proposal.returnValues) {
                                $scope.lastCreateTxHash = receipt.transactionHash;
                                const proposalEvent = receipt.events.Proposal.returnValues; // < ! all strings
                                $scope.setAlertSuccess(
                                    "You created a proposal with the text: '"
                                    + proposalEvent.proposalText
                                    + "', for smart contract : '"
                                    + proposalEvent.forContract
                                    + "', with proposal ID: "
                                    + proposalEvent.proposalId
                                );

                                // $scope.voting.proposalText = proposalEvent.proposalText;
                                $scope.votingLoadForm.forContract = proposalEvent.forContract;
                                $scope.votingLoadForm.proposalId = parseInt(proposalEvent.proposalId);
                                // $scope.voting.resultsInBlock = proposalEvent.resultsInBlock;
                                $scope.load();
                            }

                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.createIsWorking = false;
                            $scope.$apply();
                        })
                };

                $scope.voting = {};
                $scope.loadIsWorking = false;
                $scope.load = function () {

                    $scope.loadIsWorking = true;

                    if (!$rootScope.web3.utils.isAddress($scope.votingLoadForm.forContract)) {
                        $scope.setAlertDanger(
                            $scope.votingLoadForm.forContract + " is not a valid Ethereum address"
                        );
                        $scope.loadIsWorking = false;
                        return;
                    }

                    $scope.voting = {};
                    $scope.voting.forContract = $scope.votingLoadForm.forContract;
                    $scope.voting.proposalId = parseInt($scope.votingLoadForm.proposalId);

                    try {
                        $scope.erc20Contract = new $rootScope.web3.eth.Contract(
                            $scope.erc20ContractAbi, // < the same for all networks
                            $scope.voting.forContract
                        );
                        $log.debug("erc20Contract ", $scope.erc20Contract._address, ":");
                        $log.debug($scope.erc20Contract);
                    } catch (e) {
                        $log.error(e);
                        $scope.setAlertDanger(
                            "ERROR:", $scope.voting.forContract, " is probably not a valid ERC20 contract"
                        );
                        return;
                    }

                    // TODO: debug >
                    // name() and symbol() does not work in MetaMask with web3@1.0.0-beta.35
                    // but does work in console
                    // $log.debug("start collecting data");
                    // $scope.erc20Contract.methods.name().call().then((result) => {$log.debug(result);});

                    // $scope.erc20Contract.methods.name().call({from: $rootScope.web3.eth.defaultAccount})
                    //     .then((result) => {
                    //         $log.debug(result);
                    //         $scope.voting.erc20ContractName = result;
                    //         return $scope.erc20Contract.methods.symbol().call({from: $rootScope.web3.eth.defaultAccount});
                    //     })
                    //     .then((result) => {
                    //         $log.debug(result);
                    //         $scope.voting.erc20TokenSymbol = result;
                    //         return $scope.contract.methods.votingCounterForContract($scope.voting.forContract).call({from: $rootScope.web3.eth.defaultAccount});
                    //     })

                    $scope.contract.methods.votingCounterForContract($scope.voting.forContract).call({from: $rootScope.web3.eth.defaultAccount})
                        .then((result) => {
                            $scope.voting.votingCounterForContract = parseInt(result);
                            if ($scope.voting.proposalId > result) {
                                throw "There is no proposal/voting with id/number "
                                + $scope.voting.proposalId
                                + " in smart contract "
                                + $scope.voting.forContract;
                            }
                            return $scope.contract.methods.proposalText($scope.voting.forContract, $scope.voting.proposalId).call({from: $rootScope.web3.eth.defaultAccount});
                        })
                        .then((result) => {
                            $scope.voting.proposalText = result;
                            return $scope.contract.methods.resultsInBlock($scope.voting.forContract, $scope.voting.proposalId)
                                .call({from: $rootScope.web3.eth.defaultAccount});
                        })
                        .then((result) => {
                            $scope.voting.resultsInBlock = parseInt(result);
                            return $rootScope.web3.eth.getBlockNumber();
                        })
                        .then((result) => {
                            result = parseInt(result);
                            if (result >= $scope.voting.resultsInBlock) {
                                $scope.voting.votingFinished = true;
                                $scope.calculate();
                            }
                            return $scope.contract.methods.boolVotedFor(
                                $scope.erc20Contract._address,
                                $scope.voting.proposalId,
                                $rootScope.web3.eth.defaultAccount)
                                .call({from: $rootScope.web3.eth.defaultAccount});
                        })
                        .then((result) => {
                            $scope.voting.boolVotedFor = result;
                            return $scope.contract.methods.boolVotedAgainst(
                                $scope.erc20Contract._address,
                                $scope.voting.proposalId,
                                $rootScope.web3.eth.defaultAccount)
                                .call({from: $rootScope.web3.eth.defaultAccount});
                        })
                        .then((result) => {
                            $scope.voting.boolVotedAgainst = result;
                            if ($scope.voting.boolVotedFor || $scope.voting.boolVotedAgainst) {
                                $scope.voting.alreadyVoted = true;
                            }

                            if ($location.host() === "localhost") {
                                $scope.voting.thisUrl = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/#!/" + "?contract=" + $scope.erc20Contract._address + "&proposal=" + $scope.voting.proposalId;
                            } else {
                                $scope.voting.thisUrl = $location.protocol() + "://" + $location.host() + "/#!/" + "?contract=" + $scope.erc20Contract._address + "&proposal=" + $scope.voting.proposalId;
                            }

                            $log.debug("$scope.voting.thisUrl:", $scope.voting.thisUrl);

                            $scope.voting.copyUrl = function () {
                                ngClipboard.toClipboard($scope.voting.thisUrl);
                            };

                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.loadIsWorking = false;
                            $scope.$apply();
                            // $log.debug("$scope.voting :");
                            // $log.debug($scope.voting);
                        })
                };

                $scope.voteForIsWorking = false;
                $scope.voteFor = function () {
                    $scope.voteForIsWorking = true;
                    $scope.setAlertSuccess(null);
                    $scope.contract.methods.voteFor(
                        $scope.voting.forContract,
                        parseInt($scope.voting.proposalId)
                    ).send({from: $rootScope.ethereum.selectedAddress})
                        .then((receipt) => {
                            $log.debug("tx voteFor receipt:");
                            $log.debug(receipt);
                            $scope.setAlertSuccess("Vote sent successfully");
                            $scope.load();
                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.voteForIsWorking = false;
                        })
                };

                $scope.voteAgainstIsWorking = false;
                $scope.voteAgainst = function () {
                    $scope.voteAgainstIsWorking = true;
                    $scope.setAlertSuccess(null);
                    $scope.contract.methods.voteAgainst(
                        $scope.voting.forContract, $scope.voting.proposalId
                    )
                        .send({from: $rootScope.ethereum.selectedAddress})
                        /*
                        .on('transactionHash', (hash) => {
                            $log.debug("tx hash:", hash)
                        })
                        .on('confirmation', (confirmationNumber, receipt) => {
                            $log.debug("confirmationNumber:", confirmationNumber, "receipt:", receipt);
                        })
                        .on('receipt', (receipt) => {
                            $log.debug("receipt:");
                            $log.debug(receipt);
                        })
                        .on('error', (error) => {
                            $log.error(error);
                        });*/
                        .then((receipt) => {
                            $log.debug("tx voteAgainst receipt:");
                            $log.debug(receipt);
                            $scope.setAlertSuccess("Vote sent successfully");
                            $scope.load();
                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            $scope.voteAgainstIsWorking = false;
                        })

                };

                $scope.calculate = function () {

                    totalSupply();

                    $scope.contract.methods.numberOfVotersFor(
                        $scope.voting.forContract,
                        parseInt($scope.voting.proposalId)
                    )
                        .call({from: $rootScope.ethereum.selectedAddress})
                        .then((result) => {

                            $scope.voting.numberOfVotersFor = parseInt(result);
                            countForVotes();

                            return $scope.contract.methods.numberOfVotersAgainst(
                                $scope.voting.forContract,
                                parseInt($scope.voting.proposalId)
                            )
                                .call({from: $rootScope.ethereum.selectedAddress});
                        })
                        .then((result) => {
                            $scope.voting.numberOfVotersAgainst = parseInt(result);
                            countAgainstVotes();
                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            // $log.debug("$scope.voting:");
                            // $log.debug($scope.voting);
                        })
                };

                function countForVotes() {
                    // $log.debug("start counting 'for' votes...");
                    let votes = 0;
                    let votersCounter = 0;

                    // start with: i=1
                    for (let i = 1; i <= $scope.voting.numberOfVotersFor; i++) {

                        $scope.contract.methods.votedFor(
                            $scope.voting.forContract,
                            parseInt($scope.voting.proposalId),
                            i
                        ).call({from: $rootScope.ethereum.selectedAddress})
                            .then((address) => {
                                return $scope.erc20Contract.methods.balanceOf(address)
                                    .call({from: $rootScope.ethereum.selectedAddress}, $scope.voting.resultsInBlock);
                            })
                            .then((balanceInTokens) => {
                                votes = votes + parseInt(balanceInTokens);
                                votersCounter++;
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                if (votersCounter === $scope.voting.numberOfVotersFor) {
                                    $scope.voting.votesFor = votes;

                                    $log.debug("$scope.voting (with FOR results):");
                                    $log.debug($scope.voting);
                                }
                                $scope.$apply();
                            })

                    }
                } // end of function countForVotes();

                function countAgainstVotes() {
                    // $log.debug("start counting 'against' votes...");
                    let votes = 0;
                    let votersCounter = 0;

                    // start with: i=1
                    for (let i = 1; i <= $scope.voting.numberOfVotersAgainst; i++) {

                        $scope.contract.methods.votedAgainst(
                            $scope.voting.forContract,
                            parseInt($scope.voting.proposalId),
                            i
                        ).call({from: $rootScope.ethereum.selectedAddress})
                            .then((address) => {
                                return $scope.erc20Contract.methods.balanceOf(address)
                                    .call({from: $rootScope.ethereum.selectedAddress}, $scope.voting.resultsInBlock);
                            })
                            .then((balanceInTokens) => {
                                votes = votes + parseInt(balanceInTokens);
                                votersCounter++;
                            })
                            .catch((error) => {
                                $log.error(error);
                                $scope.setAlertDanger(error);
                            })
                            .finally(() => {
                                if (votersCounter === $scope.voting.numberOfVotersAgainst) {
                                    $scope.voting.votesAgainst = votes;

                                    $log.debug("$scope.voting (with AGAINST results):");
                                    $log.debug($scope.voting);
                                }
                                $scope.$apply();
                            })

                    }
                } // end of function countAgainstVotes();

                function totalSupply() {
                    $scope.erc20Contract.methods.totalSupply().call(
                        {from: $rootScope.web3.eth.defaultAccount},
                        $scope.voting.resultsInBlock // <<< block
                    )
                        .then((result) => {
                            $scope.voting.totalSupply = parseInt(result);
                            // $log.debug("total supply:", $scope.voting.totalSupply);
                        })
                        .catch((error) => {
                            $log.error(error);
                            $scope.setAlertDanger(error);
                        })
                        .finally(() => {
                            // $log.debug("$scope.voting:");
                            // $log.debug($scope.voting);
                        })
                } // end of function totalSupply()

                // ui-router $stateParams:
                $log.debug("$stateParams:");
                $log.debug($stateParams);
                if ($stateParams && $stateParams.contract && $stateParams.proposal) {
                    $scope.createVotingForm.erc20ContractAddress = $stateParams.contract;
                    $scope.votingLoadForm.forContract = $stateParams.contract;
                    $scope.votingLoadForm.proposalId = parseInt($stateParams.proposal);
                    $scope.changeTab('load');
                    $scope.load();
                }


                $scope.events = [];
                $scope.maxEventsNumber = 100;
                $scope.contract.events.allEvents(
                    {fromBlock: $scope.contractsDeployedOnBlocks[$rootScope.currentNetwork.network_id]},
                    function (error, event) {
                        if (!error) {

                            // $log.debug("Event:");
                            // $log.debug(event);

                            if ($scope.maxEventsNumber && $scope.events.length > $scope.maxEventsNumber) {
                                $scope.allEvents.shift();
                            }

                            $scope.events.push(event);

                        } else {
                            $log.error(error);
                        }
                    })

            }

            $timeout($rootScope.progressbar.complete(), 1000);
        }
    ]
);
