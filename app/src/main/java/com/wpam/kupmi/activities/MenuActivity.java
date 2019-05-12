package com.wpam.kupmi.activities;

import android.content.Context;
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
import com.wpam.kupmi.activities.requestForm.RequestFormActivity;
import com.wpam.kupmi.activities.requestsSearch.RequestsSearchActivity;

public class MenuActivity extends AppCompatActivity {

    private Button makeRequest;
    private Button lookForRequests;
    private Button viewActive;
    private Button settings;
    private Button signOff;

    private static final String TAG = "MENU_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        makeRequest = (Button) findViewById(R.id.menu_request_button);
        lookForRequests = (Button) findViewById(R.id.menu_look_for_button);
        viewActive = (Button) findViewById(R.id.menu_view_active);
        settings = (Button) findViewById(R.id.menu_settings);
        signOff = (Button) findViewById(R.id.menu_logout);

        final Context context = getBaseContext();

        makeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Click, make a request");
                startActivity(new Intent(context, RequestFormActivity.class));
            }
        });

        lookForRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Click, look for requests");
                startActivity(new Intent(context, RequestsSearchActivity.class));
            }
        });

        viewActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                MenuActivity.this.startActivity(new Intent(MenuActivity.this,
                                        MainActivity.class));
                                MenuActivity.this.finish();
                                Log.i(TAG, "Click, signed out");
                            }
                        });
            }
        });

    }
}