package net.cryptonomica.constants;

import com.google.api.server.spi.Constant;
import com.google.appengine.api.utils.SystemProperty;

/**
 * Contains the client IDs and scopes for allowed clients consuming your API.
 */
public class Constants {
    // public static final String WEB_CLIENT_ID = "602780521094-jim3gi59m9d2clhsi2kvuvad59c9m57l.apps.googleusercontent.com"; // cryptonomica-test
    public static final String WEB_CLIENT_ID = "762021407984-9ab8gugumsg30rrqgvma9htfkqd3uid5.apps.googleusercontent.com";
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
    //
}
