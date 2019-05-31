package net.cryptonomica.service;

import java.text.DecimalFormat;

public class NumbersAndCurrencyService {

    /*
     * see: http://qaru.site/questions/4386503/how-to-always-round-off-upto-2-decimal-places-in-java
     * */
    public static String centsToEuroString(Integer cents) {

        // final Double priceForKeyVerificationInEuro = priceForKeyVerificationInCents / 100.0;
        final DecimalFormat twoDForm = new DecimalFormat("0.00");

        return twoDForm.format(cents / 100.0);
    }

}
