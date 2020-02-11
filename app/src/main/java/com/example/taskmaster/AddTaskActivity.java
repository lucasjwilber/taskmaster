package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AddTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
    }
    //thanks to https://developer.android.com/training/animation/reveal-or-hide-view
    public void addTaskButtonClicked(View v) {
        final View submitMessage = findViewById(R.id.submitMessage);
        submitMessage.setVisibility(View.VISIBLE);
        submitMessage.animate()
                .alpha(0.0f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        submitMessage.setVisibility(View.INVISIBLE);
                        submitMessage.setAlpha(1.0f);
                    }
                });
    }
}
