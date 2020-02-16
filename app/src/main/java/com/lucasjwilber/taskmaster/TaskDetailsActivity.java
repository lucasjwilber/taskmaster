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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        applyTheme(theme);
    }

    protected void applyTheme(String theme) {

        TextView title = findViewById(R.id.taskDetailsTitle);
        View background = findViewById(R.id.taskDetailsActBg);
        TextView body = findViewById(R.id.taskDetailsBody);
        Window window = getWindow();
        ActionBar actionBar = getSupportActionBar();

        switch (theme) {
            case "City":
                int lightGreen = getResources().getColor(R.color.cityLightGreen);
                int darkGreen = getResources().getColor(R.color.cityDarkGreen);
                int lightGray = getResources().getColor(R.color.cityLightGray);
                int mediumGray = getResources().getColor(R.color.cityMediumGray);
                int darkGray = getResources().getColor(R.color.cityDarkGray);

                title.setTextColor(darkGray);
                background.setBackgroundColor(lightGray);
                body.setTextColor(darkGray);
                window.setNavigationBarColor(darkGray);
                window.setStatusBarColor(darkGray);
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(mediumGray));
                }
                break;
            case "Cafe":
                window.setNavigationBarColor(getResources().getColor(R.color.coffeeMedium));
                break;
        }
    }
}
