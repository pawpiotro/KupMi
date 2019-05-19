package com.wpam.kupmi.firebase.auth;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

    public void reauthenticate(String password,
                               final OnCompleteListener<Void> completeListener,
                               final OnFailureListener failureListener)
    {
        if (password == null)
            password = "";

        if (completeListener != null && failureListener != null)
        {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null)
            {
                String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                AuthCredential credential = EmailAuthProvider.getCredential(email,
                        password);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(completeListener)
                        .addOnFailureListener(failureListener);
            }
        }
    }

    // Before this method, you need to invoke user reauthentication.
    public void deleteUser(final OnCompleteListener<Void> completeListener,
                           final OnFailureListener failureListener)
    {
        if (completeListener != null && failureListener != null)
        {
            final FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null)
            {
                currentUser.delete().addOnCompleteListener(completeListener)
                        .addOnFailureListener(failureListener);
            }
        }
    }

    // Private methods
}
