#!/usr/bin/env bash

mvn clean

git add . #  .gitignore
git commit -a
git push --all
#
mvn install
mvn appengine:update
#
# works correct with versions:
# appcfg.sh update ./src/main/webapp/ --noisy
# appcfg.sh update ./target/cryptonomica-net-v-1 --noisy

