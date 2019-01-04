#!/usr/bin/env bash

# see:
## https://github.com/web3j/web3j#java-smart-contract-wrappers
## https://docs.web3j.io/smart_contracts.html#smart-contract-wrappers

# compile:
#solc --version
#solc ./src/main/resources/TestContract.sol --bin --abi --optimize --gas --overwrite -o ./src/main/resources/ > ./src/main/resources/Gas.Estimation.txt

./smart.contracts/web3j-3.5.0/bin/web3j solidity generate ./smart.contracts/CryptonomicaVerification.bin ./smart.contracts/CryptonomicaVerification.abi -o ./src/main/java -p net.cryptonomica.ethereum
