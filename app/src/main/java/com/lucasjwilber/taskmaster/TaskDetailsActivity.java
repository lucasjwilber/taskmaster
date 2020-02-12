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
        Intent intent = getIntent();
        String taskName = intent.getStringExtra("taskName");
        TextView title = findViewById(R.id.taskDetailsTitle);
        title.setText(taskName);
    }
}
