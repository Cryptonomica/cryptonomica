package net.cryptonomica.servlets;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Utilities to use in servlets
 */
public class ServletUtils {

    /* ---- Logger: */
    private static final Logger LOG = Logger.getLogger(ServletUtils.class.getName());
    /* --- Gson: */
    private static Gson GSON = new Gson();

    /*
     * see:
     * https://stackoverflow.com/questions/3831680/httpservletrequest-get-json-post-data
     * see also alternative methods in
     * https://stackoverflow.com/questions/1548782/retrieving-json-object-literal-from-httpservletrequest
     *
     * */
    public static JSONObject getJsonObjectFromRequest(HttpServletRequest request) throws IOException {

        StringBuffer jb = new StringBuffer();
        String line = null;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        JSONObject jsonObject = null;

        try {
            jsonObject = HTTP.toJSONObject(jb.toString());

            LOG.warning("jsonObject:");
            LOG.warning(jsonObject.toString());
            for (String key : jsonObject.keySet()) {
                LOG.warning(key);
                LOG.warning(jsonObject.get(key).getClass().getName());
                LOG.warning(jsonObject.get(key).toString());
            }

        } catch (JSONException e) {
            throw new IOException("Error parsing JSON request string");
        }

        // Work with the data using methods like...
        // int someInt = jsonObject.getInt("intParamName");
        // String someString = jsonObject.getString("stringParamName");
        // JSONObject nestedObj = jsonObject.getJSONObject("nestedObjName");
        // JSONArray arr = jsonObject.getJSONArray("arrayParamName");
        // etc...

        return jsonObject;
    }

    public static JSONObject getJsonObjectFromRequestWithIOUtils(HttpServletRequest request) throws IOException {

        String jsonString = IOUtils.toString(request.getInputStream());
        JSONObject jsonObject = new JSONObject(jsonString);
        LOG.warning("jsonObject");
        LOG.warning(jsonObject.toString());

//        JSONObject messageJsonObject = jsonObject.getJSONObject("message");
//        LOG.warning("messageJsonObject:");
//        LOG.warning(messageJsonObject.toString());
//        String messageTextStr = messageJsonObject.getString("text");
//        LOG.warning("messageTextStr: " + messageTextStr);
//        LOG.warning("jsonObject:");
//        LOG.warning(jsonObject.toString());

        for (String key : jsonObject.keySet()) {
            LOG.warning(key);
            LOG.warning(jsonObject.get(key).getClass().getName());
            LOG.warning(jsonObject.get(key).toString());
        }

        return jsonObject;

    }

    public static String getRequestParameters(HttpServletRequest request) {

        // see: http://edwin.baculsoft.com/2014/04/logging-http-request-parameters-using-http-servlet/

        String result = "{";

        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String parametername = enumeration.nextElement();
            result += "\"" + parametername + "\":\"" + request.getParameter(parametername) + "\",";
        }
        result = removeLastComma(result) + "}";
        // LOG.warning(result);
        return result;
    } // end of getRequestParameters()

    public static String getCookies(HttpServletRequest request) {
        String result = "[";
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            result += "";
        } else {
            for (Cookie cookie : cookies) {
                result += "{"
                        + "\"name\": \"" + cookie.getName() + "\","
                        + "\"value\": \"" + cookie.getValue() + "\","
                        + "\"domain\": \"" + cookie.getDomain() + "\","
                        + "\"path\": \"" + cookie.getPath() + "\","
                        + "\"comment\": \"" + cookie.getComment()
                        + "},";
            }
            result = removeLastComma(result);
        }
        result += "]";
        // LOG.warning(result);
        return result;
    } // end of getCookies()

    public static String getRequestHeaders(HttpServletRequest request) {

        String result = "{";

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            result += "\"" + headerName + "\":\"" + request.getHeader(headerName) + "\",";
        }
        result = removeLastComma(result) + "}";
        // LOG.warning(result);
        return result;
    } // end of getRequestHeaderNames()

    public static String getAllRequestData(HttpServletRequest request) {

        String headerNamesStr = ServletUtils.getRequestHeaders(request);
        String requestParametersStr = ServletUtils.getRequestParameters(request);
        String cookiesStr = ServletUtils.getCookies(request);
        String requestDataSrt = "{\"request\": "
                //
                + "{"
                + "\"requestHeaders\": " + headerNamesStr + ","
                + "\"requestParameters\": " + requestParametersStr + ","
                + "\"cookies\":" + cookiesStr
                + "}"
                //
                + "}";
        return requestDataSrt;
    }

    public static void sendTxtResponse(HttpServletResponse response, final String text) throws IOException {

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
        response.setContentType("text/plain");

        response.setCharacterEncoding("UTF-8");
        PrintWriter pw = response.getWriter(); //get the stream to write the data
        pw.println(text);
        pw.close(); //closing the stream

    } // end of sendTxtResponse()

    public static void sendJsonResponse(HttpServletResponse response, final String jsonStr) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter pw = response.getWriter(); //get the stream to write the data
        pw.println(jsonStr);
        pw.close(); //closing the stream

    } // end of sendJsonResponse()

    public static String removeLastComma(String str) {

        int commaIndex = str.lastIndexOf(","); // -1 if there is no such occurrence.
        if (commaIndex > 0 && commaIndex == str.length() - 1) {
            str = str.substring(0, commaIndex);
        }
        return str;
    }

    /* see:
     * https://stackoverflow.com/questions/4050087/how-to-obtain-the-last-path-segment-of-an-uri
     * */
    public static String getUrlKey(HttpServletRequest request) {

        // https://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServletRequest.html#getRequestURI--
        String requestURI = request.getRequestURI();
        LOG.warning("request.getRequestURI(): " + request.getRequestURI());

        String urlKey = requestURI.substring(requestURI.lastIndexOf('/') + 1);

        return urlKey;
    }


}
