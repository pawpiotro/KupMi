package com.wpam.kupmi.model;

import static com.wpam.kupmi.utils.StringUtils.isNullOrEmpty;

public enum RequestTag {
    DELIVERY,
    LOAN,
    REPAIR,
    ACTIVITY,
    OTHER,
    ALL;

    public String lowerCaseName() {
        return name().toLowerCase();
    }

    public String firstCapitalLetterName() {
        String lowerCaseName = lowerCaseName();

        return lowerCaseName.substring(0, 1).toUpperCase() + lowerCaseName.substring(1);
    }

    public String hashtagName() {
        return "#" + firstCapitalLetterName();
    }

    public static RequestTag getInstance(String tagName) {

        if (isNullOrEmpty(tagName))
            return ALL;

        switch (tagName.toUpperCase()) {
            case "DELIVERY":
                return DELIVERY;
            case "LOAN":
                return LOAN;
            case "REPAIR":
                return REPAIR;
            case "ACTIVITY":
                return ACTIVITY;
            case "OTHER":
                return OTHER;
            default:
                return ALL;
        }
    }

    public static RequestTag[] ALL_TAGS = {DELIVERY, LOAN, REPAIR, ACTIVITY, OTHER};
}
