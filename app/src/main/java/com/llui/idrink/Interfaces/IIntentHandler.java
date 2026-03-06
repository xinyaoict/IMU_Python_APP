package com.llui.idrink.Interfaces;

import android.content.Intent;
/**
 * Interface defining methods for preparing an intent and processing received intents.
 * Used in BaseActivity
 */
public interface IIntentHandler {

    void prepareIntent(Intent intent);

    void processReceivedIntent(Intent intent);
}
