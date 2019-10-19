package com.github.warren_bank.sms_automatic_reply_gps.data_model;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;

public final class Preferences {
    private static final String PREFS_FILENAME = "PREFS";
    private static final String PREF_ENABLED   = "ENABLED";
    private static final String PREF_LISTITEMS = "LISTITEMS";

    public static boolean isEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefs_editor = sharedPreferences.edit();
        prefs_editor.putBoolean(PREF_ENABLED, enabled);
        prefs_editor.apply();
    }

    public static ArrayList<ListItem> getListItems(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(PREF_LISTITEMS, null);

        return (json == null)
          ? new ArrayList<ListItem>()
          : ListItem.fromJson(json)
        ;
    }

    public static void setListItems(Context context, ArrayList<ListItem> listItems) {
        String json = ListItem.toJson(listItems);

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefs_editor = sharedPreferences.edit();
        prefs_editor.putString(PREF_LISTITEMS, json);
        prefs_editor.apply();
    }
}
