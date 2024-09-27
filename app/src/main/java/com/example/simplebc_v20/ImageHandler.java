package com.example.simplebc_v20;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageHandler {
    private final Context context;
    private File outputImage;
    private final FileUploader fileUploader;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<Intent> cameraLauncher;

    public ImageHandler(Context context, FileUploader fileUploader) {
        this.context = context;
        this.fileUploader = fileUploader;
    }

    // Register the camera launcher
    public void registerCameraLauncher(AppCompatActivity activity) {
        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        processImage(outputImage);
                    }
                }
        );
    }

    // Start the camera activity
    public void startCamera() {
        if (cameraLauncher != null) {
            Intent intent = getCameraIntent();
            cameraLauncher.launch(intent);
        }
    }

    // Method to create image URI and get Camera Intent
    private Uri createImageUri() {
        try {
            outputImage = new File(context.getFilesDir(), "photo.jpg");
            if (outputImage.exists()) {
                outputImage.delete();  // Remove any previous file
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context, "com.example.simplebc_v20.fileprovider", outputImage);
        } else {
            return Uri.fromFile(outputImage);
        }
    }

    private Intent getCameraIntent() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, createImageUri());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    // Calculate file hash
    private String calculateFileHash(File file) {
        StringBuilder fileContent = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return Tools.calculateHash(fileContent.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to handle the complete process: capturing, hashing, and uploading the image
    public void processImage(File imageFile) {
        executorService.execute(() -> {
            String hash = calculateFileHash(imageFile);
            fileUploader.uploadFile(imageFile, serverResponse -> {
                // Handle upload result
                ((MainActivity) context).runOnUiThread(() -> {
                    // Pass the server response and hash to the MainActivity
                    ((MainActivity) context).handleImageResult(serverResponse, hash);
                });
            });
        });
    }
}
