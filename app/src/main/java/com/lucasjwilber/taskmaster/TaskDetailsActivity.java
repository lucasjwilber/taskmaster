package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TaskDetailsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_task_details);
    }
    protected void onResume() {
        super.onResume();
        //render task details
        Intent intent = getIntent();
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskBody = intent.getStringExtra("taskBody");
        TextView title = findViewById(R.id.taskDetailsTitle);
        TextView body = findViewById(R.id.taskDetailsBody);
        title.setText(taskTitle);
        body.setText(taskBody);
    }

}
