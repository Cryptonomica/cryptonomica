package net.cryptonomica.testing;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TestCountries {

    public static void main(String[] args) {

        List<String> countryCodes = Arrays.asList(Locale.getISOCountries());
        System.out.println(countryCodes.contains("US"));
        System.out.println(countryCodes.contains("IL"));

    }


}
