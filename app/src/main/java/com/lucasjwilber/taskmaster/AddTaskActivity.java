package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
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
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
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
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

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
    String TAG = "ljw";

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

            //init transferUtility
            if (transferUtility == null) {
                transferUtility = TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();
                Log.i("Storage", "transferUtil initiated");
                Log.i("Storage", transferUtility().toString());
            }
        }
    }


    public void addTaskButtonClicked(View v) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Submitted!",
                Toast.LENGTH_SHORT);

//        String title = titleInput.getText().toString();
//        String body = bodyInput.getText().toString();
//        String teamName = spinner.getSelectedItem().toString();
//        String teamID = teamNamesToIDs.get(teamName);
//        CreateTaskInput input = CreateTaskInput.builder()
//                .title(title)
//                .body(body)
//                .teamID(teamID)
//                .state("NEW") //default/initial state is "NEW"
//                .build();
//
//        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(input).build())
//                .enqueue(new GraphQLCall.Callback<CreateTaskMutation.Data>() {
//                    @Override
//                    public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
//                        Log.i("ljw", "Added Task with amplify successfully:\n" + response.data().createTask().toString());
//                    }
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e) {
//                        Log.e("ljw", "failed adding task with amplify:\n" + e.toString());
//                    }
//                });


//        uploadAndSave();

        uploadFile();

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

    // Photo selector application code.
    // Thanks to https://aws.amazon.com/blogs/mobile/building-an-android-app-with-aws-amplify-part-2/
    public void uploadImageClicked(View v) {
        choosePhoto();
//        uploadAndSave();
//        uploadFile();
    }

    private void save() {
        CreateTaskInput input = getCreateTaskInput();

        CreateTaskMutation addTaskMutation = CreateTaskMutation.builder()
                .input(input)
                .build();

        mAWSAppSyncClient.mutate(addTaskMutation).
//                refetchQueries(ListTasksQuery.builder().build()).
                enqueue(new GraphQLCall.Callback<CreateTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
                        Log.i(TAG, "successfully uploaded image:\n" + response.data());
                        Log.i(TAG, "photopath is " + photoPath);
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "failed to upload image:\n" + e.toString());
                        Log.i(TAG, "photopath is " + photoPath);
                    }
                });
    }

    public void choosePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission not granted! Requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        } else {
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission is granted.");
        }

        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(i, RESULT_LOAD_IMAGE);
        startActivityForResult(i, 777);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            // String picturePath contains the path of selected Image
            photoPath = picturePath;


            Log.i("ljw", "staged photo for upload:\n" + photoPath);
//            uploadAndSave();
        }
    }

    public static synchronized TransferUtility transferUtility() {
        return transferUtility;
    }

    private String getS3Key(String localPath) {
        //We have read and write ability under the public folder
        return "public/" + new File(localPath).getName();
    }

    public void uploadWithTransferUtility(String localPath) {
        String key = getS3Key(localPath);

        Log.d(TAG, "Uploading file from " + localPath + " to " + key);

//
//        File photoFile = new File(photoPath);
//        InputStream is = new FileInputStream(photoFile);
//        por = new PutObjectRequest("bucket name", key, is, new ObjectMetadata());

        TransferObserver uploadObserver = transferUtility().upload(key, new File(localPath));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.d(TAG, "Upload is completed. ");

                    // Upload is successful. Save the rest and send the mutation to server.
                    save();
                }
            }
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Log.e(TAG, "Failed to upload photo. ", ex);

                runOnUiThread(() -> Toast.makeText(AddTaskActivity.this, "Failed to upload photo", Toast.LENGTH_LONG).show());
            }

        });
    }


    private CreateTaskInput getCreateTaskInput() {
        String title = titleInput.getText().toString();
        String body = bodyInput.getText().toString();
        String teamName = spinner.getSelectedItem().toString();
        String teamID = teamNamesToIDs.get(teamName);

        Log.i("ljw", teamID);
        Log.i("ljw", "creating task input\nphotopath = " + photoPath);
        if (photoPath != null && !photoPath.isEmpty()){
            return CreateTaskInput.builder()
                    .title(title)
                    .body(body)
                    .teamID(teamID)
                    .state("NEW") //default/initial state is "NEW"
                    .imagePath(getS3Key(photoPath))
                    .build();
        } else {
            return CreateTaskInput.builder()
                    .title(title)
                    .body(body)
                    .teamID(teamID)
                    .state("NEW") //default/initial state is "NEW"
                    .build();
        }
    }


    private void uploadAndSave(){
        if (photoPath != null) {
            // For higher Android levels, we need to check permission at runtime
            //TODO: this has been moved to choosePhoto(), test if this can be safely removed here
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Log.d(TAG, "READ_EXTERNAL_STORAGE permission not granted! Requesting...");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } else {
                Log.d(TAG, "READ_EXTERNAL_STORAGE permission is granted.");
            }

            // Upload a photo first. We will only call save on its successful callback.
            uploadWithTransferUtility(photoPath);
        } else {
            save();
        }
    }

    //this is just a test for the s3 bucket
    private void uploadFile() {
        File sampleFile = new File(getApplicationContext().getFilesDir(), "sample.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(sampleFile));
            writer.append("Howdy World!");
            writer.close();
        }
        catch(Exception e) {
            Log.e("StorageQuickstart", Objects.requireNonNull(e.getMessage()));
        }

        Amplify.Storage.uploadFile(
                "uploadTest.txt",
//                sampleFile.getAbsolutePath(),
                photoPath,
                new ResultListener<StorageUploadFileResult>() {
                    @Override
                    public void onResult(StorageUploadFileResult result) {
                        Log.i("StorageQuickStart", "Successfully uploaded: " + result.getKey());
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.i("StorageQuickstart", "Upload error.", error);
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Permission is not granted
                            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission not granted!");
                        } else {
                            Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission is granted.");
                        }

                    }
                }
        );
    }
}
