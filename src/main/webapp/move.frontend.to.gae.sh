#!/usr/bin/env bash

cd ../cryptonomica/src/main/webapp/
ls -latF
shopt -s extglob
rm -rv !("WEB-INF")
shopt -u extglob
ls -latF
cd ../../../../cryptonomica.github.io/
cp -rvu . ../cryptonomica/src/main/webapp/
rm -rfv ../cryptonomica/src/main/webapp/.idea
rm -rfv ../cryptonomica/src/main/webapp/cryptonomica.github.io.iml
rm -rfv ../cryptonomica/src/main/webapp/.gitignore
rm -rfv ../cryptonomica/src/main/webapp/.git
ls -latF ../cryptonomica/src/main/webapp/
