package com.wpam.kupmi.activities.requestForm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.wpam.kupmi.R;
import com.wpam.kupmi.model.RequestTag;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RequestFormDesc extends Fragment {

    private ImageButton next;
    private Spinner tagsSpinner;
    private EditText descEditText;

    private RequestFormActivity parentActivity;

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

        next = (ImageButton) getView().findViewById(R.id.request_form_desc_next_button);
        tagsSpinner = (Spinner) getView().findViewById(R.id.request_form_tag_selection);
        descEditText = (EditText) getView().findViewById(R.id.request_form_desc);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentActivity,
                R.array.tags_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(adapter);

        tagsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // not good
//                if(parent.getChildAt(0) != null)
//                    ((TextView) parent.getChildAt(0)).setTextSize(20);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.setDescription(descEditText.getText().toString());
                Log.i("DESC", tagsSpinner.getSelectedItem().toString());
                parentActivity.setTag(RequestTag.getInstance(tagsSpinner.getSelectedItem().toString()));
                parentActivity.goToSummary();
            }
        });

    }

    private List<String> tokenize(String input) {
        input = input.replaceAll("\\s+", "");
        List<String> result = new LinkedList<>(Arrays.asList(input.split("#")));
        // remove empty token
        if (!result.isEmpty()) {
            result.remove(0);
        }
        return result;
    }
}
