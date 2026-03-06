package com.llui.idrink.Adapters;

import static com.xsens.dot.android.sdk.models.DotDevice.BATT_STATE_CHARGING;
import static com.xsens.dot.android.sdk.models.DotDevice.CONN_STATE_CONNECTED;
import static com.xsens.dot.android.sdk.models.DotDevice.CONN_STATE_CONNECTING;
import static com.xsens.dot.android.sdk.models.DotDevice.CONN_STATE_DISCONNECTED;
import static com.xsens.dot.android.sdk.models.DotDevice.CONN_STATE_RECONNECTING;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.llui.idrink.Interfaces.DeviceCallback;
import com.llui.idrink.Interfaces.DeviceDataInterface;
import com.llui.idrink.Interfaces.DotDeviceErrorInterface;
import com.llui.idrink.Interfaces.SensorClickInterface;
import com.llui.idrink.Interfaces.SyncInterface;
import com.llui.idrink.R;
import com.xsens.dot.android.sdk.events.DotData;
import com.xsens.dot.android.sdk.models.DotDevice;

import java.util.ArrayList;
/**
 * DotDeviceAdapter is a RecyclerView adapter that binds DotDevice objects to views for display in a list.
 * It supports different item layouts based on the 'isSmall' flag and handles interaction callbacks for
 * sensor click events, data changes, sync status updates, and errors.
 */
public class DotDeviceAdapter extends RecyclerView.Adapter<DotDeviceAdapter.ScanViewHolder> implements DeviceCallback {

    // The application context
    private final Context mContext;

    // Send the click event to fragment
    private SensorClickInterface mListener;
    private DeviceDataInterface dataInterface;
    private SyncInterface syncInterface;
    private DotDeviceErrorInterface errorInterface;

    // Put all scanned devices into one list
    private LiveData<ArrayList<DotDevice>> mSensorList;
    private final boolean isSmall;

