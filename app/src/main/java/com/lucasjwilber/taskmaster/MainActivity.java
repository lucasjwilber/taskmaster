package com.lucasjwilber.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String username = "Guest";
    Intent shareIntent;
    private static PinpointManager pinpointManager;

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

        setContentView(R.layout.activity_main);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("ljw", "onResult: " + userStateDetails.getUserState());

                        //auth
                        switch (userStateDetails.getUserState()) {
                            case GUEST:
                                Log.i("userState", "user is in guest mode");
                                break;
                            case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                            case SIGNED_OUT:
                                Log.i("userState", "user is signed out");
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                break;
                            case SIGNED_IN:
                                Log.i("ljw", "user is signed in");

                                //if taskmaster was opened in order to share an image redirect to AddTask
                                shareIntent = getIntent();
                                String action = shareIntent.getAction();
                                String type = shareIntent.getType();
                                if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {

                                    Log.i("ljw", "opened Main with shared intent");
                                    Intent addTaskIntent = new Intent(getApplicationContext(), AddTaskActivity.class);
                                    Uri uri = Objects.requireNonNull(shareIntent.getParcelableExtra(Intent.EXTRA_STREAM));
                                    String imagePath = getUriRealPath(getApplicationContext(), uri);
                                    Log.i("ljw", "image uri is " + uri);
                                    Log.i("ljw", "shared image is on path " + imagePath);
//                                    /content:/com.google.android.apps.photos.contentprovider/-1/1/file%3A%2F%2F%2Fdata%2Fuser%2F0%2Fcom.google.android.apps.photos%2Fcache%2Fshare-cache%2Fmedia.tmp%3Ffilename%3DLnRrYf6e-3.jpg/ORIGINAL/NONE/146970106
                                    addTaskIntent.putExtra("sharedImageURI", imagePath);
                                    startActivity(addTaskIntent);
                                }

                                final TextView usernameView = findViewById(R.id.mainActUsername);
                                username = AWSMobileClient.getInstance().getUsername();
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message input) {
                                        usernameView.setText(username);
                                    }
                                };
                                handler.obtainMessage().sendToTarget();
                                break;
