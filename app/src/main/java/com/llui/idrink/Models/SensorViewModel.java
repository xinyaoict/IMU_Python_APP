package com.llui.idrink.Models;

import static com.xsens.dot.android.sdk.models.DotDevice.CONN_STATE_CONNECTED;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.llui.idrink.Interfaces.SyncCallback;
import com.llui.idrink.Interfaces.SyncInterface;
import com.xsens.dot.android.sdk.interfaces.DotDeviceCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.DotSyncManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/**
 * ViewModel responsible for managing sensor data and synchronization state.
 */

public class SensorViewModel extends ViewModel implements SyncCallback {

    private static final String TAG = SensorViewModel.class.getSimpleName();
    private static final int SYNCING_REQUEST_CODE = 1001;

    // A variable to queue multiple threads.
    private static final Object LOCKER = new Object();

    // A callback function to notify battery information
    // A callback function to notify data changes event

    private SyncInterface syncInterface;

    // A list contains XsensDotDevice
    private MutableLiveData<ArrayList<DotDevice>> mSensorList = new MutableLiveData<>();
    private Boolean mIsSyncing = false;
    public void setSyncInterface(SyncInterface syncInterface) {
        this.syncInterface = syncInterface;
    }

    public boolean isSyncing() {
        return mIsSyncing;
    }


    // Method to get LiveData of sensor list
    public MutableLiveData<ArrayList<DotDevice>> getAllSensorsLiveData() {return mSensorList;}
    // Method to get the number of connected devices
    public ArrayList<DotDevice> getConnectedDevices() {
        ArrayList<DotDevice> devices = mSensorList.getValue();
        if (devices == null) {
            return new ArrayList<>();
        }

        ArrayList<DotDevice> connectedDevices = new ArrayList<>();
        for (DotDevice device : devices) {
            if (device.getConnectionState() == DotDevice.CONN_STATE_CONNECTED) {
                connectedDevices.add(device);
            }
        }
        return connectedDevices;
    }
    public LiveData<ArrayList<DotDevice>> getSyncedDevices() {
        MutableLiveData<ArrayList<DotDevice>> syncedDevicesLiveData = new MutableLiveData<>();
        ArrayList<DotDevice> devices = mSensorList.getValue();
        if (devices == null) {
            devices = new ArrayList<>();  // Set an empty ArrayList to LiveData
        }
        ArrayList<DotDevice> syncedDevices = new ArrayList<>();
        for (DotDevice device : devices) {
            if (device.getConnectionState() == DotDevice.CONN_STATE_CONNECTED && device.isSynced()) {
                syncedDevices.add(device);
            }
        }
        syncedDevicesLiveData.setValue(syncedDevices);
        return syncedDevicesLiveData;
    }

