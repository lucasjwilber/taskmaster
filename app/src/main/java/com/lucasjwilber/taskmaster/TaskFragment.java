package com.lucasjwilber.taskmaster;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateTaskSubscription;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TaskFragment extends Fragment {

    //this is what actually handles the connections to aws
    private AWSAppSyncClient mAWSAppSyncClient;


    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TaskFragment newInstance(int columnCount) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Task> allTasks = new ArrayList<>();
        mAWSAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<ListTasksQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<ListTasksQuery.Data> response) {

                        Handler handler = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message input) {
                                recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(response.data().listTasks().items(), null));
                            }
                        };
                        handler.obtainMessage().sendToTarget();

                        //returns a list of task items (not a Task.class list)
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            //fetch tasks from db
            //TODO: need to recreate Main after doing this, or change it to use LiveData
//            TasksDatabase db = TasksDatabase.getTasksDatabase(inflater.getContext());
            List<Task> allTasks = new ArrayList<>();
            Task task = new Task("hardcoded task", "bleh");
            allTasks.add(task);

//            recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(allTasks, mListener));

            mAWSAppSyncClient = AWSAppSyncClient.builder()
                    .context(view.getContext().getApplicationContext())
                    .awsConfiguration(new AWSConfiguration(view.getContext().getApplicationContext()))
                    .build();
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ListTasksQuery.Item item);
    }

//    private GraphQLCall.Callback<ListTasksQuery.Data> tasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
//        @Override
//        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
//            Log.i("amplify", response.data().listTasks().items().toString());
//        }
//
//        @Override
//        public void onFailure(@Nonnull ApolloException e) {
//            Log.e("amplify", e.toString());
//        }
//    };

}
