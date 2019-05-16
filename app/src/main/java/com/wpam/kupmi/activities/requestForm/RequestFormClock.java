package com.wpam.kupmi.activities.requestForm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TimePicker;
import com.wpam.kupmi.R;
import com.wpam.kupmi.utils.DateUtils;
import java.util.Calendar;
import java.util.Locale;


public class RequestFormClock extends Fragment {

    // Private fields
    private TimePicker timePicker;
    private ImageButton next;

    private Calendar date;

    private RequestFormActivity parentActivity;

    // Override Fragment
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
        date = DateUtils.getNextHour(Locale.getDefault());

        timePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        timePicker.setHour(DateUtils.getHour(date));
        timePicker.setMinute(DateUtils.getMinute(date));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);

                Log.i("TIME_PICKER", date.getTime().toString());
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateUtils.updateDate(date, Locale.getDefault());
                parentActivity.setDeadline(date);
                parentActivity.goToDesc();
            }
        });
    }
}
