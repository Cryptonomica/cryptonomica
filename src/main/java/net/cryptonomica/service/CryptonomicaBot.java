package net.cryptonomica.service;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CryptonomicaBot {

    /* --- Logger: */
    // https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html
    // Find or create a logger for a named subsystem. If a logger has already been created with the given name it is returned.
    // Otherwise a new logger is created.
    // - When running Tomcat on unixes, the console output is usually redirected to the file named catalina.out
    private static final String className = CryptonomicaBot.class.getName();
    private static final Logger LOG = Logger.getLogger(className);

    //    public static Message sendMessageToCryptonomicaAdminsChat(final String message) {
    public static HTTPResponse sendMessageToCryptonomicaAdminsChat(final String message) {

        final String CryptonomicaBotToken = ApiKeysUtils.getApiKey("CryptonomicaBotToken");

        final String urlAddress = "https://api.telegram.org/bot" + CryptonomicaBotToken + "/sendMessage";

        final String cryptonomicaAdminsChatId = ApiKeysUtils.getApiKey("cryptonomicaAdminsChatId");

        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("chat_id", cryptonomicaAdminsChatId);
        parameterMap.put("text", message);

        // https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/urlfetch/HTTPResponse
        //
        HTTPResponse httpResponse = HttpService.makePostRequestWithParametersMap(urlAddress, parameterMap);

        byte[] httpResponseContentBytes = httpResponse.getContent();
        String httpResponseContentString = null;
        if (httpResponseContentBytes != null) {
            httpResponseContentString = new String(httpResponseContentBytes, StandardCharsets.UTF_8);

            LOG.warning(
                    "httpResponse.getFinalUrl() : " + httpResponse.getFinalUrl() + "\n"
                            + "httpResponse.getResponseCode(): " + httpResponse.getResponseCode() + "\n"
                            + "httpResponse.getContent(): " + httpResponseContentString + "\n"
            );
            LOG.warning("headers: ");
            for (HTTPHeader httpHeader : httpResponse.getHeaders()) {
                String headerName = httpHeader.getName();
                String headerValue = httpHeader.getValue();
                LOG.warning(headerName + " : " + headerValue);
            }

        } else {
            LOG.severe(
                    "httpResponse.getContent() is null"
            );
        }

        return httpResponse;
    }


}
