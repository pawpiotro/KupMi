package com.wpam.kupmi.activities.settings;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.auth.AuthManager;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.services.user.IUserDataStatus;
import com.wpam.kupmi.services.user.UserService;
import com.wpam.kupmi.utils.DialogUtils;
import java.util.Objects;

import static com.wpam.kupmi.utils.ActivityUtils.returnToMainActivity;
import static com.wpam.kupmi.utils.DialogUtils.showOKDialog;
import static com.wpam.kupmi.utils.DisplayUtils.convertPixelsToDp;

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
            Toast.makeText(this,
                    R.string.authorize_user_error, Toast.LENGTH_SHORT).show();
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
            userService.enableUserQuery(user.getUserUID(), false, this);
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
            Toast.makeText(this,
                    R.string.authorize_user_error, Toast.LENGTH_SHORT).show();
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

            userNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    switch (actionId)
                    {
                        case EditorInfo.IME_ACTION_DONE:
                        case EditorInfo.IME_ACTION_NEXT:
                        case EditorInfo.IME_ACTION_PREVIOUS:
                            String oldName = user.getName();
                            String newName = v.getText().toString();
                            if (!oldName.equals(newName))
                            {
                                DatabaseManager.getInstance().updateUserName(user.getUserUID(),
                                        newName);
                            }
                    }
                    return false;
                }
            });

            phoneNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    switch (actionId)
                    {
                        case EditorInfo.IME_ACTION_DONE:
                        case EditorInfo.IME_ACTION_NEXT:
                        case EditorInfo.IME_ACTION_PREVIOUS:
                            String oldPhoneNumber = user.getPhoneNumber();
                            String newPhoneNumber = v.getText().toString();
                            if (!oldPhoneNumber.equals(newPhoneNumber))
                            {
                                DatabaseManager.getInstance().updateUserPhoneNumber(user.getUserUID(),
                                        newPhoneNumber);
                            }
                    }
                    return false;
                }
            });

            Button deleteUserButton = findViewById(R.id.settings_delete_account_button);
            deleteUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Something wrong with input container margins.
                    LinearLayout inputContainer = new LinearLayout(SettingsActivity.this);
                    inputContainer.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                            , LinearLayout.LayoutParams.WRAP_CONTENT);
                    int margin = convertPixelsToDp(10, SettingsActivity.this);
                    lp.setMargins(margin, 0, margin, 0);

                    final EditText passwordInput = new EditText(SettingsActivity.this);
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordInput.setLayoutParams(lp);

                    inputContainer.addView(passwordInput, passwordInput.getLayoutParams());

                    final OnCompleteListener<Void> reauthenticatePositiveListener = new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Query acceptedRequesterRequests = DatabaseManager.getInstance().getRequestsQuery(
                                        RequestUserKind.REQUESTER, user.getUserUID(), false);
                                acceptedRequesterRequests.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!isAcceptedRequest(dataSnapshot.getChildren()))
                                        {
                                            Query acceptedSupplierRequests = DatabaseManager.getInstance().getRequestsQuery(
                                                    RequestUserKind.SUPPLIER, user.getUserUID(), false);
                                            acceptedSupplierRequests.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (!isAcceptedRequest(dataSnapshot.getChildren()))
                                                    {

                                                        AuthManager.getInstance().deleteUser(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    DialogUtils.showOKDialog(SettingsActivity.this, R.string.info_title,
                                                                            R.string.delete_user_success, android.R.drawable.ic_dialog_alert,
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    returnToMainActivity(SettingsActivity.this);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }, new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e(TAG, "deleteUserButton.OnClickListener(Auth) - Error: " +
                                                                        e.getMessage());

                                                                Toast.makeText(SettingsActivity.this,
                                                                        R.string.delete_user_failure, Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(SettingsActivity.this,
                                                                R.string.delete_user_accepted_requests, Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    Log.e(TAG, "deleteUserButton.OnClickListener(Supplier) - DatabaseError: " +
                                                            databaseError.getMessage());

                                                    Toast.makeText(SettingsActivity.this,
                                                            R.string.delete_user_failure, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        else
                                        {
                                            Toast.makeText(SettingsActivity.this,
                                                    R.string.delete_user_accepted_requests, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "deleteUserButton.OnClickListener(Requester) - DatabaseError: " +
                                                databaseError.getMessage());

                                        Toast.makeText(SettingsActivity.this,
                                                R.string.delete_user_failure, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    };

                    final OnFailureListener reauthenticateNegativeListener = new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsActivity.this,
                                    R.string.delete_user_invalid_password, Toast.LENGTH_SHORT).show();
                        }
                    };

                    final DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AuthManager.getInstance().reauthenticate(passwordInput.getText().toString(),
                                    reauthenticatePositiveListener,
                                    reauthenticateNegativeListener);
                        }
                    };

                    DialogUtils.showInputDialog(SettingsActivity.this,
                            R.string.question_title, R.string.delete_user_question,
                            android.R.drawable.ic_dialog_alert,
                            inputContainer, positiveListener, null);
                }
            });
        }
    }
    // Private / protected methods
    private boolean isAcceptedRequest(Iterable<DataSnapshot> snapshots)
    {
        if (snapshots != null) {
            DatabaseManager databaseManager = DatabaseManager.getInstance();

            for (DataSnapshot snapshot : snapshots) {
                if (databaseManager.getRequestState(snapshot) == RequestState.ACCEPTED)
                    return true;
            }
        }

        return false;
    }
}
