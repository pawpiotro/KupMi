package com.wpam.kupmi.activities.activeRequests;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.wpam.kupmi.R;
import com.wpam.kupmi.firebase.database.model.DbRequest;

import java.util.ArrayList;

public class ActiveRequestsAsRequester extends Fragment {

    // Private fields
    private static final String TAG = "ACTIVE_REQUEST_REQUESTER";
    private ArrayList<DbRequest> requests = new ArrayList<>();

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private ActiveRequestsActivity parentActivity;

    // Override Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActiveRequestsActivity) getActivity();


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_requests_as_requester, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) getView().findViewById(R.id.active_requester_recycler_view);
        recyclerView.setLayoutManager((new LinearLayoutManager(parentActivity)));

        Log.i(TAG, parentActivity.getUser().getUserUID());

        //TODO:
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("requests")
                .child("requester")
                .child("kvS3nHE9NpUQXPIWXsb1436n5xk1")
                .limitToLast(20);

        /*

        .setQuery(..., new SnapshotParser<Chat>() {
            @NonNull
            @Override
            public Chat parseSnapshot(@NonNull DataSnapshot snapshot) {
                return ...;
            }
        });

         */

        FirebaseRecyclerOptions<DbRequest> options =
                new FirebaseRecyclerOptions.Builder<DbRequest>()
                        .setQuery(query, DbRequest.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<DbRequest, RequesterViewHolder>(options) {
            @Override
            public RequesterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_requests_requester_item_layout, parent, false);
                return new RequesterViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(RequesterViewHolder holder, final int position, DbRequest model) {
                if (model != null)
                    holder.bindData(model);
                requests.add(position, model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Click: " + position);
                    }
                });
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                //TODO:
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                //TODO:
            }

        };

        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    // Internal / private classes
    class RequesterViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tag;
        private TextView date;
        private TextView topic;

        RequesterViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tag = itemView.findViewById(R.id.active_requests_requester_tag);
            date = itemView.findViewById(R.id.active_requests_requester_date);
            topic = itemView.findViewById(R.id.active_requests_requester_topic);

        }

        void bindData(DbRequest viewModel) {
            tag.setText(viewModel.getTag());
            date.setText(viewModel.getDeadline());
            topic.setText(viewModel.getTitle());

            int state = viewModel.getState().intValue();
            // change layout depending on state
            switch(state) {
                case 0:
                    itemView.setBackgroundResource(R.drawable.border_red);
                    break;
                default:
                    break;
            }
        }
    }
}
