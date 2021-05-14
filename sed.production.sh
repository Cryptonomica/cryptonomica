#!/usr/bin/env bash

# sed manual: https://www.gnu.org/software/sed/manual/sed.html

# use sed to modify files for production (cryptonomica-server)
# see:
# https://unix.stackexchange.com/questions/272061/bash-sh-script-to-replace-text-between-some-tags-strings-in-a-text-file
# https://stackoverflow.com/questions/30268770/how-to-insert-html-tag-with-sed
# escape '/' with '\'
# c is the change command in sed, it means "replace entire line(s)".  You cannot simply leave the #start and #end lines untouched.  If you want to keep them, you must re-insert them

gcloud config set project cryptonomica-server

# ./pom.xml : 25
sed -i '/<!-- endpoints.project.id START -->/,/<!-- endpoints.project.id END -->/c\
        <!-- endpoints.project.id START -->\
        <endpoints.project.id>cryptonomica-server<\/endpoints.project.id>\
        <!-- endpoints.project.id END -->' ./pom.xml

sed -i '/<!-- sandbox SED_START -->/,/<!-- sandbox SED_END -->/c\
        <!-- sandbox SED_START -->\
        <!-- sandbox SED_END -->' ./src/main/webapp/index.html

sed -i '/static Boolean SED_START/,/static Boolean SED_END/c\
    static Boolean SED_START;\
    public static final Boolean PRODUCTION = Boolean.TRUE;\
    public static final String WEB_CLIENT_ID = "762021407984-9ab8gugumsg30rrqgvma9htfkqd3uid5.apps.googleusercontent.com";\
    public static final Integer priceForOneYerInEuroCents = 60 * 100;\
    public static final Integer discountInPercentForTwoYears = 20;\
    public static final String host = "cryptonomica.net";\
    public static final String supportEmailAddress = "support@cryptonomica.net";\
    public static final String adminEmailAddress = "admin@cryptonomica.net";\
    public static final String verificationServiceEmailAddress = "verification@cryptonomica.net";\
    public static final String emailSubjectPrefix = "[cryptonomica] ";\
    static Boolean SED_END;' ./src/main/java/net/cryptonomica/constants/Constants.java

sed -i '/var SED_START;/,/var SED_END;/c\
            var SED_START;\
            $rootScope.PRODUCTION = true;\
            var SED_END;' ./src/main/webapp/app/app.js



