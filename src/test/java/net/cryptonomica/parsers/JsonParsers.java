package net.cryptonomica.parsers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 *
 */
public class JsonParsers {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(JsonParsers.class.getName());


    public static void main(String[] args) {

        SampleObj sampleObj = new SampleObj();
        sampleObj.apikey = "qwerty1234567890";
        sampleObj.publisher = "mail@gmail.com";
        sampleObj.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt";


        String strAddDocRequestObj = new Gson().toJson(sampleObj);
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(strAddDocRequestObj);

        LOG.warning("strAddDocRequestObj: " + strAddDocRequestObj);
        LOG.warning("je: " + je);

        byte[] payload1 = strAddDocRequestObj.getBytes(StandardCharsets.UTF_8);
        byte[] payload2 = je.toString().getBytes(StandardCharsets.UTF_8);

        LOG.warning("payload1: " + new String(payload1, StandardCharsets.UTF_8));
        LOG.warning("payload2: " + new String(payload2, StandardCharsets.UTF_8));
    }
}
