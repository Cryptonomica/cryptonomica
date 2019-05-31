package net.cryptonomica.mail;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.cryptonomica.constants.Constants;

public class SendMailService {

    public static void sendEmailToAddress(
            String emailTo,
            String messageSubject,
            String messageText
    ) {
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(
                TaskOptions.Builder
                        .withUrl("/_ah/SendGridServlet")
                        .param("email", emailTo)
                        .param("messageSubject", Constants.emailSubjectPrefix + messageSubject)
                        .param("messageText", messageText)
        );
    }

}
