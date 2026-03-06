package com.llui.idrink.Utils;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import com.llui.idrink.Recorders.RecordingSynchronizerManager;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.FilterProfileInfo;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final String EMPTY_STRING = "";

    public static String changeDateFormatFromYMDToDMY(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        Date newDate = null;
        try {
            newDate = inputFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return outputFormat.format(newDate);
    }

    public static String formatAudioTimeToStringPresentation(long time) {
        DecimalFormat df = new DecimalFormat("00");

        int hours = (int) (time / (3600 * 1000));
        int remaining = (int) (time % (3600 * 1000));

        int minutes = (int) (remaining / (60 * 1000));
        remaining = (int) (remaining % (60 * 1000));

        int seconds = (int) (remaining / 1000);
        remaining = (int) (remaining % (1000));

        int milliseconds = (int) (remaining / 100);

        String text = "";

        if (hours > 0) {
            text += df.format(hours) + ":";
        }

        text += df.format(minutes) + ":";
        text += df.format(seconds) + ":";
        text += Integer.toString(milliseconds);

        return text;
    }

    public static long parseTimeToMilliseconds(String formattedTime) {
        String[] parts = formattedTime.split(":");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid time format: " + formattedTime);
        }

        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        int milliseconds = Integer.parseInt(parts[2]);

        long totalTimeInMillis =
                (long) minutes * 60 * 1000 +
                        seconds * 1000L +
                        milliseconds * 100;

        return totalTimeInMillis;
    }

    public static File createFolder(File parent, String child) {
        File directory = new File(parent, child);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + directory.getAbsolutePath());
            }
        }
        return directory;
    }

    public static File[] getFilesFromInternalStorageFolder(String folderName) {
        File directory = new File(folderName);

        if (!directory.exists()) {
            return null;
        }

        return directory.listFiles();
    }

    public static void deleteFilesFromDir(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    public static boolean containsNumber(String input) {
        Pattern pattern = Pattern.compile(".*\\d.*");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static <T> T[] concatTwoNonPrimitiveArrays(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public static long getChronometerElapsedTimeInMillis(Chronometer chronometer) {
        long baseTime = chronometer.getBase(); // Get the base time of the chronometer
        long elapsedTime = SystemClock.elapsedRealtime() - baseTime; // Calculate elapsed time
        return elapsedTime;
    }
    public static String getFilterProfileName(DotDevice device) {

        int index = device.getCurrentFilterProfileIndex();
        ArrayList<FilterProfileInfo> list = device.getFilterProfileInfoList();

        for (FilterProfileInfo info : list) {

            if (info.getIndex() == index) return info.getName();
        }

        return "General";
    }
    public static void syncWithOtherRecorder() {
        RecordingSynchronizerManager.RecordingSynchronizer synchronizer = RecordingSynchronizerManager.getSynchronizer();

        synchronizer.countDown();
        Log.i("TestRecord", "Synchronizing Count : " + synchronizer.getLatch().getCount());
        try {
            // Wait for all recorders to be ready
            synchronizer.getLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static DotDevice getDotDeviceByAddress(ArrayList<DotDevice> sensorList, String address) {
        // Iterate through the list to find the device with the specified address
        for (DotDevice device : sensorList) {
            if (device.getAddress().equals(address)) {
                return device; // Return the found device
            }
        }
        return null;
    }
}
