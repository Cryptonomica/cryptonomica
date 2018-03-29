'use strict';

// example from:
// https://github.com/trufflesuite/truffle-artifactor#artifactorgenerateoptions-networks

const networks = {
    "1": {        // Main network
        "networkName": "Main Ethereum Network",
        "address": undefined, // contract address
        "contractAddress": undefined,
        "ownerAddress": undefined,
        "etherscanLinkPrefix": undefined
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
