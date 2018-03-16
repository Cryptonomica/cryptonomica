package net.cryptonomica.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;

import java.util.logging.Logger;

public class TelegramBotFactory {

    /* --- Logger:
    https://docs.oracle.com/javase/8/docs/api/java/util/logging/Logger.html
    Find or create a logger for a named subsystem. If a logger has already been created with the given name it is returned.
    Otherwise a new logger is created.
     - When running Tomcat on unixes, the console output is usually redirected to the file named catalina.out
    */
    private static final String className = CryptonomicaBot.class.getName();
    private static final Logger LOG = Logger.getLogger(className);


    public static TelegramBot getCryptonomicaBot() {

        final String cryptonomicaBotToken = ApiKeysUtils.getApiKey("CryptonomicaBotToken");
        TelegramBot bot = new TelegramBot(cryptonomicaBotToken);

        return bot;

    }

    public static Long getCryptonomicaAdminsChatId() {

        final String cryptonomicaAdminsChatIdString = ApiKeysUtils.getApiKey("cryptonomicaAdminsChatId");
        Long chat_id = Long.parseLong(cryptonomicaAdminsChatIdString);
        LOG.warning("Cryptonomica Admins Chat Id: " + chat_id);
        return chat_id;
    }


}
