package com.github.warren_bank.sms_automatic_reply_gps.event;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

public final class GPSSender {
    private static final String TAG = "GPSSender";

    public static void notify(Context context, String recipient) {
        LocationManager LM = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!LM.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return;

        LM.requestSingleUpdate(
            LocationManager.GPS_PROVIDER,
            new SMSLocationListener(recipient),
            null
        );
    }

    private static final class SMSLocationListener implements LocationListener {
        String recipient;

        public SMSLocationListener(String to) {
            recipient = to;
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                double lat      = location.getLatitude();
                double lon      = location.getLongitude();
                float  accuracy = location.getAccuracy();
                float  speed    = location.getSpeed();

                ArrayList<String> messages = new ArrayList<String>();

                messages.add(
                    String.format(
                        Locale.US,
                        "Current Location:\n  Lat: %1$s\n  Lon: %2$s\n  Accuracy (meters): %3$s\n  Speed (meters/sec): %4$s",
                        lat,
                        lon,
                        accuracy,
                        speed
                    )
                );
                messages.add(
                    String.format(
                        Locale.US,
                        "Google Maps:\nhttps://www.google.com/maps/search/?api=1&query=%1$s,%2$s",
                        lat,
                        lon
                    )
                );

                Log.i(TAG, "GPS location sent.\nto: " + recipient + "\n" + messages.get(0) + "\n" + messages.get(1));

                SmsManager sms = SmsManager.getDefault();
                sms.sendMultipartTextMessage(recipient, null, messages, null, null);
            }
            catch (Exception e) {
                Log.e(TAG, "Error sending SMS containing GPS location", e);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }           
    }
}
