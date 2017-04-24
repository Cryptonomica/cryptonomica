#!/usr/bin/env bash

# sudo npm install -g browser-sync

cd ./src/main/webapp/ && browser-sync start --server --port 9527 --no-open
google-chrome --incognito http://localhost:3001 http://localhost:9527
#browser-sync start --server --port 9527 --no-open


