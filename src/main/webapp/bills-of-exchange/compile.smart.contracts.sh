#!/usr/bin/env bash

solc --version
rm -rf ./contracts/compiled/bills-of-exchange-factory/*
#rm -rf ./contracts/compiled/CryptonomicaVerificationMockUp/*
solc ./contracts/BillsOfExchangeFactory.sol --bin --abi --optimize --gas --overwrite -o ./contracts/compiled/bills-of-exchange-factory > ./contracts/compiled/bills-of-exchange-factory/estimation.txt
#solc ./contracts/CryptonomicaVerificationMockUp.sol --bin --abi --optimize --gas --overwrite -o ./contracts/compiled/CryptonomicaVerificationMockUp > ./contracts/compiled/CryptonomicaVerificationMockUp/estimation.txt
tree -s ./contracts/compiled/
