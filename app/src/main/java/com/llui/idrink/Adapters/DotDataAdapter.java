package com.llui.idrink.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.llui.idrink.R;
import com.xsens.dot.android.sdk.events.DotData;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * DotDataAdapter is a RecyclerView adapter that binds DotData objects to views for display in a list.
 * It uses a list of HashMaps to store sensor data and formats orientation and free acceleration data
 * to two decimal places for clarity.
 */
public class DotDataAdapter extends RecyclerView.Adapter<DotDataAdapter.DataViewHolder> {


    // The keys of HashMap
    public static final String KEY_ADDRESS = "address", KEY_TAG = "tag", KEY_DATA = "data";

    // Put all data from sensors into one list
    private final ArrayList<HashMap<String, Object>> mDataList;

    public DotDataAdapter(ArrayList<HashMap<String, Object>> dataList) {
        mDataList = dataList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new DataViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {

        String tag = (String) mDataList.get(position).get(KEY_TAG);
        DotData xsData = (DotData) mDataList.get(position).get(KEY_DATA);

        holder.sensorName.setText(tag);

        double[] eulerAngles = xsData.getEuler();
         String eulerAnglesStr =
                String.format("%.2f", eulerAngles[0]) + ", " +
                        String.format("%.2f", eulerAngles[1]) + ", " +
                        String.format("%.2f", eulerAngles[2]);
        holder.orientationData.setText(eulerAnglesStr);

        float[] freeAcc = xsData.getFreeAcc();
        String freeAccStr =
                String.format("%.2f", freeAcc[0]) + ", " +
                        String.format("%.2f", freeAcc[1]) + ", " +
                        String.format("%.2f", freeAcc[2]);
        holder.freeAccData.setText(freeAccStr);

    }

    @Override
    public int getItemCount() {

        return mDataList == null ? 0 : mDataList.size();
    }

    /**
     * A Customized class for ViewHolder of RecyclerView.
     */
    static class DataViewHolder extends RecyclerView.ViewHolder {

        View rootView;
        TextView sensorName;
        TextView orientationData;
        TextView freeAccData;

        DataViewHolder(View v) {

            super(v);

            rootView = v;
            sensorName = v.findViewById(R.id.sensor_name);
            orientationData = v.findViewById(R.id.orientation_data);
            freeAccData = v.findViewById(R.id.free_acc_data);
        }
    }
}
