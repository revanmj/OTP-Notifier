package pl.revanmj.smspasswordnotifier.activities;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.os.Bundle;

import pl.revanmj.smspasswordnotifier.BuildConfig;
import pl.revanmj.smspasswordnotifier.MessageProcessor;
import pl.revanmj.smspasswordnotifier.R;
import pl.revanmj.smspasswordnotifier.data.SharedSettings;

public class MainActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final SwitchPreference useWhitelist = (SwitchPreference) findPreference(SharedSettings.KEY_USE_WHITELIST);
        final PreferenceScreen editNumbers = (PreferenceScreen) findPreference("edit_numbers");

        useWhitelist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean value = (Boolean) newValue;
                if (!value)
                    editNumbers.setEnabled(false);
                else
                    editNumbers.setEnabled(true);
                return true;
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean use_whitelist = settings.getBoolean(SharedSettings.KEY_USE_WHITELIST, true);
        if (!use_whitelist)
            editNumbers.setEnabled(false);
        else
            editNumbers.setEnabled(true);

        Preference appVersion = findPreference("app_version");
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            appVersion.setTitle("Wersja " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final PreferenceScreen testNoti = (PreferenceScreen) findPreference("test_noti");
        testNoti.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MessageProcessor.showNotification(MainActivity.this, "123456", "ExampleSender");
                return false;
            }
        });
        if (!BuildConfig.DEBUG) {
            getPreferenceScreen().removePreference(testNoti);
        }
    }
}
