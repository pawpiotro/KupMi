package com.wpam.kupmi.firebase.database;

import android.util.Pair;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wpam.kupmi.firebase.database.config.DatabaseConfig;
import com.wpam.kupmi.firebase.database.model.DbModel;
import com.wpam.kupmi.firebase.database.model.DbRequest;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.utils.DateUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    public void addRequest(Request request)
    {
        DbRequest dbRequest = new DbRequest();
        dbRequest.setRequesterUID(request.getRequesterUID());
        dbRequest.setSupplierUID(request.getSupplierUID());
        dbRequest.setDeadline(DateUtils.getDateText(request.getDeadline(), DatabaseConfig.dateFormat,
                DatabaseConfig.dateFormatCulture));
        dbRequest.setDescription(request.getDescription());
        dbRequest.setTags(request.getTags());
        dbRequest.setState((long) request.getState().getStateId());

        Pair<Double, Double> requestLoc = request.getLocation();
        GeoHash locHash = new GeoHash(new GeoLocation(requestLoc.first, requestLoc.second));

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
