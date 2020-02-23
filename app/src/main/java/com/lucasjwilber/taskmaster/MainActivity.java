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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity {

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

        setContentView(R.layout.activity_main);



//        final CreateTeamInput installTeam = CreateTeamInput.builder()
//                .name("Install")
//                .build();
//
//        mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(opsTeam).build())
//                .enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
//                    @Override
//                    public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
//                        Log.i("ljw", response.data().createTeam().toString());
//                    }
//
//                    @Override
//                    public void onFailure(@Nonnull ApolloException e) {
//                        Log.i("ljw", "failed adding team" + opsTeam.name());
//                    }
//                });
//

        //delete the preceding:^
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        String username = prefs.getString("username", "My ");
        String selectedTeam = prefs.getString("selectedTeam", "Operations");
        TextView titleView = findViewById(R.id.mainActUsername);
        titleView.setText(username);
        TextView mainActTitle = findViewById(R.id.mainActTitle);
        mainActTitle.setText(selectedTeam);

        //apply theme changes that I couldn't set in <style>s
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