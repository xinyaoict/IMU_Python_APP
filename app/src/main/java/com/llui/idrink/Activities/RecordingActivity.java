package com.llui.idrink.Activities;

import static com.llui.idrink.Utils.PermissionUtils.checkRecordAudioPermission;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.MediaController;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;

import com.llui.idrink.Adapters.DotDataAdapter;
import com.llui.idrink.Adapters.DotDeviceAdapter;
import com.llui.idrink.Interfaces.DeviceDataInterface;
import com.llui.idrink.Interfaces.DotDeviceErrorInterface;
import com.llui.idrink.Enums.DataType;
import com.llui.idrink.Models.Measurement;
import com.llui.idrink.Models.MeasurementSession;
import com.llui.idrink.Enums.MeasurementStatus;
import com.llui.idrink.R;
import com.llui.idrink.Utils.FileManager;
import com.llui.idrink.Recorders.MyAudioRecorder;
import com.llui.idrink.Recorders.MyInternalSensorManager;
import com.llui.idrink.Recorders.MySensorRecorder;
import com.llui.idrink.Recorders.MyVideoRecorder;
import com.llui.idrink.Recorders.RecordingSynchronizerManager;
import com.llui.idrink.databinding.ActivityRecordingBinding;
import com.xsens.dot.android.sdk.events.DotData;

import java.io.File;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;



/**
 * RecordingActivity manages the recording functionality for a medical measurement session,
 * integrating video, audio, and sensor data capture.
 * It extends BaseActivity and implements interfaces for handling device data and errors.
 *
 * This activity initializes various components including video, audio, and sensor recorders,
 * manages UI interactions such as starting/stopping recordings, and handles sensor data updates.
 * Key functionalities include:
 * - Initializing and managing video recording using MyVideoRecorder.
 * - Initializing and managing audio recording using MyAudioRecorder.
 * - Initializing and managing sensor data recording using MySensorRecorder.
 * - Managing UI elements like chronometer, record button, and checkbox for hiding sensor data.
 * - Handling user interactions for starting and stopping recordings, updating UI based on recording states.
 * - Managing Bluetooth device scanning and recording failures.
 * - Handling touch events on the preview view to mark patient positions during recording.
 * - Handling data changes and errors from connected devices and updating UI accordingly.
 * - Writing measurement data to files and managing measurement status based on recording outcomes.
 *
 * Additionally, RecordingActivity ensures proper resource initialization and cleanup,
 * maintains recording state flags, and provides feedback to the user during the recording process.
 */
public class RecordingActivity extends BaseActivity implements DeviceDataInterface, DotDeviceErrorInterface, RecordingSynchronizerManager.RecordingObserver {
    private ActivityRecordingBinding binding;

    private double[] qHand;
    private double[] qWrist;
    private LineChart angleChart;
    private LineData lineData;
    private int timeIndex = 0;

    // Time Management
    private static final String TRIAL_TIME = "00:05";
    private boolean isTrialTimePassed = false;
    private Chronometer chronometer;

    // Recorders
    private MyVideoRecorder videoRecorder;
    private MyAudioRecorder audioRecorder;
    private MyInternalSensorManager internalSensorManager;
    private MySensorRecorder sensorRecorder;

    // Data handling
    private int activeSessionId, activeMeasurementId;
    private Measurement measurement;
    private DotDeviceAdapter mDotDeviceAdapter;
    private DotDataAdapter mDotDataAdapter;
    private boolean hasRecordingFailed = false;
    private boolean isRecording = false;

    private LineDataSet angleDataSet;

    private String handAddress = null;
    private String wristAddress = null;

    private Handler handler = new Handler();
    private float fakeAngle = 0;

    // Init Methods
    @Override
    public void init() {
        checkRecordAudioPermission(this);
        initChronometer();
        initMeasurement();
        initDotDeviceAdapter();
        initHideDotDataCheckBox();
        initCameraLayout();
        initRecorders();
        if (isVideoPreview()) {
            initVideoPreview();
            initViewOptionsButtons();
        }
        setupAngleChart();
    }


    private void updateQuaternionData(String address, DotData data) {
        Log.d("IMU_DATA", "Received data from: " + address);

        double[] euler = data.getEuler();  // ← get Euler angles

        if (euler == null || euler.length != 3) {
            Log.w("IMU_DATA", "Invalid Euler angles");
            return;
        }
        Log.d("IMU_DATA", "Euler = [" + euler[0] + ", " + euler[1] + ", " + euler[2] + "]");

        if (address.equalsIgnoreCase(handAddress)) {
            qHand = euler;  // ← save euler
        } else if (address.equalsIgnoreCase(wristAddress)) {
            qWrist = euler;
        }

        if (qHand != null && qWrist != null) {
            Log.d("DEBUG_FLOW", "Both eulerHand and eulerWrist are ready");
            float angle = computeFlexionExtension(qHand, qWrist);  // transfer to Python to calculate
            Log.d("DEBUG_ANGLE", "Angle: " + angle);
            addAngleToChart(angle);
        }
    }



