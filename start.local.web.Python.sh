#!/usr/bin/env bash

cd src/main/webapp/
python3 -m http.server 9527

# python -m SimpleHTTPServer 9527

#cd src/main/webapp/ && python -m SimpleHTTPServer 9527

#google-chrome --incognito http://localhost:9527