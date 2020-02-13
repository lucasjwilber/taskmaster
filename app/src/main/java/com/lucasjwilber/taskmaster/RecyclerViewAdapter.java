package com.lucasjwilber.taskmaster;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.TasksViewHolder> {
    public ArrayList<Task> allTasks;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class TasksViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public TasksViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecyclerViewAdapter(ArrayList<Task> allTasks) {
        allTasks = allTasks;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapter.TasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_main, parent, false);

        v.setText("task one");

        TasksViewHolder vh = new TasksViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TasksViewHolder holder, int position) {
        holder.textView.setText(allTasks[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return allTasks.size();
    }
}