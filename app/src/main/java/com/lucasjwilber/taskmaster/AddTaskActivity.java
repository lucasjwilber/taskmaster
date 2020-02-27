package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.Nonnull;
import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;
    private Hashtable<String, String> teamNamesToIDs;
    private static volatile TransferUtility transferUtility;
    private static int RESULT_LOAD_IMAGE = 1;
    private String photoPath;
    TextView titleInput;
    TextView bodyInput;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        titleInput = findViewById(R.id.addTask_taskNameInput);
        bodyInput = findViewById(R.id.addTask_taskDescInput);
        spinner = findViewById(R.id.addTaskTeamSpinner);

        teamNamesToIDs = new Hashtable<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //apply theme
        {
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
        }

        //query aws for saved teams to populate team select spinner
        {
            mAWSAppSyncClient = AWSAppSyncClient.builder()
                    .context(getApplicationContext())
                    .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                    .build();

//            mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
//                    .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
//                    .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
//                        @Override
//                        public void onResponse(@Nonnull Response<ListTeamsQuery.Data> response) {
//                            Log.i("ljw", response.data().listTeams().toString());
//                        }
//
//                        @Override
//                        public void onFailure(@Nonnull ApolloException e) {
//                            Log.i("ljw", "failed fetching teams");
//                        }
//                    });

            mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                    //use cache here since teams change infrequently
                    .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                    .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
                        @Override
                        public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {

                            List<ListTeamsQuery.Item> teams = response.data().listTeams().items();
                            List<String> teamNames = new ArrayList<>();
                            for (int i = 0; i < teams.size(); i++) {
                                teamNames.add(teams.get(i).name());
                                teamNamesToIDs.put(teams.get(i).name(), teams.get(i).id());
                            }

                            //thanks to 'Simplest Solution' @ https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list
                            final ArrayAdapter<String> teamSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_item,
                                    teamNames);
                            final Spinner teamSpinner = findViewById(R.id.addTaskTeamSpinner);

                            //update UI on main thread
                            Handler handler = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message input) {
                                    teamSpinner.setAdapter(teamSpinnerAdapter);
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
    }




    //thanks to https://developer.android.com/guide/topics/ui/notifiers/toasts
    public void addTaskButtonClicked(View v) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Submitted!",
                Toast.LENGTH_SHORT);
//replace all of below with this when starting storage later
//        CreateTaskInput input = getCreateTaskInput();
        String title = titleInput.getText().toString();
        String body = bodyInput.getText().toString();
        String teamName = spinner.getSelectedItem().toString();
        String teamID = teamNamesToIDs.get(teamName);
        CreateTaskInput input = CreateTaskInput.builder()
                .title(title)
                .body(body)
                .teamID(teamID)
                .state("NEW") //default/initial state is "NEW"
                .build();

        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(input).build())
                .enqueue(new GraphQLCall.Callback<CreateTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
                        Log.i("ljw", "Added Task with amplify successfully:");
                        Log.i("ljw", response.toString());
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e("ljw", "failed adding task with amplify:");
                        Log.e("ljw", e.toString());
                    }
                });

        //display custom toast:
        {
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
        finish();
    }


    /////////photo upload
    // Photo selector application code.
    // Thanks to https://aws.amazon.com/blogs/mobile/building-an-android-app-with-aws-amplify-part-2/



    public void uploadImageClicked(View v) {

    }
}
