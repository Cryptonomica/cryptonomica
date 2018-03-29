package net.cryptonomica.servlets;

import net.cryptonomica.mail.ReceiveMailService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

        /*
         * https://cloud.google.com/appengine/docs/standard/java/mail/receiving-mail-with-mail-api
         *
         * */

public class MailHandlerServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(MailHandlerServlet.class.getName());

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ReceiveMailService.processReceivedEmail(request);
    }

    // doGet - not needed
    // see example on: https://cloud.google.com/appengine/docs/standard/java/mail/receiving-mail-with-mail-api

}
