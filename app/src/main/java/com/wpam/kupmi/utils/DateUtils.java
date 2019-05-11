package com.wpam.kupmi.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils
{
    public static Calendar getDate(String dateText, String dateFormat, Locale locale)
    {
        try
        {
            Calendar date = Calendar.getInstance(locale);
            final SimpleDateFormat format = new SimpleDateFormat(dateFormat, locale);

            date.setTime(format.parse(dateText));

            return date;
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    public static int getHour(Calendar date)
    {
        return date.get(Calendar.HOUR);
    }

    public static int getHourOfDay(Calendar date)
    {
        return date.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(Calendar date)
    {
        return date.get(Calendar.MINUTE);
    }

    public static void updateDate(Calendar date, Locale locale)
    {
        Calendar now = Calendar.getInstance(locale);

        if (date != null && date.before(now))
            date.add(Calendar.DATE, 1);
    }

    public static Calendar getNextHour(Locale locale)
    {
        Calendar date = Calendar.getInstance(locale);
        date.add(Calendar.HOUR_OF_DAY, 1);
        date.set(Calendar.MINUTE, 0);

        return date;
    }

    public static String getDateText(Calendar date, String dateFormat, Locale locale)
    {
        final SimpleDateFormat format = new SimpleDateFormat(dateFormat, locale);
        return format.format(date.getTime());
    }

    public static String getDateText(Calendar date, Context ctx)
    {
        Date dateTime = date.getTime();
        return DateFormat.getDateFormat(ctx).format(dateTime) + " " +
                DateFormat.getTimeFormat(ctx).format(dateTime);
    }
}
