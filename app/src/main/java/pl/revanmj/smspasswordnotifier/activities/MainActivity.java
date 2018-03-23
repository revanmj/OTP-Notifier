package pl.revanmj.smspasswordnotifier.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

import pl.revanmj.smspasswordnotifier.BuildConfig;
import pl.revanmj.smspasswordnotifier.MessageProcessor;
import pl.revanmj.smspasswordnotifier.R;
import pl.revanmj.smspasswordnotifier.data.SharedSettings;
import pl.revanmj.smspasswordnotifier.data.WhitelistProvider;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainPreferenceFragment mainFragment = new MainPreferenceFragment();
        mainFragment.setActivity(this);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mainFragment).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {
        private Activity mActivity;

        public void setActivity(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);

            final SwitchPreferenceCompat useWhitelist = (SwitchPreferenceCompat) findPreference(SharedSettings.KEY_USE_WHITELIST);
            final PreferenceScreen editNumbers = (PreferenceScreen) findPreference("edit_numbers");

            useWhitelist.setOnPreferenceChangeListener((preference, newValue) -> {
                Boolean value = (Boolean) newValue;
                if (!value)
                    editNumbers.setEnabled(false);
                else
                    editNumbers.setEnabled(true);
                return true;
            });

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
            boolean use_whitelist = settings.getBoolean(SharedSettings.KEY_USE_WHITELIST, true);
            if (!use_whitelist)
                editNumbers.setEnabled(false);
            else
                editNumbers.setEnabled(true);

            boolean isFirstRun = settings.getBoolean(SharedSettings.KEY_IS_FIRST_RUN, true);
            if (isFirstRun) {
                firstTimeWhitelist(mActivity);
                settings.edit().putBoolean(SharedSettings.KEY_IS_FIRST_RUN, false).commit();
            }

            Preference appVersion = findPreference("app_version");
            PackageManager manager = mActivity.getPackageManager();
            PackageInfo info;
            try {
                info = manager.getPackageInfo(mActivity.getPackageName(), 0);
                appVersion.setTitle("Version " + info.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            final PreferenceScreen testNoti = (PreferenceScreen) findPreference("test_noti");
            testNoti.setOnPreferenceClickListener(preference -> {
                MessageProcessor.showNotification(mActivity, "123456", "ExampleSender");
                return false;
            });
            if (!BuildConfig.DEBUG) {
                getPreferenceScreen().removePreference(testNoti);
            }

            if (ContextCompat.checkSelfPermission(mActivity,
                    Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.RECEIVE_SMS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    showPermissionExplanaition(mActivity);

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.RECEIVE_SMS},
                            MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

                    // MY_PERMISSIONS_REQUEST_RECEIVE_SMS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SwitchPreferenceCompat headsUpSwitch = (SwitchPreferenceCompat) findPreference("headsup_notifications");
                getPreferenceScreen().removePreference(headsUpSwitch);

                NotificationManager mNotificationManager =
                        (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                // The id of the channel.
                String id = getString(R.string.noti_channel_id);
                // The user-visible name of the channel.
                CharSequence name = getString(R.string.noti_channel_name);
                // The user-visible description of the channel.
                String description = getString(R.string.noti_channel_description);

                NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);

                mChannel.setDescription(description);
                mChannel.enableLights(true);
                mChannel.setShowBadge(false);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                mChannel.setLightColor(Color.BLUE);
                mNotificationManager.createNotificationChannel(mChannel);
            } else {
                PreferenceScreen editNotifications = (PreferenceScreen) findPreference("edit_notifications");
                getPreferenceScreen().removePreference(editNotifications);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    showPermissionExplanaition(this);
                }
            }
        }
    }

    public static void showPermissionExplanaition(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.title_no_permission);
        builder.setMessage(R.string.message_app_no_permission);
        builder.setPositiveButton(R.string.button_grant, (dialogInterface, i) -> {
            dialogInterface.dismiss();

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
        });
        builder.setNegativeButton(R.string.button_exit, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            activity.finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void firstTimeWhitelist(Activity activity) {
        ArrayList<String> defaultWhitelist = new ArrayList<String>() {{
            add("AUTHMSG"); // Humble Bundle
            add("Apple");
            add("FACEBOOK");
            add("Google");
            add("Instagram");
            add("Twitter");
            add("Verify"); // Microsoft
        }};

        ArrayList<ContentValues> defaultSenders = new ArrayList<>();
        for (String sender: defaultWhitelist) {
            ContentValues tmp = new ContentValues();
            tmp.put(WhitelistProvider.KEY_SENDER, sender);
            defaultSenders.add(tmp);
        }

        ContentValues[] cv = new ContentValues[defaultSenders.size()];
        activity.getContentResolver()
                .bulkInsert(WhitelistProvider.CONTENT_URI, defaultSenders.toArray(cv));
    }
}
