package com.example.simplebc_v20;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Initializer {
    private final Activity activity;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static String ipfsUrl;
    private String address;

    private boolean Isipfs = false;
    private boolean IsAddress = false;
    private boolean IsBlockFile = false;

    public Initializer(Activity activity){
        this.activity = activity;
    }

    public String getIpfsUrl() {
        return ipfsUrl;
    }

    public String getAddress() {
        return address;
    }

    // 初始化逻辑
    public void initializeApp() throws IOException {
        fetchIPFSUrl();
        generateRandomAddress();
        createFileIfNotExists();

        if(IsAddress && IsBlockFile && Isipfs) {
            activity.findViewById(R.id.take_photo).setEnabled(true);
        }
    }

    public void fetchIPFSUrl() {
        ipfsUrl = Tools.getIPFSUrl();
        Isipfs = true;
    }

    // 生成随机的 30 位地址
    private void generateRandomAddress() {
        StringBuilder sb = new StringBuilder(30);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 30; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        address = sb.toString();
        IsAddress = true;
    }

    //检测目录本地的保存区块的文件
    private void createFileIfNotExists() {
        try {
            activity.openFileInput("blocks.txt").close();
            IsBlockFile = true;
        } catch (IOException e) {
            try (FileOutputStream fos = activity.openFileOutput("blocks.txt", Context.MODE_PRIVATE)) {
                // 创建一个空文件
                fos.write("".getBytes());
                IsBlockFile = true;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
