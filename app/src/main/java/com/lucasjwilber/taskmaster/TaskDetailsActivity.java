package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.w3c.dom.Text;

import java.util.List;
import javax.annotation.Nonnull;

import type.CreateTaskInput;


public class TaskDetailsActivity extends AppCompatActivity {


    private String taskId;
    private String sentTitle;
    private String sentBody;
    private String sentState;
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

        //this intent comes from onBindViewHolder() in MyTaskRecyclerViewAdapter
        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        sentTitle = intent.getStringExtra("taskTitle");
        sentBody = intent.getStringExtra("taskBody");
        sentState = intent.getStringExtra("taskState");

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();
    }
    protected void onResume() {
        super.onResume();

        TextView titleView = findViewById(R.id.taskDetailsTitle);
        TextView bodyView = findViewById(R.id.taskDetailsBody);
        RadioButton rb;

        switch (sentState) {
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
        titleView.setText(sentTitle);
        bodyView.setText(sentBody);
    }

    public void stateRadioButtonChanged(View v) {
        //get selected state
        //TODO: could maybe just use v.getText().toString();
        RadioGroup stateRg = findViewById(R.id.taskStateRadioGroup);
        RadioButton stateRb = findViewById(stateRg.getCheckedRadioButtonId());
        String state = stateRb.getText().toString();

//        CreateTaskInput input = CreateTaskInput.builder()
//                .title(title)
//                .body(body)
//                .state("NEW") //default/initial state is "NEW"
//                .build();
//        //enqueue the mutation
//        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(input).build())
//                .enqueue(mutationCallback);
    }

    public void deleteTaskButtonClicked(View v) {
//        TextView titleView = findViewById(R.id.taskDetailsTitle);
//        String taskTitle = titleView.getText().toString();
//
//        CreateTaskInput input = CreateTaskInput.builder()
//                .title(title)
//                .body(body)
//                .build();
    }

}
