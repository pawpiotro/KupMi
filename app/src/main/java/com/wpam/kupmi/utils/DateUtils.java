package com.wpam.kupmi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static String getDateText(Date date, String dateFormat, Locale locale)
    {
        final SimpleDateFormat format = new SimpleDateFormat(dateFormat, locale);
        return format.format(date);
    }
}