    public DotDeviceAdapter(Context context, LiveData<ArrayList<DotDevice>> scannedSensorList, boolean isSmall) {
        this.isSmall = isSmall;
        mContext = context;
        mSensorList = scannedSensorList;
        setCallback();
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int sensorItem;
        if (isSmall) { sensorItem = R.layout.item_sensor_small;
        } else {
            sensorItem = R.layout.item_sensor;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(sensorItem, parent, false);
        return new ScanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {

        final ArrayList<DotDevice> devices = mSensorList.getValue();
        DotDevice device = devices.get(position);

        if (device != null) {
            setConnectionUI(holder, device);
            setBatteryUI(holder,device);
            setTagUI(holder,device);
            setClick(holder);
            setSync(holder, device);
        }

    }

    @Override
    public int getItemCount() {
        if (mSensorList.getValue() == null) {
            return 0;
        } else {
            return mSensorList.getValue().size();
        }
    }
    public int getItemConnectedCount() {
        if (mSensorList.getValue() == null) {
            return 0;
        }

        int connectedCount = 0;
        for (DotDevice device : mSensorList.getValue()) {
            if (device.getConnectionState() == CONN_STATE_CONNECTED) {
                connectedCount++;
            }
        }

        return connectedCount;
    }
    private void setBatteryUI(@NonNull ScanViewHolder holder, DotDevice device) {
        int batteryPercentage = device.getBatteryPercentage();
        int batteryState = device.getBatteryState();

        String batteryStr = "";
        if (batteryPercentage != -1)
            batteryStr = batteryPercentage + "% ";
        if (batteryState == BATT_STATE_CHARGING)
            batteryStr = batteryStr + mContext.getString(R.string.batt_state_charging);
        holder.sensorBattery.setText(batteryStr);

    }
    private void setTagUI(@NonNull ScanViewHolder holder, DotDevice device) {
        String tag = device.getTag().isEmpty() ? device.getName() : device.getTag();
        holder.sensorTag.setText(tag);
    }
    private void setConnectionUI(@NonNull ScanViewHolder holder, DotDevice device) {
        int state = device.getConnectionState();
        switch (state) {

            case CONN_STATE_DISCONNECTED:
                holder.sensorImage.setImageResource(R.drawable.sensor_logo_gray);
                break;

            case CONN_STATE_RECONNECTING:


            case CONN_STATE_CONNECTING:
                holder.sensorImage.setImageResource(R.drawable.sensor_logo_blue);
                break;

            case CONN_STATE_CONNECTED:

                holder.sensorImage.setImageResource(R.drawable.sensor_logo_green);
                break;

        }
    }
    private void setSync(@NonNull ScanViewHolder holder, DotDevice device) {
        if (device.isSynced()) {
            holder.sensorSyncIcon.setVisibility(View.VISIBLE);

        } else {
            holder.sensorSyncIcon.setVisibility(View.INVISIBLE);

        }
    }
    private void setClick(@NonNull ScanViewHolder holder) {
        holder.rootView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && mListener != null) {
                    mListener.onSensorClick(v, currentPosition);
                }
            }
        });
    }
    public void setDataInterface(DeviceDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }
    public void setSyncInterface(SyncInterface syncInterface) {
        this.syncInterface = syncInterface;
    }
    public void setErrorInterface(DotDeviceErrorInterface errorInterface) {
        this.errorInterface = errorInterface;
    }
    public void setSensorClickListener(SensorClickInterface listener) {
        mListener = listener;
    }
    private void setCallback() {
        if (mSensorList.getValue()!= null) {
            ArrayList<DotDevice> devices = mSensorList.getValue();
            for (DotDevice device : mSensorList.getValue()) {
                    device.setDotDeviceCallback(this);
            }
        }

    }
    @Override
    public void onDotConnectionChanged(String s, int i) {
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(() -> {
            notifyDataSetChanged();
            if (errorInterface != null) {
                errorInterface.onError();
            }
            if (i == CONN_STATE_DISCONNECTED) {
                Toast.makeText(activity, "Device disconnected: " + s, Toast.LENGTH_SHORT).show();
                if (syncInterface != null) {
                    syncInterface.onDeviceNotReadyToSync(getItemConnectedCount());
                }
            }
        });
    }

    @Override
    public void onDotTagChanged(String s, String s1) {
        Log.i("MEGATEST", "Tag changed to " + s1);
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(() -> {
            notifyDataSetChanged();
        });
    }

    @Override
    public void onDotBatteryChanged(String s, int i, int i1) {
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(() -> {
            notifyDataSetChanged();
        });
    }

    @Override
    public void onDotDataChanged(String s, DotData dotData) {
        Log.i("DotDeviceCallback", "onDotDataChanged() - address = " + s + " - time stamp " + (dotData.getSampleTimeFine()));
        if (dataInterface!= null) {
            dataInterface.onDataChanged(s,dotData);
        }
    }
    @Override
    public void onDotInitDone(String s) {
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(() -> {
            notifyDataSetChanged();
            if (syncInterface != null) {
                syncInterface.onDeviceReadyToSync(getItemConnectedCount());
            }
        });
        // TODO: TAG input doesn't work?
        //showTagInputDialog(getDotDeviceByAddress(s));
    }

    @Override
    public void onSyncStatusUpdate(String s, boolean b) {
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(() -> {
            notifyDataSetChanged();
        });
        Log.i("Test", "Status changed " + b);
    }

    /**
     * A Customized class for ViewHolder of RecyclerView.
     */
    static class ScanViewHolder extends RecyclerView.ViewHolder {

        View rootView;
        TextView sensorTag;
        TextView sensorBattery;
        ImageView sensorImage;
        ImageView sensorSyncIcon;


        ScanViewHolder(View v) {

            super(v);

            rootView = v;
            sensorSyncIcon = v.findViewById(R.id.sync_icon);
            sensorTag = v.findViewById(R.id.sensor_tag);
            sensorBattery = v.findViewById(R.id.sensor_battery);
            sensorImage = v.findViewById((R.id.sensor_image));
        }
    }
    private void showTagInputDialog(DotDevice device) {
        // Create a Spinner for input
        final Spinner spinner = new Spinner(mContext);

        // Define the sensor tags
        String[] sensorTags = {"left hand", "left shoulder", "thorax", "right hand", "right shoulder"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, sensorTags);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Create the AlertDialog
        new AlertDialog.Builder(mContext)
                .setTitle("Choose Sensor Tag")
                .setMessage("Please select the tag of the sensor:")
                .setView(spinner)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String sensorTag = (String) spinner.getSelectedItem();
                        device.setTag(sensorTag);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }
    private DotDevice getDotDeviceByAddress(String address) {
        // Check if mSensorList is not null
        if (mSensorList == null || mSensorList.getValue() == null) {
            return null;
        }

        // Get the list of DotDevices
        ArrayList<DotDevice> sensorList = mSensorList.getValue();

        // Iterate through the list to find the device with the specified address
        for (DotDevice device : sensorList) {
            if (device.getAddress().equals(address)) {
                return device; // Return the found device
            }
        }

        // Return null if no device with the specified address is found
        return null;
    }

    public String getSensorTagByAddress(String address) {
        if (mSensorList == null || mSensorList.getValue() == null) return null;
        for (DotDevice device : mSensorList.getValue()) {
            if (device.getAddress().equals(address)) {
                return device.getTag();  // 返回对应设备的tag
            }
        }
        return null;
    }



}
