package com.wpam.kupmi.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.wpam.kupmi.R;
import java.util.Collections;
import java.util.List;

import static com.wpam.kupmi.utils.NetworkUtils.*;
import static com.wpam.kupmi.utils.DialogUtils.*;

public class MainActivity extends Activity
{
    // Firebase authentication section
    public static final List<AuthUI.IdpConfig> authProviders = Collections.singletonList(
            new AuthUI.IdpConfig.EmailBuilder().build());
    public static final int RC_SIGN_IN = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            startActivity(new Intent(this, MapsActivity.class));
            finish();
        }
        else
        {
            Button authEmailButton = findViewById(R.id.authEmailButton);
            authEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View v) {
                    if (isNetworkAvailable(MainActivity.this)) {
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(authProviders)
                                        .setLogo(R.drawable.fui_ic_check_circle_black_128dp)
                                        .setTheme(R.style.AppTheme)
                                        .build(),
                                RC_SIGN_IN);
                    }
                    else
                    {
                        showOKDialog(MainActivity.this, R.string.error_title, R.string.no_network_connection,
                                android.R.drawable.ic_dialog_alert);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            if (resultCode == RESULT_OK)
            {
                startActivity(new Intent(this, MapsActivity.class));
                finish();
            }
            else
            {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
            }
        }
    }
}
