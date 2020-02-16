package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Database;
import androidx.room.Room;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class AddTaskActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
    }

    @Override
    public void onResume() {
        super.onResume();
//        setContentView(R.layout.activity_add_task);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        applyTheme(theme);
    }

    public void applyTheme(String theme) {
        TextView title = findViewById(R.id.addTaskActTitle);
        View background = findViewById(R.id.addTaskActBg);
        TextView nameLabel = findViewById(R.id.addTask_taskNameLabel);
        TextView nameInput = findViewById(R.id.addTask_taskNameInput);
        TextView descLabel = findViewById(R.id.addTask_taskDescLabel);
        TextView descInput = findViewById(R.id.addTask_taskDescInput);
        Button taskSubmitBtn = findViewById(R.id.submitTaskButton);
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
                nameLabel.setTextColor(darkGray);
                descLabel.setTextColor(darkGray);
                taskSubmitBtn.setBackgroundTintList(ColorStateList.valueOf(darkGreen));
                taskSubmitBtn.setTextColor(lightGray);
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

    //thanks to https://developer.android.com/guide/topics/ui/notifiers/toasts
    public void addTaskButtonClicked(View v) {
        Context context = getApplicationContext();
        CharSequence text = "Submitted!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

        //save to db:
        TextView titleInput = findViewById(R.id.addTask_taskNameInput);
        String title = titleInput.getText().toString();
        TextView bodyInput = findViewById(R.id.addTask_taskDescInput);
        String body = bodyInput.getText().toString();

//        TasksDatabase db = Room.databaseBuilder(getApplicationContext(),
//                TasksDatabase.class, "database-name").build();
//
//        db.userDao().insert(new Task(title, body));


        //display custom toast:
        //thanks to https://stackoverflow.com/questions/11288475/custom-toast-on-android-a-simple-example
        View toastView = toast.getView();
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(30);
        toastMessage.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        toastView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toast.setGravity(Gravity.CENTER, 0, -40);
        toast.show();
    }

}
