package com.llui.idrink.Interfaces;
/**
 * Interface definition for a callback to be invoked when an error occurs with Dot devices.
 * This is used when the state of the device changes during the recording.
 */

public interface DotDeviceErrorInterface {
    void onError();
}
