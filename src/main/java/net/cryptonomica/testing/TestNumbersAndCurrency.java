package net.cryptonomica.testing;

import net.cryptonomica.service.NumbersAndCurrencyService;

public class TestNumbersAndCurrency {

    public static void main(String[] args) {

        System.out.println(
                NumbersAndCurrencyService.centsToEuroString(998)
        );

    }


}
