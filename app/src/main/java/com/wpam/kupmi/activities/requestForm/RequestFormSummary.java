package com.wpam.kupmi.activities.requestForm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wpam.kupmi.R;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.utils.DateUtils;

public class RequestFormSummary extends Fragment {

    // Private Fields
    private static final String TAG = "REQUEST_FORM_SUMMARY_FRAGMENT";

    private ImageButton next;
    private TextView coords;
    private TextView time;
    private TextView tags;
    private TextView title;
    private TextView desc;

    private RequestFormActivity parentActivity;

    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_form_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        next = getView().findViewById(R.id.request_form_summary_next_button);
        coords = getView().findViewById(R.id.request_form_summary_coords);
        time = getView().findViewById(R.id.request_form_summary_time);
        tags = getView().findViewById(R.id.request_form_summary_tags);
        title = getView().findViewById(R.id.request_form_summary_title);
        desc = getView().findViewById(R.id.request_form_summary_desc);

        parentActivity = (RequestFormActivity) getActivity();
        Request request = parentActivity.getRequest();

        String address = request.getLocationAddress();
        if (address != null) {
            coords.setText(address);
        } else {
            double lat = request.getLocation().first;
            double lon = request.getLocation().second;
            String coordinates = lat + ", " + lon;
            coords.setText(coordinates);
        }

        String strDate = DateUtils.getDateText(request.getDeadline(), getContext());
        time.setText(strDate);

//        List<String> tagsList = request.getTags();
//        if (!tagsList.isEmpty()) {
//            StringBuilder formattedTags = new StringBuilder();
//            for (Object s : tagsList.toArray()) {
//                formattedTags.append("#");
//                formattedTags.append(s.toString());
//                formattedTags.append(" ");
//            }
//            tags.setText(formattedTags);
//        }
        tags.setText(request.getTag().firstCapitalLetterName());
        title.setText(request.getTitle());
        desc.setText(request.getDescription());

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.insertIntoDB();
                parentActivity.finish();
            }
        });
    }

}
