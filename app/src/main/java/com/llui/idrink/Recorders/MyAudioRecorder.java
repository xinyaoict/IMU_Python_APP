package com.llui.idrink.Recorders;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.llui.idrink.Utils.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * MyAudioRecorder is a utility class for recording audio using Android's AudioRecord API.
 * It provides methods to start and stop audio recording and write the recorded audio data
 * to a specified output file.
 */
public class MyAudioRecorder {
    private static final String TAG = "MyAudioRecorder";
    private static final int SAMPLE_RATE = 44100; // Sample rate (Hz)
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO; // Mono channel
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 16-bit PCM encoding
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord = null;
    private boolean isRecording = false;
    private Thread recordingThread = null;
    private String audioOutputFile;
    @SuppressLint("MissingPermission")
    public MyAudioRecorder(String outputFile) {

        audioOutputFile = outputFile;
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
        );

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed");
        }
    }
    @SuppressLint("MissingPermission")
    public void startRecording() {

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isRecording) {
                    Utils.syncWithOtherRecorder();
                    audioRecord.startRecording();
                    isRecording = true;
                } else {
                    writeAudioDataToFile();
                }
            }
        });
        recordingThread.start();
    }

    public void stopRecording() {
        if (isRecording) {
            isRecording = false;
            try {
                recordingThread.join(); // Wait for recording thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
        }
    }

    private void writeAudioDataToFile() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(audioOutputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fos != null) {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                    if (read > 0) {
                        fos.write(buffer, 0, read);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
