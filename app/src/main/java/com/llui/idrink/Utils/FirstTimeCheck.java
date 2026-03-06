package com.llui.idrink.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class FirstTimeCheck {
    private static final String PREFS_NAME = "MyAppPreferences";
    private static final String KEY_FIRST_TIME = "isFirstTime";

    public static boolean isFirstTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_FIRST_TIME, true);
    }

    public static void setFirstTime(Context context, boolean isFirstTime) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_FIRST_TIME, isFirstTime);
        editor.apply();
    }
}
