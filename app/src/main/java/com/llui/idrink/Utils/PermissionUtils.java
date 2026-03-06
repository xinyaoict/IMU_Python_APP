package com.llui.idrink.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class PermissionUtils {

    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1002;
    private static final int PERMISSION_REQUEST_INTERNET = 1003;
    private static final int PERMISSION_REQUEST_ACCESS_NETWORK_STATE = 1004;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1005;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 1006;
    private static final int PERMISSION_REQUEST_BLUETOOTH_ADMIN = 1007;
    private static final int PERMISSION_REQUEST_BLUETOOTH_CONNECT = 1008;
    private static final int PERMISSION_REQUEST_BLUETOOTH_SCAN = 1009;

    // Permission map: Maps each permission to its corresponding request code
    private static final Map<String, Integer> permissionRequestCodes = new HashMap<>();

    static {
        permissionRequestCodes.put(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA);
        permissionRequestCodes.put(Manifest.permission.RECORD_AUDIO, PERMISSION_REQUEST_RECORD_AUDIO);
        permissionRequestCodes.put(Manifest.permission.INTERNET, PERMISSION_REQUEST_INTERNET);
        permissionRequestCodes.put(Manifest.permission.ACCESS_NETWORK_STATE, PERMISSION_REQUEST_ACCESS_NETWORK_STATE);
        permissionRequestCodes.put(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        permissionRequestCodes.put(Manifest.permission.BLUETOOTH, PERMISSION_REQUEST_BLUETOOTH);
        permissionRequestCodes.put(Manifest.permission.BLUETOOTH_ADMIN, PERMISSION_REQUEST_BLUETOOTH_ADMIN);
        permissionRequestCodes.put(Manifest.permission.BLUETOOTH_CONNECT, PERMISSION_REQUEST_BLUETOOTH_CONNECT);
        permissionRequestCodes.put(Manifest.permission.BLUETOOTH_SCAN, PERMISSION_REQUEST_BLUETOOTH_SCAN);
    }

    // Check if a permission is granted
    private static void checkPermission(Context context, String permission) {
        if (!(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)) {
            requestPermission((Activity) context, permission);
        }
    }

    // Request a permission
    private static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                getPermissionRequestCode(permission));
    }

    // Get the request code for a specific permission
    private static int getPermissionRequestCode(String permission) {
        if (permissionRequestCodes.containsKey(permission)) {
            return permissionRequestCodes.get(permission);
        } else {
            throw new IllegalArgumentException("Permission not supported: " + permission);
        }
    }

    // Check camera permission
    public static void checkCameraPermission(Context context) {
        checkPermission(context, Manifest.permission.CAMERA);
    }

    // Check record audio permission
    public static void checkRecordAudioPermission(Context context) {
        checkPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    // Check internet permission
    public static void checkInternetPermission(Context context) {
        checkPermission(context, Manifest.permission.INTERNET);
    }

    // Check access network state permission
    public static void checkAccessNetworkStatePermission(Context context) {
        checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
    }

    // Check read external storage permission
    public static void checkReadExternalStoragePermission(Context context) {
        checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    // Check Bluetooth permission
    public static void checkBluetoothPermission(Context context) {
        checkPermission(context, Manifest.permission.BLUETOOTH);
        checkPermission(context, Manifest.permission.BLUETOOTH_ADMIN);
        checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
        checkPermission(context, Manifest.permission.BLUETOOTH_SCAN);
    }
}