//                            case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
//                                Log.i("userState", "need to login again");
//                                break;
                            case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                                Log.i("userState", "user logged in via federation, but currently needs new tokens");
                                break;
                            default:
                                Log.e("userState", "unsupported");
                        }

                        //storage
                        try {
                            Amplify.addPlugin(new AWSS3StoragePlugin());
                            Amplify.configure(getApplicationContext());
                            Log.i("ljw", "All set and ready to go!");
                        } catch (Exception e) {
                            Log.i("ljw", e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.i("ljw", "Initialization error.", e);
                        Log.i("ljw", "Initialization error.", e);
                    }
                }
        );

        // Initialize PinpointManager
        pinpointManager = getPinpointManager(getApplicationContext());

        //  saved for potential 'add team' feature:
        {
        //        final CreateTeamInput newTeam = CreateTeamInput.builder()
        //                .name("Install")
        //                .build();
        //        mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(newTeam).build())
        //                .enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
        //                    @Override
        //                    public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
        //                        Log.i(TAG, response.data().createTeam().toString());
        //                    }
        //                    @Override
        //                    public void onFailure(@Nonnull ApolloException e) {
        //                        Log.i(TAG, "failed adding team" + opsTeam.name());
        //                    }
        //                });
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        final TextView usernameView = findViewById(R.id.mainActUsername);
        username = AWSMobileClient.getInstance().getUsername();
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message input) {
                usernameView.setText(username);
            }
        };
        handler.obtainMessage().sendToTarget();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = prefs.getString("theme", "Cafe");
        String selectedTeam = prefs.getString("selectedTeam", "Operations");
        TextView mainActTitle = findViewById(R.id.mainActTitle);
        mainActTitle.setText(selectedTeam);

        //apply theme changes that I couldn't set in <style>s
        {
            ImageView logo = findViewById(R.id.mainActLogo);
            ImageView settingsImage = findViewById(R.id.settingsgear);
            Window window = getWindow();
            switch (theme) {
                case "Cafe":
                    logo.setImageResource(R.drawable.notepadlogocafe);
                    settingsImage.setImageResource(R.drawable.settingsgear);
                    window.setStatusBarColor(getResources().getColor(R.color.coffeeDarkest));
                    window.setNavigationBarColor(getResources().getColor(R.color.coffeeMedium));
                    break;
                case "City":
                    logo.setImageResource(R.drawable.notepadlogocity);
                    settingsImage.setImageResource(R.drawable.settingsgearcity);
                    window.setStatusBarColor(getResources().getColor(R.color.cityDarkGray));
                    window.setNavigationBarColor(getResources().getColor(R.color.cityDarkGray));
                    break;
                case "Night":
                    logo.setImageResource(R.drawable.notepadlogonight);
                    settingsImage.setImageResource(R.drawable.settingsgearnight);
                    window.setStatusBarColor(getResources().getColor(R.color.nightBlue));
                    window.setNavigationBarColor(getResources().getColor(R.color.nightBlue));
                    break;
            }
        }
    }

    public void goToAddTasksActivity(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }
    public void goToAllTasksActivity(View v) {
        Intent intent = new Intent(this, AllTasksActivity.class);
        startActivity(intent);
    }
    public void goToSettingsActivity(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    Log.i("INIT", userStateDetails.getUserState().toString());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("INIT", "Initialization error.", e);
                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("ljw", "getInstanceId failed", task.getException());
                            return;
                        }
                        final String token = task.getResult().getToken();
                        Log.d("ljw", "Registering push notifications token: " + token);
                        pinpointManager.getNotificationClient().registerDeviceToken(token);
                    });
        }
        return pinpointManager;
    }

















    /* Get uri related content real local file path. */
    private String getUriRealPath(Context ctx, Uri uri)
    {
        String ret = "";

        if( isAboveKitKat() )
        {
            // Android OS above sdk version 19.
            ret = getUriRealPathAboveKitkat(ctx, uri);
        }else
        {
            // Android OS below sdk version 19
            ret = getImageRealPath(getContentResolver(), uri, null);
        }

        return ret;
    }

    private String getUriRealPathAboveKitkat(Context ctx, Uri uri)
    {
        String ret = "";

        if(ctx != null && uri != null) {

            if(isContentUri(uri))
            {
                if(isGooglePhotoDoc(uri.getAuthority()))
                {
                    ret = uri.getLastPathSegment();
                }else {
                    ret = getImageRealPath(getContentResolver(), uri, null);
                }
            }else if(isFileUri(uri)) {
                ret = uri.getPath();
            }else if(isDocumentUri(ctx, uri)){

                // Get uri related document id.
                String documentId = DocumentsContract.getDocumentId(uri);

                // Get uri authority.
                String uriAuthority = uri.getAuthority();

                if(isMediaDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if(idArr.length == 2)
                    {
                        // First item is document type.
                        String docType = idArr[0];

                        // Second item is document real id.
                        String realDocId = idArr[1];

                        // Get content uri by document type.
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        if("image".equals(docType))
                        {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        }else if("video".equals(docType))
                        {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        }else if("audio".equals(docType))
                        {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        // Get where clause with real document id.
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;

                        ret = getImageRealPath(getContentResolver(), mediaContentUri, whereClause);
                    }

                }else if(isDownloadDoc(uriAuthority))
                {
                    // Build download uri.
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");

                    // Append download document id at uri end.
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));

                    ret = getImageRealPath(getContentResolver(), downloadUriAppendId, null);

                }else if(isExternalStoreDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if(idArr.length == 2)
                    {
                        String type = idArr[0];
                        String realDocId = idArr[1];

                        if("primary".equalsIgnoreCase(type))
                        {
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }
                }
            }
        }

        return ret;
    }

    /* Check whether current android os version is bigger than kitkat or not. */
    private boolean isAboveKitKat()
    {
        boolean ret = false;
        ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        return ret;
    }

    /* Check whether this uri represent a document or not. */
    private boolean isDocumentUri(Context ctx, Uri uri)
    {
        boolean ret = false;
        if(ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri);
        }
        return ret;
    }

    /* Check whether this uri is a content uri or not.
     *  content uri like content://media/external/images/media/1302716
     *  */
    private boolean isContentUri(Uri uri)
    {
        boolean ret = false;
        if(uri != null) {
            String uriSchema = uri.getScheme();
            if("content".equalsIgnoreCase(uriSchema))
            {
                ret = true;
            }
        }
        return ret;
    }

    /* Check whether this uri is a file uri or not.
     *  file uri like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
     * */
    private boolean isFileUri(Uri uri)
    {
        boolean ret = false;
        if(uri != null) {
            String uriSchema = uri.getScheme();
            if("file".equalsIgnoreCase(uriSchema))
            {
                ret = true;
            }
        }
        return ret;
    }


    /* Check whether this document is provided by ExternalStorageProvider. */
    private boolean isExternalStoreDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.externalstorage.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by DownloadsProvider. */
    private boolean isDownloadDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.providers.downloads.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by MediaProvider. */
    private boolean isMediaDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.android.providers.media.documents".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Check whether this document is provided by google photos. */
    private boolean isGooglePhotoDoc(String uriAuthority)
    {
        boolean ret = false;

        if("com.google.android.apps.photos.content".equals(uriAuthority))
        {
            ret = true;
        }

        return ret;
    }

    /* Return uri represented document file real local path.*/
    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if(cursor!=null)
        {
            boolean moveToFirst = cursor.moveToFirst();
            if(moveToFirst)
            {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }

        return ret;
    }

}