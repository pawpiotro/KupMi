package com.wpam.kupmi.activities.requestForm;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.wpam.kupmi.R;

import java.util.Calendar;


public class RequestFormClock extends Fragment {

    private TimePicker timePicker;
    private ImageButton next;

    private Calendar calendar;

    private RequestFormActivity parentActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_form_clock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timePicker = (TimePicker) getView().findViewById(R.id.request_form_clock_input);
        next = (ImageButton) getView().findViewById(R.id.request_form_clock_next_button);

        parentActivity = (RequestFormActivity) getActivity();
        calendar = Calendar.getInstance();

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                Log.i("TIME_PICKER", calendar.getTime().toString());
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.setDeadline(calendar.getTime());
                parentActivity.goToDesc();
            }
        });
    }
}
