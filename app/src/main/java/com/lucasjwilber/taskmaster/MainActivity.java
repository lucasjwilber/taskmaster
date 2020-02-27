package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateTeamMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateTeamInput;

public class MainActivity extends AppCompatActivity {
    String TAG = "ljw";
    String username = "Guest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");

        AWSAppSyncClient mAWSAppSyncClient = mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();
////////////
//        final CreateTeamInput newTeam = CreateTeamInput.builder()
//                .name("Install")
//                .build();
//        final CreateTeamInput newTeam2 = CreateTeamInput.builder()
//                .name("Operations")
//                .build();
//        final CreateTeamInput newTeam3 = CreateTeamInput.builder()
//                .name("Engineering")
//                .build();
//
//        mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(newTeam).build())
//                .enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
//                    @Override
//                    public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
//                        Log.i(TAG, response.data().createTeam().toString());
//                    }
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e) {
//                        Log.i(TAG, "failed adding team" + newTeam.name());
//                    }
//                });
//
//        mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(newTeam2).build())
//                .enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
//                    @Override
//                    public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
//                        Log.i(TAG, response.data().createTeam().toString());
//                    }
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e) {
//                        Log.i(TAG, "failed adding team" + newTeam2.name());
//                    }
//                });
//
//        mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(newTeam3).build())
//                .enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
//                    @Override
//                    public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
//                        Log.i(TAG, response.data().createTeam().toString());
//                    }
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e) {
//                        Log.i(TAG, "failed adding team" + newTeam3.name());
//                    }
//                });



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

        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("ljw", "onResult: " + userStateDetails.getUserState());

                        //auth
                        switch (userStateDetails.getUserState()) {
                            case GUEST:
                                Log.i("userState", "user is in guest mode");
                                break;
                            case SIGNED_OUT:
                                Log.i("userState", "user is signed out");
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                break;
                            case SIGNED_IN:
                                Log.i("ljw", "user is signed in");
                                final TextView usernameView = findViewById(R.id.mainActUsername);
                                username = AWSMobileClient.getInstance().getUsername();
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message input) {
                                        usernameView.setText(username);
                                    }
                                };
                                handler.obtainMessage().sendToTarget();
                                break;
                            case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                                Log.i("userState", "need to login again");
                                break;
                            case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                                Log.i("userState", "user logged in via federation, but currently needs new tokens");
                                break;
                            default:
                                Log.e("userState", "unsupported");
                        }

                        //storage
                        try {
                            Amplify.addPlugin(new AWSS3StoragePlugin());
                            Amplify.configure(getApplicationContext());
                            Log.i("StorageQuickstart", "All set and ready to go!");
                        } catch (Exception e) {
                            Log.e("StorageQuickstart", e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("ljw", "Initialization error.", e);
                        Log.e("StorageQuickstart", "Initialization error.", e);
                    }
                }
        );
//  saved for inevitable 'add team' feature:
        {
//        final CreateTeamInput newTeam = CreateTeamInput.builder()
//                .name("Install")
//                .build();
//        mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(newTeam).build())
//                .enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
//                    @Override
//                    public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
//                        Log.i(TAG, response.data().createTeam().toString());
//                    }
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e) {
//                        Log.i(TAG, "failed adding team" + opsTeam.name());
//                    }
//                });
        }

    }

    @Override
    public void onResume(){
        //TODO: fix flash of Main before login redirect
        super.onResume();

        final TextView usernameView = findViewById(R.id.mainActUsername);
        username = AWSMobileClient.getInstance().getUsername();
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message input) {
                usernameView.setText(username);
            }
        };
        handler.obtainMessage().sendToTarget();



        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        String selectedTeam = prefs.getString("selectedTeam", "Operations");
        TextView mainActTitle = findViewById(R.id.mainActTitle);
        mainActTitle.setText(selectedTeam);

        //apply theme changes that I couldn't set in <style>s
        {
            ImageView logo = findViewById(R.id.mainActLogo);
            ImageView settingsImage = findViewById(R.id.settingsgear);
            Window window = getWindow();
            switch (theme) {
                case "Cafe":
                    logo.setImageResource(R.drawable.notepadlogocafe);
                    settingsImage.setImageResource(R.drawable.settingsgear);
                    window.setStatusBarColor(getResources().getColor(R.color.coffeeDarkest));
                    window.setNavigationBarColor(getResources().getColor(R.color.coffeeMedium));
                    break;
                case "City":
                    logo.setImageResource(R.drawable.notepadlogocity);
                    settingsImage.setImageResource(R.drawable.settingsgearcity);
                    window.setStatusBarColor(getResources().getColor(R.color.cityDarkGray));
                    window.setNavigationBarColor(getResources().getColor(R.color.cityDarkGray));
                    break;
                case "Night":
                    logo.setImageResource(R.drawable.notepadlogonight);
                    settingsImage.setImageResource(R.drawable.settingsgearnight);
                    window.setStatusBarColor(getResources().getColor(R.color.nightBlue));
                    window.setNavigationBarColor(getResources().getColor(R.color.nightBlue));
                    break;
            }
        }

    }

    public void goToAddTasksActivity(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    public void goToAllTasksActivity(View v) {
        Intent intent = new Intent(this, AllTasksActivity.class);
        startActivity(intent);
    }

    public void goToSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}