package com.llui.idrink.Activities;

import static com.llui.idrink.Utils.FileManager.createJsonCatalogFile;
import static com.llui.idrink.Utils.PermissionUtils.checkBluetoothPermission;
import static com.llui.idrink.Utils.PermissionUtils.checkInternetPermission;
import static com.llui.idrink.Utils.PermissionUtils.checkReadExternalStoragePermission;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;

import com.llui.idrink.Adapters.DotDeviceAdapter;
import com.llui.idrink.Adapters.HistoricalAdapter;
import com.llui.idrink.Interfaces.CommentDialogListener;
import com.llui.idrink.Interfaces.OnHistoricalClickListener;
import com.llui.idrink.Interfaces.SensorClickInterface;
import com.llui.idrink.Interfaces.SyncInterface;
import com.llui.idrink.Models.MeasurementSession;
import com.llui.idrink.Models.SFTP;
import com.llui.idrink.Models.SensorViewModel;
import com.llui.idrink.R;
import com.llui.idrink.Utils.CommentDialogManager;
import com.llui.idrink.Utils.DialogUtils;
import com.llui.idrink.Utils.FileManager;
import com.llui.idrink.Utils.ScanButtonAnimator;
import com.llui.idrink.databinding.ActivityMenuBinding;
import com.xsens.dot.android.sdk.interfaces.DotScannerCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.utils.DotScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuActivity manages the main functionality and UI interactions for the menu screen of a medical measurement application.
 * It implements several interfaces: CommentDialogListener for handling comments, SensorClickInterface for sensor item clicks,
 * SyncInterface for synchronization status updates, and DotScannerCallback for Bluetooth device scanning.
 *
 * This activity initializes UI components, handles user interactions, and manages the lifecycle of sensor devices and data.
 * Key functionalities include:
 * - Displaying session information and patient details.
 * - Managing historical measurement sessions with options to navigate to detailed sessions.
 * - Connecting and disconnecting Bluetooth-enabled sensor devices via a RecyclerView adapter.
 * - Initiating and managing Bluetooth device scanning using DotScanner for sensor detection.
 * - Syncing sensor data and providing feedback on synchronization status.
 * - Handling user interactions for confirming, sending data, adding comments, scanning, and syncing sensors.
 * - Reacting to sensor clicks to connect/disconnect devices and handle tag inputs.
 * - Managing UI updates based on sensor scanning and synchronization states.
 * - Responding to voice input for adding comments and processing recognized text.
 *
 * Additionally, MenuActivity ensures proper resource cleanup on activity destroy to release sensor resources
 * and stop ongoing operations.
 */
public class MenuActivity extends BaseActivity implements CommentDialogListener, SensorClickInterface, SyncInterface, DotScannerCallback, OnHistoricalClickListener {
    private static final String TAG = MenuActivity.class.getSimpleName();

    private ActivityMenuBinding binding;
    private int activeSessionId;
    private CommentDialogManager commentDialogManager;
    private String mainComment = "";

    // Scanning Implementation
    private static final int SCANNING_TIME = 10000;
    private SensorViewModel mSensorViewModel;
    private DotDeviceAdapter mDotDeviceAdapter;
    private DotScanner mXsDotScanner;
    private ScanButtonAnimator scanButtonAnimator;
    private boolean mIsScanning = false;
    private boolean isCameraEnabled = true;


    @Override
    public void init() {
        checkInternetPermission(this);
        checkBluetoothPermission(this);
        checkReadExternalStoragePermission(this);
        initHeading();
        initScanButtonAnimator();
        initSensorViewModel();
        initDotDeviceAdapter();
        initScanner();
        initSendDataBtn();
        initHistoricalSessionList();
    }

    private void initScanButtonAnimator() {
        this.scanButtonAnimator = new ScanButtonAnimator(binding.scanButton,SCANNING_TIME);
    }

    private void initHeading(){
        binding.sessionInfo.setText(getString(R.string.measurement_session_info, activeSessionId + 1));
        binding.patientInfo.setText(getString(R.string.patient_info, patientInfo.getPatientId(), patientInfo.getCaseId()));
    }
    private void initSendDataBtn(){
        if (activeSessionId==0) {
            binding.sendDataButton.setVisibility(View.INVISIBLE);
        } else {
            binding.sendDataButton.setVisibility(View.VISIBLE);

        }
    }

