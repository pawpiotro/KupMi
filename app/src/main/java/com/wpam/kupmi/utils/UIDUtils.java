package com.wpam.kupmi.utils;

import java.util.UUID;

public class UIDUtils
{
    public static String getUID()
    {
        return UUID.randomUUID().toString();
    }
}
