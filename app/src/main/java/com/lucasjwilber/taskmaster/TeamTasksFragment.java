package com.lucasjwilber.taskmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amazonaws.amplify.generated.graphql.GetTeamQuery;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.Nonnull;

public class TeamTasksFragment extends Fragment {

    private AWSAppSyncClient mAWSAppSyncClient;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private Hashtable<String, String> teamNamesToIDs = new Hashtable<>();

    //necessary for fragment instantiation
    public TeamTasksFragment() {}

    @SuppressWarnings("unused")
    public static TeamTasksFragment newInstance(int columnCount) {
        TeamTasksFragment fragment = new TeamTasksFragment();
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

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getContext())
                .awsConfiguration(new AWSConfiguration(getContext()))
                .build();

        //get all stored teams from aws to populate teamNamesToIds
        mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {
                        List<ListTeamsQuery.Item> teams = response.data().listTeams().items();
                        for (int i = 0; i < teams.size(); i++) {
                            teamNamesToIDs.put(teams.get(i).name(), teams.get(i).id());
                        }
                        Log.i("ljw", "filled teamNamesToIds hash table");
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i("ljw", "failed querying teams list");
                        Log.e("ljw", e.toString());
                    }
                });
    }

    @Override
    public void onResume() {
        Log.i("ljw", "onresume start");
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        final String selectedTeam = prefs.getString("selectedTeam", "Operations");
        //if the hashtable isn't populated yet (done asynchronously in oncreate) use the Operations team id as default
        String teamId = teamNamesToIDs.get(selectedTeam) != null ? teamNamesToIDs.get(selectedTeam) : "d316e3df-c7ab-4a61-9b8e-12c489b97eeb";

        mAWSAppSyncClient.query(GetTeamQuery.builder().id(teamId).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<GetTeamQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<GetTeamQuery.Data> response) {
                        Log.i("ljw", "successful team query");
                        final List<GetTeamQuery.Item> tasks = response.data().getTeam().tasks().items();
                        Log.i("ljw", tasks.toString());

                        Handler handler = new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(Message input) {
                                recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(tasks, mListener));
                            }
                        };
                        handler.obtainMessage().sendToTarget();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i("ljw", "failed getting team query");
                        Log.i("ljw", e.toString());
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
            //set empty one as default, then overwrite it when tasks are loaded
            recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(new ArrayList<GetTeamQuery.Item>(), null));
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
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(GetTeamQuery.Item item);
    }
}
