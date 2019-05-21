package com.wpam.kupmi.activities.singleRequest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.lib.PermissionsClassLib;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.services.user.IUserDataStatus;
import com.wpam.kupmi.services.user.UserService;

import static com.wpam.kupmi.model.RequestUserRating.NEGATIVE_RATING;
import static com.wpam.kupmi.model.RequestUserRating.NEUTRAL_RATING;
import static com.wpam.kupmi.model.RequestUserRating.POSITIVE_RATING;
import static com.wpam.kupmi.utils.StringUtils.isNullOrEmpty;

public class SingleRequestUserFragment extends Fragment implements IUserDataStatus {
    private static final String TAG = "SINGLE_REQUEST_USER_FRAGMENT";
    private SingleRequestActivity parentActivity;

    private ImageView imageView;
    private ImageView userPlaceholder;
    private TextView nameView;
    private TextView emailView;
    private TextView phoneView;
    private TextView repView;

    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton callButton;

    private User user;

    private int currentRating = NEUTRAL_RATING;
    private Query ratingQuery;
    private ValueEventListener ratingQueryListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_request_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentActivity = (SingleRequestActivity) getActivity();
        imageView = getView().findViewById(R.id.single_request_user_imageview);
        userPlaceholder = getView().findViewById(R.id.single_request_user_placeholder);
        nameView = getView().findViewById(R.id.single_request_user_name);
        emailView = getView().findViewById(R.id.single_request_user_email);
        phoneView = getView().findViewById(R.id.single_request_user_phone);
        repView = getView().findViewById(R.id.single_request_user_reputation);

        upButton = getView().findViewById(R.id.single_request_user_up_button);
        downButton = getView().findViewById(R.id.single_request_user_down_button);
        callButton = getView().findViewById(R.id.single_request_user_call_button);

        showPlaceholder(true);
        updateUserData();

        ratingQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long rating = dataSnapshot.getValue(Long.class);
                currentRating = rating != null ? rating.intValue() : NEUTRAL_RATING;

                showRepButtons(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "ratingQueryListener - " + databaseError.getMessage());
            }
        };

        upButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (user != null && !isNullOrEmpty(user.getEmail()))
                setRating((long) POSITIVE_RATING);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && !isNullOrEmpty(user.getEmail()))
                    setRating((long) NEGATIVE_RATING);
            }
        });


        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "No call permission");
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                            PermissionsClassLib.CALL_PHONE_PERMISSION_CODE);
                } else {
                    callPhoneNumber();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        RequestState requestState = parentActivity.getRequest().getState();
        String userUID = getUserID();
        String requestUID = parentActivity.getRequest().getRequestUID();
        if (!isNullOrEmpty(userUID) && !isNullOrEmpty(requestUID)
                && (requestState == RequestState.DONE || requestState == RequestState.UNDONE))
        {
            ratingQuery = DatabaseManager.getInstance().getRequestUserRating(
                    requestUID, userUID);
            if (ratingQuery != null)
                ratingQuery.addValueEventListener(ratingQueryListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (ratingQuery != null) {
            ratingQuery.removeEventListener(ratingQueryListener);
            ratingQuery = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "RequestCode: " + requestCode);
        switch (requestCode) {
            case PermissionsClassLib.CALL_PHONE_PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission granted");
                    callPhoneNumber();
                } else {
                    Toast.makeText(parentActivity, "You can't call without this permission.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void updateUserData(){
        String userID;
        userID = getUserID();
        UserService userService = new UserService();
        userService.enableUserQuery(userID, false, this);
        updateButtons(parentActivity.getRequest().getState());
    }

    private String getUserID()
    {
        if (parentActivity.getRequestUserKind().equals(RequestUserKind.REQUESTER))
            return parentActivity.getRequest().getSupplierUID();
        else
            return parentActivity.getRequest().getRequesterUID();
    }

    private void setData(User user) {
        if (user == null)
            return;
        if (nameView == null || emailView == null
                || phoneView == null || repView == null)
            return;

        nameView.setText(user.getName());
        emailView.setText(user.getEmail());
        phoneView.setText(user.getPhoneNumber());
        Long rep = user.getReputation();
        if (rep != null)
            repView.setText(rep.toString());

        showPlaceholder(false);
    }

    @Override
    public void DataIsLoaded(User user) {
        this.user = user;
        setData(user);
    }

    private void showPlaceholder(boolean b) {
        if (userPlaceholder == null)
            return;
        if (b) {
            userPlaceholder.bringToFront();
            userPlaceholder.setVisibility(View.VISIBLE);
        } else {
            userPlaceholder.setVisibility(View.GONE);
        }
    }


    private void updateButtons(RequestState state) {
        switch (state) {
            case DONE:
            case UNDONE:
                // Another listener should do that.
                //showRepButtons(true);
                showCallButton(false);
                break;
            case ACCEPTED:
                showRepButtons(false);
                showCallButton(true);
                break;
            case ACTIVE:
            case UNKNOWN:
                showCallButton(false);
                showRepButtons(false);
            default:
                break;
        }
    }

    private void showRepButtons(boolean b) {
        if (upButton != null && downButton != null) {
            if (b)
            {
                switch (currentRating)
                {
                    case POSITIVE_RATING:
                        upButton.bringToFront();
                        upButton.setVisibility(View.VISIBLE);
                        downButton.setVisibility(View.GONE);

                        upButton.setClickable(false);
                        downButton.setClickable(false);
                        break;
                    case NEGATIVE_RATING:
                        downButton.bringToFront();
                        downButton.setVisibility(View.VISIBLE);
                        upButton.setVisibility(View.GONE);

                        upButton.setClickable(false);
                        downButton.setClickable(false);
                        break;
                    case NEUTRAL_RATING:
                        upButton.bringToFront();
                        downButton.bringToFront();
                        upButton.setVisibility(View.VISIBLE);
                        downButton.setVisibility(View.VISIBLE);

                        upButton.setClickable(true);
                        downButton.setClickable(true);
                        break;
                    default:
                        upButton.setVisibility(View.GONE);
                        downButton.setVisibility(View.GONE);
                        break;
                }
            }
            else
            {
                upButton.setVisibility(View.GONE);
                downButton.setVisibility(View.GONE);
            }

        }
    }

    private void showCallButton(boolean b) {
        if (callButton != null) {
            if (b) {
                callButton.bringToFront();
                callButton.setVisibility(View.VISIBLE);
            } else {
                callButton.setVisibility(View.GONE);
            }

        }
    }

    private void callPhoneNumber(){
        String phone = phoneView.getText().toString();
        if(!phone.equals("")){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        } else {
            Toast.makeText(parentActivity, "No phone number given.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRating(Long rating)
    {
        String userUID = getUserID();
        String requestUID = parentActivity.getRequest().getRequestUID();
        if (!isNullOrEmpty(userUID) && !isNullOrEmpty(requestUID))
        {
            DatabaseManager.getInstance().updateRequestUserRating(requestUID, userUID, rating,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(parentActivity, "Rating added!", Toast.LENGTH_SHORT).show();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(parentActivity, "Adding rating failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
