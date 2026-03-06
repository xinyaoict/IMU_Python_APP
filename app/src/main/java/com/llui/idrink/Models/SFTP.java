package com.llui.idrink.Models;

import static com.llui.idrink.BuildConfig.*;
import static com.llui.idrink.BuildConfig.SFTP_HOST;
import static com.llui.idrink.BuildConfig.SFTP_PASS;
import static com.llui.idrink.BuildConfig.SFTP_PORT;
import static com.llui.idrink.BuildConfig.SFTP_USER;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.llui.idrink.Utils.DebugLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Provides functionality to upload patient data files via SFTP (Secure File Transfer Protocol).
 * The SFTP class uses JSch library to establish a secure connection to the specified SFTP server,
 * authenticate using credentials, and upload patient data files to a designated remote directory.
 *
 * The uploadFile() method initiates the SFTP session, establishes a connection, and handles the
 * uploading process for a specified Patient object and local directory containing data files.
 *
 * The setUploadFolderPath() method constructs the remote upload directory path based on patient
 * information (case ID, date) and creates necessary directories on the server if they do not exist.
 *
 * The createDirectory() method checks and creates directories on the SFTP server as needed,
 * ensuring the correct folder structure for file uploads.
 *
 * The uploadData() method iterates through files in the local directory, streams each file's data
 * to the SFTP server, and logs successful uploads with appropriate debug messages.
 */
public class SFTP {
    private String remoteUploadFolderPath;
    private final Handler toastHandler = new Handler(Looper.getMainLooper());


    public boolean uploadFile(Context context, Patient patient, File toUploadDataFolderPath) {
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(SFTP_PASS);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            DebugLogger.debugLog("SFTPCONNECT", "Channel SFTP is connected: " + channelSftp.isConnected());
            setUploadFolderPath(channelSftp, patient);
            uploadData(channelSftp, toUploadDataFolderPath);
            showUploadToast(context,true);
            DebugLogger.debugLog("SFTPCONNECT" , "File uploaded successfully.");
            return true;

        } catch (JSchException e) {
            e.printStackTrace();
            showUploadToast(context,false);
            return false;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void setUploadFolderPath(ChannelSftp channelSFTP, Patient patient){
        remoteUploadFolderPath = SFTP_DIR;
        remoteUploadFolderPath = createDirectory(channelSFTP, remoteUploadFolderPath, patient.getCaseId());
        remoteUploadFolderPath = createDirectory(channelSFTP, remoteUploadFolderPath, "igait");
        remoteUploadFolderPath = createDirectory(channelSFTP, remoteUploadFolderPath, patient.getDate());
    }

    private String createDirectory(ChannelSftp channelSFTP, String parentFolderPath, String folderName) {
        String dirPath = parentFolderPath + "/" + folderName;
        try {
            channelSFTP.cd(dirPath);
        } catch (Exception e) {
            try {
                channelSFTP.mkdir(dirPath);
                channelSFTP.cd(dirPath);
            } catch (Exception innerException) {
                innerException.printStackTrace();
            }
        }

        return dirPath;
    }

    private void uploadData(ChannelSftp channelSFTP, File toUploadDataFolderPath){
        for(File file: toUploadDataFolderPath.listFiles()){
            if(file.isFile()) {
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    String remoteFilePath = remoteUploadFolderPath + "/" + file.getName();
                    channelSFTP.put(inputStream, remoteFilePath);
                    DebugLogger.debugLog("SFTPCONNECT", "Uploaded file: " + remoteFilePath);
                } catch (IOException | SftpException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private void showUploadToast(Context context, boolean isSuccess) {
        String text;
        if (isSuccess) {
            text = "All files uploaded successfully!";
        } else {
            text = "There was an error while uploading the files";
        }
        toastHandler.post(() -> {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        });
    }
}
