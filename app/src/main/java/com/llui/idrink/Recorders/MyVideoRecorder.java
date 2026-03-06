package com.llui.idrink.Recorders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.llui.idrink.Utils.Utils;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
/**
 * MyVideoRecorder is a utility class for recording video using the CameraX API.
 * It handles initializing the camera, starting and stopping video recording,
 * and releasing camera resources based on the Android lifecycle.
 */
public class MyVideoRecorder {


    private Context mContext;
    private final LifecycleOwner mLifecycleOwner;

    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private FileOutputOptions fileOutputOptions;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private MutableLiveData<Boolean> isRecording = new MutableLiveData<>(false);
    private File outputFile;
    private final PreviewView previewView;
    private Executor recordingThread = null;



    public MyVideoRecorder(Context context,LifecycleOwner lifecycleOwner, PreviewView previewView, File outputFile) {
        this.mContext = context;
        this.mLifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        this.outputFile = outputFile;
        initCamera();
    }


    @SuppressLint("MissingPermission")
    public void startRecording() {
        Log.i("MyVideoRecorder", "TEST");

        //ensure send data button is gone
        if (videoCapture != null) {
            Recorder recorder = videoCapture.getOutput();
            //recording video only
            recording = recorder.prepareRecording(mContext, fileOutputOptions)
                    .start(recordingThread, videoRecordEvent -> {
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            Utils.syncWithOtherRecorder();
                            Activity activity = (Activity) mContext;
                            activity.runOnUiThread(() -> {
                                isRecording.setValue(true);
                            });
                            Log.d("MyVideoRecorder", "Recording started");
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            Activity activity = (Activity) mContext;
                            activity.runOnUiThread(() -> {
                                isRecording.setValue(false);
                            });
                            Log.d("MyVideoRecorder", "Recording stopped");
                            if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                                Log.d("MyVideoRecorder", "Video saved to: " + outputFile.getAbsolutePath());
                            } else {
                                Log.e("MyVideoRecorder", "Video recording error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError());
                            }
                        }
                    });
        } else {
            Log.e("MyVideoRecorder", "VideoCapture is null");
        }
    }

    public void stopRecording() {
        if (Boolean.TRUE.equals(isRecording.getValue()) && recording != null) {
            recording.stop();
            recording = null;
            Log.d("MyVideoRecorder", "Recording manually stopped");
        }
    }

    private void initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext);
        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);


        cameraProviderFuture.addListener(() -> {
            try {
                Log.d("MyVideoRecorder", "CameraProvider obtained, initializing settings");
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                initCameraSettings(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.e("MyVideoRecorder", "Error initializing camera provider", e);
            }
        }, ContextCompat.getMainExecutor(mContext));

    }

     private void initCameraSettings(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        // TODO: Handle weird ratio behavior
        Recorder recorder = new Recorder.Builder()
                .setAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        videoCapture = VideoCapture.withOutput(recorder);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle(mLifecycleOwner, cameraSelector, preview, videoCapture);
        fileOutputOptions = new FileOutputOptions.Builder(outputFile).build();

        // Thread
         recordingThread = Executors.newSingleThreadExecutor();

    }
}
