package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
        //this intent comes from onBindViewHolder() in MyTaskRecyclerViewAdapter
        Intent intent = getIntent();
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskBody = intent.getStringExtra("taskBody");
        String state = intent.getStringExtra("taskState");
        TextView title = findViewById(R.id.taskDetailsTitle);
        TextView body = findViewById(R.id.taskDetailsBody);
        RadioButton rb;
        switch (state) {
            case "ASSIGNED":
                rb = findViewById(R.id.state_rb_assigned);
                break;
            case "IN PROGRESS":
                rb = findViewById(R.id.state_rb_inProgress);
                break;
            case "COMPLETE":
                rb = findViewById(R.id.state_rb_complete);
                break;
            case "NEW":
            default:
                rb = findViewById(R.id.state_rb_new);
                break;
        }
        rb.toggle();
        title.setText(taskTitle);
        body.setText(taskBody);
    }

    //TODO: this isn't working, fix it
    public void stateRadioButtonChanged(View v) {
        RadioGroup stateRg = findViewById(R.id.taskStateRadioGroup);
        RadioButton stateRb = findViewById(stateRg.getCheckedRadioButtonId());
        String state = stateRb.getText().toString();

        TasksDatabase db = TasksDatabase.getTasksDatabase(getApplicationContext());

        TextView title = findViewById(R.id.taskDetailsTitle);
        String taskTitle = title.getText().toString();
        Task task = db.userDao().findByName(taskTitle);
        Task updatedTask = new Task(task.title, task.body);
        updatedTask.setState(state);

        //replace old task with new one
        db.userDao().delete(task);
        db.userDao().insert(updatedTask);
    }

}
