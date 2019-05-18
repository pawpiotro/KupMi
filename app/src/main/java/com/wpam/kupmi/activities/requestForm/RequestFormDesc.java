package com.wpam.kupmi.activities.requestForm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import com.wpam.kupmi.R;
import com.wpam.kupmi.model.RequestTag;

public class RequestFormDesc extends Fragment {

    // Private fields
    private static final String TAG = "DESC";

    private ImageButton next;
    private Spinner tagsSpinner;
    private EditText titleEditText;
    private EditText descEditText;

    private RequestFormActivity parentActivity;

    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_form_desc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = (RequestFormActivity) getActivity();

        next = getView().findViewById(R.id.request_form_desc_next_button);
        tagsSpinner = getView().findViewById(R.id.request_form_tag_selection);
        titleEditText = getView().findViewById(R.id.request_form_title);
        descEditText = getView().findViewById(R.id.request_form_desc);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentActivity,
                R.array.tags_array, R.layout.spinner_item); //android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(adapter);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.setDescription(descEditText.getText().toString());
                parentActivity.setTitle(titleEditText.getText().toString());
                Log.i(TAG, tagsSpinner.getSelectedItem().toString());
                parentActivity.setTag(RequestTag.getInstance(tagsSpinner.getSelectedItem().toString()));
                parentActivity.goToSummary();
            }
        });

    }
}
