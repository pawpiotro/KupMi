package com.wpam.kupmi.activities.requestForm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.wpam.kupmi.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RequestFormDesc extends Fragment {

    private ImageButton next;
    private EditText tagsEditText;
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
        next = (ImageButton) getView().findViewById(R.id.request_form_desc_next_button);
        tagsEditText = (EditText) getView().findViewById(R.id.request_form_desc_tags);
        descEditText = (EditText) getView().findViewById(R.id.request_form_desc);


        parentActivity = (RequestFormActivity) getActivity();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.setDescription(descEditText.getText().toString());
                Log.i("DESC", tagsEditText.getText().toString());
                parentActivity.setTags(tokenize(tagsEditText.getText().toString()));
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
