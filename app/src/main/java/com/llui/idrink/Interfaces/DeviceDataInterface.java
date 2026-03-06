package com.llui.idrink.Interfaces;

import com.xsens.dot.android.sdk.events.DotData;
/**
 * Interface definition for a callback to be invoked when sensor data changes.
 */
public interface DeviceDataInterface {
    void onDataChanged(String address, DotData data);
}
