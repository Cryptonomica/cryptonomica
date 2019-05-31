package net.cryptonomica.service;

import java.time.LocalDate;
import java.util.Date;

public class DateAndTimeService {

    // Unix time is is the number of seconds that have elapsed since
    // 00:00:00 Thursday, 1 January 1970,[2] Coordinated Universal Time (UTC), minus leap seconds
    private static Long utcUnixTimeFromYearMonthDayPrivate(Integer year, Integer month, Integer day) {

        /*
         * @param   year    the year minus 1900.
         * @param   month   the month between 0-11.
         * @param   date    the day of the month between 1-31.
         * @param   hrs     the hours between 0-23.
         * @param   min     the minutes between 0-59.
         * @param   sec     the seconds between 0-59.
         * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT for
         *  the date and time specified by the arguments.
         * */
        Long unixTime = Date.UTC(
                year - 1900,
                month - 1,
                day.intValue(),
                0,
                0,
                0
        ) / 1000;

        return unixTime;
    }

    public static Long utcUnixTimeFromLocalDate(LocalDate localDate) {
        return utcUnixTimeFromYearMonthDayPrivate(
                localDate.getYear(),
                localDate.getMonth().getValue(),
                localDate.getDayOfMonth()
        );
    }

    public static Long utcUnixTimeFromYearMonthDay(Integer year, Integer month, Integer day) {
        // create LocalDate object to validate arguments:
        LocalDate localDate = LocalDate.of(year, month, day);
        return utcUnixTimeFromLocalDate(localDate);
    }

    public static Date addDaysToDate(Date from, Integer numberOfDays) {

        /*
        // see: https://stackoverflow.com/a/36041712
        Date today=new Date();
        long ltime=today.getTime()+8*24*60*60*1000;
        Date today8=new Date(ltime);
        */

        Long millisecondsInDay = Long.valueOf(24 * 60 * 60 * 1000);
        Long newTime = from.getTime() + numberOfDays * millisecondsInDay;
        return new Date(newTime);

    }


}
