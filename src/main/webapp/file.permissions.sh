#!/usr/bin/env bash

#sudo chown -R viktor:viktor .
sudo chown -R vi .
sudo find . -type d -print0 | xargs -0 chmod 755 && sudo find . -type f -print0 | xargs -0 chmod 644

#  chmod +x *.sh && ls -laF
