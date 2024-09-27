package com.example.simplebc_v20;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockListener {

    private final Activity activity;
    private final UDPHelper udpHelper;

    private String lastBlockHash = "";
    private int lastBlockNum = 0;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BlockListener(Activity activity, UDPHelper udpHelper) {
        this.activity = activity;
        this.udpHelper = udpHelper;
    }

    public int getLastBlockNum() { return lastBlockNum; }

    public String getLastBlockHash() { return lastBlockHash;}

    public void startListening() {
        executorService.execute(() -> {
            while (true) {
                Log.i("StartListening", "Start Listening!");
                udpHelper.receiveData(data -> {
                    if (data != null) {
                        Block latestBlock = getBlockFromString(data);

                        if (!latestBlock.getTailHash().equals(lastBlockHash)) {
                            saveBlockToLocal(latestBlock);
                            lastBlockHash = latestBlock.getTailHash();
                            lastBlockNum = latestBlock.getChainNum();
                            Tools.add2Log("New Block Saved! Block number \n" + lastBlockNum, activity);
                        }

                        Thread.sleep(100);
                    }
                });
            }
        });
    }

    private static @NonNull Block getBlockFromString(String data) throws JSONException {
        Block latestBlock = new Block();
        JSONObject jsonObject = new JSONObject(data);

        latestBlock.setPreHash(jsonObject.getString("pre_hash"));
        latestBlock.setTimestamp(jsonObject.getLong("timestamp"));
        latestBlock.setChainNumber(jsonObject.getInt("chain_num"));
        latestBlock.setNonce(jsonObject.getInt("nonce"));
        latestBlock.setAddress(jsonObject.getString("address"));
        latestBlock.setPictureUrl(jsonObject.getString("p_url"));
        latestBlock.setPictureHash(jsonObject.getString("p_hash"));
        latestBlock.setTailHash(jsonObject.getString("tail_hash"));
        return latestBlock;
    }


    private void saveBlockToLocal(Block block) throws IOException{
        try (FileOutputStream fos = activity.openFileOutput("blocks.txt", Context.MODE_APPEND)) {
            fos.write(block.getBlockString().getBytes());
            Log.d("BlockListener", "Saved: " + block.getBlockString());
        }
    }
}
