package com.wpam.kupmi.activities.singleRequest;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
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
import com.wpam.kupmi.firebase.database.config.DatabaseConfig;
import com.wpam.kupmi.firebase.database.model.DbRequest;
import com.wpam.kupmi.firebase.database.model.DbRequestDetails;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.utils.DateUtils;

import java.util.Calendar;

public class SingleRequestFragment extends Fragment {

    private static final String TAG = "SINGLE_REQUEST_FRAGMENT";

    private SingleRequestActivity parentActivity;
    private TextView descView;
    private TextView tagView;
    private TextView titleView;
    private TextView addressView;
    private TextView stateView;
    private TextView deadlineView;

    private ImageButton cancelButton;
    private ImageButton acceptButton;

    private Query requestQuery;
    private Query requestsDetailsQuery;

    private ValueEventListener requestQueryListener;
    private ValueEventListener requestsDetailsQueryListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = (SingleRequestActivity) getActivity();
        descView = getView().findViewById(R.id.single_request_desc);
        stateView = getView().findViewById(R.id.single_request_state);
        titleView = getView().findViewById(R.id.single_request_title);
        tagView = getView().findViewById(R.id.single_request_tag);
        deadlineView = getView().findViewById(R.id.single_request_deadline);
        addressView = getView().findViewById(R.id.single_request_address);

        cancelButton = getView().findViewById(R.id.single_request_cancel_button);
        acceptButton = getView().findViewById(R.id.single_request_accept_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                Log.i(TAG, "request uid:"+ parentActivity.getRequest().getRequestUID());
                Log.i(TAG, "requester uid:" + parentActivity.getRequest().getRequesterUID());
                Log.i(TAG, "supplier uid:" + AuthManager.getInstance().getCurrentUserUid());
                dbManager.updateRequestState(parentActivity.getRequest().getRequestUID(),
                        parentActivity.getRequest().getRequesterUID(),
                        AuthManager.getInstance().getCurrentUserUid(),
                        RequestState.ACCEPTED, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(parentActivity, "Request accepted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(parentActivity, "Accepting request failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                requestQuery.removeEventListener(requestQueryListener);
                requestQuery.removeEventListener(requestsDetailsQueryListener);
                parentActivity.finish();
            }
        });

        updateButtons(((SingleRequestActivity) getActivity()).getRequest().getState());
        DatabaseManager dbManager = DatabaseManager.getInstance();

        Log.i(TAG, parentActivity.getRequestUserKind().firstCapitalLetterName());
        Log.i(TAG, parentActivity.getRequest().getState().firstCapitalLetterName());



        if(parentActivity.getRequestUserKind().equals(RequestUserKind.SUPPLIER)
            && parentActivity.getRequest().getState().equals(RequestState.ACTIVE)){
            requestQuery = dbManager.getRequestQuery(RequestUserKind.REQUESTER,
                    parentActivity.getRequest().getRequesterUID(),
                    parentActivity.getRequest().getRequestUID());
        } else {
            AuthManager authManager = AuthManager.getInstance();
            requestQuery = dbManager.getRequestQuery(parentActivity.getRequestUserKind(),
                    authManager.getCurrentUserUid(),
                    parentActivity.getRequest().getRequestUID());
        }
        requestQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //String dbRequestUID = dataSnapshot.getKey();
                DbRequest dbRequest = dataSnapshot.getValue(DbRequest.class);
                if (dbRequest != null) {

                    String userUID = dbRequest.getUserUID();
                    String deadline = dbRequest.getDeadline();
                    String title = dbRequest.getTitle();
                    String tag = dbRequest.getTag();
                    Long state = dbRequest.getState();

                    Calendar deadlineCal = DateUtils.getDate(deadline, DatabaseConfig.DATE_FORMAT, DatabaseConfig.DATE_FORMAT_CULTURE);
                    RequestState reqState = RequestState.getInstance(state.intValue());

                    titleView.setText(title);
                    tagView.setText(RequestTag.getInstance(tag).hashtagName());
                    deadlineView.setText(DateUtils.getDateText(deadlineCal, parentActivity));
                    stateView.setText(reqState.firstCapitalLetterName());

                    parentActivity.getRequest().setTag(RequestTag.getInstance(tag));
                    parentActivity.getRequest().setTitle(title);
                    parentActivity.getRequest().setState(reqState);
                    parentActivity.getRequest().setDeadline(deadlineCal);
                    if(!(parentActivity.getRequestUserKind().equals(RequestUserKind.SUPPLIER)
                            && parentActivity.getRequest().getState().equals(RequestState.ACTIVE))) {
                        if (parentActivity.getRequestUserKind().equals(RequestUserKind.REQUESTER))
                            parentActivity.getRequest().setSupplierUID(userUID);
                        else
                            parentActivity.getRequest().setRequesterUID(userUID);
                    }

                    updateButtons(reqState);
                    parentActivity.updateUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled - databaseError: " + databaseError.getMessage());
            }
        };

        requestQuery.addValueEventListener(requestQueryListener);

        requestsDetailsQuery = dbManager.getRequestDetailsQuery(parentActivity.getRequest().getRequestUID());
        requestsDetailsQueryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DbRequestDetails dbRequestDetails = dataSnapshot.getValue(DbRequestDetails.class);
                if (dbRequestDetails != null) {
                    addressView.setText(dbRequestDetails.getLocationAddress());
                    descView.setText(dbRequestDetails.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled - databaseError: " + databaseError.getMessage());
            }
        };
        requestsDetailsQuery.addValueEventListener(requestsDetailsQueryListener);
    }

    private void updateButtons(RequestState state) {
        switch (state) {
            case ACCEPTED:
                showCancelButton();
                break;
            case ACTIVE:
                //TODO: check if from ActiveRequests or Search
                showAcceptButton();
                break;
            case UNKNOWN:
            case DONE:
            case UNDONE:
            default:
                hideButtons();
                break;
        }
    }

    private void showAcceptButton() {
        if (acceptButton != null) {
            acceptButton.bringToFront();
            acceptButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.GONE);
        }
    }

    private void showCancelButton() {
        if (cancelButton != null) {
            cancelButton.bringToFront();
            cancelButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.GONE);
        }
    }

    private void hideButtons() {
        if (cancelButton != null)
            cancelButton.setVisibility(View.GONE);
        if (acceptButton != null)
            acceptButton.setVisibility(View.GONE);
    }


}
