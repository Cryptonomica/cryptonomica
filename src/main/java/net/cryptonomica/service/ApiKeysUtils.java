package net.cryptonomica.service;

import com.googlecode.objectify.Key;
import net.cryptonomica.entities.AppSettings;

import java.util.logging.Logger;

import static net.cryptonomica.service.OfyService.ofy;

/**
 * Utilities to work with API Keys
 */
public class ApiKeysUtils {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(ApiKeysUtils.class.getName());

    public static String getApiKey(String apiKeyName) {

        String API_KEY = ofy()
                .load()
                .key(Key.create(AppSettings.class, apiKeyName))
                .now()
                .getValue();

        return API_KEY;
    } // end of getApiKey();
}
