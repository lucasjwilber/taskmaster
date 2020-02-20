package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTaskActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

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

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();
    }

    //thanks to https://developer.android.com/guide/topics/ui/notifiers/toasts
    public void addTaskButtonClicked(View v) {
        Context context = getApplicationContext();
        CharSequence text = "Submitted!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

        //get edittext fields
        TextView titleInput = findViewById(R.id.addTask_taskNameInput);
        String title = titleInput.getText().toString();
        TextView bodyInput = findViewById(R.id.addTask_taskDescInput);
        String body = bodyInput.getText().toString();
        //create the mutation
        CreateTaskInput input = CreateTaskInput.builder()
                .title(title)
                .body(body)
                .build();
        //enqueue the mutation
        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(input).build())
                .enqueue(mutationCallback);

        finish();
        //RIP for now, custom toast

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

    private GraphQLCall.Callback<CreateTaskMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
            Log.i("amplify", "Added Task");
            Log.i("amplify", response.toString());
        }
        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("amplify", e.toString());
        }
    };

}
