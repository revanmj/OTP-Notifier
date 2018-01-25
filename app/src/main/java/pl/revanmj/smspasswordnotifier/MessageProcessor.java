package pl.revanmj.smspasswordnotifier;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pl.revanmj.smspasswordnotifier.data.SharedSettings;
import pl.revanmj.smspasswordnotifier.data.WhitelistProvider;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by revanmj on 13.01.2017.
 */

public class MessageProcessor {
    private static final String LOG_TAG = "NumbersFilter";
    private Context mContext;

    MessageProcessor(Context context) {
        mContext = context;
    }

    void processSms(Context context, SmsMessage sms) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean use_whitelist = settings.getBoolean(SharedSettings.KEY_USE_WHITELIST, true);

        String phoneNumber = sms.getDisplayOriginatingAddress();

        Cursor cursor = mContext.getContentResolver().query(
                Uri.withAppendedPath(WhitelistProvider.CONTENT_URI, phoneNumber),
                null, null, null, null);
        int cursorCount = cursor != null ? cursor.getCount() : 0;

        if (use_whitelist && cursorCount < 1) {
            Log.d(LOG_TAG, "processSms - shouldExtractPassword returned false, exiting...");
            if (cursor != null)
                cursor.close();
            return;
        }

        String message = sms.getDisplayMessageBody();
        String regex = cursor != null ? cursor.getString(WhitelistProvider.REGEX) : null;
        if (cursor != null)
            cursor.close();
        String code = CodeExtractor.extractCode(message, regex);

        if (code == null) {
            Log.d(LOG_TAG, "processSms - extracted code is null, exiting...");
            return;
        }

        boolean use_clipboard = settings.getBoolean(SharedSettings.KEY_USE_CLIPBOARD, true);
        if (use_clipboard)
            copyCode(context, code);

        showNotification(context, code, phoneNumber);

    }

    static void copyCode(Context context, String code) {
        // Copy code to the clipboard
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("someLabel",code);
        clipboard.setPrimaryClip(clip);

        // Show to user that code was copied
        Toast.makeText(context, R.string.toast_code_copied, Toast.LENGTH_SHORT).show();
    }

    public static void showNotification(Context context, String code, String sender) {
        // Instert space in the middle of the code (if 6 digits or longer) for better readability
        code = insertSpaceInTheMiddle(code);

        /*// Prepare formatted string with notification content text
        String lineFormat = sender + ": %s";
        int lineParamStartPos = lineFormat.indexOf("%s");
        if (lineParamStartPos < 0) {
            throw new InvalidParameterException("Something's wrong with your string! LINT could have caught that.");
        }
        String lineFormatted = String.format(lineFormat, code);

        // Adding bold to the OTP
        Spannable sb = new SpannableString(lineFormatted);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), lineParamStartPos, lineParamStartPos + code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/

        // Accent color for L/M/N
        int color = ContextCompat.getColor(context, R.color.colorPrimary);

        // Creating intent for notification action
        Intent intent = new Intent(BroadcastListener.COPY_CODE);
        intent.putExtra("code", code);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // creating normal notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, context.getString(R.string.noti_channel_id))
                        .setSmallIcon(R.mipmap.ic_noti_key)
                        //.setSubText(sender) // next to app name
                        .setContentTitle(code)
                        .setContentText(sender)
                        .setColor(color)
                        .addAction(
                                R.drawable.ic_noti_copy,
                                context.getResources().getString(R.string.label_copy_code),
                                pendingIntent);

        // Generate unique id for notification
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean heads_up = settings.getBoolean(SharedSettings.KEY_HEADSUP_NOTIFICATIONS, false);
        if (heads_up && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            mBuilder.setSound(Uri.parse("android.resource://pl.revanmj.smspasswordnotifier/" + R.raw.silent));
        }

        // Show notification
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(id, mBuilder.build());

    }

    private static String insertSpaceInTheMiddle(String original){
        if (original.length() > 5 && original.length() % 2 == 0) {
            int distance = original.length() / 2;
            StringBuilder sb = new StringBuilder();
            char[] charArrayOfOriginal = original.toCharArray();
            for (int ch = 0; ch < charArrayOfOriginal.length; ch++) {
                if (ch % distance == 0 && ch > 0)
                    sb.append(' ').append(charArrayOfOriginal[ch]);
                else
                    sb.append(charArrayOfOriginal[ch]);
            }
            return sb.toString();
        } else
            return original;
    }
}
