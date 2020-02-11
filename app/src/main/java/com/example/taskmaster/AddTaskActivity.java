package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AddTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
    }
    public void addTaskButtonClicked(View v) {
        View submitMessage = findViewById(R.id.submitMessage);
        submitMessage.setVisibility(View.VISIBLE);
    }
}
