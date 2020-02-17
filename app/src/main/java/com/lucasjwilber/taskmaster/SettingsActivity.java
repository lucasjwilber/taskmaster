package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = prefs.getString("username", "My");
        EditText usernameInput = findViewById(R.id.usernameInput);
        usernameInput.setText(username);
    }


    //thanks to https://developer.android.com/training/data-storage/shared-preferences
    //and https://stackoverflow.com/questions/18179124/android-getting-value-from-selected-radiobutton
    public void saveSettingsClicked(View v) {
        //get and save username
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        TextView usernameInput = findViewById(R.id.usernameInput);
        String username = usernameInput.getText().toString();

        if (username.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Please set your Username.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);

            //custom toast
            //thanks to https://stackoverflow.com/questions/11288475/custom-toast-on-android-a-simple-example
            View toastView = toast.getView();
            TextView toastMessage = toastView.findViewById(android.R.id.message);
            toastMessage.setTextSize(30);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String theme = prefs.getString("theme", "Cafe");
            switch (theme) {
                case "Cafe":
                    toastMessage.setTextColor(getResources().getColor(R.color.coffeeDarkest));
                    toastView.setBackgroundColor(getResources().getColor(R.color.coffeeLight));
                    break;
                case "City":
                    toastMessage.setTextColor(getResources().getColor(R.color.cityLightGray));
                    toastView.setBackgroundColor(getResources().getColor(R.color.cityMediumGray));
                    break;
            }
            toast.setGravity(Gravity.CENTER, 0, -40);
            toast.show();
        } else {
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
}
