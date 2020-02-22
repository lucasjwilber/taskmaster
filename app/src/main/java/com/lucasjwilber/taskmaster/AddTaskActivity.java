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
import android.widget.ArrayAdapter;
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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

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

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

//        spinner for team selection
        mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                //use cache here since teams change infrequently
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {

                        //thanks to https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list
                        List<ListTeamsQuery.Item> teams = response.data().listTeams().items();
                        List<String> teamNames = new ArrayList<>();
                        for (int i = 0; i < teams.size(); i++) {
                            teamNames.add(teams.get(i).name());
                        }
                        Log.i("ljw", teamNames.toString());

                        //thanks to 'Simplest Solution' @ https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list
                        final ArrayAdapter<String> teamSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_spinner_item,
                                teamNames);
                        final Spinner teamSpinner = findViewById(R.id.addTaskTeamSpinner);

                        //update UI on main thread
                        Handler handler = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message input) {
                                teamSpinner.setAdapter(teamSpinnerAdapter);                            }
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
//                .teamID(teamID) hardcoded one below is for testing
                .teamID("94a9958a-9769-4e05-98a8-58b45f46a2a4")
                .state("NEW") //default/initial state is "NEW"
                .build();
        //enqueue the mutation
        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(input).build())
                .enqueue(mutationCallback);

        finish();
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

    public void teamChanged(View v) {
//        String teamId = (String) ( (Spinner) findViewById(R.id.addTaskTeamSpinner)).getSelectedItem();
//        Log.i("ljw", "selected id");
//        Log.i("ljw", teamId);
    }

}
