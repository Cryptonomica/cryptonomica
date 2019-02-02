package net.cryptonomica.testing;

import net.cryptonomica.service.DateAndTimeService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;

public class TestDate {

    public static void main(String[] args) {

        // lastDayOfMonth(2018, 8);

        DateAndTimeService.utcUnixTimeFromYearMonthDay(0,14,44);

    }

    public static void lastDayOfMonth(Integer year, Integer month) {

        Date start = new Date(year - 1900, month - 1, 1, 0, 0, 0);

        // see:
        // https://stackoverflow.com/questions/21242110/convert-java-util-date-to-java-time-localdate
        LocalDate firstDay = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Integer lastDayOfMonth = firstDay.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

        System.out.println("last day of "
                + firstDay.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " of "
                + firstDay.getYear()
                + " is "
                + lastDayOfMonth
        );
    }

    public static void printDateExample() {
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
