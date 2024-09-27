package com.example.simplebc_v20;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileUploader {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Thread pool for file upload

    public void uploadFile(File sourceFile, UploadCallback callback) {
        executorService.execute(() -> {
            String response = uploadFileToServer(sourceFile);
            // Notify the result via callback
            callback.onUploadComplete(response);
        });
    }

    private String uploadFileToServer(File sourceFile) {
        String serverUrl = Tools.getIPFSUrl();
        if (!sourceFile.isFile()) {
            Log.e("UploadFile", "Source File not exist :" + sourceFile.getAbsolutePath());
            return "File not found";
        }

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        String boundary = "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(serverUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", sourceFile.getName());

            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + sourceFile.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int serverResponseCode = conn.getResponseCode();
            Log.i("UploadFileTask", "Server Response Code: " + serverResponseCode);

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();

        } catch (MalformedURLException ex) {
            Log.e("UploadFileTask", "MalformedURLException: " + ex.getMessage(), ex);
        } catch (IOException e) {
            Log.e("UploadFileTask", "IOException: " + e.getMessage(), e);
        } finally {
            try {
                if (dos != null) dos.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                Log.e("UploadFileTask", "IOException: " + e.getMessage(), e);
            }
        }
        return "Upload failed";
    }

    public interface UploadCallback {
        void onUploadComplete(String response);
    }
}
