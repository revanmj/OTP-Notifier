package pl.revanmj.smspasswordnotifier;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by revanmj on 13.01.2017.
 */

public class SMSListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {

                // A PDU is a "protocol data unit". This is the industrial standard for SMS message
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {

                    // This will create an SmsMessage object from the received pdu
                    SmsMessage sms = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        sms = SmsMessage.createFromPdu((byte[]) pdusObj[i], "3gpp");
                    } else {
                        sms = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    }

                    // Get sender phone number
                    String phoneNumber = sms.getDisplayOriginatingAddress();
                    if (NumbersFilter.shouldExtractPassword(phoneNumber)) {
                        String message = sms.getDisplayMessageBody();
                        String code = CodeExtractor.extractCode(message);

                        if (code == null)
                            return;

                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("someLabel",code);
                        clipboard.setPrimaryClip(clip);

                        // creating expandable notification
                        NotificationCompat.BigTextStyle notiStyle = new NotificationCompat.BigTextStyle();
                        notiStyle.setBigContentTitle(context.getString(R.string.noti_title));
                        notiStyle.setSummaryText(phoneNumber + ": " + code);

                        // creating normal notification
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.mipmap.ic_noti_key)
                                        .setContentTitle(context.getString(R.string.noti_title))
                                        .setContentText(phoneNumber + ": " + code)
                                        .setStyle(notiStyle);

                        // showing notification
                        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.notify(001, mBuilder.build());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
