package net.cryptonomica.constants;

import com.google.api.server.spi.Constant;
import com.google.appengine.api.utils.SystemProperty;

/**
 * Contains the client IDs and scopes for allowed clients consuming your API.
 */
public class Constants {

    /* Values that have to be changed if switching from sandbox to production and vice-versa. SED_START and SED_END variables
     *  are only to mark start and and point for sed (https://www.gnu.org/software/sed/manual/sed.html)
     * */
    static Boolean SED_START;
    public static final Boolean PRODUCTION = Boolean.FALSE;
    public static final String WEB_CLIENT_ID = "517360814873-7pn1cta2addmcvug6rb1jr2u20vv2qnt.apps.googleusercontent.com";
    public static final Integer priceForOneYerInEuroCents = 1 * 100;
    public static final Integer discountInPercentForTwoYears = 60;
    public static final String host = "sandbox-cryptonomica.appspot.com";
    public static final String supportEmailAddress = "support@cryptonomica.zendesk.com";
    public static final String adminEmailAddress = "admin@cryptonomica.net";
    public static final String verificationServiceEmailAddress = "verification@cryptonomica.net";
    public static final String emailSubjectPrefix = "[sandbox-cryptonomica] ";
    static Boolean SED_END;

    public static final String ANDROID_CLIENT_ID = "replace this with your Android client ID";
    public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

    // public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    public static final String EMAIL_SCOPE = Constant.API_EMAIL_SCOPE;
    public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

    // -------->>> !!!!
    static final String applicationId = SystemProperty.applicationId.get();
    public static final String GAE_PROJECT_DOMAIN =
            applicationId + ".appspot.com";
    public final static String GAE_PROJECT_URL = "https://" + applicationId + ".appspot.com";
    public final static String CLOUDSTORAGE_DEFAULT_BUCKET = GAE_PROJECT_DOMAIN;

}
