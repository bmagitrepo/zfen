package com.zoomlee.zoo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 11.03.15.
 */
public class TimeUtil {

    private static final SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private static final SimpleDateFormat formatUTC = new SimpleDateFormat("MMM d, yyyy", Locale.US);

    static {
        formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * return timestamp in seconds of the current day's end
     *
     * @return
     */
    public static long getServerEndDayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return (calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset()) / 1000;
    }

    public static Calendar getCalendarForCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        long serverTimestamp = calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(serverTimestamp);
        return calendar;
    }

    public static Calendar normalize(Calendar calendar) {
        long serverTimestamp = calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(serverTimestamp);
        return calendar;
    }

    public static Calendar getCalendarForServerTime(long serverTimestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(serverTimestamp * 1000);
        return calendar;
    }

    /**
     * @param serverTimestamp in seconds
     * @return in milliseconds
     */
    public static long getLocalTimestamp(long serverTimestamp) {
        long localTimestamp = serverTimestamp * 1000 - Calendar.getInstance().getTimeZone().getRawOffset();
        return localTimestamp;
    }

    /**
     *
     * @return timestamp in seconds
     */
    public static long getServerCurrentTimestamp() {
        Calendar calendar = Calendar.getInstance();
        return (System.currentTimeMillis() + calendar.getTimeZone().getRawOffset())/1000;
    }

    /**
     * format date in UTC timezone
     * @param date
     * @return MMM d, yyyy
     */
    public static String formatDateUTC(Date date) {
        return formatUTC.format(date);
    }

    /**
     * format date in UTC timezone
     * @param timestamp milliseconds
     * @return MMM d, yyyy
     */
    public static String formatDateUTC(long timestamp) {
        return formatDateUTC(new Date(timestamp));
    }

    /**
     * @param date
     * @return MMM d, yyyy
     */
    public static String formatDate(Date date) {
        return format.format(date);
    }

    /**
     * @param timestamp milliseconds
     * @return MMM d, yyyy
     */
    public static String formatDate(long timestamp) {
        return format.format(new Date(timestamp));
    }


    /**
     * parse date in UTC timezone
     * @param value
     * @return
     * @throws ParseException
     */
    public static Date parseDateUTC(String value) throws ParseException {
        return formatUTC.parse(value);
    }

    /**
     *
     * @param timestamp in seconds
     * @return timestamp of year's end in seconds
     */
    public static long getYearsEnd(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);

        cal.set(Calendar.MONTH, 11); // 11 = december
        cal.set(Calendar.DAY_OF_MONTH, 31); // new years eve
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return cal.getTimeInMillis() / 1000L;
    }

    /**
     *
     * @param timestamp in seconds
     * @return timestamp of next year's begin in seconds
     */
    public static long getNextYearsBegin(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000L);

        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return cal.getTimeInMillis() / 1000L;
    }
}
