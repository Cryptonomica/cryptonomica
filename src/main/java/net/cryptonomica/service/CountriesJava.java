package net.cryptonomica.service;

import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Locale;

/**
 * this is for testing
 */
public class CountriesJava {

    private static final DatePrinter DATE_PRINTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss zzz");

    public static void main(String[] args) {
        int i = 1;
//        for (Locale locale : Locale.getAvailableLocales()) {
        for (Locale locale : Locale.getAvailableLocales()) {
            System.out.println(i++);
            System.out.println(locale.getCountry() + "  " + locale.getDisplayCountry());
        }
        //
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        int j = 1;
        for (String code : Locale.getISOCountries()) {
            System.out.println(j++ + ": " + code);
        }

        System.out.println(DATE_PRINTER.format(new Date()));
    } // end: main
}
