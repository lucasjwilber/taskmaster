package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.ResultListener;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;
    private FusedLocationProviderClient fusedLocationClient;
    private Hashtable<String, String> teamNamesToIDs;
    private String photoPath;
    String uuid;
    TextView titleInput;
    TextView bodyInput;
    Spinner spinner;
    Intent shareIntent;
    String sharedImage;
    public String latLong;
    public String formattedAddress;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

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

//      TODO:  if (shared prefs say that user enabled location) {
        getLocation();

        //init awsMobileClient
        AWSConfiguration awsConfig = new AWSConfiguration(getApplicationContext());
        AWSMobileClient.getInstance().initialize(getApplicationContext(), awsConfig, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("INIT", userStateDetails.getUserState().toString());
                Log.i("ljw", "user is logged in, username is " + AWSMobileClient.getInstance().getUsername());

                //instantiate the user id so s3 accepts our requests
                final AWSCredentials credentials = AWSMobileClient.getInstance().getCredentials();

//                if we got here via a share image intent...
                shareIntent = getIntent();
                sharedImage = shareIntent.getStringExtra("sharedImageURI");
                if (sharedImage != null && sharedImage.length() > 0) {
                    uuid = UUID.randomUUID().toString();
                    photoPath = sharedImage;
                    Log.i("ljw", "photopath updated with shared image:\n" + photoPath);
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("INIT", "Initialization error.", e);
            }
        });

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

        CheckBox locationCB = findViewById(R.id.locationCheckBox);



        //getCreateTaskInput() creates an imageless task if photoPath is null, vice versa
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

        //location permission
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    Log.i("ljw", "location permission granted");
                    getLocation();
                }

            } else {
                Log.i("ljw", "location permission denied");
            }

        }


        //external storage permission:
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

        //TODO: add options to create w/o location based on sharedprefs data for location preference

        Log.i("ljw", "creating task with location " + formattedAddress);

        if (photoPath != null && !photoPath.isEmpty()) {
            Log.i("ljw", "detected image attached, creating task with imagePath");
            return CreateTaskInput.builder()
                    .title(title)
                    .body(body)
                    .teamID(teamID)  //small chance this could be null if task is created before hashtable populates in onCreate
                    .state("NEW") //default/initial state is "NEW"
                    .imagePath(uuid) //name of file in bucket's public folder
                    .location(formattedAddress)
                    .build();
        } else {
            Log.i("ljw", "no image detected, creating task w/o imagePath");
            return CreateTaskInput.builder()
                    .title(title)
                    .body(body)
                    .teamID(teamID)  //small chance this could be null if task is created before hashtable populates in onCreate
                    .state("NEW") //default/initial state is "NEW"
                    .location(formattedAddress)
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

    public void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        Log.i("ljw", "successfully got location");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            latLong = location.getLatitude() + "," + location.getLongitude();
                            Log.i("ljw", latLong);

                            //call geocode to get address with latLong
                            //TODO: hide api key
                            Log.i("ljw", "calling api...");
                            AsyncTask.execute(() -> {

                                try {
                                    URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latLong + "");
                                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                    con.setRequestMethod("GET");
                                    Log.i("ljw", "called api, reading response...");
                                    BufferedReader in = new BufferedReader(
                                            new InputStreamReader(con.getInputStream()));
                                    String line;
                                    StringBuilder content = new StringBuilder();
                                    Log.i("ljw", "building string from response...");
                                    while ((line = in.readLine()) != null) {
                                        content.append(line);
                                        if (line.contains("formatted_address")) {
                                            formattedAddress = line.split("\" : \"")[1];
                                            formattedAddress = formattedAddress.substring(0, formattedAddress.length() - 2);
                                            Log.i("ljw", "found formatted addresss: " + formattedAddress);
                                            break;
                                        }
                                    }
                                    in.close();
                                    con.disconnect();

                                } catch (MalformedURLException e) {
                                    Log.i("ljw", "malformedURLexception:\n" + e.toString());
                                } catch (ProtocolException e) {
                                    Log.i("ljw", "protocol exception:\n" + e.toString());
                                } catch (IOException e) {
                                    Log.i("ljw", "IO exception:\n" + e.toString());
                                }

                            });
                        }
                    })
                    .addOnFailureListener(this, error -> {
                        Log.i("ljw", "error getting location:\n" + error.toString());
                    });
        }
    }


}
