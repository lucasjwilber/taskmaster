package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;
    private Hashtable<String, String> teamNamesToIDs;
    private String photoPath;
    String uuid;
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

            mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                    //use cache here since teams change infrequently
                    .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
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

    public void addTaskButtonClicked(View v) {
        if (photoPath != null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                "Uploading image...",
                Toast.LENGTH_SHORT);
            //customize toast:
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

            Amplify.Storage.uploadFile(
                    uuid,
                    new File(photoPath).getAbsolutePath(),
                    new ResultListener<StorageUploadFileResult>() {
                        @Override
                        public void onResult(StorageUploadFileResult result) {
                            Log.i("ljw", "Successfully uploaded: " + result.getKey());
                            Log.i("ljw", "creating and uploading task...");

                            uploadTask();
                        }
                        @Override
                        public void onError(Throwable error) {
                            Log.i("ljw", "Upload error.", error);
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Error uploading image.",
                                    Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, -40);
                            toast.show();
                        }
                    }
            );
        } else {
            uploadTask();
        }
    }

    public void uploadImageClicked(View v) {
        choosePhoto();
    }

    public void uploadTask() {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Submitted!",
                Toast.LENGTH_SHORT);

        //creates an imageless task if photoPath is null, vice versa
        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(getCreateTaskInput()).build())
                .enqueue(new GraphQLCall.Callback<CreateTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
                        Log.i("ljw", "Added Task with amplify successfully:\n" + response.data().createTask().toString());
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e("ljw", "failed adding task with amplify:\n" + e.toString());
                    }
                });
        // customize toast:
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

    public void choosePhoto() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("ljw", "requesting WRITE_EXTERNAL_STORAGE permission");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            Log.i("ljw", "permission granted, proceeding");
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode != 0) {
            Log.i("ljw", "request code for WRITE permission was " + requestCode + ". returning");
            return;
        }
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i("ljw", "WRITE permission granted, proceeding...");
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("ljw", "photo selected...");

        if (requestCode == 0 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            stageImageForUpload(selectedImage);
            Log.i("ljw", "photo is ready for upload");
        } else {
            Log.i("ljw", "error with photo selection:\n" + requestCode + "\n" + resultCode + "\n" + data);
        }
    }

    //create task with or without image property based on status of photoPath
    private CreateTaskInput getCreateTaskInput() {
        String title = titleInput.getText().toString();
        String body = bodyInput.getText().toString();
        String teamName = spinner.getSelectedItem().toString();
        String teamID = teamNamesToIDs.get(teamName);

        if (photoPath != null && !photoPath.isEmpty()){
            Log.i("ljw", "detected image attached, creating task with imagePath");
            return CreateTaskInput.builder()
                    .title(title)
                    .body(body)
                    .teamID(teamID)
                    .state("NEW") //default/initial state is "NEW"
                    .imagePath(uuid) //name of file in bucket's public folder
                    .build();
        } else {
            Log.i("ljw", "no image detected, creating task w/o imagePath");
            return CreateTaskInput.builder()
                    .title(title)
                    .body(body)
                    .teamID(teamID)
                    .state("NEW") //default/initial state is "NEW"
                    .build();
        }
    }

    public void stageImageForUpload(Uri uri) {
        //get path of selected image
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        photoPath = cursor.getString(columnIndex);
        Log.i("ljw", "selected photo with path:\n" + photoPath);
        cursor.close();

        uuid = UUID.randomUUID().toString();
    }

}
