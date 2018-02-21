#!/usr/bin/env bash

# sudo npm install -g browser-sync

browser-sync start --server --port 9527 --no-open
google-chrome --incognito http://localhost:3001 http://localhost:9527

