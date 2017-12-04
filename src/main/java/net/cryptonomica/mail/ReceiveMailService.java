package net.cryptonomica.mail;

import net.cryptonomica.service.ApiKeysUtils;
import org.apache.commons.io.IOUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

        /*
         * You can set up your app to receive incoming email at addresses in the following format:
         *  anything@appid.appspotmail.com //  like mail@cryptonomica-server.appspotmail.com
         *  ( https://cloud.google.com/appengine/docs/standard/go/mail/sending-receiving-with-mail-api)
         *  Email messages are sent to your app as HTTP POST requests using the following URL:
         *  /_ah/mail/<ADDRESS>
         *  ( https://cloud.google.com/appengine/docs/standard/java/mail/receiving-mail-with-mail-api )
         * */

public class ReceiveMailService {

    private static final Logger LOG = Logger.getLogger(ReceiveMailService.class.getName());

    public static void processReceivedEmail(HttpServletRequest request) throws IOException {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            MimeMessage message = new MimeMessage(session, request.getInputStream());

            // Call getContentType() to extract the message content type.
            // The getContent() method returns an object that implements the Multipart interface.
            // Call getCount() to determine the number of parts
            // Call getBodyPart(int index) to return a particular body part.
            // or:
            String messageString = IOUtils.toString(message.getInputStream(), String.valueOf(StandardCharsets.UTF_8));
            LOG.warning("Received e-mail message from: " + message.getFrom()[0]);
            LOG.warning(messageString);

            String githubEmaiAddress = ApiKeysUtils.getApiKey("githubEmaiAddress");
            if (message.getFrom()[0].toString().equalsIgnoreCase(githubEmaiAddress)) {
//                String approvedHeaderValue = message.getHeader("Approved")[0];
                processMailFromGithub(message);
            }

        } catch (MessagingException e) {
            LOG.severe(e.getMessage());
        }
    }

    public static void processMailFromGithub(MimeMessage message) throws MessagingException {

        String approvedHeaderValue = message.getHeader("Approved")[0];
        String githubApprovedHeader = ApiKeysUtils.getApiKey("githubApprovedHeader");

        if (githubApprovedHeader.equalsIgnoreCase(approvedHeaderValue)) {


        }

    }

}
