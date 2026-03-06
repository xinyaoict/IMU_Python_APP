package com.llui.idrink.Recorders;

import static com.xsens.dot.android.sdk.models.DotPayload.PAYLOAD_TYPE_COMPLETE_EULER;

import android.content.Context;
import android.util.Log;

import com.llui.idrink.Models.Measurement;
import com.llui.idrink.Models.Patient;
import com.llui.idrink.Utils.FileManager;
import com.llui.idrink.Utils.Utils;
import com.xsens.dot.android.sdk.BuildConfig;
import com.xsens.dot.android.sdk.events.DotData;
import com.xsens.dot.android.sdk.interfaces.DotDeviceCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.utils.DotLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.xsens.dot.android.sdk.models.DotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION;

/**
 * This class is responsible for managing the recording process of sensor data.
 * It handles starting and stopping the recording, setting measurement modes,
 * and updating loggers with new data.
 */
public class MySensorRecorder {

    public static final String KEY_ADDRESS = "address", KEY_TAG = "tag", KEY_DATA = "data", KEY_LOGGER = "logger";

    String TAG = MySensorRecorder.class.getSimpleName();
    ArrayList<DotDevice> connectedDevices;
    List<HashMap<String, Object>> mLoggerList = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> mDataList = new ArrayList<>();

    private boolean isLogging = false;
    private Map<String, Long> sensorsStartTime = new HashMap<>();
    private Thread recordingThread = null;


    public MySensorRecorder(ArrayList<DotDevice> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    public void startRecording(Context context, Patient patient, Measurement measurement) {
        if (recordingThread != null && recordingThread.isAlive()) {
            Log.w(TAG, "Recording is already running");
            return;
        }

        recordingThread = new Thread(() -> {
            // ✅ 设置每个 device 为 quaternion 模式
            for (DotDevice device : connectedDevices) {
                device.setMeasurementMode(PAYLOAD_TYPE_COMPLETE_QUATERNION);
                Log.d(TAG, "✅ Quaternion mode set for device: " + device.getAddress());
            }

            Utils.syncWithOtherRecorder();
            setMeasurement(true);
            createLoggers(context, patient, connectedDevices, measurement);
        });
        recordingThread.start();
    }

    public void stopRecording() {
        if (recordingThread != null) {
            stopLoggers();
            setMeasurement(false);
            isLogging = false;
            recordingThread.interrupt();
            recordingThread = null;
        }
    }

    private void setupDevicesForQuaternion() {
        if (connectedDevices != null) {
            for (DotDevice device : connectedDevices) {
                device.setMeasurementMode(PAYLOAD_TYPE_COMPLETE_QUATERNION);
                Log.d(TAG, "✅ Quaternion mode set for device: " + device.getAddress());
            }
        }
    }

    private void setMeasurement(boolean enabled) {
        if (connectedDevices != null) {
            for (DotDevice device : connectedDevices) {
                if (enabled) {
                    device.startMeasuring();
                    Log.d(TAG, "Measurement started for device: " + device.getAddress());
                } else {
                    device.stopMeasuring();
                    Log.d(TAG, "Measurement stopped for device: " + device.getAddress());
                }
            }
        }
    }

    private void createLoggers(Context context, Patient patient, ArrayList<DotDevice> devices, Measurement measurement) {
        mLoggerList.clear();
        for (DotDevice device : devices) {
            String filePath = FileManager.getSensorFilePath(context, patient, measurement, device);

            DotLogger logger = new DotLogger(
                    context,
                    DotLogger.TYPE_CSV,
                    PAYLOAD_TYPE_COMPLETE_EULER,
                    filePath,
                    device.getTag(),
                    device.getFirmwareVersion(),
                    device.isSynced(),
                    device.getCurrentOutputRate(),
                    Utils.getFilterProfileName(device),
                    BuildConfig.VERSION_NAME,
                    0);

            // Use mac address as a key to find logger object.
            HashMap<String, Object> map = new HashMap<>();
            map.put(KEY_ADDRESS, device.getAddress());
            map.put(KEY_LOGGER, logger);
            mLoggerList.add(map);
        }
        isLogging = true;

    }

    private void updateLoggers(String address, DotData data) {
        for (HashMap<String, Object> map : mLoggerList) {
            String _address = (String) map.get(KEY_ADDRESS);
            if (_address != null) {

                if (_address.equals(address)) {

                    DotLogger logger = (DotLogger) map.get(KEY_LOGGER);
                    if (logger != null && isLogging) logger.update(data);
                }
            }
        }
    }

    private void stopLoggers() {
        for (HashMap<String, Object> map : mLoggerList) {
            DotLogger logger = (DotLogger) map.get(KEY_LOGGER);
            if (logger != null) {
                logger.stop();
                Log.d(TAG, "Logger stopped for device: " + map.get(KEY_ADDRESS));
            }
        }
        mLoggerList.clear();
    }

    public void setCallback(DotDeviceCallback dotDeviceCallback) {
        for (DotDevice device : connectedDevices) {
            device.setDotDeviceCallback(dotDeviceCallback);
        }
    }
    public void onDataChanged(String address, DotData data) {
        boolean isExist = false;

        synchronized (mDataList) {
            for (HashMap<String, Object> map : mDataList) {

                String _address = (String) map.get(KEY_ADDRESS);
                if (_address.equals(address)) {
                    // If the data is exist, try to update it.
                    map.put(KEY_DATA, data);
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                DotDevice device = Utils.getDotDeviceByAddress(connectedDevices,address);
                // It's the first data of this sensor, create a new set and add it.
                String tag = device.getTag().isEmpty() ? device.getName() : device.getTag();
                HashMap<String, Object> map = new HashMap<>();
                map.put(KEY_ADDRESS, address);
                map.put(KEY_DATA, data);
                map.put(KEY_TAG, tag);
                mDataList.add(map);
            }
        }
        updateLoggers(address, data);
    }
    public  ArrayList<HashMap<String, Object>> getDataList() {return this.mDataList;}
}
