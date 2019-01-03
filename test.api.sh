#!/usr/bin/env bash

# see:
# https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine-java8/endpoints-v2-backend

curl -H "Content-Type: application/json" -X POST -d '{"message":"echo"}' https://cryptonomica-server.appspot.com/_ah/api/testAPI/v1/echo

curl -H "Content-Type: application/json" -X GET https://cryptonomica-server.appspot.com/_ah/api/testAPI/v1/echo

