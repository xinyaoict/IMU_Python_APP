package com.llui.idrink.Interfaces;
/**
 * Interface definition for callbacks to be invoked during synchronization events.
 */
public interface SyncInterface {
    void onDeviceReadyToSync(int devicesNb);
    void onDeviceNotReadyToSync(int devicesNb);
    void syncResult(boolean allSynced);
}
