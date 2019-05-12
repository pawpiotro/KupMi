package com.wpam.kupmi.utils;

import android.util.Pair;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

public class CoordinatesUtils
{
    public static GeoLocation getGeoLocation(Pair<Double, Double> location)
    {
        if (location != null)
            return new GeoLocation(location.first, location.second);
        return null;
    }

    public static Pair getCoordsPair(LatLng location)
    {
        if (location != null)
            return Pair.create(location.latitude, location.longitude);
        return null;
    }

    public static LatLng getLatLng(GeoLocation location)
    {
        if (location != null)
            return new LatLng(location.latitude, location.longitude);
        return null;
    }
}
