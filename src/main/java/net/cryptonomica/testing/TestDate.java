package net.cryptonomica.testing;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TestDate {

    public static void main(String[] args) {

        Date dateExample = new Date(2018, 9, 13);

        // this will be: year 2018+1900, month: 9 started from 0 (10), day: 13

        //   Date dateExample = new Date();

        System.out.println(dateExample.toLocaleString());

        System.out.println("year:");
        Integer yearD = dateExample.getYear();
        System.out.println(yearD);

        System.out.println("month:");
        Integer monthD = dateExample.getMonth();
        System.out.println(monthD);

        System.out.println("day:");
        Integer dayD = dateExample.getDay();
        System.out.println(dayD);
        //
        LocalDate localDate = dateExample.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println("ZoneId.systemDefault().toString() : ");
        System.out.println(ZoneId.systemDefault().toString());
        System.out.println("localDate: " + localDate.toString());
        Integer yearL = localDate.getYear();
        Integer monthL = localDate.getMonthValue();
        Integer dayL = localDate.getDayOfMonth();
        System.out.println("Year: " + yearL.toString() + " Month: " + monthL.toString() + " Day: " + dayL.toString());

    }
}
