#!/usr/bin/env bash

gcloud config set project cryptonomica-server

mvn clean

mvn endpoints-framework:openApiDocs

mvn endpoints-framework:discoveryDocs

gcloud service-management deploy target/openapi-docs/openapi.json
# You can safely ignore the warnings about the paths in openapi.json not requiring an API key
# ( https://cloud.google.com/endpoints/docs/frameworks/java/get-started-frameworks-java )

# for first rum before deploy run:
# gcloud app create
# if created already should be a message:
# ERROR: (gcloud.app.create) The project [cryptonomica-server] already contains an App Engine application in region [us-central].  You can deploy your application using `gcloud app deploy`.
mvn appengine:deploy

# after deploy API should be accessible on
# https://apis-explorer.appspot.com/apis-explorer/?base=https://cryptonomica-server.appspot.com/_ah/api
# and web-site on
# https://cryptonomica.net and https://cryptonomica-server.appspot.com
# logs:
# https://console.cloud.google.com/logs/viewer?project=cryptonomica-server
# google dev console dashboard:
# https://console.cloud.google.com/home/dashboard?project=cryptonomica-server
#