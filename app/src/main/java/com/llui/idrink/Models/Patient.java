package com.llui.idrink.Models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.llui.idrink.Utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Patient class represents a model for storing patient information and measurement sessions.
 * It provides methods to set patient data, manage measurement sessions, and retrieve patient details.
 */
public class Patient {

    private static Patient patient;
    private String patientId, caseId;
    private String date, stopTime;
    private List<MeasurementSession> measurementSessions;

    private Patient() {
    }

    public static Patient getPatient() {
        if (patient == null) {
            patient = new Patient();
        }

        return patient;
    }

    public void setPatientData(String patientId, String caseId, Context context){
        if (patient == null) {
            patient = new Patient();
        }

        this.patientId = patientId;
        this.caseId = caseId;
        this.date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        this.stopTime = null;
        this.measurementSessions = new ArrayList<>();
    }
    public void setStopTime() {
        this.stopTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }
    public String getStopTime() {return  this.stopTime;}
    public String getPointDate() { return this.date.replace('_', '.');}


    public String getPatientId(){
        return this.patientId;
    }

    public String getCaseId(){
        return this.caseId;
    }
    public String getDate() { return this.date; }

    public String getFormattedDate(){
        return Utils.changeDateFormatFromYMDToDMY(this.date);
    }
    public List<MeasurementSession> getMeasurementSessions() {
        return measurementSessions;
    }

    public void addNewMeasurementSessions(MeasurementSession session){
        this.measurementSessions.add(session);
    }

    public MeasurementSession getActiveMeasurementSession(int index){
        return measurementSessions.get(index);
    }
    public void removeMeasurementSession(int index) {
        if (index >= 0 && index < measurementSessions.size()) {
            measurementSessions.remove(index);
        } else {
            Log.e( "Patient", "Invalid index: " + index);
        }
    }
    @NonNull
    @Override
    public String toString() {
        return "PID: " + patientId + "; CID: " + caseId  + " " + getFormattedDate();
    }


}
