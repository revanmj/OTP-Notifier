package pl.revanmj.smspasswordnotifier.activities

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import androidx.preference.PreferenceFragmentCompat

import java.util.ArrayList

import pl.revanmj.smspasswordnotifier.BuildConfig
import pl.revanmj.smspasswordnotifier.MessageProcessor
import pl.revanmj.smspasswordnotifier.R
import pl.revanmj.smspasswordnotifier.data.SharedSettings
import pl.revanmj.smspasswordnotifier.data.WhitelistItem
import pl.revanmj.smspasswordnotifier.data.WhitelistViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainFragment = MainPreferenceFragment(this)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, mainFragment).commit()
    }

    class MainPreferenceFragment(private val activity: Activity) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)

            val useWhitelistPref = findPreference<Preference>(SharedSettings.KEY_USE_WHITELIST) as SwitchPreferenceCompat?
            val editNumbersPref = findPreference<Preference>("edit_numbers") as PreferenceScreen?

            useWhitelistPref?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue as Boolean
                editNumbersPref?.isEnabled = value
                true
            }

            val settings = PreferenceManager.getDefaultSharedPreferences(activity)
            val useWhitelist = settings.getBoolean(SharedSettings.KEY_USE_WHITELIST, true)
            editNumbersPref?.isEnabled = useWhitelist

            val isFirstRun = settings.getBoolean(SharedSettings.KEY_IS_FIRST_RUN, true)
            if (isFirstRun) {
                firstTimeWhitelist(activity)
                settings.edit().putBoolean(SharedSettings.KEY_IS_FIRST_RUN, false).commit()
            }

            val appVersion = findPreference<Preference>("app_version")
            val manager = activity.packageManager
            val info: PackageInfo
            try {
                info = manager.getPackageInfo(activity.packageName, 0)
                appVersion!!.title = "Version " + info.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val testNoti = findPreference<Preference>("test_noti") as PreferenceScreen?
            testNoti!!.setOnPreferenceClickListener {
                MessageProcessor.showNotification(activity, "123456", "ExampleSender")
                false
            }
            // Hide test notification option from release builds
            if (!BuildConfig.DEBUG) {
                preferenceScreen.removePreference(testNoti)
            }

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.RECEIVE_SMS)) {
                    showPermissionExplanation(activity)

                } else {
                    ActivityCompat.requestPermissions(activity,
                            arrayOf(Manifest.permission.RECEIVE_SMS),
                            MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
                }
            }
            // Use system notification settings when on Oreo or higher
            // and hide internal settings in such case
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val headsUpSwitch = findPreference<Preference>("headsup_notifications") as SwitchPreferenceCompat?
                preferenceScreen.removePreference(headsUpSwitch!!)

                val mNotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val id = getString(R.string.noti_channel_id)
                val name = getString(R.string.noti_channel_name)
                val description = getString(R.string.noti_channel_description)

                val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)

                mChannel.description = description
                mChannel.enableLights(true)
                mChannel.setShowBadge(false)
                mChannel.lightColor = Color.BLUE
                mNotificationManager.createNotificationChannel(mChannel)
            } else {
                val editNotifications = findPreference<Preference>("edit_notifications") as PreferenceScreen?
                preferenceScreen.removePreference(editNotifications!!)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_RECEIVE_SMS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    showPermissionExplanation(this)
                }
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 1

        fun showPermissionExplanation(activity: Activity) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.title_no_permission)
            builder.setMessage(R.string.message_app_no_permission)
            builder.setPositiveButton(R.string.button_grant) { dialogInterface, _ ->
                dialogInterface.dismiss()
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.RECEIVE_SMS),
                        MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
            }
            // Close app if permission is not granted
            builder.setNegativeButton(R.string.button_exit) { dialogInterface, _ ->
                dialogInterface.dismiss()
                activity.finish()
            }

            val dialog = builder.create()
            dialog.show()
        }

        fun firstTimeWhitelist(activity: Activity) {
            val defaultWhitelist = object : ArrayList<String>() {
                init {
                    add("AUTHMSG") // Humble Bundle
                    add("Apple")
                    add("FACEBOOK")
                    add("Google")
                    add("Instagram")
                    add("Twitter")
                    add("Verify") // Microsoft
                }
            }

            val defaultSenders = ArrayList<WhitelistItem>()
            for (sender in defaultWhitelist) {
                val tmp = WhitelistItem()
                tmp.name = sender
                defaultSenders.add(tmp)
            }

            val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
                    .create(WhitelistViewModel::class.java)
            viewModel.insert(*defaultSenders.toTypedArray())
        }
    }
}
