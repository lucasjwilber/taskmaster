package com.lucasjwilber.taskmaster;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.GetTeamQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.lucasjwilber.taskmaster.TaskFragment.OnListFragmentInteractionListener;
import java.util.List;

public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    private final List<GetTeamQuery.Item> mValues;
    private final OnListFragmentInteractionListener mListener;
    private AWSAppSyncClient mAWSAppSyncClient;

    public MyTaskRecyclerViewAdapter(List<GetTeamQuery.Item> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(view.getContext().getApplicationContext())
                .awsConfiguration(new AWSConfiguration(view.getContext().getApplicationContext()))
                .build();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.taskTitleView.setText(mValues.get(position).title());
        holder.taskStateView.setText(mValues.get(position).state());
        holder.taskBodyView.setText(mValues.get(position).body());
        holder.taskIdView.setText(mValues.get(position).id());

        holder.taskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send everything with the intent to Task Details
                Intent intent = new Intent(v.getContext(), TaskDetailsActivity.class);
                String taskTitle = holder.taskTitleView.getText().toString();
                String taskId = holder.taskIdView.getText().toString();
                String taskBody = holder.taskBodyView.getText().toString();
                String taskState = holder.taskStateView.getText().toString();
                intent.putExtra("taskTitle", taskTitle);
                intent.putExtra("taskBody", taskBody);
                intent.putExtra("taskId", taskId);
                intent.putExtra("taskState", taskState);

                v.getContext().startActivity(intent);
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View taskView;
        public final TextView taskTitleView;
        public final TextView taskStateView;
        public final TextView taskBodyView;
        public final TextView taskIdView;
        public GetTeamQuery.Item mItem;

        public ViewHolder(View view) {
            super(view);
            taskView = view;
            taskTitleView = view.findViewById(R.id.taskTitle);
            taskStateView = view.findViewById(R.id.taskState);
            taskBodyView = view.findViewById(R.id.taskBody);
            taskIdView = view.findViewById(R.id.taskId);
        }
    }
}
