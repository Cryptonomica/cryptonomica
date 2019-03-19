#!/usr/bin/env bash

# see: https://browsersync.io/docs/command-line
# sudo npm install -g browser-sync

browser-sync start --server '.' --files '.' --port 9527 --no-open

# google-chrome --incognito http://localhost:3001 http://localhost:9527

