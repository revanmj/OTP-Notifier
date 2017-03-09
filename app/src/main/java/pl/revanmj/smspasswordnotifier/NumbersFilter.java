package pl.revanmj.smspasswordnotifier;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by revanmj on 13.01.2017.
 */

public class NumbersFilter {
    private static final String LOG_TAG = "NumbersFilter";

    private static final Set<String> NUMBERS_SET = new HashSet<String>() {{
        add("PayPal");
        add("Info");
        add("AUTHMSG");
    }};

    public static boolean shouldExtractPassword(String address) {
        Log.d(LOG_TAG, "shouldExtractPassword - address[" + address + "]");
        if (NUMBERS_SET.contains(address))
            return true;

        return false;
    }

    public static void processSms(Context context, SmsMessage sms) {
        String phoneNumber = sms.getDisplayOriginatingAddress();

        if (!shouldExtractPassword(phoneNumber)) {
            Log.d(LOG_TAG, "processSms - shouldExtractPassword returned false, exiting...");
            return;
        }

        String message = sms.getDisplayMessageBody();
        String code = CodeExtractor.extractCode(message);

        if (code == null) {
            Log.d(LOG_TAG, "processSms - extracted code is null, exiting...");
            return;
        }

        copyCode(context, code);
        showNotification(context, code, phoneNumber);

    }

    private static void copyCode(Context context, String code) {
        // Copy code to the clipboard
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("someLabel",code);
        clipboard.setPrimaryClip(clip);
    }

    private static void showNotification(Context context, String code, String sender) {
        // Instert space in the middle of the code (if 6 digits or longer) for better readability
        code = insertSpaceInTheMiddle(code);

        // Prepare formatted string with notification content text
        String lineFormat = sender + ": %s";
        int lineParamStartPos = lineFormat.indexOf("%s");
        if (lineParamStartPos < 0) {
            throw new InvalidParameterException("Something's wrong with your string! LINT could have caught that.");
        }
        String lineFormatted = String.format(lineFormat, code);

        // Addin bold to the OTP
        Spannable sb = new SpannableString(lineFormatted);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), lineParamStartPos, lineParamStartPos + code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Accent color for L/M/N
        int color = ContextCompat.getColor(context, R.color.colorPrimary);

        // creating normal notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_noti_key)
                        .setContentTitle(context.getString(R.string.noti_title))
                        .setContentText(sb)
                        .setColor(color);

        // Show notification
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(001, mBuilder.build());

    }

    public static String insertSpaceInTheMiddle(String original){
        if (original.length() > 5 && original.length() % 2 == 0) {
            int distance = original.length() / 2;
            StringBuilder sb = new StringBuilder();
            char[] charArrayOfOriginal = original.toCharArray();
            for (int ch = 0; ch < charArrayOfOriginal.length; ch++) {
                if (ch % distance == 0)
                    sb.append(' ').append(charArrayOfOriginal[ch]);
                else
                    sb.append(charArrayOfOriginal[ch]);
            }
            return sb.toString();
        } else
            return original;
    }
}
