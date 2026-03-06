package com.llui.idrink.Interfaces;

import com.xsens.dot.android.sdk.interfaces.DotDeviceCallback;
import com.xsens.dot.android.sdk.models.FilterProfileInfo;

import java.util.ArrayList;
/**
 * DeviceCallback extends DotDeviceCallback from the used SDK in order to used default methods
 * for the methods that are needed for this context.
 */
public interface DeviceCallback extends DotDeviceCallback {

    default void onDotServicesDiscovered(String s, int i) {}

    default void onDotFirmwareVersionRead(String s, String s1) {}

    default void onDotButtonClicked(String s, long l) {}

    default void onDotPowerSavingTriggered(String s) {}

    default void onReadRemoteRssi(String s, int i) {}

    default void onDotOutputRateUpdate(String s, int i) {}

    default void onDotFilterProfileUpdate(String s, int i) {}

    default void onDotGetFilterProfileInfo(String s, ArrayList<FilterProfileInfo> arrayList) {}

}
