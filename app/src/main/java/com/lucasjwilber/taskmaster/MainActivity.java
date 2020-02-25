package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;

public class MainActivity extends AppCompatActivity {
    String TAG = "ljw";

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
        setContentView(R.layout.activity_main);
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
        //initialize amplify auth:
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("ljw", "onResult: " + userStateDetails.getUserState());
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
                                Log.i("userState", "user is signed in");
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
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("ljw", "Initialization error.", e);
                    }
                }
        );

        //user auth state change listener:
        AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
            @Override
            public void onUserStateChanged(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case GUEST:
                        Log.i("userState", "user is in guest mode");
                        break;
                    case SIGNED_OUT:
                        Log.i("userState", "user is signed out");
                        break;
                    case SIGNED_IN:
                        Log.i("userState", "user is signed in");
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
            }
        });

        AWSMobileClient.getInstance().showSignIn(
                this,
                SignInUIOptions.builder()
                        .nextActivity(MainActivity.class)
                        .build(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.d("ljw", "onResult: " + result.getUserState());
                        switch (result.getUserState()){
                            case SIGNED_IN:
                                Log.i("INIT", "logged in!");
                                break;
                            case SIGNED_OUT:
                                Log.i("ljw", "onResult: User did not choose to sign-in");
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: ", e);
                    }
                }
        );
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        String username = prefs.getString("username", "Guest");
        String selectedTeam = prefs.getString("selectedTeam", "Operations");
        TextView titleView = findViewById(R.id.mainActUsername);
        titleView.setText(username);
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