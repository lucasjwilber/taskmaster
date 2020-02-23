package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String savedTeam = prefs.getString("selectedTeam", "Operations");
        //apply theme
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
        //set username to current username
        {
            String username = prefs.getString("username", "My");
            EditText usernameInput = findViewById(R.id.usernameInput);
            usernameInput.setText(username);
        }
        //set theme radio button to current theme
        {
            RadioGroup themeRg = findViewById(R.id.colorThemeRadioGroup);
            switch (theme) {
//                TODO: re-enable when fixed
//                case "City":
//                    themeRg.check(R.id.radioButtonCity);
//                    break;
//                case "Night":
//                    themeRg.check(R.id.radioButtonNight);
//                    break;
                case "Cafe":
                default:
                    themeRg.check(R.id.radioButtonCafe);
                    break;
            }
        }
        //query aws for team names to populate team selection spinner
        {
            AWSAppSyncClient mAWSAppSyncClient = AWSAppSyncClient.builder()
                    .context(getApplicationContext())
                    .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                    .build();

            mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                    //use cache here since teams change infrequently
                    .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                    .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
                        @Override
                        public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {

                            //thanks to https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list
                            List<ListTeamsQuery.Item> teams = response.data().listTeams().items();
                            List<String> teamNames = new ArrayList<>();
                            final Spinner teamSpinner = findViewById(R.id.settingsTeamsSpinner);
                            int spinnerPosition = 0;

                            for (int i = 0; i < teams.size(); i++) {
                                String teamName = teams.get(i).name();
                                teamNames.add(teamName);
                                //this is used to set the spinner to the team saved in shared prefs
                                if (teamName.equals(savedTeam)) spinnerPosition = i;
                            }

                            //thanks to 'Simplest Solution' @ https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list
                            final ArrayAdapter<String> teamSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(),
                                    android.R.layout.simple_spinner_item,
                                    teamNames);

                            //update UI on main thread
                            final int finalSpinnerPosition = spinnerPosition;
                            Handler handler = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message input) {
                                    teamSpinner.setAdapter(teamSpinnerAdapter);
                                    teamSpinner.setSelection(finalSpinnerPosition);
                                }
                            };
                            handler.obtainMessage().sendToTarget();
                        }
                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            Log.i("ljw", "failed querying teams list");
                            Log.e("ljw", e.toString());
                        }
                    });
        }
    }

    //thanks to https://developer.android.com/training/data-storage/shared-preferences
    //and https://stackoverflow.com/questions/18179124/android-getting-value-from-selected-radiobutton
    public void saveSettingsClicked(View v) {
        //get and save username
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        TextView usernameInput = findViewById(R.id.usernameInput);
        String username = usernameInput.getText().toString();

        if (username.equals("")) {
            //display "set a username" toast
            {
                Context context = getApplicationContext();
                CharSequence text = "Please set your Username.";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);

                //custom toast
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
                        toastView.setBackgroundColor(getResources().getColor(R.color.nightBlack));
                        break;
                }
                toast.setGravity(Gravity.CENTER, 0, -40);
                toast.show();
            }
        } else {
            //save selected username
            editor.putString("username", usernameInput.getText().toString());
            //get and save selected theme
            {
                RadioGroup colorThemeRG = findViewById(R.id.colorThemeRadioGroup);
                int selectedId = colorThemeRG.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                String theme = radioButton.getText().toString();
                editor.putString("theme", theme);
            }
            //get and save selected team
            {
                Spinner teamSpinner = findViewById(R.id.settingsTeamsSpinner);
                String selectedTeam = teamSpinner.getSelectedItem().toString();
                Log.i("ljw selected team", selectedTeam);
                editor.putString("selectedTeam", selectedTeam);
            }
            editor.apply();
            //go back to main activity
            finish();
        }
    }
}
