package net.cryptonomica.parsers;

import com.google.gson.Gson;

/**
 *
 */
public class Exceptions {

    public static void main(String[] args) {

        // http://stackoverflow.com/questions/22271099/convert-exception-to-json
        // parsing exception to JSON
        try {
            //something
            throwExceptionMethod();
        } catch (Exception e) {
            Gson gson = new Gson();
            System.out.println(gson.toJson(e));
        }
    } // end of main

    public static void throwExceptionMethod() throws Exception {
        throw new Exception("this is an exception");
    }

}
