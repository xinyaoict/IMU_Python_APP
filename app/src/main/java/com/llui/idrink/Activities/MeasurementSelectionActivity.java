package com.llui.idrink.Activities;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.llui.idrink.Adapters.MeasurementAdapter;
import com.llui.idrink.Adapters.DotDeviceAdapter;
import com.llui.idrink.Interfaces.CommentDialogListener;
import com.llui.idrink.Interfaces.OnMeasurementClickListener;
import com.llui.idrink.Models.MeasurementSession;
import com.llui.idrink.Enums.MeasurementStatus;
import com.llui.idrink.R;
import com.llui.idrink.Utils.CommentDialogManager;
import com.llui.idrink.Utils.DialogUtils;
import com.llui.idrink.Utils.FileManager;
import com.llui.idrink.databinding.ActivityMeasurementSelectionBinding;
import com.xsens.dot.android.sdk.models.DotDevice;

import java.util.ArrayList;
import java.util.Iterator;
/**
 * MeasurementSelectionActivity is launched either when a measurement session is started
 * or when a measurement session is selected from the historical.
 * It holds the list of measures in a measurement session with a visual feedback on the length of the measures
 * and the number measures done.
 * Clicking on a measurement button leads to the recording Activity.
 */
public class MeasurementSelectionActivity extends BaseActivity implements OnMeasurementClickListener, CommentDialogListener {
    private static final Object LOCKER = new Object();

    private static final int STANDARD_NB_OF_MEASUREMENTS = 3;
    private ActivityMeasurementSelectionBinding binding;

    private int activeSessionId, activeMeasurementId;
    private MeasurementSession measurementSession;
    private CommentDialogManager commentDialogManager;
    private DotDeviceAdapter mDotDeviceAdapter;
    @Override
    public void init() {
        initSession();
        initListView();
        initCompleteSessionBtn();
        initDotDeviceAdapter();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void initSession(){
        measurementSession = patientInfo.getActiveMeasurementSession(activeSessionId);
        binding.measurementSessionInfo.setText(measurementSession.getLayoutTitle());
        if (measurementSession.getMeasurements().size()<STANDARD_NB_OF_MEASUREMENTS) {
            for(int idx = 0; idx < STANDARD_NB_OF_MEASUREMENTS; idx++) {
                measurementSession.addMeasurement();
            }
        }

    }
    private void initDotDeviceAdapter() {
        mDotDeviceAdapter = new DotDeviceAdapter(this, measurementSession.getSensorList(), true);
        binding.sensorRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.sensorRecyclerView.setAdapter(mDotDeviceAdapter);
        if (mDotDeviceAdapter.getItemCount() == 0) {
            binding.sensorRecyclerViewDivider.setVisibility(View.INVISIBLE);
        }
    }
    private void initListView() {
        MeasurementAdapter measurementAdapter = new MeasurementAdapter(this,patientInfo.getActiveMeasurementSession(activeSessionId).getMeasurements(),this);
        binding.measurementBtnWrapper.setAdapter(measurementAdapter);
        binding.measurementBtnWrapper.setSelection(measurementAdapter.getCount() - 1);

    }
    private void initCompleteSessionBtn() {
        if (isStandardNbReached()) {
            increaseBackgroundColorSendDataBtn();
        }
    }

    @Override
    public void listenBtn() {
        listenCompleteMeasurementSession();
        listenHelpMeasurementButton();
    }
    private void listenCompleteMeasurementSession(){
        binding.completeSessionBtn.setOnClickListener(v -> {
            if(!isStandardNbReached()) {
                showConfirmationDialog();
            } else {
                completeSession();
            }
        });
    }
    private void listenHelpMeasurementButton() {
        binding.helpButton.setOnClickListener(v -> {
            DialogUtils.showMeasurementButtonDialog(this);
        });
    }

    @Override
    public void prepareIntent(Intent intent) {
        intent.putExtra("activeSessionId", activeSessionId);
        intent.putExtra("activeMeasurementId", activeMeasurementId);
    }

    @Override
    public void processReceivedIntent(Intent intent) {
        activeSessionId = intent.getIntExtra("activeSessionId", 0);
    }
    @Override
    public void onMeasurementClick(int position) {
        activeMeasurementId = position;
        navigateToNextActivity(RecordingActivity.class);
    }

    @Override
    public void onPlusMeasurementClick(int position) {
        measurementSession.addMeasurement();
        onMeasurementClick(position+1);
    }

    @Override
    public void onCommentClick(int position) {
        String commentTxt = measurementSession.getMeasurements().get(position).getComment();
        commentDialogManager = new CommentDialogManager(this, (CommentDialogListener) this,commentTxt);
        commentDialogManager.showCommentDialog(position);
    }

    @Override
    public void onCommentSubmitted(String comment, int index) {
        measurementSession.getMeasurements().get(index).setComment(comment);
        //new comment rewrite file
        FileManager.createJsonFile(this,patientInfo,measurementSession.getMeasurements().get(index),activeSessionId);
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
    public void setBinding() {
        binding = ActivityMeasurementSelectionBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }
    private void completeSession(){
        // Check if no data recorded
        if (measurementSession.measurementsTaken() == 0) {
            patientInfo.removeMeasurementSession(activeSessionId);
        } else {
            activeSessionId++;
        }
        navigateToNextActivity(MenuActivity.class);
    }

    private void showConfirmationDialog(){
        long measurementsTaken = measurementSession.measurementsTaken();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.are_you_sure_dialog))
                .setMessage(getString(R.string.are_you_sure_info, measurementsTaken, STANDARD_NB_OF_MEASUREMENTS))
                .setPositiveButton(getString(R.string.confirm), ((dialogInterface, i) -> completeSession()))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    private boolean isStandardNbReached() {
        return (measurementSession.getMeasurements().get(STANDARD_NB_OF_MEASUREMENTS-1).getStatus()!= MeasurementStatus.NOT_STARTED);
    }

    private void increaseBackgroundColorSendDataBtn(){
        binding.completeSessionBtn.setBackgroundColor(getColor(R.color.forestGreen));
    }
    private MeasurementSession getMeasurementSession() {
        return patientInfo.getActiveMeasurementSession(activeSessionId);
    }
    public void disconnectAllSensors() {

        if (!measurementSession.getConnectedSensors().getValue().isEmpty()) {

            synchronized (LOCKER) {

                for (Iterator<DotDevice> it = measurementSession.getConnectedSensors().getValue().iterator(); it.hasNext(); ) {
                    // Use Iterator to make sure it's thread safety.
                    DotDevice device = it.next();
                    device.disconnect();
                }
            }
        }
    }
}
