package com.llui.idrink.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.llui.idrink.Interfaces.IActivityCreator;
import com.llui.idrink.Interfaces.IIntentHandler;
import com.llui.idrink.Models.Patient;
import com.llui.idrink.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * BaseActivity is an abstract class that provides common functionality
 * for all activities in the application.
 */
public abstract class BaseActivity extends AppCompatActivity implements IIntentHandler, IActivityCreator {
    private static final int REQUEST_PERMISSIONS = 1;

    private final String[] appPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    protected Patient patientInfo;
    private boolean useNavBackArrow;
    private Class<?> navBackArrowActivityClass;
    protected ViewBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patientInfo = Patient.getPatient();
        retrieveIntent();
        setBinding();
        init();
        listenBtn();
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            logOut();
            return true;
        } else if(itemId == android.R.id.home && navBackArrowActivityClass != null){
            navigateToNextActivity(navBackArrowActivityClass);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void enableNavBackArrow(Class<?> navBackArrowActivityClass){
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.navBackArrowActivityClass = navBackArrowActivityClass;
    }

    @Override
    public void onBackPressed() {
        // When user press the inbuilt android back button
    }

    protected void navigateToNextActivity(Class<?> nextClass) {
        Intent intent = new Intent(this, nextClass);
        prepareIntent(intent);
        this.startActivity(intent);
        this.finish();
    }

    protected void retrieveIntent() {
        Intent intent = this.getIntent();
        processReceivedIntent(intent);
    }

    protected void initFooter() {
        TextView footerTxt = findViewById(R.id.footerTxt);
        footerTxt.setText(patientInfo.toString());
    }

    @Override
    public void prepareIntent(Intent intent) {
    }

    @Override
    public void processReceivedIntent(Intent intent) {
    }
    protected boolean checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            for (String permission : permissionsNeeded) {
                Log.d("PermissionsNeeded", "Permission needed: " + permission);
            }
        } else {
            Log.d("PermissionsNeeded", "No Permissions needed");

        }
            return permissionsNeeded.isEmpty();
    }

    protected void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Log.d("Permissions", "All permissions are granted");
            } else {
                Log.d("Permissions", "Some permissions are denied");
            }
        }
    }
    private void logOut() {
        showWarningDataNotSent();
    }
    private void showWarningDataNotSent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!")
                .setMessage("You haven't sent your data. Everything will be lost.")
                .setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateToNextActivity(LoginActivity.class);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
