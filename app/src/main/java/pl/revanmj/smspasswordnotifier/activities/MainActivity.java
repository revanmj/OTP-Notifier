package pl.revanmj.smspasswordnotifier.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

import pl.revanmj.smspasswordnotifier.BuildConfig;
import pl.revanmj.smspasswordnotifier.MessageProcessor;
import pl.revanmj.smspasswordnotifier.R;
import pl.revanmj.smspasswordnotifier.data.SharedSettings;
import pl.revanmj.smspasswordnotifier.data.WhitelistItem;
import pl.revanmj.smspasswordnotifier.data.WhitelistViewModel;

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

                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.RECEIVE_SMS)) {
                    showPermissionExplanaition(mActivity);

                } else {
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.RECEIVE_SMS},
                            MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SwitchPreferenceCompat headsUpSwitch = (SwitchPreferenceCompat) findPreference("headsup_notifications");
                getPreferenceScreen().removePreference(headsUpSwitch);

                NotificationManager mNotificationManager =
                        (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                String id = getString(R.string.noti_channel_id);
                CharSequence name = getString(R.string.noti_channel_name);
                String description = getString(R.string.noti_channel_description);

                NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);

                mChannel.setDescription(description);
                mChannel.enableLights(true);
                mChannel.setShowBadge(false);
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

        ArrayList<WhitelistItem> defaultSenders = new ArrayList<>();
        for (String sender : defaultWhitelist) {
            WhitelistItem tmp = new WhitelistItem();
            tmp.setName(sender);
            defaultSenders.add(tmp);
        }

        WhitelistViewModel viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(activity.getApplication())
                .create(WhitelistViewModel.class);
        viewModel.insert(defaultSenders.toArray(new WhitelistItem[defaultSenders.size()]));
    }
}
