package com.wpam.kupmi.activities.activeRequests;

import android.content.Intent;
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
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.wpam.kupmi.R;
import com.wpam.kupmi.activities.singleRequest.SingleRequestActivity;
import com.wpam.kupmi.firebase.database.DatabaseManager;
import com.wpam.kupmi.firebase.database.config.DatabaseConfig;
import com.wpam.kupmi.firebase.database.model.DbRequest;
import com.wpam.kupmi.lib.Constants;
import com.wpam.kupmi.model.Request;
import com.wpam.kupmi.model.RequestState;
import com.wpam.kupmi.model.RequestTag;
import com.wpam.kupmi.model.RequestUserKind;
import com.wpam.kupmi.model.User;
import com.wpam.kupmi.utils.DateUtils;

import java.util.HashMap;
import java.util.Objects;

public class ActiveRequestsFragment extends Fragment {

    // Private fields
    private static final String TAG = "ACTIVE_REQUEST_REQUESTER";
    private HashMap<String, Request> requests = new HashMap<>();
    private RequestUserKind userKind;

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private ActiveRequestsActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parentActivity = (ActiveRequestsActivity) getActivity();
        userKind = getArguments() != null ?
                RequestUserKind.getInstance(Objects.requireNonNull(
                        getArguments().getString(ActiveRequestsActivity.USER_KIND_PARAM))) : RequestUserKind.UNKNOWN;

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_requests_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = getView().findViewById(R.id.active_requester_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(parentActivity);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        Log.i(TAG, parentActivity.getUser().getUserUID());

        User user = parentActivity.getUser();

        if (user != null && userKind != RequestUserKind.UNKNOWN) {
            Query query = DatabaseManager.getInstance().getRequestQuery(userKind,
                    user.getUserUID());

            FirebaseRecyclerOptions<Request> options =
                    new FirebaseRecyclerOptions.Builder<Request>()
                            .setQuery(query, new SnapshotParser<Request>() {
                                @NonNull
                                @Override
                                public Request parseSnapshot(@NonNull DataSnapshot snapshot)
                                {
                                    DbRequest dbRequest = snapshot.getValue(DbRequest.class);

                                    if (dbRequest != null)
                                    {
                                        Request request = new Request(snapshot.getKey());

                                        if (userKind == RequestUserKind.REQUESTER)
                                            request.setRequesterUID(dbRequest.getUserUID());
                                        else if (userKind == RequestUserKind.SUPPLIER)
                                            request.setSupplierUID(dbRequest.getUserUID());
                                        request.setDeadline(
                                                DateUtils.getDate(
                                                        dbRequest.getDeadline(),
                                                        DatabaseConfig.DATE_FORMAT,
                                                        DatabaseConfig.DATE_FORMAT_CULTURE));
                                        request.setTitle(dbRequest.getTitle());
                                        request.setTag(RequestTag.getInstance(dbRequest.getTag()));
                                        request.setState(RequestState.getInstance(
                                                dbRequest.getState().intValue()));

                                        return request;
                                    }

                                    return null;
                                }
                            })
                            .build();

            adapter = new FirebaseRecyclerAdapter<Request, RequesterViewHolder>(options) {
                @Override
                public RequesterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_requests_requester_item_layout, parent, false);
                    return new RequesterViewHolder(itemView);
                }

                @Override
                protected void onBindViewHolder(RequesterViewHolder holder, final int position, Request model) {
                    if (model != null)
                    {
                        holder.bindData(model);
                        requests.put(model.getRequestUID(), model);

                    }
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
    }

    @Override
    public void onStart() {
        super.onStart();

        if (recyclerView.getAdapter() != null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (recyclerView.getAdapter() != null)
            adapter.stopListening();
    }

    // Internal / private classes
    class RequesterViewHolder extends RecyclerView.ViewHolder {

        private View itemView;
        private TextView tag;
        private TextView date;
        private TextView topic;

        private String requestUID;

        RequesterViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tag = itemView.findViewById(R.id.active_requests_requester_tag);
            date = itemView.findViewById(R.id.active_requests_requester_date);
            topic = itemView.findViewById(R.id.active_requests_requester_topic);
        }

        void bindData(Request viewModel) {
            tag.setText(viewModel.getTag().firstCapitalLetterName());
            date.setText(DateUtils.getDateText(viewModel.getDeadline(), getContext()));
            topic.setText(viewModel.getTitle());
            RequestState state = viewModel.getState();
            requestUID = viewModel.getRequestUID();

            // change layout depending on state
            switch (state)
            {
                case ACTIVE:
                    itemView.setBackgroundResource(R.drawable.border_blue);
                    break;
                case ACCEPTED:
                    itemView.setBackgroundResource(R.drawable.border_red);
                case DONE:
                    itemView.setBackgroundResource(R.drawable.border_green);
                case UNDONE:
                    itemView.setBackgroundResource(R.drawable.border_black);
                default:
                    break;
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Request request = requests.get(requestUID);
                    Intent intent = new Intent(parentActivity, SingleRequestActivity.class);
                    intent.putExtra(Constants.REQUEST, request);
                    intent.putExtra(Constants.REQUEST_FLAG, true);
                    startActivity(intent);
                }
            });

        }
    }
}
