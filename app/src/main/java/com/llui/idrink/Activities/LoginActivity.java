package com.llui.idrink.Activities;

import static com.llui.idrink.Utils.PermissionUtils.checkCameraPermission;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.llui.idrink.databinding.ActivityLoginBinding;
import com.llui.idrink.Utils.FirstTimeCheck;
/**
 * LoginActivity is the first activity launched in SuperWalk.
 * Upon correct patient id and case id (7 digits) input, it goes to the next activity.
 * QR scanning is enabled in the format of "patientId;caseId"
 */
public class LoginActivity extends BaseActivity {
    private DecoratedBarcodeView scannerView;
    private ActivityLoginBinding binding;
    private String patientID, caseID;
    private boolean isForceToViewInstructions = false;


    @Override
    public void init() {
        checkCameraPermission(this);
        scannerView = binding.scannerView;
        startQRCodeScanning();
    }
    private void startQRCodeScanning() {
        scannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleQRCode(result.getText());

            }
        });
    }
    private void handleQRCode(String qrCodeData) {
        // Extract PatientID and CaseID from QR code data
        String[] parts = qrCodeData.split(";");
        if (parts.length == 2) {
            String patientID = parts[0];
            String caseID = parts[1];
            //Automatically Enter it in the editText
            binding.editTextPatientId.setText(patientID);
            binding.editTextCaseId.setText(caseID);

            Log.d("QRCodeHandler", "PatientID: " + patientID + ", CaseID: " + caseID);
        } else {
            // Handle the case where the QR code data is not in the expected format
            Log.e("QRCodeHandler", "Invalid QR code data: " + qrCodeData);
        }
    }
    @Override
    public void listenBtn() {
        listenConfirm();
        listenHelp();
    }
    private void listenConfirm()
    {
        binding.loginButton.setOnClickListener(v -> {
            if(isCorrectPatientInput()) {
                createPatient();
                if (FirstTimeCheck.isFirstTime(this)) {
                    isForceToViewInstructions = true;
                    navigateToNextActivity(InstructionActivity.class);
                    // Set the flag to false so that this block is not executed again
                    FirstTimeCheck.setFirstTime(this, false);
                } else {
                    navigateToNextActivity(MenuActivity.class);
                }
            }
        });
    }
    private void listenHelp() {
        binding.helpButton.setOnClickListener(v -> {
            navigateToNextActivity(InstructionActivity.class);
        });
    }
    private boolean isCorrectPatientInput(){
        patientID = binding.editTextPatientId.getText().toString();
        caseID = binding.editTextCaseId.getText().toString();

        boolean correctInput = false;

        if(patientID.length() != 7) {
            Toast.makeText(LoginActivity.this, "Patient ID not correct", Toast.LENGTH_LONG).show();
        } else if(caseID.length() != 7) {
            Toast.makeText(LoginActivity.this, "Case ID not correct", Toast.LENGTH_LONG).show();
        } else {
            correctInput = true;
        }

        return correctInput;
    }

    private void createPatient(){
        patientInfo.setPatientData(patientID, caseID,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scannerView != null) {
            scannerView.resume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.pause();
        }
    }
    @Override
    public void setBinding() {
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }
    @Override
    public void prepareIntent(Intent intent) {
        intent.putExtra("isForcedToView", isForceToViewInstructions);
    }
}