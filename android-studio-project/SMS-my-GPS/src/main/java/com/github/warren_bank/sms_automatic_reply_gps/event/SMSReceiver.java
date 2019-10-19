package com.github.warren_bank.sms_automatic_reply_gps.event;

import com.github.warren_bank.sms_automatic_reply_gps.R;
import com.github.warren_bank.sms_automatic_reply_gps.data_model.ListItem;
import com.github.warren_bank.sms_automatic_reply_gps.data_model.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.ArrayList;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG          = "SMSReceiver";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public void onReceive(Context context, Intent intent) {
        if (!Preferences.isEnabled(context))
            return;

        final String action = intent.getAction();
        final Bundle extras = intent.getExtras();

        if (extras == null)
            return;

        final ArrayList<ListItem> listItems = Preferences.getListItems(context);

        if (listItems.isEmpty())
            return;

        if (action.equals(SMS_RECEIVED)) {
            final SmsMessage[] messages = get_SmsMessages(extras);

            boolean is_match = false;
            String sender    = null;
            String body;

            for (SmsMessage message : messages) {
                if (message == null)
                    continue;

                try {
                    sender   = message.getOriginatingAddress().trim();
                    body     = message.getMessageBody().trim();
                    is_match = ListItem.matches(listItems, sender, body);

                    Log.i(TAG, "SMS received.\nfrom: " + sender + "\nmessage: " + body);

                    if (is_match)
                        break;
                }
                catch (Exception e) { continue; }
            }

            if (is_match)
                GPSSender.notify(context, sender);
        }
    }

    private final static SmsMessage[] get_SmsMessages(Bundle extras) {
        final Object[] pdus = (Object[])extras.get("pdus");
        final String format = extras.getString("format", "3gpp");
        final SmsMessage[] messages = new SmsMessage[pdus.length];

        for (int i = 0; i < pdus.length; i++) {
            try {
                messages[i] = (Build.VERSION.SDK_INT >= 23)
                    ? SmsMessage.createFromPdu((byte[])pdus[i], format)
                    : SmsMessage.createFromPdu((byte[])pdus[i]);
            }
            catch (Exception e) {}
        }
        return messages;
    }
}
