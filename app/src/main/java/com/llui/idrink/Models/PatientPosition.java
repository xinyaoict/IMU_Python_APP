package com.llui.idrink.Models;
/**
 * PatientPosition represents a data class for storing patient's position coordinates and timestamp in the recording frame.
 * It encapsulates the x and y coordinates of the patient's position and the timestamp
 * when the position was selected by clicking on the screen.
 */
public class PatientPosition {
    private final int xCoordinate;
    private final int yCoordinate;
    private final long timestamp;

    public PatientPosition(int [] position, long timestamp) {
        this.xCoordinate = position[0];
        this.yCoordinate = position[1];
        this.timestamp = timestamp;
    }

    public String getXCoordinate() {
        return String.valueOf(xCoordinate);
    }


    public String getYCoordinate() {
        return String.valueOf(yCoordinate);
    }


    public String getTimestamp() {
        return String.valueOf(timestamp);
    }

}

