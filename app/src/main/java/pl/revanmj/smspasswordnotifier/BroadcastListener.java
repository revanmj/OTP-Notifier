package pl.revanmj.smspasswordnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by revanmj on 13.01.2017.
 */

public class BroadcastListener extends BroadcastReceiver {
    private static final String NEW_SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String COPY_CODE = "pl.revanmj.smspasswordnotifier.COPY_CODE";
    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();

        if (intent.getAction().equals(NEW_SMS_ACTION)) {
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

                        // Process message
                        MessageProcessor mp = new MessageProcessor(context);
                        mp.processSms(context, sms);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(COPY_CODE)) {
            if (bundle != null) {
                String code = bundle.getString("code");
                MessageProcessor.copyCode(context, code);
            }
        } else if (intent.getAction().equals("pl.revanmj.smspasswordnotifier.TEST_NOTI"))
            MessageProcessor.showNotification(context, "123456", "ExampleSender");
    }
}
