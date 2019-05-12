package com.wpam.kupmi.firebase.database;

import android.util.Pair;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.wpam.kupmi.firebase.database.config.DatabaseConfig;
import com.wpam.kupmi.firebase.database.model.DbModel;
import com.wpam.kupmi.firebase.database.model.DbRequest;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.utils.DateUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.wpam.kupmi.utils.CoordinatesUtils.getGeoLocation;

public class DatabaseManager
{
    // Private fields

    private FirebaseDatabase db;
    private DatabaseReference dbRef;

    // Constructors
    private DatabaseManager()
    {
        this.db = FirebaseDatabase.getInstance();
        this.dbRef = db.getReference();
    }

    // Public methods
    public static DatabaseManager getInstance()
    {
        return new DatabaseManager();
    }

    public GeoQuery getRequestsLocationsQuery(Pair<Double, Double> location, double radius, GeoQueryEventListener listener)
    {
        if (location != null && radius >= 0.0f) {
            GeoFire geoRef = new GeoFire(dbRef.child(DbModel.REQUESTS_LOCATIONS_KEY));
            GeoQuery geoQuery = geoRef.queryAtLocation(getGeoLocation(location), radius);
            geoQuery.addGeoQueryEventListener(listener);

            return geoQuery;
        }
        return null;
    }

    public void updateLocationRequestsLocationsQuery(GeoQuery geoQuery, Pair<Double, Double> location)
    {
        if (geoQuery != null)
        {
            GeoLocation geoLocation = getGeoLocation(location);
            if (geoLocation != null)
                geoQuery.setCenter(geoLocation);
        }
    }

    public void updateRadiusRequestsLocationsQuery(GeoQuery geoQuery, double radius)
    {
        if (geoQuery != null && radius >= 0.0f)
            geoQuery.setRadius(radius);
    }

    public void removeRequestsLocationsListeners(GeoQuery geoQuery)
    {
        if (geoQuery != null)
            geoQuery.removeAllListeners();
    }

    public void addRequest(Request request)
    {
        DbRequest dbRequest = new DbRequest();
        dbRequest.setRequesterUID(request.getRequesterUID());
        dbRequest.setSupplierUID(request.getSupplierUID());
        dbRequest.setDeadline(DateUtils.getDateText(request.getDeadline(), DatabaseConfig.DATE_FORMAT,
                DatabaseConfig.DATE_FORMAT_CULTURE));
        dbRequest.setDescription(request.getDescription());
        dbRequest.setTags(request.getTags());
        dbRequest.setState((long) request.getState().getStateId());
        dbRequest.setLocationAddress(request.getLocationAddress());

        Pair<Double, Double> requestLoc = request.getLocation();
        GeoHash locHash = new GeoHash(getGeoLocation(requestLoc));

        Map<String, Object> updates = new HashMap<>();
        updates.put(createPath(DbModel.REQUESTS_KEY, request.getRequestUID()), dbRequest);
        updates.put(createPath(DbModel.REQUESTS_LOCATIONS_KEY, request.getRequestUID(), "/g"),
                locHash.getGeoHashString());
        updates.put(createPath(DbModel.REQUESTS_LOCATIONS_KEY, request.getRequestUID(), "/l"),
                Arrays.asList(requestLoc.first, requestLoc.second));
        dbRef.updateChildren(updates);
    }

    // Private methods
    private void setObject(String structureKey, String objectKey, Object object)
    {
        dbRef.child(structureKey).child(objectKey).setValue(object);
    }

    private void setObjectAttribute(String structureKey, String objectKey, String attributeName, Object newValue)
    {
        dbRef.child(structureKey).child(objectKey).child(attributeName).setValue(newValue);
    }

    private static String createPath(String... elements)
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
