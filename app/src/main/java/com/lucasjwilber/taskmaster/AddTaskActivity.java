package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AddTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
    }
    //thanks to https://developer.android.com/guide/topics/ui/notifiers/toasts
    public void addTaskButtonClicked(View v) {
        Context context = getApplicationContext();
        CharSequence text = "Submitted!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

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
