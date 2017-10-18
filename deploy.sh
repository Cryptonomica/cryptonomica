#!/usr/bin/env bash

mvn clean package
mvn endpoints-framework:discoveryDocs
mvn appengine:deploy