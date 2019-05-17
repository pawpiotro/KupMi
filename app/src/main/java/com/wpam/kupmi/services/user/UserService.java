package com.wpam.kupmi.services.user;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wpam.kupmi.firebase.auth.AuthManager;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.firebase.database.model.DbUser;
import com.wpam.kupmi.model.User;

public class UserService
{
    // Private fields
    private final static String TAG = "USER_SERVICE";

    private DatabaseManager dbManager;
    private Query userQuery;
    private ValueEventListener userQueryListener;

    private User user;

    // Getters and setters
    public User getUser()
    {
        return user;
    }

    // Constructors
    public UserService()
    {
        dbManager = DatabaseManager.getInstance();
    }

    // Public methods
    public void enableUserQuery(String userUID, boolean isSingleListener, IUserDataStatus dataStatus)
    {
        if (userUID != null)
        {
            userQuery = dbManager.getUserQuery(userUID);
            if (userQuery != null)
            {
                if (isSingleListener)
                    dbManager.addSingleQueryListener(userQuery, new UserListener(dataStatus));
                else
                {
                    userQueryListener = new UserListener(dataStatus);
                    dbManager.addQueryListener(userQuery, userQueryListener);
                }
            }
        }
    }

    public void disableUserQuery()
    {
        if (userQuery != null && userQueryListener != null)
        {
            dbManager.removeQueryListener(userQuery, userQueryListener);
            userQueryListener = null;
            userQuery = null;
        }
    }

    public void clearUser()
    {
        user = null;
    }

    // Private classes
    private class UserListener implements ValueEventListener
    {
        private IUserDataStatus dataStatus;

        UserListener(IUserDataStatus dataStatus)
        {
            this.dataStatus = dataStatus;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String dbUserUID = dataSnapshot.getKey();
            DbUser dbUser = dataSnapshot.getValue(DbUser.class);
            if (dbUser != null)
                user = new User(dbUserUID, dbUser.getName(), dbUser.getEmail(),
                        dbUser.getPhoneNumber(), dbUser.getReputation());
            else
                user = null;
            dataStatus.DataIsLoaded(user);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "onCancelled - databaseError: " + databaseError.getMessage());
        }
    }
}
