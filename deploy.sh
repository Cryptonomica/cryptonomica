#!/usr/bin/env bash


gcloud config set project cryptonomica-server

# see:
# https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/appengine-java8/endpoints-v2-backend

mvn clean package

mvn endpoints-framework:openApiDocs

# mvn endpoints-framework:discoveryDocs

# old:
# gcloud service-management deploy target/openapi-docs/openapi.json
# You can safely ignore the warnings about the paths in openapi.json not requiring an API key
# ( https://cloud.google.com/endpoints/docs/frameworks/java/get-started-frameworks-java )

# >>> new:
gcloud endpoints services deploy target/openapi-docs/openapi.json
# result like:
# Service Configuration [2018-02-21r0] uploaded for service [cryptonomica-server.appspot.com]

# for first rum before deploy run:
# gcloud app create
# if created already should be a message:
# ERROR: (gcloud.app.create) The project [cryptonomica-server] already contains an App Engine application in region [us-central].
# You can deploy your application using `gcloud app deploy`.
mvn appengine:deploy

# after deploy API should be accessible on
# https://apis-explorer.appspot.com/apis-explorer/?base=https://cryptonomica-server.appspot.com/_ah/api
# and web-site on
# https://cryptonomica.net and https://cryptonomica-server.appspot.com
# logs:
# https://console.cloud.google.com/logs/viewer?project=cryptonomica-server
# google dev console dashboard:
# https://console.cloud.google.com/home/dashboard?project=cryptonomica-server

# test:
echo 'sending test request to API:'
curl -H "Content-Type: application/json" -X POST -d '{"message":"echo"}' https://cryptonomica-server.appspot.com/_ah/api/testAPI/v1/echo

## remove old versions in dev console
# https://console.cloud.google.com/appengine/versions?project=cryptonomica-server
## flush cache on
# https://console.cloud.google.com/appengine/memcache?project=cryptonomica-server
#
