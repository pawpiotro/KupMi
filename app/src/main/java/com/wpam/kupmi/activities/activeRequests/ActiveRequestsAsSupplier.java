package com.wpam.kupmi.activities.activeRequests;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.wpam.kupmi.model.Request;

public class ActiveRequestsAsSupplier extends Fragment {

    private Request[] requestsData;

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private ActiveRequestsActivity parentActivity;

    public class SupplierViewHolder extends RecyclerView.ViewHolder {
        //TODO: add fields

        private TextView tag;
        private TextView date;
        private TextView topic;
        private TextView address;
        private TextView rep;

        public SupplierViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            tag = (TextView) getView().findViewById(R.id.active_requests_supplier_tag);
            date = (TextView) getView().findViewById(R.id.active_requests_supplier_date);
            topic = (TextView) getView().findViewById(R.id.active_requests_supplier_topic);
            address = (TextView) getView().findViewById(R.id.active_requests_supplier_address);
            rep = (TextView) getView().findViewById(R.id.active_requests_supplier_requester_rep);

        }

        public void bindData(Request viewModel) {
            tag.setText(viewModel.getTag().toString());
            date.setText(viewModel.getDeadline().getTime().toString());
            topic.setText(viewModel.getDescription());
            address.setText(viewModel.getLocationAddress());
            rep.setText("0"); //TODO:
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActiveRequestsActivity) getActivity();


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_requests_as_supplier, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) getView().findViewById(R.id.active_supplier_recycler_view);
        recyclerView.setLayoutManager((new LinearLayoutManager(parentActivity)));

        //TODO:
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("requests")
                .child("supplier")
                .child("")
                .limitToLast(10);

        /*

        .setQuery(..., new SnapshotParser<Chat>() {
            @NonNull
            @Override
            public Chat parseSnapshot(@NonNull DataSnapshot snapshot) {
                return ...;
            }
        });

         */

        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(query, Request.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Request, ActiveRequestsAsSupplier.SupplierViewHolder>(options) {
            @Override
            public ActiveRequestsAsSupplier.SupplierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_requests_supplier_item_layout, parent, false);
                return new ActiveRequestsAsSupplier.SupplierViewHolder(itemLayoutView);
            }

            @Override
            protected void onBindViewHolder(ActiveRequestsAsSupplier.SupplierViewHolder holder, int position, Request model) {
                holder.bindData(model);
            }

            @Override
            public int getItemCount() {
                return requestsData.length;
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
}
