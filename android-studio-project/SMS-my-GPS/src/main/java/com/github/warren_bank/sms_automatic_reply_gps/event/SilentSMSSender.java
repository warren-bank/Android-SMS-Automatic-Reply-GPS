package com.github.warren_bank.sms_automatic_reply_gps.event;

import android.telephony.SmsManager;
import android.util.Log;

public final class SilentSMSSender {
    private static final String TAG = "SilentSMSSender";

    public static boolean send(String phoneNum) {
        try {
            final byte[] payload = new byte[]{0x0A, 0x06, 0x03, (byte) 0xB0, (byte) 0xAF, (byte) 0x82, 0x03, 0x06, 0x6A, 0x00, 0x05};

            SmsManager sms = SmsManager.getDefault();
            sms.sendDataMessage(phoneNum, null, (short) 9200, payload, null, null);
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "Error sending Silent SMS to " + phoneNum, e);
            return false;
        }
    }
}
