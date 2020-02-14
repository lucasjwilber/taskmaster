package com.lucasjwilber.taskmaster;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lucasjwilber.taskmaster.TaskFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Task} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    private final List<Task> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyTaskRecyclerViewAdapter(List<Task> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.taskTitleView.setText(mValues.get(position).getTitle());
        holder.taskBodyView.setText(mValues.get(position).getBody());
        holder.taskStateView.setText(mValues.get(position).getState());

        holder.taskView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.rgb(244,0,0));
                Log.i("click", holder.taskTitleView.getText().toString());
//                Intent intent = new Intent(this, TaskDetailsActivity.class);
//                intent.putExtra("taskTitle", holder.taskTitleView.getText().toString());
//                startActivity(intent);

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
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
        public final TextView taskBodyView;
        public final TextView taskStateView;
        public Task mItem;

        public ViewHolder(View view) {
            super(view);
            taskView = view;
            taskTitleView = (TextView) view.findViewById(R.id.taskTitle);
            taskBodyView = (TextView) view.findViewById(R.id.taskBody);
            taskStateView = (TextView) view.findViewById(R.id.taskState);
        }

//        @Override
//        public String toString() {
//            return super.toString() + " '" + mContentView.getText() + "'";
//        }
    }
}
