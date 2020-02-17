package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = prefs.getString("username", "My ");
        applyUsername(username);

        //these need to be manually redrawn until I figure out how to
        // recreate() when the theme changes without causing a recreate loop
        String theme = prefs.getString("theme", "Cafe");
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
        }

    }

    public void applyUsername(String username) {
        TextView titleView = findViewById(R.id.mainActTitle);

        String titleText;
        if (username.lastIndexOf("s") == username.length() - 1) {
            titleText = username + "' Tasks";
        } else {
            titleText = username + "'s Tasks";
        }
        titleView.setText(titleText);
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