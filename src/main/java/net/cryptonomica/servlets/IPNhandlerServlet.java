package net.cryptonomica.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Logger;

// https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/Queue
// https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/QueueFactory

/**
 * This servlet receives a HTTP POST request,
 * reads its headers and parameters and sends them to admin's email
 * + writes them to log.
 */
public class IPNhandlerServlet extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(IPNhandlerServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* --- Read and log request headers: */
        Enumeration headerNames = req.getHeaderNames();
        LOG.warning("[Headers]: ");
        StringBuilder stringBuilderHeaders = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            Object nextElement = headerNames.nextElement();
            String headerName = nextElement.toString();
            String header = headerName + ": " + req.getHeader(headerName) + " \n";
            stringBuilderHeaders.append(header);
        }
        String headers = stringBuilderHeaders.toString();
        LOG.warning(headers);

        /* --- Read and log request parameters: */
        LOG.warning("[Parameters]:");
        String parameters = new Gson().toJson(req.getParameterMap());
        LOG.warning(parameters);

        /* --- send email with headers ans parameters: */
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                // "/_ah/SendGridServlet")
                                "/_ah/SendCustomMessageToEmailGAE") // see:
                        // https://cloud.google.com/appengine/docs/quotas#Mail
                        .param("email",
                                "admin@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "New payment info (EmailGAE)"
                        )
                        .param("messageText",
                                "HEADERS: \n" + headers + "\n"
                                        + "PARAMETERS: \n" + parameters + "\n"
                        )
        );
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        // "/_ah/SendCustomMessageToEmailGAE") // see:
                        // https://cloud.google.com/appengine/docs/quotas#Mail
                        .param("email",
                                "admin@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "New payment info (SendGrid)"
                        )
                        .param("messageText",
                                "HEADERS: \n" + headers + "\n"
                                        + "PARAMETERS: \n" + parameters + "\n"
                        )
        );
    }

    @Override
    /* for testing: reads headers and parameters and writes them to response */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Enumeration headerNames = req.getHeaderNames();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter pw = resp.getWriter(); //get the stream to write the data
        pw.println("request headers: ");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement().toString();
            pw.println(headerName + ": " + req.getHeader(headerName));
        }
        pw.println("~~~~~~~~~~~~");
        pw.println("request parameters:");
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        pw.println(
                gson.toJson(
                        req.getParameterMap()
                )
        );
        pw.close(); //closing the stream

        /* --- send email with headers ans parameters: */
                /* --- Read and log request headers: */
//        Enumeration headerNames = req.getHeaderNames(); // see above
        LOG.warning("[Headers]: ");
        StringBuilder stringBuilderHeaders = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            Object nextElement = headerNames.nextElement();
            String headerName = nextElement.toString();
            String header = headerName + ": " + req.getHeader(headerName) + " \n";
            stringBuilderHeaders.append(header);
        }
        String headers = stringBuilderHeaders.toString();
        LOG.warning(headers);

        /* --- Read and log request parameters: */
        LOG.warning("[Parameters]:");
        String parameters = new Gson().toJson(req.getParameterMap());
        LOG.warning(parameters);

        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        // "/_ah/SendCustomMessageToEmailGAE") // see:
                        // https://cloud.google.com/appengine/docs/quotas#Mail
                        .param("email",
                                "admin@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "from doGet (IPNhandlerServlet) - SendGridServlet"
                        )
                        .param("messageText",
                                "HEADERS: \n" + headers + "\n"
                                        + "PARAMETERS: \n" + parameters + "\n"
                        )
        );
        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                // "/_ah/SendGridServlet")
                                "/_ah/SendCustomMessageToEmailGAE") // see:
                        // https://cloud.google.com/appengine/docs/quotas#Mail
                        .param("email",
                                "admin@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "from doGet (IPNhandlerServlet) - SendCustomMessageToEmailGAE"
                        )
                        .param("messageText",
                                "HEADERS: \n" + headers + "\n"
                                        + "PARAMETERS: \n" + parameters + "\n"
                        )
        );

    }
}

