
# gcloud source repos clone default hello-habrahabr-webapp
# ERROR: (gcloud.source.repos.clone) Your current active account [craftonomica@gmail.com] does not have any valid credentials
# Please run:

#   $ gcloud auth login

# to obtain new credentials, or if you have already logged in with a
# different account:

#   $ gcloud config set account ACCOUNT

# to select an already authenticated account to use.

gcloud config set account "ageyev@gmail.com"
gcloud config set project "cryptonomica-test"
gcloud config list
rm ~/.appcfg_oauth2_tokens_java
