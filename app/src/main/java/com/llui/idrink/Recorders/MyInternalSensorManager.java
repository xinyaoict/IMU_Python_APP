package com.llui.idrink.Recorders;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * MySensorRecorder is a utility class for recording sensor data using Android's SensorManager API.
 * It registers sensors like accelerometer, gyroscope, magnetometer, and rotation vector,
 * and writes the collected data to a CSV file.
 */
public class MyInternalSensorManager implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor rotationVectorSensor;
    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private FileWriter csvWriter;
    private static final String TAG = "MySensorManager";

    public MyInternalSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorThread = new HandlerThread("SensorThread");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    public void start(String filePath) {
        initCSVWriter(filePath);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        sensorThread.quitSafely();
        closeCSVWriter();
    }

    private void initCSVWriter(String filePath) {
        File csvFile = new File(filePath);

        try {
            csvWriter = new FileWriter(csvFile);
            csvWriter.append("Timestamp,SensorType,Values\n");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing CSV writer", e);
        }
    }

    private void writeSensorDataToCSV(long timestamp, String sensorType, float[] values) {
        if (csvWriter != null) {
            try {
                csvWriter.append(String.valueOf(timestamp)).append(",");
                csvWriter.append(sensorType).append(",");
                for (int i = 0; i < values.length; i++) {
                    csvWriter.append(String.valueOf(values[i]));
                    if (i < values.length - 1) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
                csvWriter.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing to CSV", e);
            }
        }
    }

    private void closeCSVWriter() {
        if (csvWriter != null) {
            try {
                csvWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing CSV writer", e);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();
        String sensorType;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorType = "Accelerometer";
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensorType = "Gyroscope";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorType = "Magnetometer";
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                sensorType = "RotationVector";
                break;
            default:
                sensorType = "Unknown";
                break;
        }

        writeSensorDataToCSV(timestamp, sensorType, event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No implementation needed for this example
    }
}
