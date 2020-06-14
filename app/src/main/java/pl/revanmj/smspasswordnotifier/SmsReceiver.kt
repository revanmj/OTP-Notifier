package pl.revanmj.smspasswordnotifier

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

import androidx.core.content.ContextCompat

/**
 * Created by revanmj on 13.01.2017.
 */

class SmsReceiver : BroadcastReceiver() {
    companion object {
        private val LOG_TAG = SmsReceiver::class.java.simpleName
        const val ACTION_COPY_CODE = "pl.revanmj.smspasswordnotifier.COPY_CODE"
        const val ACTION_TEST_NOTI = "pl.revanmj.smspasswordnotifier.TEST_NOTI"
        const val KEY_PDUS = "pdus"
    }

    private val mHandler: Handler

    init {
        val handlerThread = HandlerThread("SmsProcessingThread")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                // Just in case some OEM allowed this broadcast to be sent without permission being granted
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(LOG_TAG, "We've got SMS broadcast, yet permission is not granted!")
                    return
                }
                try {
                    if (bundle?.get(KEY_PDUS) != null) {
                        // A PDU is a "protocol data unit". This is the industrial standard for SMS message
                        val pdus : Array<ByteArray> = bundle.get(KEY_PDUS) as Array<ByteArray>
                        pdus.forEach {
                            mHandler.post {
                                // This will create an SmsMessage object from the received pdu
                                val sms: SmsMessage = SmsMessage.createFromPdu(it, "3gpp")
                                MessageProcessor.processSms(context, sms)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ACTION_COPY_CODE -> {
                val code = bundle?.getString("code")
                if (code != null) {
                    MessageProcessor.copyCode(context, code)
                }
            }
            ACTION_TEST_NOTI -> {
                MessageProcessor.showNotification(context, "123456", "ExampleSender")
            }
        }
    }
}
