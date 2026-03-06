package com.llui.idrink.Utils;

import android.content.Context;

import com.llui.idrink.Enums.DataType;
import com.llui.idrink.Models.Measurement;
import com.llui.idrink.Models.Patient;
import com.llui.idrink.Models.PatientPosition;
import com.xsens.dot.android.sdk.models.DotDevice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class FileManager {

    public static File getSessionFolder(Context context, Patient patient) {
        // Get the external files directory specific to this app
        File baseDir = context.getExternalFilesDir(null);

        // Folder Structure CID / idrink / date_time / files

        // Construct the patient-case directory path
        File caseDir = Utils.createFolder(baseDir,patient.getCaseId());
        File iDrinkDir = Utils.createFolder(caseDir,"idrink");

        // Construct the session path
        return Utils.createFolder(iDrinkDir, patient.getDate());
    }
    public static String getFilePath(Context context, Patient patient, Measurement measurement, DataType dataType) {
        File sessionFolder = getSessionFolder(context, patient);
        // CID_DATETIME_DATATYPE.
        String fileName = patient.getCaseId()+"_"+measurement.getStartTimestamp()+"_"+dataType.toString();

        fileName += dataType.getFileExtension();

        return new File(sessionFolder,fileName).getAbsolutePath();
    }
    public static void createJsonFile(Context context, Patient patient, Measurement measurement, int activeSessionId) {
        try {
            // Create a JSON object with the required information
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("patient_ID", patient.getPatientId());
            jsonObject.put("case_ID", patient.getCaseId());
            jsonObject.put("session_start_time", patient.getActiveMeasurementSession(activeSessionId).getStartTimestamp());
            jsonObject.put("measurement_start_time", measurement.getStartTimestamp());
            jsonObject.put("number_of_imus", patient.getActiveMeasurementSession(activeSessionId).getNbIMUs());
            jsonObject.put("comment", measurement.getComment());
            jsonObject.put("mainComment", measurement.getMainComment());

            // Get the file path
            String filePath = getFilePath(context, patient, measurement,DataType.JSON);

            // Write the JSON object to the file
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jsonObject.toString());
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void createJsonCatalogFile(Context context, Patient patient) {
        String directoryPath = getSessionFolder(context,patient).getAbsolutePath();
        // Get all files in the specified directory
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            System.out.println("The directory does not exist or is empty.");
            return;
        }
        try {
            // Prepare JSON structure
            JSONObject jsonObject = new JSONObject();
            // ProcessingState
            JSONObject processingState = getProcessingState();
            jsonObject.put("processing_state", processingState);
            // MetaData
            JSONObject metaData = getMetaData();
            jsonObject.put("meta_data", metaData);
            // Measurement data
            JSONObject measurementData = getMeasurementData(listOfFiles, patient);
            jsonObject.put("measurement_data", measurementData);
            // Patient Data
            JSONObject patientData = getPatientData(patient);
            jsonObject.put("patient_data", patientData);
            // Clinic Data
            JSONObject clinicData = getClinicData();
            jsonObject.put("clinic_data", clinicData);
            // Write the JSON object to the file
            String filePath = directoryPath + File.separator + patient.getCaseId() +"."+ patient.getPointDate() + ".catalog.idrink.json";
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jsonObject.toString(2));
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public static JSONObject getProcessingState() {
        JSONObject processingState = new JSONObject();
        try {
            processingState.put("type", "integer");
            processingState.put("analytics_service", 0);
            processingState.put("hl7_export", 0);
            processingState.put("visualization", 0);
            processingState.put("description", "Indicates the current processing state of the file. 0 = not processed, 1 = processed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processingState;
    }
    private static JSONObject getMetaData() {
        JSONObject metaData = new JSONObject();
        JSONObject assessment = new JSONObject();
        JSONObject lastUpdated = new JSONObject();
        String currentDateTime = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        try {
            // Assessment
            assessment.put("type", "string");
            assessment.put("value", "idrink");
            assessment.put("description", "Type of assessment");
            metaData.put("assessment", assessment);
            // Last Updated
            lastUpdated.put("type", "string");
            lastUpdated.put("value", currentDateTime);
            lastUpdated.put("description", "Date and time of the last update");
            metaData.put("last_updated", lastUpdated);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metaData;
    }
    private static JSONObject getMeasurementData(File [] listOfFiles, Patient patient) {
        JSONObject measurementData = new JSONObject();

        JSONObject pipelineVersion = getPipelineVersion();
        JSONObject bronzeFiles = getBronzeFiles(listOfFiles, patient);
        JSONObject goldFiles = getGoldFiles();
        try {
            measurementData.put("pipeline_version", pipelineVersion);
            measurementData.put("bronze_files", bronzeFiles);
            measurementData.put("gold_files", goldFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return measurementData;
    }
    private static JSONObject getPatientData(Patient patient) {
        JSONObject patientData = new JSONObject();
        JSONObject fid = new JSONObject();
        try {
            fid.put("type", "integer");
            fid.put("value", patient.getCaseId());
            fid.put("description", "Patient's unique identifier");
            patientData.put("fid", fid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patientData;
    }


    private static JSONObject getPipelineVersion() {
        JSONObject pipelineVersion = new JSONObject();
        try {
            pipelineVersion.put("type", "string");
            pipelineVersion.put("value", "0.0.1");
            pipelineVersion.put("description", "Version of the data processing pipeline used");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pipelineVersion;
    }
    private static JSONObject getBronzeFiles(File[] listOfFiles, Patient patient) {
        JSONObject bronzeFiles = new JSONObject();
        JSONArray bronzeFilesArray = new JSONArray();
        try {
            bronzeFiles.put("type", "array");
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    bronzeFilesArray.put(file.getName());
                }
            }
            bronzeFiles.put("value", bronzeFilesArray);
            String dirInBucket = patient.getCaseId() +"/idrink/" + patient.getDate();
            bronzeFiles.put("dir_in_bucket", dirInBucket);
            bronzeFiles.put("description", "List of filenames for raw data files");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bronzeFiles;
    }
    private static JSONObject getGoldFiles() {
        JSONObject goldFiles = new JSONObject();
        try {
            goldFiles.put("type", "array");
            goldFiles.put("value", new JSONArray());
            goldFiles.put("dir_in_bucket", "");
            goldFiles.put("description", "List of filenames for output files");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goldFiles;
    }
    private static JSONObject getClinicData() {
        JSONObject clinicData = new JSONObject();
        JSONObject clinicEntry = new JSONObject();
        try {
            clinicEntry.put("type", "integer");
            clinicEntry.put("value", 1);
            clinicEntry.put("description", "Clinic unique identifier");
            clinicData.put("clinic_id", clinicEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clinicData;
    }
    public static void writePatientPosition(Context context, Patient patient,Measurement measurement, PatientPosition patientPosition) {
        String filePath = getFilePath(context,patient,measurement,DataType.POSITION);
        //Last info needed we can create the doc
        File csvFile = new File(filePath);

        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.append("X,Y,timeStamp\n");
            writer.append(patientPosition.getXCoordinate())
                    .append(",")
                    .append(patientPosition.getYCoordinate())
                    .append(",")
                    .append(patientPosition.getTimestamp())
                    .append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getSensorFilePath(Context context, Patient patient, Measurement measurement, DotDevice device) {
        String fileName = patient.getCaseId() + "_" + measurement.getStartTimestamp() + "_" + device.getAddress() + ".csv";
        return getSessionFolder(context,patient) + File.separator+fileName;
    }
}
