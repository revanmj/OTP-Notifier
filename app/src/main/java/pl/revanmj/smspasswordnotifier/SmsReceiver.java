package pl.revanmj.smspasswordnotifier;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Created by revanmj on 13.01.2017.
 */

public class SmsReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = SmsReceiver.class.getSimpleName();

    public static final String COPY_CODE = "pl.revanmj.smspasswordnotifier.COPY_CODE";

    private Handler mHandler;

    public SmsReceiver() {
        super();
        HandlerThread handlerThread = new HandlerThread("SmsProcessingThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            // Just in case some OEM allowed this broadcast to be sent without permission being granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(LOG_TAG, "We've got SMS broadcast, yet permission is not granted!");
                return;
            }
            try {
                if (bundle != null) {
                    // A PDU is a "protocol data unit". This is the industrial standard for SMS message
                    final Object[] pdus = (Object[]) bundle.get("pdus");
                    for (final Object pdu : pdus) {
                        mHandler.post(() -> {
                            SmsMessage sms = null;
                            // This will create an SmsMessage object from the received pdu
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                sms = SmsMessage.createFromPdu((byte[]) pdu, "3gpp");
                            } else {
                                sms = SmsMessage.createFromPdu((byte[]) pdu);
                            }
                            MessageProcessor.processSms(context, sms);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (COPY_CODE.equals(intent.getAction())) {
            if (bundle != null) {
                String code = bundle.getString("code");
                MessageProcessor.copyCode(context, code);
            }
        } else if ("pl.revanmj.smspasswordnotifier.TEST_NOTI".equals(intent.getAction()))
            MessageProcessor.showNotification(context, "123456", "ExampleSender");
    }
}
