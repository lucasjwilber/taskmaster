package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.activity_settings);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        applyTheme(theme);
    }

    public void applyTheme(String theme) {
        TextView title = findViewById(R.id.settingsActTitle);
        View background = findViewById(R.id.settingsActBg);
        TextView usernameInput = findViewById(R.id.usernameInput);
        TextView usernameInputLabel = findViewById(R.id.usernameInputLabel);
        TextView themeLabel = findViewById(R.id.themeRadioGroupTitle);
        RadioButton cafeButton = findViewById(R.id.radioButtonCafe);
        RadioButton cityButton = findViewById(R.id.radioButtonCity);
        Button saveButton = findViewById(R.id.saveButton);
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
                usernameInput.setLinkTextColor(lightGreen);
                usernameInputLabel.setTextColor(lightGray);
                window.setStatusBarColor(darkGray);
                themeLabel.setTextColor(darkGray);
                cafeButton.setTextColor(darkGray);
                cafeButton.setLinkTextColor(lightGreen);
                cityButton.setTextColor(darkGray);
                cityButton.setLinkTextColor(lightGreen);
                saveButton.setBackgroundTintList(ColorStateList.valueOf(darkGray));
                saveButton.setTextColor(lightGray);
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(mediumGray));
                }
                break;
            case "Cafe":
//                everything's already hardcoded for this theme!
                break;
        }
    }

    //thanks to https://developer.android.com/training/data-storage/shared-preferences
    //and https://stackoverflow.com/questions/18179124/android-getting-value-from-selected-radiobutton
    public void saveSettingsClicked(View v) {
        //get and save username
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        TextView usernameInput = findViewById(R.id.usernameInput);
        editor.putString("username", usernameInput.getText().toString());

        //get and save selected theme
        RadioGroup colorThemeRG = findViewById(R.id.colorThemeRadioGroup);
        int selectedId = colorThemeRG.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        String theme = radioButton.getText().toString();
        editor.putString("theme", theme);

        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
