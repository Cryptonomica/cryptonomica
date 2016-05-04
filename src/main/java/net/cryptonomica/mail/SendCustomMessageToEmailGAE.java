package net.cryptonomica.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A servlet for sending an e-mail using GAE
 * see quotas:
 * https://cloud.google.com/appengine/docs/quotas#Mail
 */

/**
 * Created by viktor on 16/04/16.
 */
public class SendCustomMessageToEmailGAE extends HttpServlet {
    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(SendCustomMessageToEmailGAE.class
            .getName());

    /**
     * --- Send mail
     * params:
     * "email"
     * "emailCC"
     * "messageText"
     * "messageSubject"
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String email = request.getParameter("email");
        String emailCC = request.getParameter("emailCC");
        String messageSubject = request.getParameter("messageSubject");
        String messageText = request.getParameter("messageText");

        //
        LOG.warning("email: " + email);
        LOG.warning("messageText: " + messageText);
        LOG.warning("messageSubject: " + messageSubject);
        //  get session:
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        //
        try {
            Message message = new MimeMessage(session);
            InternetAddress from = new InternetAddress(
//                    String.format("noreply@%s.appspotmail.com", SystemProperty.applicationId.get()),
                    "admin@international-arbitration.org.uk",
                    "Cryptonomica");
            message.setFrom(from);
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(email, ""));
            message.addRecipient(Message.RecipientType.BCC,
                    new InternetAddress("admin@international-arbitration.org.uk", ""));

            if (emailCC != null) {
                message.addRecipient(Message.RecipientType.CC,
                        new InternetAddress(emailCC, ""));
            }

            message.setSubject(messageSubject);
            message.setText(messageText);
            Transport.send(message); // <<------------------------|
        } catch (MessagingException e) {
            LOG.log(Level.WARNING, String.format(
                    "Failed to send an mail to %s", email), e);
            throw new RuntimeException(e);

        }
    }
}
