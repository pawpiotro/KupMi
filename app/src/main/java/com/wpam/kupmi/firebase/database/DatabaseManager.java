package com.wpam.kupmi.firebase.database;

import android.util.Pair;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.geofire.core.GeoHash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wpam.kupmi.firebase.database.config.DatabaseConfig;
import com.wpam.kupmi.firebase.database.model.DbModel;
import com.wpam.kupmi.firebase.database.model.DbRequest;
import com.wpam.kupmi.firebase.database.model.DbRequestDetails;
import com.wpam.kupmi.firebase.database.model.DbUser;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.utils.DateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.wpam.kupmi.firebase.database.model.DbModel.TAGS_KEY;
import static com.wpam.kupmi.utils.CoordinatesUtils.getGeoLocation;
import static com.wpam.kupmi.utils.FirebaseUtils.createPath;

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

    public Query getUserQuery(String userUID)
    {
        if (userUID != null)
            return dbRef.child(createPath(DbModel.USERS_KEY, userUID));

        return null;
    }

    public Query getRequestQuery(RequestUserKind userKind, String userUID, String requestUID)
    {
        if (userKind != null && userUID != null && requestUID != null)
        {
            return dbRef.child(createPath(DbModel.REQUESTS_KEY, userKind.lowerCaseName(),
                    userUID, requestUID));
        }

        return null;
    }

    public Query getRequestsQuery(RequestUserKind userKind, String userUID, boolean isOrderByChild)
    {
        if (userKind != null && userUID != null)
        {
            DatabaseReference requestsRef = dbRef.child(createPath(DbModel.REQUESTS_KEY, userKind.lowerCaseName(),
                    userUID));
            if (isOrderByChild)
                return requestsRef.orderByChild(DbModel.DEADLINE_KEY);
            else
                return requestsRef;
        }

        return null;
    }

    public Query getRequestDetailsQuery(String requestUID)
    {
        if (requestUID != null)
        {
            return dbRef.child(createPath(DbModel.REQUESTS_DETAILS_KEY, requestUID));
        }

        return null;
    }

    public void addQueryListener(Query query, ValueEventListener listener)
    {
        if (query != null && listener != null)
            query.addValueEventListener(listener);
    }

    public void addSingleQueryListener(Query query, ValueEventListener listener)
    {
        if (query != null && listener != null)
            query.addListenerForSingleValueEvent(listener);
    }

    public void removeQueryListener(Query query, ValueEventListener listener)
    {
        if (query != null && listener != null)
            query.removeEventListener(listener);
    }

    public void getRequestLocationQuery(String requestUID, RequestState state,
                                            LocationCallback callback)
    {
        if (requestUID != null && state != RequestState.UNKNOWN)
        {
            GeoFire geoRef = new GeoFire(dbRef.child(
                    createPath(DbModel.REQUESTS_LOCATIONS_KEY, state.lowerCaseName())));
            geoRef.getLocation(requestUID, callback);
        }
    }

    public GeoQuery getLocationRequestsQuery(Pair<Double, Double> location, double radius,
                                             RequestState state, GeoQueryEventListener listener)
    {
        if (location != null && radius >= 0.0f) {
            GeoFire geoRef = new GeoFire(dbRef.child(
                    createPath(DbModel.REQUESTS_LOCATIONS_KEY, state.lowerCaseName())));
            GeoQuery geoQuery = geoRef.queryAtLocation(getGeoLocation(location), radius);
            geoQuery.addGeoQueryEventListener(listener);

            return geoQuery;
        }
        return null;
    }

    public void addLocationRequestsListener(GeoQuery geoQuery, GeoQueryEventListener listener)
    {
        if (geoQuery != null && listener != null)
            geoQuery.addGeoQueryEventListener(listener);
    }

    public void removeLocationRequestsListeners(GeoQuery geoQuery)
    {
        if (geoQuery != null)
            geoQuery.removeAllListeners();
    }

    public void updateGeoQueryLocation(GeoQuery geoQuery, Pair<Double, Double> location)
    {
        if (geoQuery != null)
        {
            GeoLocation geoLocation = getGeoLocation(location);
            if (geoLocation != null)
                geoQuery.setCenter(geoLocation);
        }
    }

    public void updateGeoQueryRadius(GeoQuery geoQuery, double radius)
    {
        if (geoQuery != null && radius >= 0.0f)
            geoQuery.setRadius(radius);
    }

    public List<Query> getTagsRequestsQuery(HashMap<RequestTag, ValueEventListener> tags, RequestState state)
    {
        List<Query> result = new ArrayList<>();
        if (tags != null)
        {
            for (Map.Entry<RequestTag, ValueEventListener> entry: tags.entrySet())
            {
                RequestTag tag = entry.getKey();
                if (tag != RequestTag.ALL) {
                    Query tagQuery = dbRef.child(createPath(TAGS_KEY, tag.lowerCaseName(),
                            state.lowerCaseName()));
                    tagQuery.addListenerForSingleValueEvent(entry.getValue());
                    result.add(tagQuery);
                }
            }
        }

        return result;
    }

    public RequestState getRequestState(DataSnapshot snapshot)
    {
        if (snapshot != null)
        {
            DbRequest dbRequest = snapshot.getValue(DbRequest.class);

            if (dbRequest != null)
            {
                return RequestState.getInstance(dbRequest.getState().intValue());
            }
        }

        return RequestState.UNKNOWN;
    }

    public void addRequest(Request request)
    {
        DbRequest dbRequest = new DbRequest();
        dbRequest.setUserUID("");
        dbRequest.setDeadline(DateUtils.getDateText(request.getDeadline(), DatabaseConfig.DATE_FORMAT,
                DatabaseConfig.DATE_FORMAT_CULTURE));
        dbRequest.setTitle(request.getTitle());
        dbRequest.setTag(request.getTag().lowerCaseName());
        dbRequest.setState((long) request.getState().getStateId());

        DbRequestDetails dbRequestDetails = new DbRequestDetails();
        dbRequestDetails.setDescription(request.getDescription());
        dbRequestDetails.setLocationAddress(request.getLocationAddress());

        Pair<Double, Double> requestLoc = request.getLocation();
        GeoHash locHash = new GeoHash(getGeoLocation(requestLoc));

        Map<String, Object> updates = new HashMap<>();
        updates.put(createPath(DbModel.REQUESTS_KEY, DbModel.REQUESTER_KEY, request.getRequesterUID(),
                request.getRequestUID()), dbRequest);

        updates.put(createPath(DbModel.REQUESTS_DETAILS_KEY, request.getRequestUID()), dbRequestDetails);

        updates.put(createPath(DbModel.REQUESTS_LOCATIONS_KEY, request.getState().lowerCaseName(),
                request.getRequestUID(), "/g"), locHash.getGeoHashString());
        updates.put(createPath(DbModel.REQUESTS_LOCATIONS_KEY, request.getState().lowerCaseName(),
                request.getRequestUID(), "/l"), Arrays.asList(requestLoc.first, requestLoc.second));

        updates.put(createPath(TAGS_KEY, request.getTag().lowerCaseName(), request.getState().lowerCaseName(),
                request.getRequestUID()), request.getRequesterUID());

        dbRef.updateChildren(updates);
    }

    public void updateRequestState(String requestUID, String requesterUID, String supplierUID,
                                   RequestState state)
    {
        if (requestUID != null && requesterUID != null && state != null && state != RequestState.UNKNOWN)
        {
            int requestStateId = state.getStateId();

            Map<String, Object> updates = new HashMap<>();

            // For ACCEPTED state
            if (state != RequestState.ACCEPTED && supplierUID != null && !supplierUID.equals(""))
                updates.put(createPath(DbModel.REQUESTS_KEY, DbModel.REQUESTER_KEY,
                        requesterUID, DbModel.USER_UID_KEY), supplierUID);

            updates.put(createPath(DbModel.REQUESTS_KEY, DbModel.REQUESTER_KEY, requesterUID,
                    requestUID, DbModel.STATE_KEY), requestStateId);

            dbRef.updateChildren(updates);
        }
    }

    public void updateRequestDeadline(String requestUID, String requesterUID, Calendar deadline)
    {
        if (requestUID != null && deadline != null)
        {
            dbRef.child(createPath(DbModel.REQUESTS_KEY, DbModel.REQUESTER_KEY, requesterUID,
                    DbModel.DEADLINE_KEY)).setValue(DateUtils.getDateText(deadline, DatabaseConfig.DATE_FORMAT,
                    DatabaseConfig.DATE_FORMAT_CULTURE), null);
        }
    }

    public void updateUser(User user)
    {
        if (user != null)
        {
            String userUID = user.getUserUID();

            if (userUID != null && !Objects.equals(userUID, "")) {
                DbUser dbUser = new DbUser();
                dbUser.setEmail(user.getEmail());
                dbUser.setName(user.getName());
                dbUser.setPhoneNumber(user.getPhoneNumber());
                dbUser.setReputation(user.getReputation());

                dbRef.child(createPath(DbModel.USERS_KEY, userUID)).setValue(dbUser, null);
            }
        }
    }

    public void updateUserName(String userUID, String name)
    {
        if (userUID != null && !Objects.equals(userUID, "") && name != null) {
            dbRef.child(createPath(DbModel.USERS_KEY, userUID, DbModel.NAME_KEY)).setValue(name, null);
        }
    }

    public void updateUserPhoneNumber(String userUID, String phoneNumber)
    {
        if (userUID != null && !Objects.equals(userUID, "") && phoneNumber != null) {
            dbRef.child(createPath(DbModel.USERS_KEY, userUID, DbModel.PHONE_NUMBER_KEY)).setValue(phoneNumber, null);
        }
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
}