    private float computeFlexionExtension(double[] eulerHand, double[] eulerWrist) {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyModule = py.getModule("angle_calculator");

        // KeyPoint: use Python List not ArrayList!
        PyObject result = pyModule.callAttr("compute_flexion_extension", toArray(eulerHand), toArray(eulerWrist));

        return result.toFloat();
    }

    private float[] toArray(double[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (float) array[i];
        }
        return result;
    }




    private ArrayList<Float> toList(double[] array) {
        ArrayList<Float> list = new ArrayList<>();
        for (double v : array) {
            list.add((float) v);
        }
        return list;
    }


    private float quaternionToEulerY(float[] q) {
        double w = q[0], x = q[1], y = q[2], z = q[3];
        double siny = 2.0 * (w * y - z * x);
        double cosy = 1.0 - 2.0 * (y * y + x * x);
        return (float) Math.toDegrees(Math.atan2(siny, cosy));
    }



    private void addAngleToChart(float angle) {
        if (lineData == null || angleDataSet == null) return;
        long now = System.currentTimeMillis();
        float t = (now - startTime) / 1000f;
        angleDataSet.addEntry(new Entry(t, angle));
        lineData.notifyDataChanged();
        angleChart.notifyDataSetChanged();
        angleChart.moveViewToX(t);
        angleChart.invalidate();
        Log.d("CHART", "Updated chart at t=" + t + " angle=" + angle);
    }

    private long startTime;
    private void setupAngleChart() {
        angleChart = binding.angleChart;
        angleDataSet = new LineDataSet(new ArrayList<>(), "Wrist Angle (°)");
        angleDataSet.setDrawValues(false);
        angleDataSet.setDrawCircles(false);
        angleDataSet.setColor(Color.RED);

        lineData = new LineData(angleDataSet);
        angleChart.setData(lineData);
        angleChart.getXAxis().setDrawGridLines(true);
        angleChart.getAxisLeft().setDrawGridLines(true);
        angleChart.getAxisRight().setDrawGridLines(false);
        angleChart.getXAxis().setGridColor(Color.LTGRAY);
        angleChart.getAxisLeft().setGridColor(Color.LTGRAY);
        angleChart.getXAxis().setGridLineWidth(1f);
        angleChart.getAxisLeft().setGridLineWidth(1f);

        Description desc = new Description();
        desc.setText("Real-time Flexion/Extension");
        desc.setTextSize(16f);
        angleChart.setDescription(desc);
        angleChart.invalidate();
        startTime = System.currentTimeMillis();
    }




    private float computeFlexionAngle(float[] q1, float[] q2) {
        float dot = 0;
        for (int i = 0; i < 4; i++) {
            dot += q1[i] * q2[i];
        }
        dot = Math.min(1.0f, Math.max(-1.0f, dot));
        return (float) Math.toDegrees(2 * Math.acos(dot));
    }

