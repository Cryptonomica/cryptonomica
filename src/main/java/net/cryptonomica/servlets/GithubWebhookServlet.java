package net.cryptonomica.servlets;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import net.cryptonomica.service.ApiKeysUtils;
import net.cryptonomica.service.CryptonomicaBot;
import net.cryptonomica.service.TelegramBotFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

        /*
         *
         * https://github.com/Cryptonomica/cryptonomica/settings/hooks
         * */

public class GithubWebhookServlet extends HttpServlet {

    /* --- Logger: */
    // https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html
    // Find or create a logger for a named subsystem. If a logger has already been created with the given name it is returned.
    // Otherwise a new logger is created.
    // - When running Tomcat on unixes, the console output is usually redirected to the file named catalina.out
    private static final String className = GithubWebhookServlet.class.getName();
    private static final Logger LOG = Logger.getLogger(className);

    /* --- Gson: */
    private static final Gson GSON = new Gson();

    /* --- Key */
    // see: https://stackoverflow.com/questions/34476401/objectify-context-not-started-objectifyfilter-missing
    // "Filters apply to requests ... is not being called in the context of a request"
    // private static final String GITHUB_KEY = ApiKeysUtils.getApiKey("githubWebhookUrlKey");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // check url key:
        final String githubWebhookUrlKey = ServletUtils.getUrlKey(request);
        final String GITHUB_KEY = ApiKeysUtils.getApiKey("githubWebhookUrlKey");
        if (!GITHUB_KEY.equalsIgnoreCase(githubWebhookUrlKey)) {
            throw new ServletException("githubWebhookUrlKey is invalid");
        }

        String githubEvent = request.getHeader("X-GitHub-Event");
        LOG.warning("X-GitHub-Event: " + githubEvent);

        // log all request headers:
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            LOG.warning(header + " : " + request.getHeader(header));
        }

        // JSONObject githubMessageJsonObject = ServletUtils.getJsonObjectFromRequest(request);
        JSONObject githubMessageJsonObject = ServletUtils.getJsonObjectFromRequestWithIOUtils(request);


        String messageToCryptonomicaAdminsChat = null;
        if (githubEvent != null) {
            messageToCryptonomicaAdminsChat =
                    "New event from Github: \n"
                            + "type: " + githubEvent + "\n"
                            // for push events only:
                            + "url: " + githubMessageJsonObject.getJSONObject("repository").getString("html_url");
        } else {
            messageToCryptonomicaAdminsChat = "no 'X-GitHub-Event' header received";
        }

        HTTPResponse httpResponse = CryptonomicaBot.sendMessageToCryptonomicaAdminsChat(
                messageToCryptonomicaAdminsChat
        );

        LOG.warning(
                String.valueOf(httpResponse.getResponseCode())
        );

        /*
        TelegramBot cryptonomicaBot = TelegramBotFactory.getCryptonomicaBot();
        Long cryptonomicaAdminsChatId = TelegramBotFactory.getCryptonomicaAdminsChatId();
        String textToSend = "New event from Github: \n"
                + "type: " + githubEvent + "\n";
        // see message params on
        // https://core.telegram.org/bots/api#message
        SendMessage sendMessageRequest = new SendMessage(
                cryptonomicaAdminsChatId, // chat id
                textToSend
        )
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true)
                .disableNotification(false);
        LOG.warning("sendMessageRequest: " + sendMessageRequest.getParameters());
        SendResponse sendResponse = cryptonomicaBot.execute(sendMessageRequest);
        LOG.warning(sendResponse.toString());
        */

        String responseStr = "{message: 'Thank you!'}";
        ServletUtils.sendJsonResponse(response, responseStr);

    }

// --- only POST needed
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        doPost(request, response);
//    }


}
