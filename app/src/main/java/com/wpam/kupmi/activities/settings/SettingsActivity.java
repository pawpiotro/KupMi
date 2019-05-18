package com.wpam.kupmi.activities.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.Query;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.services.user.IUserDataStatus;
import com.wpam.kupmi.services.user.UserService;

import java.util.Objects;

import static com.wpam.kupmi.utils.ActivityUtils.returnToMainActivity;
import static com.wpam.kupmi.utils.DialogUtils.showOKDialog;

public class SettingsActivity extends AppCompatActivity implements IUserDataStatus
{
    // Private fields
    private static final String TAG = "SETTINGS_ACTIVITY";

    private UserService userService;
    private User user;

    // Override AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        user = (User) Objects.requireNonNull(getIntent().getExtras()).getSerializable(Constants.USER);
        if (user == null)
        {
            showOKDialog(this, R.string.error_title, R.string.authorize_user_error,
                    android.R.drawable.ic_dialog_alert);
            returnToMainActivity(this);
        }
        else
        {
            userService = new UserService();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (userService != null)
            userService.enableUserQuery(user.getUserUID(), true, this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (userService != null)
            userService.disableUserQuery();
    }


    // Override IUserDataStatus
    @Override
    public void DataIsLoaded(User currentUser)
    {
        user = currentUser;

        if (user == null)
        {
            showOKDialog(this, R.string.error_title, R.string.authorize_user_error,
                    android.R.drawable.ic_dialog_alert);
            returnToMainActivity(this);
        }
        else
        {
            setContentView(R.layout.activity_settings);

            EditText userNameEditText = findViewById(R.id.settings_user_name_editText);
            TextView emailEditText = findViewById(R.id.settings_user_email_textView);
            EditText phoneNumberEditText = findViewById(R.id.settings_user_phone_number_editText);
            TextView reputationEditText = findViewById(R.id.settings_user_reputation_textView);

            userNameEditText.setText(user.getName());
            emailEditText.setText(user.getEmail());
            phoneNumberEditText.setText(user.getPhoneNumber());
            reputationEditText.setText(String.valueOf(user.getReputation()));

            Button deleteUserButton = findViewById(R.id.settings_delete_account_button);
            deleteUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Query acceptedRequesterRequests = DatabaseManager.getInstance().getRequestsQuery(
                            RequestUserKind.REQUESTER, user.getUserUID(), false);
                    Query acceptedSupplierRequests = DatabaseManager.getInstance().getRequestsQuery(
                            RequestUserKind.REQUESTER, user.getUserUID(), false);*/

                }
            });
        }
    }
    // Private / protected methods
}
