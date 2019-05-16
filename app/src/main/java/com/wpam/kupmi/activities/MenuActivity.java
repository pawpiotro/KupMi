package com.wpam.kupmi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.wpam.kupmi.R;
import com.wpam.kupmi.activities.activeRequests.ActiveRequestsActivity;
import com.wpam.kupmi.activities.requestForm.RequestFormActivity;
import com.wpam.kupmi.activities.requestsSearch.RequestsSearchActivity;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.User;

import java.util.Objects;

import static com.wpam.kupmi.utils.DialogUtils.showOKDialog;

public class MenuActivity extends AppCompatActivity {

    // Private fields
    private static final String TAG = "MENU_ACTIVITY";

    private User user;

    // Override AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button makeRequest = findViewById(R.id.menu_request_button);
        Button lookForRequests = findViewById(R.id.menu_look_for_button);
        Button viewActive = findViewById(R.id.menu_view_active);
        Button settings = findViewById(R.id.menu_settings);
        Button signOff = findViewById(R.id.menu_logout);

        user = (User) Objects.requireNonNull(getIntent().getExtras()).getSerializable(Constants.USER);
        if (user == null) {
            showOKDialog(this, R.string.error_title, R.string.authorize_user_error,
                    android.R.drawable.ic_dialog_alert);
            returnToMainActivity();
        }

        makeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Click, make a request");
                Intent requestFormActivityIntent = getActivityIntent(RequestFormActivity.class);
                if (requestFormActivityIntent != null)
                    startActivity(requestFormActivityIntent);
            }
        });

        lookForRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Click, look for requests");
                Intent requestsSearchActivityIntent = getActivityIntent(RequestsSearchActivity.class);
                if (requestsSearchActivityIntent != null)
                    startActivity(requestsSearchActivityIntent);
            }
        });

        viewActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activeRequestsActivityIntent = getActivityIntent(ActiveRequestsActivity.class);
                if (activeRequestsActivityIntent != null)
                    startActivity(activeRequestsActivityIntent);
                Log.i(TAG, "Click, view active requests");
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Click, settings");
            }
        });

        signOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MenuActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                user = null;
                                MenuActivity.this.returnToMainActivity();
                                Log.i(TAG, "Click, signed out");
                            }
                        });
            }
        });

    }

    // Private methods
    private Intent getActivityIntent(Class activityClass) {
        if (activityClass != null) {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra(Constants.USER, user);

            return intent;
        }

        return null;
    }

    private void returnToMainActivity() {
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }
}
