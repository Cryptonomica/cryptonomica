#!/usr/bin/env bash

# see: https://browsersync.io/docs/command-line
# sudo npm install -g browser-sync

browser-sync start --server './' --files './' --port 9527 --no-open --files
# google-chrome --incognito http://localhost:3001 http://localhost:9527


#browser-sync start --server 'src/main/webapp' --files 'src/main/webapp' --port 9527 --no-open --https
#browser-sync start --server 'src/main/webapp' --files 'src/main/webapp' --port 9527 --no-open