    // Method to handle a scanned device
    public void handleScannedDevice(Context context, BluetoothDevice device, DotDeviceCallback dotDeviceCallback) {
        ArrayList<DotDevice> devices = mSensorList.getValue();
        if (devices == null) {
            devices = new ArrayList<>();
            mSensorList.setValue(devices);
        }

        boolean isExist = false;
        for (DotDevice dotDevice : devices) {
            if (dotDevice.getAddress().equals(device.getAddress())) {
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            DotDevice dotDevice = new DotDevice(context, device,dotDeviceCallback);
            devices.add(dotDevice);
            mSensorList.setValue(devices);
            Log.d(TAG, "Device added: " + device.getAddress());
        }
    }
    public void addDotDevice(DotDevice device) {
        ArrayList<DotDevice> devices = mSensorList.getValue();
        if (devices == null) {
            devices = new ArrayList<>();
            mSensorList.setValue(devices);
        }
        devices.add(device);
        mSensorList.setValue(devices);

    }

    /**
     * Get the DotDevice object from list by mac address.
     *
     * @param address The mac address of device
     * @return The XsensDotDevice object
     */
    public DotDevice getSensor(String address) {

        final ArrayList<DotDevice> devices = mSensorList.getValue();

        if (devices != null) {

            for (DotDevice device : devices) {

                if (device.getAddress().equals(address)) return device;
            }
        }

        return null;
    }


    /**
     * Get all XsensDotDevice objects from list.
     *
     * @return The list contains all devices
     */
    public ArrayList<DotDevice> getAllSensors() {

        if (mSensorList.getValue() == null) {
            return new ArrayList<>();
        }
        else return mSensorList.getValue();
    }

    public void connectSensor(DotDevice device) {
        device.connect();
    }

    public void disconnectSensor(DotDevice device) {
        if (device != null) {
            device.disconnect();
        }
    }

    /**
     * Disconnect all devices which are exist in the list.
     */
    public void disconnectAllSensors() {

        if (mSensorList.getValue() != null) {

            synchronized (LOCKER) {

                for (Iterator<DotDevice> it = mSensorList.getValue().iterator(); it.hasNext(); ) {
                    // Use Iterator to make sure it's thread safety.
                    DotDevice device = it.next();
                    device.disconnect();
                }
            }
        }
    }

    /**
     * Cancel reconnection of one sensor.
     *
     * @param address The mac address of device
     */
    public void cancelReconnection(String address) {

        if (mSensorList.getValue() != null) {

            for (DotDevice device : mSensorList.getValue()) {

                if (device.getAddress().equals(address)) {

                    device.cancelReconnecting();
                    break;
                }
            }
        }
    }

    /**
     * Size of the connected sensors list
     * @return size of the connected list
     */
    public int getSizeList(){
        if(mSensorList.getValue() == null)
            return 0;
        else
            return mSensorList.getValue().size();
    }

    /**
     * Check the connection state of all sensors.
     *
     * @return True - If all sensors are connected
     */
    public boolean checkConnection() {

        final ArrayList<DotDevice> devices = mSensorList.getValue();

        if (devices != null) {

            for (DotDevice device : devices) {

                final int state = device.getConnectionState();
                if (state != CONN_STATE_CONNECTED) return false;
            }

        } else {

            return false;
        }

        return true;
    }

    /**
     * Get the tag name from sensor.
     *
     * @param address The mac address of device
     * @return The tag name
     */
    public String getTag(String address) {

        DotDevice device = getSensor(address);

        if (device != null) {

            String tag = device.getTag();
            return tag == null ? device.getName() : tag;
        }

        return "";
    }

    /**
     * Set the plotting and logging states for each device.
     *
     * @param plot The plot state
     * @param log  The log state
     */
    public void setStates(int plot, int log) {

        final ArrayList<DotDevice> devices = mSensorList.getValue();

        if (devices != null) {

            for (DotDevice device : devices) {

                device.setPlotState(plot);
                device.setLogState(log);
            }
        }
    }

    public void removeDisconnectedDevices() {
        List<DotDevice> sensorListValue = mSensorList.getValue();

        if (sensorListValue == null) {
            sensorListValue = new ArrayList<>(); // Initialize as empty list
        }

        ArrayList<DotDevice> devices = new ArrayList<>(sensorListValue);

        Iterator<DotDevice> iterator = devices.iterator();
        while (iterator.hasNext()) {
            DotDevice device = iterator.next();
            int state = device.getConnectionState();
            if (state == DotDevice.CONN_STATE_DISCONNECTED) {
                iterator.remove(); // Remove from temporary list
            }
        }

        mSensorList.setValue(devices); // Update LiveData with modified list
    }


    /**
     * Set outrate for each sensor
     * @param rate egal to 60hz
     */
    public void setOutputRate(int rate){
        final ArrayList<DotDevice> connectedDevices = getConnectedDevices();

        if (connectedDevices != null) {

            for (DotDevice device : connectedDevices) {

                device.setOutputRate(rate);
            }
        }
    }
    /**
     * Remove all sensor from device list directly.
     */
    public void removeAllDevice() {

        if (mSensorList.getValue() != null) {

            synchronized (LOCKER) {

                mSensorList.getValue().clear();
            }
        }
    }
    public void startSync() {
        ArrayList<DotDevice> connectedDevices = getConnectedDevices();
        if (!connectedDevices.isEmpty()) {
            connectedDevices.get(0).setRootDevice(true);
        }
        DotSyncManager.getInstance(this).startSyncing(connectedDevices,
                SYNCING_REQUEST_CODE);
        mIsSyncing = true;
    }
    public void stopSync() {
        DotSyncManager.getInstance(this).stopSyncing();
        mIsSyncing = false;
    }

    @Override
    public void onSyncingStarted(String s, boolean b, int i) {
        syncInterface.onDeviceReadyToSync(getConnectedDevices().size());
    }

    @Override
    public void onSyncingDone(HashMap<String, Boolean> syncStatusMap, boolean allSynced, int requestCode) {
        mIsSyncing = false;
        syncInterface.syncResult(allSynced);
    }

    public boolean isAllSynced() {

        return getConnectedDevices().size() == getSyncedDevices().getValue().size();
    }

}
