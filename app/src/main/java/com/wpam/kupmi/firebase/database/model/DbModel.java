package com.wpam.kupmi.firebase.database.model;

import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.model.RequestUserKind;

public class DbModel
{
    // Data structures keys
    public static final String USERS_KEY = "users";

    public static final String REQUESTS_KEY = "requests";
    public static final String REQUESTER_KEY = RequestUserKind.REQUESTER.lowerCaseName();
    public static final String SUPPLIER_KEY = RequestUserKind.SUPPLIER.lowerCaseName();

    public static final String USER_UID_KEY = "userUID";
    public static final String DEADLINE_KEY = "deadline";
    public static final String STATE_KEY = "state";

    public static final String REQUESTS_DETAILS_KEY = "requests_details";

    public static final String ACTIVE_KEY = RequestState.ACTIVE.lowerCaseName();
    public static final String ACCEPTED_KEY = RequestState.ACCEPTED.lowerCaseName();
    public static final String DONE_KEY = RequestState.DONE.lowerCaseName();
    public static final String UNDONE_KEY = RequestState.UNDONE.lowerCaseName();

    public static final String REQUESTS_LOCATIONS_KEY = "requests_locations";

    public static final String TAGS_KEY = "tags";
    public static final String DELIVERY_KEY = RequestTag.DELIVERY.lowerCaseName();
    public static final String LOAN_KEY = RequestTag.LOAN.lowerCaseName();
    public static final String REPAIR_KEY = RequestTag.REPAIR.lowerCaseName();
    public static final String ACTIVITY_KEY = RequestTag.ACTIVITY.lowerCaseName();
}
