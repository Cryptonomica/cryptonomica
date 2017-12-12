#!/usr/bin/env bash

# see:
# https://stackoverflow.com/questions/14168677/merge-development-branch-with-master

git add . && git commit -a

git checkout dev
# on branch development:
git merge master
# (resolve any merge conflicts if there are any)
git checkout master
git merge --no-ff dev
# (there won't be any conflicts now)
git checkout dev
# (back to dev)

