package com.llui.idrink.Models;

import com.llui.idrink.Enums.MeasurementStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Measurement class represents a measurement instance with its status, start timestamp,
 * and an optional comment.
 */
public class Measurement {

    private MeasurementStatus status;
    private String startTimestamp, mainComment;
    private String comment = "";

    public Measurement(MeasurementSession measurementSession){
        status = MeasurementStatus.NOT_STARTED;
        this.mainComment = measurementSession.getMainComment();
        setDate();
    }

    public MeasurementStatus getStatus() {
        return status;
    }
    public void setStatus(MeasurementStatus newStatus) {
        status = newStatus;
    }
    public void setComment(String comment) {this.comment = comment;}
    public void setDate() {
        this.startTimestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }
    public String getStartTimestamp() { return this.startTimestamp;}
    public String getMainComment() {return mainComment;}
    public String getComment() {return comment;}
}
