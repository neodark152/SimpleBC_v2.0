package com.example.simplebc_v20;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockHandler {
    private final Activity activity;
    private final int NONCE_DIFFICULTY;
    private final Block block;
    private final BlockListener blockListener;
    private final UDPHelper udpHelper;
    private int currentNonce = 0;

    private final ExecutorService executorService;


    public BlockHandler(Activity activity, Block block, BlockListener blockListener, UDPHelper udpHelper, int difficulty) throws JSONException {
        this.activity = activity;
        this.block = block;
        this.blockListener = blockListener;
        this.udpHelper = udpHelper;
        this.NONCE_DIFFICULTY = difficulty;

        this.executorService = Executors.newSingleThreadExecutor();
    }

    private void computeNonce() {
        executorService.execute(() -> {
            try {
                Log.i("ComputeNonce", "Nonce computation started with difficulty: " + NONCE_DIFFICULTY);
                Tools.add2Log("Start Mining at " + Tools.getTime(), activity);

                //禁止拍照按钮
                activity.runOnUiThread(() -> activity.findViewById(R.id.take_photo).setEnabled(false));

                currentNonce = 0;
                String targetPrefix = new String(new char[NONCE_DIFFICULTY]).replace('\0', '0');
                while (true) {
                    //首先检测是否有新的区块被挖出
                    if (blockListener.getLastBlockNum() >= block.getChainNum()) {
                        Log.i("BlockHandler", "Detect new blocks and restart mining.");
                        Tools.add2Log("Detect new blocks and restart mining.", activity);
                        resetMine();
                        continue;
                    }

                    block.setNonce(currentNonce);
                    String hash = Tools.calculateHash(block.getBlockString());

                    if (hash.startsWith(targetPrefix)) {
                        Log.d("BlockHandler", "Valid nonce found: " + currentNonce);
                        Tools.add2Log("Finish Mining at " + Tools.getTime(), activity);

                        //重新启动按钮
                        activity.runOnUiThread(() -> activity.findViewById(R.id.take_photo).setEnabled(true));

                        block.setTailHash(hash);
                        broadcastBlock();
                        break;
                    } else {
                        currentNonce++;
                        if (currentNonce % 10000 == 0) {
                            Log.d("BlockHandler", "Nonce tried: " + currentNonce);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("BlockHandler", "Error when mining ", e);
            }
        });
    }

    private void resetMine() {
        currentNonce = 0;
        block.setChainNumber(blockListener.getLastBlockNum() + 1);
        block.setTailHash("");
    }

    private void broadcastBlock() {
        executorService.execute(() -> {
            udpHelper.broadcastData(block.getBlockString());
            Tools.add2Log("New Block Broadcast! Block number is " + block.getChainNum(), activity);
        });
    }

    public void startMining() {
        computeNonce();
    }
}