    private void initHistoricalSessionList() {
        List<MeasurementSession> measurementSessionList = patientInfo.getMeasurementSessions();
        if (measurementSessionList.isEmpty()) {
            binding.noHistoryTxt.setVisibility(View.VISIBLE);
        }
        HistoricalAdapter adapter = new HistoricalAdapter(this, measurementSessionList, this);
        binding.historicalSessionList.setAdapter(adapter);
    }

    private void initDotDeviceAdapter() {
        mDotDeviceAdapter = new DotDeviceAdapter(this, mSensorViewModel.getAllSensorsLiveData(),false);
        mDotDeviceAdapter.setSensorClickListener(this);
        mDotDeviceAdapter.setSyncInterface(this);
        binding.sensorRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.sensorRecyclerView.setAdapter(mDotDeviceAdapter);
    }
    private void initSensorViewModel() {
        mSensorViewModel = new SensorViewModel();
        mSensorViewModel.setSyncInterface(this);
        if (activeSessionId > 0) {
            checkForConnectedDevices();
        }
    }
    private void initScanner() {
        // Check if Bluetooth is enabled
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            // Bluetooth is enabled
            initDotScanner();
        } else {
            // Bluetooth is disabled
            mIsScanning = false;
            binding.scanButton.setEnabled(false);
        }
        registerBluetoothListener();
    }
    private void initDotScanner() {

        if (mXsDotScanner == null) {

            mXsDotScanner = new DotScanner(this, this);
            mXsDotScanner.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop scanning to let other apps to use scan function.
        if (mXsDotScanner != null) mXsDotScanner.stopScan();
    }
    @Override
    public void listenBtn() {
        listenStartMeasurementSessionBtn();
        listenSendDataBtn();
        listenCommentBtn();
        listenScanBtn();
        listenSyncButton();
        listenHelpScanner();
        listenHelpHistorical();
        listenCamBtn();
    }
    private void listenCamBtn() {
        binding.noCamBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCameraEnabled = !isChecked;
                updateCameraImageView();
            }
        });
    }
    private void listenStartMeasurementSessionBtn(){
        binding.startMeasurementSession.setOnClickListener(v -> {
            if (mSensorViewModel.getSyncedDevices().getValue().isEmpty() && !isCameraEnabled) {
                Toast.makeText(this, "You need to connect sensors or enable the camera to start a measurement session.", Toast.LENGTH_LONG).show();
            } else if (mSensorViewModel.getConnectedDevices().isEmpty()) {
                showNoImuDialog();
            }
            else if (mSensorViewModel.isSyncing() || !mSensorViewModel.isAllSynced()) {
                showNotAllImuSyncedDialog();
            }else {
                goToMeasurementSession();
            }
        });
    }
    private void listenHelpScanner() {
        binding.helpButtonScanner.setOnClickListener(v -> {
            DialogUtils.showScannerDialog(this);
        });
    }
    private void listenHelpHistorical() {
        binding.helpButtonHistorical.setOnClickListener(v -> {
            DialogUtils.showHistoricalDialog(this);
        });
    }
    private void goToMeasurementSession() {
        MeasurementSession measurementSession = createNewMeasurementSession();
        patientInfo.addNewMeasurementSessions(measurementSession);
        navigateToNextActivity(MeasurementSelectionActivity.class);
    }
    private void listenSendDataBtn() {
        binding.sendDataButton.setOnClickListener(v -> {
            showConfirmSendDataDialog(this);
        });
    }
    private void listenCommentBtn(){
        binding.commentButton.setOnClickListener(v -> {
            commentDialogManager = new CommentDialogManager(this,this, mainComment);
            commentDialogManager.showCommentDialog(-1);

        });
    }
    private void listenScanBtn() {
        binding.scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanning();
            }
        });
    }
    private void listenSyncButton() {
        binding.syncButton.setOnClickListener(view -> {
            mSensorViewModel.setOutputRate(60);
            mSensorViewModel.startSync();
        });
    }

    private MeasurementSession createNewMeasurementSession(){
        return new MeasurementSession(mSensorViewModel.getSyncedDevices(), mainComment, isCameraEnabled);
    }

    @Override
    public void setBinding() {
        binding = ActivityMenuBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }

    @Override
    public void prepareIntent(Intent intent) {
        intent.putExtra("activeSessionId", activeSessionId);
    }

    @Override
    public void processReceivedIntent(Intent intent) {
        activeSessionId = intent.getIntExtra("activeSessionId", 0);
    }

    @Override
    public void onCommentSubmitted(String comment, int index) {
        mainComment = comment;
        binding.commentTxt.setText(comment);
        binding.commentTxt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorClick(View v, int position) {
        // Get the position of the clicked item in the RecyclerView
        int clickedPosition = binding.sensorRecyclerView.getChildAdapterPosition(v);
        // // TODO : Tag input??
        // Get the DotDevice object corresponding to the clicked position
        DotDevice clickedDevice = mSensorViewModel.getAllSensors().get(clickedPosition);
        // Log the clicked device's information
        Log.d(TAG, "Clicked device: " + clickedDevice.getName() + ", Address: " + clickedDevice.getAddress() + ", Connection state: " + clickedDevice.getConnectionState());
        // Check if the clicked device is already connected
        if (clickedDevice.getConnectionState() == DotDevice.CONN_STATE_CONNECTED) {
            // If already connected, disconnect the device
            Log.d(TAG, "Device is already connected. Disconnecting...");
            mSensorViewModel.disconnectSensor(clickedDevice);
        } else {
            // If not connected, connect the device
            Log.d(TAG, "Device is not connected. Connecting...");
            mSensorViewModel.connectSensor(clickedDevice);
        }
    }
    private void startScanning() {
        mSensorViewModel.stopSync();
        resetScanner();
        updateConnectTxt();
        startScan();
        scanButtonAnimator.startAnimation();
        updateScanningUi();
    }
    private void updateScanningUi() {
        if (mIsScanning) {
            binding.scanButton.setText(R.string.scanning);
            binding.scanButton.setEnabled(false);
        } else {
            binding.scanButton.setText(R.string.scan);
            binding.scanButton.setEnabled(true);
        }

    }
    private void resetScanner() {
        mSensorViewModel.disconnectAllSensors();
        mSensorViewModel.removeAllDevice();
        mDotDeviceAdapter.notifyDataSetChanged();
    }
    private void startScan() {
        // Start scan
        mIsScanning = mXsDotScanner.startScan();
        Handler handler = new Handler(Looper.getMainLooper());
        // Schedule a runnable to stop scanning after SCANNING_TIME milliseconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsScanning) {
                    stopScan();
                    updateScanningUi();
                }
            }
        }, SCANNING_TIME);
    }
    private void stopScan() {
        // If success for stopping, it will return True from SDK. So use !(not) here.
        mIsScanning = !mXsDotScanner.stopScan();

    }

    private void showNoImuDialog() {

        // Create the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("No IMUs synced")
                .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        goToMeasurementSession();
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
    private void showNotAllImuSyncedDialog() {

        // Create the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Some IMUs are not synced")
                .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        goToMeasurementSession();
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
    private void showConfirmSendDataDialog(Context context) {
        new AlertDialog.Builder(this)
                .setTitle("Send Data")
                .setMessage("Are you sure you want to send all the data recorded so far?\nOnce sent you will be logged out.")
                .setPositiveButton("Send Data", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        patientInfo.setStopTime();
                        createJsonCatalogFile(MenuActivity.this, patientInfo);

                        new Thread(() -> {
                            SFTP sftp = new SFTP();
                            boolean uploadSuccessful = sftp.uploadFile(getApplicationContext(), patientInfo, FileManager.getSessionFolder(MenuActivity.this, patientInfo));
                            if (uploadSuccessful) {
                                runOnUiThread(() -> navigateToNextActivity(LoginActivity.class));
                            }
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void onSensorTagEntered(String sensorTag, int clickedPosition) {
        // Handle the sensor tag input
        Toast.makeText(this, "Sensor Tag: " + sensorTag, Toast.LENGTH_SHORT).show();
        // Add your logic here to process the sensor tag
        DotDevice clickedDevice = mSensorViewModel.getAllSensors().get(clickedPosition);
        clickedDevice.setTag(sensorTag);
        mDotDeviceAdapter.notifyItemChanged(clickedPosition);
    }


    @Override
    public void onDeviceReadyToSync(int devicesNb) {
        binding.syncButton.setVisibility(View.VISIBLE);
        updateSyncButtonTxt(devicesNb);
        if (devicesNb == 0 && !mSensorViewModel.isSyncing()) {
            binding.syncButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeviceNotReadyToSync(int devicesNb) {
        updateSyncButtonTxt(devicesNb);
        if (devicesNb == 0 && !mSensorViewModel.isSyncing()) {
            binding.syncButton.setVisibility(View.GONE);
        }
    }

    private void updateConnectTxt() {
        if (mDotDeviceAdapter.getItemCount() == 0) {
            binding.connectTxt.setVisibility(View.GONE);
        } else {
            binding.connectTxt.setVisibility(View.VISIBLE);
        }
    }
    private void updateSyncButtonTxt(int devicesNb){
        if (mSensorViewModel.isSyncing()) {
            binding.syncButton.setText(R.string.syncing);
            binding.syncButton.setEnabled(false);
        } else if (mSensorViewModel.isAllSynced()) {
            binding.syncButton.setText(R.string.sync_completed);
            binding.syncButton.setEnabled(false);
        } else {
            String deviceTxt;
            if (devicesNb == 1) {
                deviceTxt = " device";
            } else {
                deviceTxt = " devices";
            }
            String newText = "Sync" + " " + devicesNb + deviceTxt;
            binding.syncButton.setText(newText);
            binding.syncButton.setEnabled(true);
        }
    }
    private void updateSyncButtonVisibility(int devicesNb) {
        if (devicesNb == 0 && !mSensorViewModel.isSyncing()) {
            binding.syncButton.setVisibility(View.GONE);
        } else {
            binding.syncButton.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void syncResult(final boolean allSynced) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!allSynced) {
                    Toast.makeText(MenuActivity.this, "Syncing failed, please try again.", Toast.LENGTH_SHORT).show();
                }
                onDeviceReadyToSync(mDotDeviceAdapter.getItemConnectedCount());
            }
        });
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onDotScanned(BluetoothDevice device, int rssi) {
        updateConnectTxt();
        mSensorViewModel.handleScannedDevice(this, device,mDotDeviceAdapter);
        mDotDeviceAdapter.notifyDataSetChanged();
        Log.d("onDotScanned", "Device Name: " + device.getName() + "Device Address: " + device.getAddress());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                commentDialogManager.setText(recognizedText);
            }
        }
    }

    @Override
    public void onHistoricalClick(int position) {
        activeSessionId = position;
        navigateToNextActivity(MeasurementSelectionActivity.class);
    }
    private void registerBluetoothListener() {
        // Register a BroadcastReceiver to listen for Bluetooth state changes
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            // Bluetooth has been turned on
                            binding.scanButton.setEnabled(true);
                            // Optionally, start scanning
                            initDotScanner();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            // Bluetooth has been turned off
                            binding.scanButton.setEnabled(false);
                            break;
                    }
                }
            }
        };
        registerReceiver(mReceiver, filter);
    }
    private void updateCameraImageView() {
        if (isCameraEnabled) {
            binding.camLogo.setImageResource(R.drawable.photo_camera_24px);
        } else {
            binding.camLogo.setImageResource(R.drawable.no_photography_24px);
        }
    }
    private void checkForConnectedDevices() {
        Log.i("MEGATEST", "sessions : " + patientInfo.getMeasurementSessions().size() + " " + activeSessionId);
        MeasurementSession lastSession = patientInfo.getActiveMeasurementSession(activeSessionId-1);
        ArrayList<DotDevice> connectedSensors = lastSession.getSensorList().getValue();

        if (!connectedSensors.isEmpty()) {
            for (DotDevice device : connectedSensors) {
                if (device.getConnectionState() == DotDevice.CONN_STATE_CONNECTED) {
                    mSensorViewModel.addDotDevice(device);
                }
            }
        }
    }
}
