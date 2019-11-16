package pl.revanmj.smspasswordnotifier

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import pl.revanmj.smspasswordnotifier.data.SharedSettings
import pl.revanmj.smspasswordnotifier.data.WhitelistViewModel

import android.content.Context.NOTIFICATION_SERVICE

/**
 * Created by revanmj on 13.01.2017.
 */

object MessageProcessor {
    private const val LOG_TAG = "NumbersFilter"

    internal fun processSms(context: Context, sms: SmsMessage) {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        val useWhitelist = settings.getBoolean(SharedSettings.KEY_USE_WHITELIST, true)

        val phoneNumber = sms.displayOriginatingAddress
        val message = sms.displayMessageBody

        val viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(context.applicationContext as Application)
                .create(WhitelistViewModel::class.java)
        val item = viewModel.getItemByName(phoneNumber)

        if (useWhitelist && item == null) {
            Log.d(LOG_TAG, "processSms - shouldExtractPassword returned false, exiting...")
            return
        }

        val code = CodeExtractor.extractCode(message, item.regex)
        if (code == null) {
            Log.d(LOG_TAG, "processSms - extracted code is null, exiting...")
            return
        }

        val useClipboard = settings.getBoolean(SharedSettings.KEY_USE_CLIPBOARD, true)
        if (useClipboard)
            copyCode(context, code)

        showNotification(context, code, phoneNumber)
    }

    internal fun copyCode(context: Context, code: String) {
        // Copy code to the clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("someLabel", code)
        clipboard.setPrimaryClip(clip)

        // Show to user that code was copied
        Toast.makeText(context, R.string.toast_code_copied, Toast.LENGTH_SHORT).show()
    }

    fun showNotification(context: Context, code: String, sender: String) {
        var formattedCode = code
        // Instert space in the middle of the code (if 6 digits or longer) for better readability
        formattedCode = insertSpaceInTheMiddle(formattedCode)

        // Accent color for L/M/N
        val color = ContextCompat.getColor(context, R.color.colorPrimary)

        // Creating intent for notification action
        val intent = Intent(SmsReceiver.ACTION_COPY_CODE)
        intent.setClass(context, SmsReceiver::class.java)
        intent.putExtra("code", formattedCode)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        // creating normal notification
        val mBuilder = NotificationCompat.Builder(context, context.getString(R.string.noti_channel_id))
                .setSmallIcon(R.mipmap.ic_noti_key)
                //.setSubText(sender) // next to app name
                .setContentTitle(formattedCode)
                .setContentText(sender)
                .setColor(color)
                .addAction(
                        R.drawable.ic_noti_copy,
                        context.resources.getString(R.string.label_copy_code),
                        pendingIntent)

        // Generate unique id for notification
        val now = Date()
        val id = Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(now))

        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        val headsUp = settings.getBoolean(SharedSettings.KEY_HEADSUP_NOTIFICATIONS, false)
        if (headsUp && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mBuilder.priority = NotificationCompat.PRIORITY_HIGH
            // Silent sound needed for heads up notifications to work
            mBuilder.setSound(Uri.parse("android.resource://pl.revanmj.smspasswordnotifier/" + R.raw.silent))
        }

        // Show notification
        val mNotifyMgr = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(id, mBuilder.build())

    }

    private fun insertSpaceInTheMiddle(original: String): String {
        if (original.length > 5 && original.length % 2 == 0) {
            val distance = original.length / 2
            val sb = StringBuilder()
            val charArrayOfOriginal = original.toCharArray()
            for (ch in charArrayOfOriginal.indices) {
                if (ch % distance == 0 && ch > 0)
                    sb.append(' ').append(charArrayOfOriginal[ch])
                else
                    sb.append(charArrayOfOriginal[ch])
            }
            return sb.toString()
        }
        return original
    }
}
