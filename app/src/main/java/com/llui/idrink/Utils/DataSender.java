package com.llui.idrink.Utils;

//import com.llui.superwalk.BuildConfig;
import com.llui.idrink.Models.Patient;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DataSender {
   // private String pk = BuildConfig.MINIO_ACCESS_KEY;
    //private String sk = BuildConfig.MINIO_SECRET_KEY;
   // private String endpointURL = BuildConfig.MINIO_ENDPOINT_URL;
    private Patient patient;

    public DataSender(Patient patient) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        this.patient = patient;
    }

    public void prepareDataPackage(){

    }

    public static void initMinioClient() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a MinioClient object with the MinIO server URL, access key, and secret key
//                    MinioClient minioClient = new MinioClient(BuildConfig.MINIO_ENDPOINT_URL,  BuildConfig.MINIO_ACCESS_KEY,  BuildConfig.MINIO_SECRET_KEY);

//                    BucketExistsArgs test = BucketExistsArgs.builder().bucket("bronze").build();
//                    boolean isExist = minioClient.bucketExists(test);
//                    if(!isExist) {
//                        minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build());
//                    }
//
//
//                    File file = new File(Environment.getExternalStorageDirectory(), "hello_world.txt");
//                    try (OutputStream os = new FileOutputStream(file)) {
//                        os.write("Hello, World!".getBytes());
//                    }
//
//                    InputStream inputStream = new FileInputStream(file);
//
//
//                    minioClient.putObject(
//                            PutObjectArgs.builder().bucket("test-bucket").object("test-object").stream(
//                                            inputStream, file.length(), -1)
//                                    .contentType("text/plain")
//                                    .build());
//
//                    inputStream.close();

                    DebugLogger.debugLog("MINIOTEST", "Object uploaded successfully.");
                }  catch (Exception e) {
                    DebugLogger.debugLog("MINIOTESTT", String.valueOf(e));
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
