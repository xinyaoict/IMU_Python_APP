package com.llui.idrink.Utils;

import android.util.Log;

public class DebugLogger {

    public static void debugLog(String tag, String content) {
        Log.d(tag, content);
    }

    public static void debugLog(String content) {
        Log.d("MYTEST", content);
    }
}
