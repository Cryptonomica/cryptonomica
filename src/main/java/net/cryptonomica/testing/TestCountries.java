package net.cryptonomica.testing;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class TestCountries {

    /* --- Logger: */
    private static final Logger LOG = Logger.getLogger(TestCountries.class.getName());
    /* --- Gson */
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {

        List<String> countryCodes = Arrays.asList(Locale.getISOCountries());
        System.out.println("countryCodes.size() : " + countryCodes.size());
        System.out.println(countryCodes.contains("US"));
        System.out.println(countryCodes.contains("IL"));

        HashMap<String, Integer> dataByCountry = new HashMap<>();

        for (int i = 0; i < countryCodes.size(); i++) {
            dataByCountry.put(countryCodes.get(i), i);
        }
        System.out.println(GSON.toJson(dataByCountry));

    }


}
