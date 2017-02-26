package net.cryptonomica.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * for testing:
 */
public class TestServlet extends HttpServlet {

    /* --- Logger: */
    private final static Logger LOG = Logger.getLogger(TestServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletUtils.sendJsonResponse(response, ServletUtils.getAllRequestData(request));

    } // end of doGet
}
