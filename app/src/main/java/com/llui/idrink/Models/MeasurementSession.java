package com.llui.idrink.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.llui.idrink.Enums.MeasurementStatus;
import com.xsens.dot.android.sdk.models.DotDevice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
/**
 * MeasurementSession represents a session containing multiple measurements with associated metadata.
 * It includes details such as speed, aid type, start timestamp, and a main comment.
 */
public class MeasurementSession {
    private final ArrayList<Measurement> measurements;
    private final String startTimestamp;
    private final LiveData<ArrayList<DotDevice>> sensorList;
    private String mainComment;
    private boolean isCameraEnabled;


    public MeasurementSession(LiveData<ArrayList<DotDevice>> syncedSensors, String mainComment, boolean isCameraEnabled){
        this.sensorList = syncedSensors;
        this.mainComment = mainComment;
        this.measurements = new ArrayList<>();
        this.startTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        this.isCameraEnabled = isCameraEnabled;
    }

    public ArrayList<Measurement> getMeasurements() {
        return measurements;
    }

    public String getLayoutTitle(){
        return "iDRINK : "+ getNbIMUs() + " IMUS " + isCameraEnabledtoString() ;
    }
    public String getHistoricalTitle() {return getNbIMUs()+ " IMUS, " +measurementsTaken() +" Measurements taken, "+ isCameraEnabledtoString();}
    public int getNbIMUs() {
        if (sensorList.getValue() == null) {
            return 0;
        }
        return sensorList.getValue().size();
    }
    public String getStartTimestamp(){return startTimestamp;}
    public String getMainComment() {return mainComment;}
    public boolean isCameraEnabled() {return isCameraEnabled;}
    public String isCameraEnabledtoString() {
        if (isCameraEnabled) {
            return " Camera Enabled";
        } else {
            return " Camera Disabled";
        }
    }
    public void setMainComment(String comment) {this.mainComment = comment;}
    public LiveData<ArrayList<DotDevice>> getConnectedSensors() {
        ArrayList<DotDevice> connectedDevices = new ArrayList<>();

        if (this.sensorList != null && this.sensorList.getValue() != null) {
            for (DotDevice device : this.sensorList.getValue()) {
                if (device.getConnectionState()==DotDevice.CONN_STATE_CONNECTED) {
                    connectedDevices.add(device);
                }
            }
        }
        MutableLiveData<ArrayList<DotDevice>> liveData = new MutableLiveData<>();
        liveData.setValue(connectedDevices);
        return liveData;
    }
    public boolean areAllDevicesConnected() {
        if (this.sensorList != null && this.sensorList.getValue() != null) {
            for (DotDevice device : this.sensorList.getValue()) {
                if (device.getConnectionState() != DotDevice.CONN_STATE_CONNECTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public LiveData<ArrayList<DotDevice>> getSensorList() {return this.sensorList;}
    public void addMeasurement() {
        measurements.add(new Measurement(this));
    }
    public int measurementsTaken() {
        for (int idx = 0; idx < measurements.size(); idx++) {
            Measurement measurement = measurements.get(idx);
            if (measurement.getStatus() == MeasurementStatus.NOT_STARTED) {
                return idx;
            }
        }
        return measurements.size(); // If all measurements are started, return the total count
    }
    public void releaseSensors() {
        if (sensorList.getValue()!= null) {
            for (DotDevice sensor : sensorList.getValue()) {
                sensor.disconnect();
            }
        }

    }

}
