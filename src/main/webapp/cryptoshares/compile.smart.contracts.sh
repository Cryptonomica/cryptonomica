#!/usr/bin/env bash

solc --version

rm -rf ./contracts/compiled/cryptoshares/*
#solc ./contracts/CryptoShares.sol --bin --abi --gas --overwrite -o ./contracts/compiled/cryptoshares/ > ./contracts/compiled/cryptoshares/gas.estimation.txt
solc ./contracts/CryptoShares.sol --bin --abi --optimize --gas --overwrite -o ./contracts/compiled/cryptoshares/ >./contracts/compiled/cryptoshares/gas.estimation.txt

rm -rf ./contracts/compiled/cryptonomicaverificationmockup/*
solc ./contracts/CryptonomicaVerificationMockUp.sol --bin --abi --optimize --gas --overwrite -o ./contracts/compiled/cryptonomicaverificationmockup >./contracts/compiled/cryptonomicaverificationmockup/gas.estimation.txt

rm -rf ./contracts/compiled/xeuromockup/*
solc ./contracts/xEUR-MockUp.sol --bin --abi --optimize --gas --overwrite -o ./contracts/compiled/xeuromockup >./contracts/compiled/xeuromockup/gas.estimation.txt

rm -rf ./contracts/compiled/tests/*
#solc ./contracts/Tests.sol --bin --abi --gas --overwrite -o ./contracts/compiled/tests >./contracts/compiled/tests/gas.estimation.txt
#solc ./contracts/Tests.sol --bin --abi --optimize --gas --overwrite -o ./contracts/compiled/tests >./contracts/compiled/tests/gas.estimation.txt

#tree -s ./contracts/compiled/
