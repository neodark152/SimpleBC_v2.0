package com.example.simplebc_v20;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class Tools {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private String ipfs_url;

    //输出最新日志到窗口
    public static void add2Log(String new_log_content, Activity activity) {
        handler.post(() -> {
            TextView text_view = activity.findViewById(R.id.log_output);
            ScrollView scroll_view = activity.findViewById(R.id.scroll_view);

            String currentText = text_view.getText().toString();
            String newText = currentText + "\n" + new_log_content;
            text_view.setText(newText);
            scroll_view.post(() -> scroll_view.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    //时间戳
    public static long getTimestamp() { return System.currentTimeMillis(); }

    //根据时间戳获取时间
    public static String getTime() {
        Date date = new Date(getTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
        return(sdf.format(date));
    }

    public static String getIPFSUrl() {return "http://192.168.2.13:15000/upload";}

    // Calculate the hash of the given string input
    public static String calculateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
