#!/usr/bin/env bash

mvn clean

git add . #  .gitignore
git commit -a
git push --all
#
mvn install
mvn appengine:update
