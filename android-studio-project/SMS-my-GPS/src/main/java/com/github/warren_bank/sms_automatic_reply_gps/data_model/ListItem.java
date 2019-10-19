package com.github.warren_bank.sms_automatic_reply_gps.data_model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

public final class ListItem {
    public String sender;
    public String message_prefix;

    public ListItem(String sender, String message_prefix) {
        this.sender         = sender;
        this.message_prefix = message_prefix;
    }

    @Override
    public String toString() {
        return sender;
    }

    // helpers

    public static ArrayList<ListItem> fromJson(String json) {
        ArrayList<ListItem> arrayList;
        Gson gson = new Gson();
        arrayList = gson.fromJson(json, new TypeToken<ArrayList<ListItem>>(){}.getType());
        return arrayList;
    }

    public static String toJson(ArrayList<ListItem> arrayList) {
        String json = new Gson().toJson(arrayList);
        return json;
    }

    public static boolean matches(ArrayList<ListItem> arrayList, String sender, String message) {
        ListItem item;
        int prefix_length;
        String prefix;

        for (int i=0; i < arrayList.size(); i++) {
            try {
                item = arrayList.get(i);

                // required
                if (!item.sender.equals(sender))
                    continue;

                prefix_length = item.message_prefix.length();

                // optional
                if (prefix_length == 0)
                    return true;

                if (prefix_length > message.length())
                    continue;

                prefix = message.substring(0, prefix_length);

                if (item.message_prefix.equals(prefix))
                    return true;
            }
            catch(Exception e) { continue; }
        }
        return false;
    }
}
