package net.cryptonomica.servlets;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.gson.Gson;
import net.cryptonomica.service.ApiKeysUtils;
import net.cryptonomica.service.CryptonomicaBot;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TelegramWebhookServlet extends HttpServlet {

    /* --- Logger: */
    // https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html
    // Find or create a logger for a named subsystem. If a logger has already been created with the given name it is returned.
    // Otherwise a new logger is created.
    // - When running Tomcat on unixes, the console output is usually redirected to the file named catalina.out
    private static final String className = GithubWebhookServlet.class.getName();
    private static final Logger LOG = Logger.getLogger(className);

    /* --- Gson: */
    private static final Gson GSON = new Gson();

    /* to receive information using Telegram webhook ( Update object) */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // check url key:
        final String CryptonomicaBotTelegramUrlKey = ServletUtils.getUrlKey(request);
        final String TELEGRAM_URL_KEY = ApiKeysUtils.getApiKey("CryptonomicaBotTelegramUrlKey");
        if (!TELEGRAM_URL_KEY.equalsIgnoreCase(CryptonomicaBotTelegramUrlKey)) {
            throw new ServletException("CryptonomicaBotTelegramUrlKey is invalid");
        }

        // Whenever there is an update for the bot, we will send an HTTPS POST request to the specified url, containing a JSON-serialized Update
        // ( https://core.telegram.org/bots/api#setwebhook )
        // Update is an object that represents an incoming update
        // ( https://core.telegram.org/bots/api#update )

        JSONObject updateJsonObject = ServletUtils.getJsonObjectFromRequestWithIOUtils(request);

        JSONObject message = updateJsonObject.getJSONObject("message");
        // message_id represented by int:
        int message_id = message.getInt("message_id");
        // the actual UTF-8 text of the message, 0-4096 characters.
        String text = message.getString("text");
        JSONObject chat = message.getJSONObject("chat");
        // chat_id represented by int:
        int chat_id = chat.getInt("id");
        JSONObject from = message.getJSONObject("from");

        LOG.warning("message: " + message.toString());
        LOG.warning("message_id: " + message_id);
        LOG.warning("message text: " + text);
        LOG.warning("chat: " + chat.toString());
        LOG.warning("chat_id: " + chat_id);
        LOG.warning("from: " + from.toString());

        // see:
        // https://core.telegram.org/bots/api#sendmessage
        Map<String, String> parameterMap = new HashMap<>();
        String sendMessageText = "really, " + text + "?";
        parameterMap.put("text", sendMessageText);
        parameterMap.put("chat_id", Integer.toString(chat_id));
        parameterMap.put("reply_to_message_id", Integer.toString(message_id));

        HTTPResponse httpResponse = CryptonomicaBot.sendMessageWithParameterMap(parameterMap);
        LOG.warning("httpResponse: " + httpResponse);

        ServletUtils.sendJsonResponse(response, "O.K.");

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
