#!/usr/bin/env bash

mvn clean
mvn endpoints-framework:discoveryDocs
mvn appengine:deploy