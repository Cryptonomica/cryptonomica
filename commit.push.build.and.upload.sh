#!/usr/bin/env bash

mvn clean
git add . #  .gitignore
git commit -a
git push google --all
git push origin --all
#
mvn install
mvn appengine:update
mvn clean

# upload only:
# mvn clean install && mvn appengine:update && mvn clean
