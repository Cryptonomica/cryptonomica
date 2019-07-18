#!/usr/bin/env bash

# compile:
solc --version
solc ./contracts/VotingForERC20.sol --bin --abi --optimize --gas --overwrite -o ./contracts/ > ./contracts/Gas.Estimation.txt
ls ./contracts/