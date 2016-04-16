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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 *
 */
public class IPNhandlerServlet extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(IPNhandlerServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* --- send email with payment details: */
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
                                "New payment info"
                        )
                        .param("messageText",
                                new Gson().toJson(req)
                        )
        );

        /* --- log request headers: */
        Enumeration headerNames = req.getHeaderNames();
        LOG.warning("[Headers start]: ");
        while (headerNames.hasMoreElements()) {
            Object nextElement = headerNames.nextElement();
            String headerName = nextElement.toString();
            LOG.warning(headerName + req.getHeader(nextElement.toString()));
        }
        LOG.warning("[Headers end]// ");
        LOG.warning("[Parameters]");
        LOG.warning(new Gson().toJson(req.getParameterMap()));

        /* --- read and log request body: */
        // http://stackoverflow.com/questions/14525982/getting-request-payload-from-post-request-in-java-servlet
        // http://stackoverflow.com/questions/3831680/httpservletrequest-get-json-post-data
        // see also:
        // http://stackoverflow.com/questions/1548782/retrieving-json-object-literal-from-httpservletrequest/15109169#15109169
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = req.getReader(); // <<---
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String reqBody = stringBuilder.toString();
        LOG.warning("[request body]:");
        LOG.warning(reqBody);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Enumeration headerNames = req.getHeaderNames();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter pw = resp.getWriter(); //get the stream to write the data
        pw.println("request headers: ");
        while (headerNames.hasMoreElements()) {
            pw.println(headerNames.nextElement().toString());
        }
        pw.println("~~~~~~~~~~~~");
        pw.println("request parameters:");
        pw.println(
                gson.toJson(
                        req.getParameterMap()
                )
        );
        pw.close(); //closing the stream
    }
}
