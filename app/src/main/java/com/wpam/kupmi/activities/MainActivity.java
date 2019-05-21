package com.wpam.kupmi.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.auth.AuthManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.services.user.IUserDataStatus;
import com.wpam.kupmi.services.user.UserService;
import java.util.Collections;
import java.util.List;

import static com.wpam.kupmi.utils.NetworkUtils.*;

public class MainActivity extends Activity implements IUserDataStatus
{
    // Private fields
    private static final List<AuthUI.IdpConfig> authProviders = Collections.singletonList(
            new AuthUI.IdpConfig.EmailBuilder().build());
    private static final int RC_SIGN_IN = 0x01;

    private UserService userService;
    private ProgressBar bar;
    private Button authEmailButton;
    private TextView loggingInTextView;
    private RelativeLayout waitLayout;

    // Override Activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        userService = new UserService();

        setContentView(R.layout.activity_main);
        bar = findViewById(R.id.main_activity_progress_bar);
        authEmailButton = findViewById(R.id.authEmailButton);
        loggingInTextView = findViewById(R.id.main_activity_logging_in_textview);
        waitLayout = findViewById(R.id.main_activity_wait_layout);

        AuthManager authManager = AuthManager.getInstance();
        if (AuthManager.getInstance().isSignIn())
        {
            setBarVisible(true);
            String userUID = authManager.getCurrentUserUid();
            userService.enableUserQuery(userUID, true, this);
        }
        else
        {
            setAuthView();
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
                setBarVisible(true);
                userService.enableUserQuery(AuthManager.getInstance().getCurrentUserUid(),
                        true, this);
            }
            else
            {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
            }
        }
    }

    // Override IUserDataStatus
    @Override
    public void DataIsLoaded(User user)
    {
        setBarVisible(false);
        if (user == null)
        {
            Toast.makeText(this,
                    R.string.deactivated_user, Toast.LENGTH_SHORT).show();
            setAuthView();
        }
        else
        {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra(Constants.USER, user);
            startActivity(intent);
            finish();
        }
    }

    // Private methods
    private void setAuthView()
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
                    setBarVisible(false);
                    Toast.makeText(MainActivity.this,
                            R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Public methods

    public void setBarVisible(boolean b) {
        if (waitLayout == null)
            return;
        if (b) {
            waitLayout.setVisibility(View.VISIBLE);
//            bar.bringToFront();
//            loggingInTextView.bringToFront();
//            bar.setVisibility(View.VISIBLE);
//            loggingInTextView.setVisibility(View.VISIBLE);
            authEmailButton.setVisibility(View.GONE);
        } else {
//            bar.setVisibility(View.GONE);
//            loggingInTextView.setVisibility(View.GONE);
            waitLayout.setVisibility(View.GONE);
            authEmailButton.setVisibility(View.VISIBLE);
        }
    }
}
