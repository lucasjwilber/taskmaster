package com.lucasjwilber.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.CreateTaskmasterUserMutation;
import com.amazonaws.amplify.generated.graphql.GetTaskmasterUserQuery;
import com.amazonaws.amplify.generated.graphql.ListTaskmasterUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskmasterUserInput;

public class LoginActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init AWSAppSyncClient
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        //initialize amplify auth:
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("ljw", "onResult: " + userStateDetails.getUserState());
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("ljw", "Initialization error.", e);
                    }
                }
        );

        //user auth state change listener:
        AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
            @Override
            public void onUserStateChanged(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case GUEST:
                        Log.i("userState", "user is in guest mode");
                        break;
                    case SIGNED_OUT:
                        Log.i("userState", "user is signed out");
                        break;
                    case SIGNED_IN:
                        Log.i("userState", "user is signed in");
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(intent);

                        // set user in users table

                        finish();
                        break;
                    case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                        Log.i("userState", "need to login again");
                        break;
                    case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                        Log.i("userState", "user logged in via federation, but currently needs new tokens");
                        break;
                    default:
                        Log.e("userState", "unsupported");
                }
            }
        });

        AWSMobileClient.getInstance().showSignIn(
                this,
                SignInUIOptions.builder()
                        .nextActivity(MainActivity.class)
                        .build(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.d("ljw", "onResult: " + result.getUserState());
                        switch (result.getUserState()){
                            case SIGNED_IN:
                                Log.i("INIT", "logged in!");

                                String username = AWSMobileClient.getInstance().getUsername();

                                // determine if user exists in the users table:
                                mAWSAppSyncClient.query(ListTaskmasterUsersQuery.builder().build())
                                        .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                                        .enqueue(new GraphQLCall.Callback<ListTaskmasterUsersQuery.Data>() {
                                            @Override
                                            public void onResponse(@Nonnull Response<ListTaskmasterUsersQuery.Data> response) {
                                                Log.i("ljw", "user query successful...");
                                                assert response.data() != null;
                                                List<ListTaskmasterUsersQuery.Item> allUsers = response.data().listTaskmasterUsers().items();
                                                Log.i("ljw", "users: \n" + allUsers.toString());

                                                boolean userIsInTableAlready = false;
                                                for (ListTaskmasterUsersQuery.Item user : allUsers) {
                                                    if (user.username().equals(username)) userIsInTableAlready = true;
                                                    Log.i("ljw", "looking for " + username + "\n found: " + user.username());
                                                }

                                                Log.i("ljw", "checked all users, userIsInTableAlready is " + userIsInTableAlready);

                                                if (!userIsInTableAlready) {
                                                    Log.i("ljw", "adding a new user to db");
                                                    CreateTaskmasterUserInput input = CreateTaskmasterUserInput.builder()
                                                            .teamID("Operations") //use Operations for default team for now
                                                            .username(username)
                                                            .build();

                                                    mAWSAppSyncClient.mutate(CreateTaskmasterUserMutation.builder().input(input).build())
                                                            .enqueue(new GraphQLCall.Callback<CreateTaskmasterUserMutation.Data>() {
                                                                @Override
                                                                public void onResponse(@Nonnull Response<CreateTaskmasterUserMutation.Data> response) {
                                                                    Log.i("ljw", "created new user successfully:\n" + response.data().createTaskmasterUser().toString());
                                                                }
                                                                @Override
                                                                public void onFailure(@Nonnull ApolloException e) {
                                                                    Log.e("ljw", "failed to create new user:\n" + e.toString());
                                                                }
                                                            });
                                                }
                                            }
                                            @Override
                                            public void onFailure(@Nonnull ApolloException e) {
                                                Log.i("ljw", "failed querying users table");
                                            }
                                        });

                                finish();
                                break;
                            case SIGNED_OUT:
                                Log.i("ljw", "onResult: User did not choose to sign-in");
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                break;
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("ljw", "onError: ", e);
                    }
                }
        );

    }
}
