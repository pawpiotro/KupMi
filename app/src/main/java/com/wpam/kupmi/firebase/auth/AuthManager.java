package com.wpam.kupmi.firebase.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager
{
    // Private fields
    private FirebaseAuth auth;

    // Constructors
    private AuthManager()
    {
        auth = FirebaseAuth.getInstance();
    }

    // Public methods
    public static AuthManager getInstance()
    {
        return new AuthManager();
    }

    public boolean isSignIn()
    {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserUid()
    {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null)
            return currentUser.getUid();
        return null;
    }

    public String getCurrentUserName()
    {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null)
            return currentUser.getDisplayName();
        return null;
    }
}
