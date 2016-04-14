#!/usr/bin/env bash

# see: http://habrahabr.ru/post/271385/

mvn clean install
mvn appengine:update
# appcfg.sh update ./src/main/webapp/ --noisy
# appcfg.sh update ./target/cryptonomica-net-v-1 --noisy
