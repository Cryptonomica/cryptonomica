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
 */
public class HttpService {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(HttpService.class.getName());

    public static HTTPResponse makePostRequest(
            String urlAddress,
            String bodyStr) {

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

}
