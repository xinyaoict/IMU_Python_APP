package com.llui.idrink.Interfaces;

import com.xsens.dot.android.sdk.interfaces.DotSyncCallback;

import java.util.HashMap;
/**
 * Interface definition for a callback to be invoked during synchronization events.
 * Extends DotSyncCallback from the SDK in order to give default methods for the methods
 * not needed in this context.
 */

public interface SyncCallback extends DotSyncCallback {

    default void onSyncingProgress(int i, int i1) {}

    default void onSyncingResult(String s, boolean b, int i) {}

    default void onSyncingStopped(String s, boolean b, int i) {}
}
