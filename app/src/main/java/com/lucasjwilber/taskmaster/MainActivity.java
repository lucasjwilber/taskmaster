package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String username = sharedPref.getString("username", "My ");
//        TextView titleView = findViewById(R.id.titleMain);
//        String titleText = username + "Tasksss";
//        titleView.setText(titleText);
    }

    @Override
    public void onResume(){
        super.onResume();
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = prefs.getString("username", "My ");
        TextView titleView = findViewById(R.id.titleMain);

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