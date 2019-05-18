package com.wpam.kupmi.activities.singleRequest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.wpam.kupmi.R;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.services.user.IUserDataStatus;
import com.wpam.kupmi.services.user.UserService;

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

        String userID = parentActivity.getRequest().getRequesterUID();
        UserService userService = new UserService();
        userService.enableUserQuery(userID, true, this);
        userService.getUser();

        RequestState state = parentActivity.getRequest().getState();
        if (parentActivity.isPartialDataAvailable())
            updateButtons(state);

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
        imageView.setImageResource(R.mipmap.sample_avatar);
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
                showRepButtons(true);
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
            if (b) {
                upButton.bringToFront();
                downButton.bringToFront();
                upButton.setVisibility(View.VISIBLE);
                downButton.setVisibility(View.VISIBLE);
            } else {
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
}
