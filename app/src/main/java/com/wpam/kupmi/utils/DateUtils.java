package com.wpam.kupmi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils
{
    public static Date getDate(String dateText, String dateFormat, Locale locale)
    {
        try
        {
            final SimpleDateFormat format = new SimpleDateFormat(dateFormat, locale);
            return format.parse(dateText);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    // Only 24H format
    // TODO: Check timezone issue
    public static Date getDateFromTime(String timeText, String timeFormat, Locale locale)
    {
        Date now = new Date();
        Date date = getDate(timeText, timeFormat, locale);

        if (date != null && date.before(now))
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            date = cal.getTime();
        }

        return date;
    }

    public static String getDateText(Date date, String dateFormat, Locale locale)
    {
        final SimpleDateFormat format = new SimpleDateFormat(dateFormat, locale);
        return format.format(date);
    }
}
