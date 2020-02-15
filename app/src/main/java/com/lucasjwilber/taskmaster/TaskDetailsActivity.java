package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TaskDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
    }
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskBody = intent.getStringExtra("taskBody");
        TextView title = findViewById(R.id.taskDetailsTitle);
        TextView body = findViewById(R.id.taskDetailsBody);
        title.setText(taskTitle);
        body.setText(taskBody);
    }
}
