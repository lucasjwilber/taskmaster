package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.Adapter rvAdapter;
    public RecyclerView.LayoutManager rvLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String username = prefs.getString("username", "My ");
        applyUsername(username);

        String theme = prefs.getString("theme", "Cafe");
        applyTheme(theme);
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

    public void applyTheme(String theme) {
        ImageView logo = findViewById(R.id.mainActLogo);
        TextView title = findViewById(R.id.mainActTitle);
        ImageView settingsImage = findViewById(R.id.settingsgear);
        View background = findViewById(R.id.mainActBG);
        Button addTask = findViewById(R.id.button_addTask);
        Button allTasks = findViewById(R.id.button_allTasks);
        Window window = getWindow();
        ActionBar actionBar = getSupportActionBar();

        switch (theme) {
            case "City":
                int lightGreen = getResources().getColor(R.color.cityLightGreen);
                int darkGreen = getResources().getColor(R.color.cityDarkGreen);
                int lightGray = getResources().getColor(R.color.cityLightGray);
                int mediumGray = getResources().getColor(R.color.cityMediumGray);
                int darkGray = getResources().getColor(R.color.cityDarkGray);

                logo.setImageResource(R.drawable.notepadlogocity);
                title.setTextColor(darkGray);
                settingsImage.setImageResource(R.drawable.settingsgearcity);
                background.setBackgroundColor(lightGray);
                addTask.setTextColor(lightGray);
                allTasks.setTextColor(lightGray);
                addTask.setBackgroundTintList(ColorStateList.valueOf(darkGreen));
                allTasks.setBackgroundTintList(ColorStateList.valueOf(darkGreen));
                window.setStatusBarColor(darkGray);
                window.setNavigationBarColor(darkGray);
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(mediumGray));
                }

                break;
            case "Cafe":
                window.setNavigationBarColor(getResources().getColor(R.color.coffeeMedium));
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