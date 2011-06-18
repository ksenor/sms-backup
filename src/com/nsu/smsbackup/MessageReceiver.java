package com.nsu.smsbackup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.nsu.smsbackup.mail.Postman;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author andy
 */
public class MessageReceiver extends BroadcastReceiver {

    private static final String TAG = "com.nsu.smsbackup.MessageReceiver";

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called");

        if (!ACTION.equals(intent.getAction()))
            return;

        final Bundle bundle = intent.getExtras();
        if (bundle == null)
            return;

        final SmsMessage[] messages = readMessages(bundle);
        Log.d(TAG, "Got messages: " + messages.length);
        if (messages.length == 0)
            return;

        // get account details
        final SharedPreferences prefs = context.getSharedPreferences("smsbackup", MODE_PRIVATE);
        final String username = prefs.getString("email", null);
        final String password = prefs.getString("password", null);
        if (username == null || password == null) {
            Log.e(TAG, "Account details unspecified");
            return;
        }

        final Postman postman = new Postman(username, password);

        for (SmsMessage message : messages) {
            // send e-mail with message contents to the current user from himself
            postman.backup(message.getOriginatingAddress(), message.getMessageBody());
        }
    }

    private SmsMessage[] readMessages(Bundle bundle) {
        final Object[] pdus = (Object[]) bundle.get("pdus");
        final SmsMessage[] messages = new SmsMessage[pdus.length];

        for (int i = 0; i < messages.length; i++)
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

        return messages;
    }
}