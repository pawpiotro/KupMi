package com.wpam.kupmi.lib;

public class Constants
{
    private static final String PACKAGE_NAME = "com.wpam.kupmi";

    // Location service
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int UPDATE_INTERVAL = 10000;
    public static final int FASTEST_INTERVAL = 1000;
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String GET_ADDRESS_RECEIVER = PACKAGE_NAME + ".GET_ADDRESS_RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String GET_ADDRESS_RESULT_DATA_KEY_LAT = PACKAGE_NAME + ".GET_ADDRESS_RESULT_DATA_KEY_LAT";
    public static final String GET_ADDRESS_RESULT_DATA_KEY_LON = PACKAGE_NAME + ".GET_ADDRESS_RESULT_DATA_KEY_LON";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String ADDRESS_DATA_EXTRA = PACKAGE_NAME + ".ADDRESS_DATA_EXTRA";

    // Map
    public static final String MAP_LAT = PACKAGE_NAME + ".MAP_LAT";
    public static final String MAP_LON = PACKAGE_NAME + ".MAP_LON";
    public static final float MAP_ZOOM = 15.0f;
    public static final double DEF_RADIUS = 100.0;
    public static final double MAX_RADIUS = 1000.0;
    public static final double MIN_RADIUS = 10.0;

    // User
    public static final String USER = PACKAGE_NAME + ".USER";
    public static final String USER_KIND_PARAM = PACKAGE_NAME + ".USER_KIND";

    // Request
    public static final String REQUEST_PACKAGE = ".SINGLE_REQUEST";
    public static final String REQUEST = PACKAGE_NAME + REQUEST_PACKAGE + ".REQUEST";
    public static final String REQUEST_PARTIAL_DATA_FLAG = PACKAGE_NAME + REQUEST_PACKAGE + ".REQUEST_FLAG";
    public static final String REQUEST_UID = PACKAGE_NAME + REQUEST_PACKAGE + ".REQUEST_UID";
    public static final String REQUEST_USER_ID = PACKAGE_NAME + REQUEST_PACKAGE + ".REQUEST_USER_ID";
}
