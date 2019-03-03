#!/usr/bin/env bash

# sed manual: https://www.gnu.org/software/sed/manual/sed.html

# use sed to modify files for production (cryptonomica-server)
# see:
# https://unix.stackexchange.com/questions/272061/bash-sh-script-to-replace-text-between-some-tags-strings-in-a-text-file
# https://stackoverflow.com/questions/30268770/how-to-insert-html-tag-with-sed
# escape '/' with '\'
# c is the change command in sed, it means "replace entire line(s)".  You cannot simply leave the #start and #end lines untouched.  If you want to keep them, you must re-insert them

gcloud config set project sandbox-cryptonomica

# ./pom.xml : 25
sed -i '/<!-- endpoints.project.id START -->/,/<!-- endpoints.project.id END -->/c\
        <!-- endpoints.project.id START -->\
        <endpoints.project.id>sandbox-cryptonomica<\/endpoints.project.id>\
        <!-- endpoints.project.id END -->' ./pom.xml

sed -i '/static Boolean SED_START/,/static Boolean SED_END/c\
    static Boolean SED_START;\
    public static final Boolean PRODUCTION = Boolean.FALSE;\
    public static final String WEB_CLIENT_ID = "517360814873-7pn1cta2addmcvug6rb1jr2u20vv2qnt.apps.googleusercontent.com";\
    public static final Integer priceForOneYerInEuroCents = 1 * 100;\
    public static final Integer discountInPercentForTwoYears = 60;\
    public static final String host = "sandbox-cryptonomica.appspot.com";\
    public static final String supportEmailAddress = "support@cryptonomica.zendesk.com";\
    public static final String adminEmailAddress = "admin@cryptonomica.net";\
    public static final String verificationServiceEmailAddress = "verification@cryptonomica.net";\
    public static final String emailSubjectPrefix = "[sandbox-cryptonomica] ";\
    static Boolean SED_END;' ./src/main/java/net/cryptonomica/constants/Constants.java


sed -i '/var SED_START;/,/var SED_END;/c\
            var SED_START;\
            $rootScope.PRODUCTION = false;\
            var SED_END;' ./src/main/webapp/app/app.js

