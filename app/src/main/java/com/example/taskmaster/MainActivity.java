package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void goToAddTasksActivity(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    public void goToAllTasksActivity(View v) {
        Intent intent = new Intent(this, AllTasksActivity.class);
        startActivity(intent);
    }

}