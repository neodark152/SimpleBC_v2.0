package com.example.simplebc_v20;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPHelper {

    private DatagramSocket socket;
    private final int PORT; // 固定端口

    public UDPHelper(int port) throws SocketException {
        this.PORT = port;

        socket = new DatagramSocket(port);

        Log.i("UDPHelper", "UDPHelper initialize finished!");

    }

    // 接收数据的回调接口
    public interface DataReceivedCallback {
        void onDataReceived(String data) throws JSONException, IOException, InterruptedException;
    }

    // 接收数据
    public void receiveData(DataReceivedCallback callback) {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                Log.i("UDPHelper", "Receive data: " + received);
                // 通过回调传递接收到的数据
                callback.onDataReceived(received);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 广播数据
    public void broadcastData(String message) {
        try {
            Log.i("UDPHelper", "Inside executor thread");
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("192.168.2.255"), PORT);
            socket.send(packet);
            Log.i("UDPHelper", "Broadcast data: " + message);
        } catch (Exception e) {
            Log.e("UDPHelper", "Error during broadcast", e);
            e.printStackTrace();
        }
    }


    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
