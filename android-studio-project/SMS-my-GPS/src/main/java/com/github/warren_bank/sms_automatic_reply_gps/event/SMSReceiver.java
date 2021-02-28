package com.github.warren_bank.sms_automatic_reply_gps.event;

import com.github.warren_bank.sms_automatic_reply_gps.R;
import com.github.warren_bank.sms_automatic_reply_gps.data_model.ListItem;
import com.github.warren_bank.sms_automatic_reply_gps.data_model.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG               = "SMSReceiver";
    private static final String TEXT_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String DATA_SMS_RECEIVED = "android.intent.action.DATA_SMS_RECEIVED";

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

        SmsMessage[] messages = null;
        String sender         = null;
        String body           = null;
        boolean is_match      = false;

        if (!is_match && action.equals(TEXT_SMS_RECEIVED)) {
            messages = get_SmsMessages(extras);

            for (SmsMessage message : messages) {
                if (message == null)
                    continue;

                try {
                    sender   = message.getOriginatingAddress().trim();
                    body     = message.getMessageBody().trim();
                    is_match = ListItem.matches(listItems, sender, body);  // must match at least one whitelist rule ('phone number' and 'message prefix' pair)

                    if (is_match)
                        break;
                }
                catch (Exception e) { continue; }
            }

            if (is_match) {
                Log.i(TAG, "SMS received.\nfrom: " + sender + "\nmessage: " + body);

                SmsManager sms   = SmsManager.getDefault();

                ArrayList<String> sendMessages = new ArrayList<String>();

                sendMessages.add("I've got your message, looking for satellites...");

                Log.i(TAG, "Ack msg sent.\nto: " + sender + "\n" + sendMessages.get(0));

                sms.sendMultipartTextMessage(sender, null, sendMessages, null, null);

                GPSSender.notify(context, sender);
            }
        }

        if (!is_match && action.equals(DATA_SMS_RECEIVED)) {
            messages = get_SmsMessages(extras);

            byte[] data;
            StringBuilder data_sb;

            for (SmsMessage message : messages) {
                if (message == null)
                    continue;

                try {
                    data = message.getUserData();
                    if ((data == null) || (data.length == 0)) continue;
                    if (data.length != 11) continue;

                    data_sb = new StringBuilder();
                    for (byte b : data) {
                        data_sb.append(String.format("%02x", b));
                    }
                    body = data_sb.toString().toLowerCase();
                    if (!body.equals("0a0603b0af8203066a0005")) continue;

                    sender   = message.getOriginatingAddress().trim();
                    is_match = ListItem.matches(listItems, sender);  // must originate from a whitelisted phone number (excluding globs)

                    if (is_match)
                        break;
                }
                catch (Exception e) { continue; }
            }

            if (is_match) {
                Log.i(TAG, "Silent SMS received.\nfrom: " + sender);

                GPSSender.notify(context, sender);
            }
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