    private void addEntryToChart(float angle) {
        LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);
        set.addEntry(new Entry(timeIndex++, angle));
        set.notifyDataSetChanged();
        lineData.notifyDataChanged();
        angleChart.notifyDataSetChanged();
        angleChart.invalidate();
    }

    private void initCameraLayout() {
        if (!patientInfo.getActiveMeasurementSession(activeSessionId).isCameraEnabled()) {
            binding.cameraLayout.setVisibility(View.INVISIBLE);
            binding.onePersonText.setVisibility(View.INVISIBLE);
        }
    }

    private void initRecorders() {
        if (isCameraEnabled()) {
            initVideoRecorder();
        }
        if (allowSensorRecording()) {
            initSensorRecorder();
        }
        initAudioRecorder();
        initInternalSensorManager();
        initRecorderSynchronizer();
    }

    private void initRecorderSynchronizer() {
        if (!allowSensorRecording() || !isCameraEnabled()) {
            RecordingSynchronizerManager.initialize(2);

        } else {
            RecordingSynchronizerManager.initialize(3);
        }
        RecordingSynchronizerManager.registerObserver(this);
    }

    private void initHideDotDataCheckBox() {
        if (!allowSensorRecording() || isPreview()) {
           binding.dataRecyclerView.setVisibility(View.GONE);
           binding.hideDotData.setVisibility(View.GONE);
        }
    }
    private void initViewOptionsButtons() {
        binding.viewOptionsGroup.setVisibility(View.VISIBLE);
        binding.viewOptionsGroup.check(R.id.radioLivePreview);
    }

    private void initChronometer() {
        chronometer = binding.chronometer;
        chronometer.setOnChronometerTickListener(chronometer -> {
            // Check if 30 seconds have passed
            if (chronometer.getText().toString().equals(TRIAL_TIME)) {
                if (!isTrialTimePassed) {
                    // Update chronometer text color or any other action needed
                    chronometer.setTextColor(Color.GREEN);
                    isTrialTimePassed = true;
                }
            }
        });
    }
    private void initMeasurement() {
        // if first time doing the measurement
        if (activeMeasurementId >= patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements().size()) {
            measurement = new Measurement(patientInfo.getActiveMeasurementSession(activeSessionId));
            patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements().add(measurement);
        } else {
            // if not first time retrieve
            measurement = patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements().get(activeMeasurementId);
        }

    }
    private void initDotDeviceAdapter() {
        mDotDeviceAdapter = new DotDeviceAdapter(this, patientInfo.getActiveMeasurementSession(activeSessionId).getSensorList(), true);
        mDotDeviceAdapter.setDataInterface(this);
        mDotDeviceAdapter.setErrorInterface(this);
        binding.sensorRecyclerView.setAdapter(mDotDeviceAdapter);
    }
    private void initDotDataAdapter() {
        mDotDataAdapter = new DotDataAdapter(sensorRecorder.getDataList());
        binding.dataRecyclerView.setAdapter(mDotDataAdapter);
    }
    private void initAudioRecorder() {audioRecorder = new MyAudioRecorder(getAudioFilePath());}
    private void initInternalSensorManager() {internalSensorManager = new MyInternalSensorManager(this);}
    private void initVideoRecorder() {
        videoRecorder = new MyVideoRecorder(this, this, binding.previewView, getVideoFile());
    }
    private void initVideoPreview() {
        binding.videoView.setMediaController(new MediaController(this));
        binding.videoView.setVideoURI(getVideoUri());
    }
    private void initSensorRecorder() {
        MeasurementSession activeMeasurementSession= patientInfo.getActiveMeasurementSession(activeSessionId);
        sensorRecorder = new MySensorRecorder(activeMeasurementSession.getConnectedSensors().getValue());
        sensorRecorder.setCallback(mDotDeviceAdapter);
        initDotDataAdapter();
    }

    // Button Methods
    @Override
    public void listenBtn() {
        listenRecordBtn();
        listenHideDotDataCheckBox();
        listenBackBtn();
        listenViewOptionsGroup();
    }
    private void listenRecordBtn() {
        binding.recordButton.setOnClickListener(v -> {
            if (!isRecording) {
                if (isPreview()) {
                    showStartRecordingWarningDialog();
                } else {
                    startRecording();
                }
            } else {
                stopRecording();
                FileManager.createJsonFile(RecordingActivity.this,patientInfo,measurement,activeSessionId);
                setMeasurementStatus();
                navigateToNextActivity(MeasurementSelectionActivity.class);
            }
        });
    }
    private void listenHideDotDataCheckBox() {
        binding.hideDotData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.dataRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.dataRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void listenViewOptionsGroup() {
        // Assuming `viewOptionsGroup` is your RadioGroup
        binding.viewOptionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if( checkedId ==  R.id.radioCapturedVideo) {
                    showRecordedVideoPreview();
                } else {
                    showLiveCameraPreview();
                }
            }
        });
    }

    private void listenBackBtn() {
        binding.backButton.setOnClickListener(v -> {
            navigateToNextActivity(MeasurementSelectionActivity.class);
        });

    }

    // Recording Methods
    private void startRecording() {
        if (isCameraEnabled()) {
            videoRecorder.startRecording();
        }
        if (allowSensorRecording()) {
            sensorRecorder.startRecording(this,patientInfo,measurement);
        }
        audioRecorder.startRecording();
        internalSensorManager.start(getSensorFilePath());
        //simulateAngleUpdates();
    }
    private void stopRecording() {
        if (isCameraEnabled()) {
            videoRecorder.stopRecording();
        }
        if (allowSensorRecording()) {
            sensorRecorder.stopRecording();
        }
        audioRecorder.stopRecording();
        internalSensorManager.stop();
        isRecording = false;
    }
    private void displayRecordingUI() {
        // Make sure no back button
        binding.viewOptionsGroup.setVisibility(View.GONE);
        if (allowSensorRecording()) {
            binding.hideDotData.setVisibility(View.VISIBLE);
        }
        binding.backButton.setEnabled(false);
        binding.redBorderView.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        binding.recordButton.setText(R.string.stop_recording);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }
    // Override & Utils Methods
    @Override
    public void setBinding() {
        binding = ActivityRecordingBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }

    @Override
    public void prepareIntent(Intent intent) {
        intent.putExtra("activeSessionId", activeSessionId);
    }

    @Override
    public void processReceivedIntent(Intent intent) {
        activeSessionId = intent.getIntExtra("activeSessionId", 0);
        activeMeasurementId = intent.getIntExtra("activeMeasurementId", 0);
    }

    private String getAudioFilePath() {
        Measurement measurement = patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements().get(activeMeasurementId);
        return FileManager.getFilePath(this, patientInfo, measurement, DataType.AUDIO);
    }
    private String getSensorFilePath() {
        Measurement measurement = patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements().get(activeMeasurementId);
        return FileManager.getFilePath(this, patientInfo, measurement, DataType.IMU);
    }

    private File getVideoFile() {
        Measurement measurement = patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements().get(activeMeasurementId);
        String filePath =  FileManager.getFilePath(this, patientInfo, measurement, DataType.VIDEO);
        return new File(filePath);
    }
    private Uri getVideoUri() {
        return Uri.fromFile(getVideoFile());
    }
    private boolean isPreview() {
        return measurement.getStatus() != MeasurementStatus.NOT_STARTED;
    }
    private boolean isVideoPreview() {
        return isPreview() && isCameraEnabled();
    }

    private void showStartRecordingWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!")
                .setMessage("Starting a new recording will delete the existing files. Do you want to continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startRecording();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onDataChanged(String address, DotData data) {
        String tag = mDotDeviceAdapter.getSensorTagByAddress(address);
        if (handAddress == null && tag != null && tag.toLowerCase().contains("hand")) {
            handAddress = address;
        } else if (wristAddress == null && tag != null && tag.toLowerCase().contains("wrist")) {
            wristAddress = address;
        }
        runOnUiThread(() -> {
            if (allowSensorRecording()) {
                sensorRecorder.onDataChanged(address, data);
            }
            updateQuaternionData(address, data);
            mDotDataAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        if (isRecording) {
            hasRecordingFailed = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            initAudioRecorder();
    }
    @Override
    public void onAllRecordersReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isRecording = true;
                displayRecordingUI();
            }
        });
    }

    private void showLiveCameraPreview() {
        stopVideoPlayback();
        binding.videoView.setVisibility(View.INVISIBLE);
        binding.previewView.setVisibility(View.VISIBLE);
        binding.recordButton.setVisibility(View.VISIBLE);
        binding.chronometer.setVisibility(View.VISIBLE);
        binding.onePersonText.setVisibility(View.VISIBLE);
        binding.previewView.setVisibility(View.VISIBLE);
        binding.previewView.requestFocus();
    }

    private void showRecordedVideoPreview() {
        binding.previewView.setVisibility(View.INVISIBLE);
        binding.recordButton.setVisibility(View.INVISIBLE);
        binding.chronometer.setVisibility(View.INVISIBLE);
        binding.onePersonText.setVisibility(View.INVISIBLE);

        binding.videoView.setVisibility(View.VISIBLE);
        binding.videoView.requestFocus();
        binding.videoView.start();
    }

    private void stopVideoPlayback() {
        if (binding.videoView.isPlaying()) {
            binding.videoView.stopPlayback();
            binding.videoView.setVideoURI(null); // Reset the URI to release resources
        }
    }

    private boolean allowSensorRecording() {
        return !patientInfo.getActiveMeasurementSession(activeSessionId).getConnectedSensors().getValue().isEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            patientInfo.getActiveMeasurementSession(activeSessionId).releaseSensors();
        }
        stopRecording();
        // Release resources
        if (videoRecorder != null) {
            videoRecorder = null;
        }
        if (audioRecorder != null) {
            audioRecorder = null;
        }
        if (sensorRecorder != null) {
            sensorRecorder = null;
        }
        if (internalSensorManager != null) {
            internalSensorManager = null;
        }
    }

    private boolean isCameraEnabled() {
        return patientInfo.getActiveMeasurementSession(activeSessionId).isCameraEnabled();
    }

    private void setMeasurementStatus(){
        if (hasRecordingFailed) {
            measurement.setStatus(MeasurementStatus.FAILED);
        } else if (isTrialTimePassed && patientInfo.getActiveMeasurementSession(activeSessionId).areAllDevicesConnected()) {
            measurement.setStatus(MeasurementStatus.COMPLETED);
        } else {
            measurement.setStatus(MeasurementStatus.PARTIAL_COMPLETED);
        }
    }

    private void simulateAngleUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fakeAngle += 5;
                addAngleToChart(fakeAngle);
                Log.d("DEBUG_FLOW", "Simulated angle = " + fakeAngle);
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

}
