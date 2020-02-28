package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.amazonaws.amplify.generated.graphql.DeleteTaskMutation;
import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.UpdateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import javax.annotation.Nonnull;
import type.DeleteTaskInput;
import type.UpdateTaskInput;

public class TaskDetailsActivity extends AppCompatActivity {

    private String taskId;
    private String taskTitle;
    private String taskBody;
    private String taskState;
    private String taskTeamID;
    private AWSAppSyncClient mAWSAppSyncClient;

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

        setContentView(R.layout.activity_task_details);

        //get task details using the id passed in with the intent
        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();
        mAWSAppSyncClient.query(GetTaskQuery.builder().id(taskId).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<GetTaskQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetTaskQuery.Data> response) {
                        Log.i("ljw", "on create query task by id");
                        Log.i("ljw", response.data().getTask().title());
                        GetTaskQuery.GetTask task = response.data().getTask();
                        taskTitle = task.title();
                        taskBody = task.body();
                        taskState = task.state();
                        taskTeamID = task.teamID();

                        TextView titleView = findViewById(R.id.taskDetailsTitle);
                        TextView bodyView = findViewById(R.id.taskDetailsBody);
                        RadioButton rb;

                        switch (taskState) {
                            case "ASSIGNED":
                                rb = findViewById(R.id.state_rb_assigned);
                                break;
                            case "IN PROGRESS":
                                rb = findViewById(R.id.state_rb_inProgress);
                                break;
                            case "COMPLETE":
                                rb = findViewById(R.id.state_rb_complete);
                                break;
                            case "NEW":
                            default:
                                rb = findViewById(R.id.state_rb_new);
                                break;
                        }
                        rb.toggle();
                        titleView.setText(taskTitle);
                        bodyView.setText(taskBody);
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i("ljw", e.toString());
                    }
                });
    }


    public void stateRadioButtonChanged(View v) {
        Log.i("ljw", "state radio button clicked");
        // TODO: could maybe just use v.getText().toString() ?
        RadioGroup stateRg = findViewById(R.id.taskStateRadioGroup);
        RadioButton stateRb = findViewById(stateRg.getCheckedRadioButtonId());
        final String newState = stateRb.getText().toString();
        Log.i("ljw", "state: " + newState);

        UpdateTaskInput input = UpdateTaskInput.builder()
                //if these aren't ALL defined it crashes
                .id(taskId)
                .teamID(taskTeamID)
                .title(taskTitle)
                .body(taskBody)
                .state(newState)
                .imagePath("none")
                .build();

        //enqueue the mutation
        mAWSAppSyncClient.mutate(UpdateTaskMutation.builder().input(input).build())
                .enqueue(new GraphQLCall.Callback<UpdateTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<UpdateTaskMutation.Data> response) {
                        Log.i("ljw", "update state response: " + response.data().toString());
                        Log.i("ljw", response.data().updateTask().state());
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i("ljw", "failed state update");
                    }
                });
    }

    public void deleteTaskButtonClicked(View v) {
        Log.i("ljw", "delete task button clicked");
        DeleteTaskInput input = DeleteTaskInput.builder().id(taskId).build();
        mAWSAppSyncClient.mutate(DeleteTaskMutation.builder().input(input).build())
                .enqueue(new GraphQLCall.Callback<DeleteTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<DeleteTaskMutation.Data> response) {
                        Log.i("ljw", "deleted task successfully");
                        Log.i("ljw", response.data().deleteTask().toString());
                        finish();
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i("ljw", "failed to delete task");
                        Log.i("ljw", e.toString());
                    }
                });
    }

}
