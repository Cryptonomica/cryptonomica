package net.cryptonomica.service;

import com.google.appengine.api.urlfetch.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * service to send http and https requests
 *
 * If you use the Java 7 runtime, you must use the URL Fetch service to issue outbound HTTP(S) requests.
 * If you use the Java 8 runtime, you aren't required to use the URL Fetch service for outbound requests,
 * although the Java 8 runtime continues to support URL Fetch. (Note that App Engine Java runtimes are based on OpenJDK.)
 * https://cloud.google.com/appengine/docs/standard/java/issue-requests
 *
 * this class uses GAE URL Fetch service
 */
public class HttpService {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(HttpService.class.getName());

    public static HTTPResponse makePostRequestWithParametersMap(String urlAddress, Map<String, String> parameterMap) {

        StringBuffer payload = new StringBuffer();

        if (parameterMap != null) {

            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                try {
                    payload.append("&").append(name).append("=")
                            .append(value);
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }
            }
        }
        String payloadStr = payload.toString();
        return makePostRequest(urlAddress, payloadStr);
    }
    public static HTTPResponse makePostRequestWithParametersMapAndApiKey(String urlAddress, String apiKey, Map<String, String> parameterMap) {

        StringBuffer payload = new StringBuffer();

        if (parameterMap != null) {

            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                try {
                    payload.append("&").append(name).append("=")
                            .append(value);
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }
            }
        }

        String payloadStr = payload.toString();
        // return makePostRequest(urlAddress, payloadStr);
        return postRequestWithAPIkey(urlAddress, payloadStr, apiKey);
    }

    public static HTTPResponse makePostRequest(
            String urlAddress,
            String bodyStr
    ) {
        // check URL
        if (urlAddress == null
                || urlAddress.length() < 7
                || urlAddress.equals("")
                ) {
            throw new IllegalArgumentException("URL is to short or empty");
        }

        Object result = null;

        URL url = null;
        try {
            url = new URL(urlAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        byte[] payload = bodyStr.getBytes(StandardCharsets.UTF_8);

        HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
        request.setPayload(payload);

        URLFetchService urlfetch = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = null;
        try {
            response = urlfetch.fetch(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    public static HTTPResponse postWithPayload(
            String _urlAdress,
            String publisher,
            String docText,
            String apiKey) {

        // check URL
        if (_urlAdress == null
                || _urlAdress.length() < 10
                || _urlAdress.equals("")
                ) {
            throw new IllegalArgumentException("URL is to short or empty");
        }

        Object result = null;

//        String strURL = "https://ethnode.cryptonomica.net/api/proofofexistence-add";
        String strURL = _urlAdress;

        URL url = null;
        try {
            url = new URL(strURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String body = "publisher=" + publisher + "&text=" + docText + "&apikey=" + apiKey;
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);

        HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
        request.addHeader(new HTTPHeader("apikey", apiKey));

        request.setPayload(payload);

        URLFetchService urlfetch = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = null;
        try {
            response = urlfetch.fetch(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public static HTTPResponse getDocBySha256(
            String _urlAddress,
            String sha256,
            String apiKey) {

        // check URL
        if (_urlAddress == null
                || _urlAddress.length() < 10
                || _urlAddress.equals("")
                ) {
            throw new IllegalArgumentException("URL is to short or empty");
        }

        Object result = null;

        //  String strURL = "https://ethnode.cryptonomica.net/api/proofofexistence-add";
        String strURL = _urlAddress;

        URL url = null;
        try {
            url = new URL(strURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String body = "sha256=" + sha256 + "&apikey=" + apiKey;
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);

        HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
        request.addHeader(new HTTPHeader("apikey", apiKey));

        request.setPayload(payload);

        URLFetchService urlfetch = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = null;
        try {
            response = urlfetch.fetch(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    //
    private static String hashMapToPostRequestBody(HashMap<String, String> queryMap) {
        if (queryMap == null || queryMap.size() < 1 || queryMap.containsKey(null)) {
            throw new IllegalArgumentException("query is empty or malformed");
        }
        String body = "";
        for (Map.Entry<String, String> entry : queryMap.entrySet()) {
            body = body + entry.getKey() + "=" + entry.getValue() + "&";
        }
        return body;
    }

    public static HTTPResponse postRequestWithAPIkey(
            final String _urlAddress,
            final HashMap<String, String> queryMap,
            final String apiKey) {
        return postRequestWithAPIkey(_urlAddress, hashMapToPostRequestBody(queryMap), apiKey);
    }

    public static HTTPResponse postRequestWithAPIkey(
            final String _urlAddress,
            final String postRequestBody, //
            final String apiKey) {

        if (postRequestBody == null || postRequestBody.equals("")) {
            throw new IllegalArgumentException("request (postRequestBody) is empty");
        }

        URL url = null;
        try {
            url = new URL(_urlAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        byte[] payload = postRequestBody.getBytes(StandardCharsets.UTF_8);

        HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
        request.addHeader(new HTTPHeader("apiKey", apiKey));
        request.setPayload(payload);

        URLFetchService urlFetch = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = null;
        try {
            response = urlFetch.fetch(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public static HTTPResponse postRequestWithCustomHeader(
            final String _urlAddress,
            final String postRequestBody, //
            final String headerName,
            final String headerValue
    ) {

        if (postRequestBody == null || postRequestBody.equals("")) {
            throw new IllegalArgumentException("request (postRequestBody) is empty");
        }

        URL url = null;
        try {
            url = new URL(_urlAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        byte[] payload = postRequestBody.getBytes(StandardCharsets.UTF_8);

        HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
        request.addHeader(
                new HTTPHeader(headerName, headerValue)
        );
        request.setPayload(payload);

        URLFetchService urlFetch = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = null;
        try {
            response = urlFetch.fetch(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

}
