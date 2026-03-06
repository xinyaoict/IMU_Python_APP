package com.llui.idrink.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.llui.idrink.Interfaces.OnMeasurementClickListener;
import com.llui.idrink.Models.Measurement;
import com.llui.idrink.Enums.MeasurementStatus;
import com.llui.idrink.R;

import java.util.ArrayList;
/**
 * MeasurementAdapter is an Adapter used in the MeasurementSelectionActivity.
 * It represents a measurement of a specific measurement session.
 * It displays a green icon when completed in an acceptable time or a yellow icon when the recording is under a certain time.
 * When clicked it leads to the recording activity of the specific measurement.
 */
public class MeasurementAdapter extends ArrayAdapter<Measurement> {

    private ArrayList<Measurement> measurements;
    private OnMeasurementClickListener listener;


    public MeasurementAdapter(@NonNull Context context, ArrayList<Measurement> measurements, OnMeasurementClickListener listener) {
        super(context, 0, measurements);
        this.measurements = measurements;
        this.listener = listener;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_measurement_button, parent, false);
        }

        MaterialButton measurementButton = convertView.findViewById(R.id.measurementBtn);

        initMeasurementBtn(measurementButton, position);

        if (measurements.get(position).getStatus()!= MeasurementStatus.NOT_STARTED) {
            initCommentBtn(convertView, position);
        }
        if (position == measurements.size()-1 & measurements.get(position).getStatus()!=MeasurementStatus.NOT_STARTED) {
            initPlusButton(convertView, position);
        }

        return convertView;
    }
    private void initMeasurementBtn(MaterialButton button, int idx) {
        Measurement measurement = measurements.get(idx);
        MeasurementStatus measurementStatus = measurement.getStatus();
        button.setText(getContext().getString(R.string.trial, idx + 1));
        setMeasurementBtnClick(button, idx);
        setMeasurementBtnIcon(measurementStatus,button);
        button.setEnabled(isMeasurementBtnEnabled(idx));
    }
    private void initPlusButton(View itemView, int idx) {
        Button plusButton = itemView.findViewById(R.id.plusButton);
        plusButton.setVisibility(View.VISIBLE);
        setPlusBtnClick(plusButton,idx);
    }
    private void initCommentBtn(View itemView, int idx) {
        ImageButton commentButton = itemView.findViewById(R.id.commentButton);
        commentButton.setVisibility(View.VISIBLE);
        setCommentBtnClick(commentButton, idx);
    }
    private void setMeasurementBtnClick(MaterialButton button, int idx) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMeasurementClick(idx);
            }
        });
    }
    private void setCommentBtnClick(ImageButton button, int idx) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCommentClick(idx);           }
        });
    }
    private void setPlusBtnClick(Button button, int idx) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlusMeasurementClick(idx);
            }
        });
    }
    private void setMeasurementBtnIcon(MeasurementStatus measurementStatus, MaterialButton button) {
        switch (measurementStatus) {
            case COMPLETED:
                button.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.check_circle_24px)); // Set the tick icon
                button.setIconTint(ContextCompat.getColorStateList(getContext(), R.color.green));
                break;
            case PARTIAL_COMPLETED:
                button.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.question_mark_24px)); // Set the tick icon
                button.setIconTint(ContextCompat.getColorStateList(getContext(), R.color.yellow));
                break;
            case FAILED:
                button.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.fail_24px)); // Set the tick icon
                button.setIconTint(ContextCompat.getColorStateList(getContext(), R.color.red));                break;
            case NOT_STARTED:
                break;
        }
    }
    private boolean isMeasurementBtnEnabled(int idx) {
        if (idx == 0) {
            return true;
        }
        if (measurements.get(idx-1).getStatus() != MeasurementStatus.NOT_STARTED){
            return true;
        }
        return false;
    }
}
