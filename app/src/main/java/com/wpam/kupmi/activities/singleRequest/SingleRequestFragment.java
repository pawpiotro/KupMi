package com.wpam.kupmi.activities.singleRequest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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

            }
        });

        final RequestState state = parentActivity.getRequest().getState();
        if (parentActivity.isPartialDataAvailable())
            updateButtons(state);

        DatabaseManager dbManager = DatabaseManager.getInstance();
        AuthManager authManager = AuthManager.getInstance();
        Query requestQuery = dbManager.getRequestQuery(parentActivity.getRequestUserKind(),
                authManager.getCurrentUserUid(), parentActivity.getRequest().getRequestUID());

        requestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //String dbRequestUID = dataSnapshot.getKey();
                DbRequest dbRequest = dataSnapshot.getValue(DbRequest.class);
                if (dbRequest != null) {
                    titleView.setText(dbRequest.getTitle());
                    tagView.setText(RequestTag.getInstance(dbRequest.getTag()).hashtagName());
                    Calendar deadline = DateUtils.getDate(dbRequest.getDeadline(), DatabaseConfig.DATE_FORMAT, DatabaseConfig.DATE_FORMAT_CULTURE);
                    deadlineView.setText(DateUtils.getDateText(deadline, parentActivity));
                    Long state = dbRequest.getState();
                    stateView.setText(RequestState.getInstance(state.intValue()).firstCapitalLetterName());
                    updateButtons(RequestState.getInstance(state.intValue()));
                }
                //TODO: chcemy trzymac aktualny request gdzies?
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled - databaseError: " + databaseError.getMessage());
            }
        });

        Query requestsDetailsQuery = dbManager.getRequestDetailsQuery(parentActivity.getRequest().getRequestUID());
        requestsDetailsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
        });
//
//        descView.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod" +
//                " tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, " +
//                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
//                " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu " +
//                "fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
//                "culpa qui officia deserunt mollit anim id est laborum." +
//                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod" +
//                " tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, " +
//                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
//                " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu " +
//                "fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
//                "culpa qui officia deserunt mollit anim id est laborum.");
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
