package com.wpam.kupmi.utils;

public class FirebaseUtils
{
    public static String createPath(String... elements)
    {
        StringBuilder res = new StringBuilder();
        String separator = "/";
        String prefix = "";

        for (String element: elements)
        {
            res.append(prefix).append(element);
            prefix = separator;
        }

        return res.toString();
    }
}
