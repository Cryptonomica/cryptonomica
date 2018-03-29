package net.cryptonomica.servlets;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.gson.Gson;
import net.cryptonomica.service.HttpService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

// https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/Queue
// https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/taskqueue/QueueFactory

/**
 * This servlet receives a HTTP POST request from PAYPAL ,
 * reads its headers and parameters and sends them to admin's email
 * + writes them to log.
 */
public class IPNhandlerServlet extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(IPNhandlerServlet.class.getName());

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

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
        // Return an immutable java.util.Map containing parameter names as keys and parameter values as map values.
        // The keys in the parameter map are of type String. The values in the parameter map are of type String array
        Map<String, String[]> parameterMap = req.getParameterMap();
        String parameters = new Gson().toJson(parameterMap);
        LOG.warning(parameters);

        /* --- send email with headers ans parameters: */
        final Queue queue = QueueFactory.getDefaultQueue();
        // queue.add(
        //         TaskOptions.Builder
        //                 .withUrl(
        //                         // "/_ah/SendGridServlet")
        //                         "/_ah/SendCustomMessageToEmailGAE") // see:
        //                 // https://cloud.google.com/appengine/docs/quotas#Mail
        //                 .param("email",
        //                         "admin@cryptonomica.net"
        //                 )
        //                 .param("messageSubject",
        //                         "New payment info (EmailGAE)"
        //                 )
        //                 .param("messageText",
        //                         "HEADERS: \n" + headers + "\n"
        //                                 + "PARAMETERS: \n" + parameters + "\n"
        //                 )
        // );
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

        /* ---- Response: */
        // After receiving the IPN message from PayPal, your listener returns an empty HTTP 200 response to PayPal.
        // Otherwise, PayPal resends the IPN message.
        // https://developer.paypal.com/docs/classic/ipn/integration-guide/IPNImplementation/
        // ServletUtils.sendJsonResponse(resp, "");
        PrintWriter pw = resp.getWriter(); //get the stream to write the data
        pw.println();
        pw.close(); //closing the stream

        /* send message to paypal and get response */
        StringBuffer payload = new StringBuffer("cmd=_notify-validate");

        if (parameterMap != null) {
            String[] encodingParam = parameterMap.get("charset");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String name = entry.getKey();
                String[] value = entry.getValue();
                try {
                    payload.append("&").append(name).append("=")
                            .append(value[0]);
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }
            }
        }
        String payloadStr = payload.toString();


        LOG.warning(payloadStr);
        HTTPResponse httpResponse = HttpService.makePostRequest(
                "https://ipnpb.paypal.com/cgi-bin/webscr",
                payloadStr
        );
        LOG.warning(httpResponse.toString());

        queue.add(
                TaskOptions.Builder
                        .withUrl(
                                "/_ah/SendGridServlet")
                        .param("email",
                                "admin@cryptonomica.net"
                        )
                        .param("messageSubject",
                                "[cryptonomica-server] PayPal response"
                        )
                        .param("messageText",
                                "Message to Paypal: \n" + payloadStr + "\n\n"
                                        + "PayPal response: \n"
                                        + httpResponse.toString()
                                        + "\n"
                                        + new String(httpResponse.getContent())
                                        + "\n"
                                        + new Date().toString()
                        )
        );

    } // end of doPost

}

