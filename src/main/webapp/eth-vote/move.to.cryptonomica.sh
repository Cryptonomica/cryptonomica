#!/usr/bin/env bash

ls -laFh ../cryptonomica/src/main/webapp/eth-vote/
rm -fr ../cryptonomica/src/main/webapp/eth-vote/*
ls -laFh ../cryptonomica/src/main/webapp/eth-vote/
cp -a . ../cryptonomica/src/main/webapp/eth-vote/
rm -fr ../cryptonomica/src/main/webapp/eth-vote/.idea
rm -fr ../cryptonomica/src/main/webapp/eth-vote/.iml
rm -fr ../cryptonomica/src/main/webapp/eth-vote/temp
ls -laFh ../cryptonomica/src/main/webapp/eth-vote/
