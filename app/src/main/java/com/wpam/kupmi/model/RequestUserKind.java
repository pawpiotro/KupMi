package com.wpam.kupmi.model;

public enum RequestUserKind
{
    REQUESTER,
    SUPPLIER,
    UNKNOWN;

    public String lowerCaseName() { return name().toLowerCase(); }

    public String firstCapitalLetterName()
    {
        String lowerCaseName = lowerCaseName();

        return lowerCaseName.substring(0, 1).toUpperCase() + lowerCaseName.substring(1);
    }

    public static RequestUserKind getInstance(String userKind)
    {
        switch (userKind.toUpperCase())
        {
            case "REQUESTER":
                return REQUESTER;
            case "SUPPLIER":
                return SUPPLIER;
            default:
                return UNKNOWN;
        }
    }
}
