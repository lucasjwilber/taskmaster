package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class AllTasksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String theme = prefs.getString("theme", "Cafe");
        applyTheme(theme);
    }

    private void applyTheme(String theme) {

        TextView title = findViewById(R.id.allTasksActTitle);
        View background = findViewById(R.id.allTasksActBg);
        ImageView logo = findViewById(R.id.allTasksActLogo);
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
                logo.setImageResource(R.drawable.notepadlogocity);
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
