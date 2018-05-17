package net.cryptonomica.testing;

import org.apache.commons.lang3.RandomStringUtils;

public class TestCryptoTools {

    private static final Integer priceForOneYearInEUR = 60;
    private static Integer randomStringLength = 11;

    public static void main(String[] args) {

        System.out.println("basic price for 1 y: EUR " + priceForOneYearInEUR);
        System.out.println(
                "basic price for 2 y: EUR " + calculatePriceForTwoYears(priceForOneYearInEUR * 100) / 100
        );

        System.out.println();
        Integer discountInPercent = 0;
        System.out.println("discount: " + discountInPercent + "%");

        System.out.println("1 y discounted price: EUR "
                + applyDiscount(priceForOneYearInEUR * 100, discountInPercent) / 100
        );
        System.out.println("2 y discounted price: EUR " + applyDiscount(
                calculatePriceForTwoYears(priceForOneYearInEUR * 100),
                discountInPercent) / 100
        );

        System.out.println("------------------");
        System.out.println(
                1000 - ((1000 / 100) * 30)
        );
    }

    private static String getRandomString(Integer len) {

        return RandomStringUtils.randomAlphanumeric(len);

    }

    private static Integer calculatePriceForTwoYears(Integer priceForOneYerInCents) {

        Integer discountInPercentForTwoYears = 20;
        Integer priceForTwoYearsInCents = (priceForOneYerInCents * 2) / 100 * (100 - discountInPercentForTwoYears);

        return priceForTwoYearsInCents;
    }

    private static Integer applyDiscount(Integer priceInCents, Integer discountInPercent) {
        System.out.println();
        System.out.println("price in cents: " + priceInCents);
        Integer discount = (priceInCents / 100) * discountInPercent;
        System.out.println("discount in cents: " + discount);
        priceInCents = priceInCents - discount;
        if (priceInCents < 100) {
            priceInCents = 100;
        }
        System.out.println("price in cents after discount: " + priceInCents);
        return priceInCents;
    }

}
