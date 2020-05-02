package com.github.warren_bank.sms_automatic_reply_gps.event;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
                double lat       = location.getLatitude();
                double lon       = location.getLongitude();
                float  accuracy  = location.getAccuracy();
                float  speed     = location.getSpeed();
                String direction = getDirection(location);

                SmsManager sms   = SmsManager.getDefault();

                ArrayList<String> messages = new ArrayList<String>();

                messages.add(
                    truncateSmsMessage(
                        sms,
                        String.format(
                            Locale.US,
                            "Current Location:\n  Lat: %1$s\n  Lon: %2$s\n  Accuracy (meters): %3$s\n  Speed (meters/sec): %4$s%5$s",
                            lat,
                            lon,
                            accuracy,
                            speed,
                            direction
                        )
                    )
                );
                messages.add(
                    truncateSmsMessage(
                        sms,
                        String.format(
                            Locale.US,
                            "Google Maps:\nhttps://maps.google.com/?q=%1$s,%2$s",
                            lat,
                            lon
                        )
                    )
                );

                Log.i(TAG, "GPS location sent.\nto: " + recipient + "\n" + messages.get(0) + "\n" + messages.get(1));

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

        // ======
        // helper
        // ======

        private static String getDirection(Location location) {
            float bearing  = location.getBearing();
            float accuracy = (Build.VERSION.SDK_INT >= 26)
                ? location.getBearingAccuracyDegrees()
                : Float.MIN_VALUE;

            if (bearing  == 0.0f) return "";
            if (accuracy == 0.0f) return "";
            if (accuracy > 22.5f) return "";

            // https://web.archive.org/web/20190315031943/http://snowfence.umn.edu/Components/winddirectionanddegreeswithouttable3.htm
            // https://gist.github.com/epierpont/fa53abc7092fc6dd16d78b8c0db9fa5a

            final String[] quadrant_names = new String[]{ "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

            int quadrant_index;
            quadrant_index = (int) ((bearing / 22.5f) + 0.5f);
            quadrant_index = quadrant_index % 16;

            String degree_range = (accuracy < 1.0f)
                ? String.format(
                    "%1$s",
                    (int) bearing
                  )
                : String.format(
                    "%1$s-%2$s",
                    (int) formatBearing(bearing - accuracy),
                    (int) formatBearing(bearing + accuracy)
                  );

            String direction = String.format(
                "\n  Direction: %1$s (%2$s deg)",
                quadrant_names[quadrant_index],
                degree_range
            );

            return direction;
        }

        private static float formatBearing(float degrees) {
            return ((360.0f + degrees) % 360.0f);
        }

        private static String truncateSmsMessage(SmsManager sms, String message) {
            ArrayList<String> parts = sms.divideMessage(message);
            return parts.get(0);
        }
    }
}
