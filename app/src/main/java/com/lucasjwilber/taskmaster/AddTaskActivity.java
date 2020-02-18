package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AddTaskActivity extends AppCompatActivity {


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

        setContentView(R.layout.activity_add_task);
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
//
        TasksDatabase db = TasksDatabase.getTasksDatabase(getApplicationContext());

        db.userDao().insert(new Task(title, body));


        //display custom toast:
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
            case "Night":
                toastMessage.setTextColor(getResources().getColor(R.color.nightLightGray));
                toastView.setBackgroundColor(getResources().getColor(R.color.nightWhite));
                break;
        }
        toast.setGravity(Gravity.CENTER, 0, -40);
        toast.show();
    }



}
