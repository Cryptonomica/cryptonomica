package net.cryptonomica.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

//
//

public class GetJSONfromURL {
    /* google example:  */
    // ...
    // try {
    //     URL url = new URL("http://www.example.com/atom.xml");
    //     BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    //     String line;
    //     while ((line = reader.readLine()) != null) {
    //         // ...
    //     }
    //     reader.close();
    // } catch (MalformedURLException e) {
    //     // ...
    // } catch (IOException e) {
    //     // ...
    // }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(GetJSONfromURL.class.getName());

    public static JSONObject getJSONObject(String stringURL) {
        // used sample code from:
        // http://samwize.com/2013/02/16/sample-get-slash-post-using-google-appengine-java-httpconnection/
        // -- but corrected, since it had errors
        // service to use for IP info:
        JSONObject jsonObj = null;
        try {
            URL url = new URL(stringURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Content-Type", "application/json");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String result = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    LOG.info("new line: " + line);
                    result = result + line;
                }
                reader.close();
                jsonObj = new JSONObject(result);
                // String count = jsonObj.getInt("count");
                LOG.info("Success: " + jsonObj.toString());
            } else {
                // Server returned HTTP error code.
                LOG.info(
                        "Request to URL " + stringURL + "returned HTTP error code (not 200) "
                );
            }
        } catch (MalformedURLException e) {
            LOG.info(
                    "Request to URL " + stringURL + "returned MalformedURLException: " + e.getMessage()
            );
        } catch (Exception e) {
            LOG.info(
                    "Request to URL " + stringURL + "returned error: " + e.getMessage()
            );
        }

        return jsonObj;
    }

    public static JSONObject getIpInfoIo(String stringIP) {
        /**
         * receive JSONobject from http://ipinfo.io/ + ipadress + /json
         */
        JSONObject response = null;
        String stringRequestURL = "http://ipinfo.io/" + stringIP + "/json";
        try {
            response = getJSONObject(stringRequestURL);
        } catch (Exception e) {
            LOG.severe("GetJSONfromURL.getIpInfoIo method with URL: " + stringRequestURL + " failed, with message: " + e.getMessage());
        }
        return response;
    }
}

