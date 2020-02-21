package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {


    private AWSAppSyncClient mAWSAppSyncClient;
    private Spinner teamSpinner;
    private TeamSpinnerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        switch (theme) {
            case "Cafe":
                setTheme(R.style.CafeTheme);
                break;
            case "City":
                setTheme(R.style.CityTheme);
                break;
            case "Night":
                setTheme(R.style.NightTheme);
                break;
        }

        setContentView(R.layout.activity_add_task);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {
                        Handler handler = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message input) {
                                //thanks to https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list
                                List<ListTeamsQuery.Item> teams = new ArrayList<>();
                                for (ListTeamsQuery.Item team : response.data().listTeams().items()) {
                                    teams.add(team);
                                }
//                                teams.addAll(response.data().listTeams().items());

                                adapter = new TeamSpinnerAdapter(getApplicationContext(),
                                        android.R.layout.simple_spinner_item,
                                        teams);
                                teamSpinner = findViewById(R.id.addTaskTeamSpinner);
                                teamSpinner.setAdapter(adapter);
                                teamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                                               int position, long id) {
                                        // Here you get the current item (a Team object) that is selected by its position
                                        Team team = adapter.getItem(position);
                                        // Here you can do the action you want to...
                                        Toast.makeText(getApplicationContext(), "ID: " + team.getId() + "\nName: " + team.getName(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapter) {  }
                                });

                                Log.i("ljw", response.data().listTeams().toString());
                            }
                        };
                        handler.obtainMessage().sendToTarget();
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i("ljw", "failed querying teams list");
                    }
                });


    }

    //thanks to https://developer.android.com/guide/topics/ui/notifiers/toasts
    public void addTaskButtonClicked(View v) {
        Context context = getApplicationContext();
        CharSequence text = "Submitted!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

        //get edittext fields
        TextView titleInput = findViewById(R.id.addTask_taskNameInput);
        String title = titleInput.getText().toString();
        TextView bodyInput = findViewById(R.id.addTask_taskDescInput);
        String body = bodyInput.getText().toString();
        //create the mutation
        CreateTaskInput input = CreateTaskInput.builder()
                .title(title)
                .body(body)
                .state("NEW") //default/initial state is "NEW"
                .build();
        //enqueue the mutation
        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(input).build())
                .enqueue(mutationCallback);

        finish();
        //RIP for now, custom toast

        //display custom toast:
        //thanks to https://stackoverflow.com/questions/11288475/custom-toast-on-android-a-simple-example
        View toastView = toast.getView();
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(30);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        switch (theme) {
            case "Cafe":
                toastMessage.setTextColor(getResources().getColor(R.color.coffeeDarkest));
                toastView.setBackgroundColor(getResources().getColor(R.color.coffeeLight));
                break;
            case "City":
                toastMessage.setTextColor(getResources().getColor(R.color.cityLightGray));
                toastView.setBackgroundColor(getResources().getColor(R.color.cityMediumGray));
                break;
            case "Night":
                toastMessage.setTextColor(getResources().getColor(R.color.nightLightGray));
                toastView.setBackgroundColor(getResources().getColor(R.color.nightWhite));
                break;
        }
        toast.setGravity(Gravity.CENTER, 0, -40);
        toast.show();
    }

    private GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
            Log.i("amplify", "Added Task");
            Log.i("amplify", response.toString());
        }
        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("amplify", e.toString());
        }
    };

}
