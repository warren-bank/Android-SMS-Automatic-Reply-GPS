package com.github.warren_bank.sms_automatic_reply_gps.security_model;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.ArrayList;

public final class RuntimePermissions {
    private static final int REQUEST_CODE = 0;

    public static boolean isEnabled(Activity activity) {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        final String[] permissions_all = new String[]{ "android.permission.RECEIVE_SMS", "android.permission.ACCESS_FINE_LOCATION", "android.permission.SEND_SMS" };

        ArrayList<String> permissions_req = new ArrayList<String>();

        for (String permission_name : permissions_all) {
            if (activity.checkSelfPermission(permission_name) != PackageManager.PERMISSION_GRANTED) {
                permissions_req.add(permission_name);
            }
        }

        if (permissions_req.isEmpty())
            return true;

        activity.requestPermissions(
            permissions_req.toArray(new String[0]),
            REQUEST_CODE
        );
        return false;
    }

    public static void onRequestPermissionsResult (Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_CODE)
            return;

        if (grantResults.length == 0)
            return;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return;
        }

        activity.recreate();
    }
}
