package com.example.simplebc_v20;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private Button takePhoto;

    private String ipfsUrl;
    private String address;
    private final int BLOCK_BROADCAST_PORT = 12345;
    private final int NONCE_DIFFICULTY = 5;

    private Initializer initializer;
    private BlockListener blockListener;
    private ImageHandler imageHandler;
    private BlockHandler blockHandler;
    private UDPHelper udpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePhoto = findViewById(R.id.take_photo);
        takePhoto.setEnabled(false);

        /* 初始化开始 */
        initializer = new Initializer(this);
        try { initializer.initializeApp(); } catch (IOException e) { throw new RuntimeException(e);}

        this.ipfsUrl = initializer.getIpfsUrl();
        this.address = initializer.getAddress();
        try { this.udpHelper = new UDPHelper(BLOCK_BROADCAST_PORT); } catch (SocketException e) { throw new RuntimeException(e); }

        Tools.add2Log("Initializing...", this);
        Tools.add2Log("IPFS Url: " + ipfsUrl, this);
        Tools.add2Log("Current address: " + address, this);
        Tools.add2Log("-----------------------\n", this);
        /* 初始化完成 */

        //持续检测最新的区块并保存到本地
        blockListener = new BlockListener(this, udpHelper);
        blockListener.startListening();

        //相机初始化
        imageHandler = new ImageHandler(this, new FileUploader());
        imageHandler.registerCameraLauncher(this);
    }

    // 用户点击按钮后的操作
    public void onActionTakePhoto(View view) { imageHandler.startCamera(); }

    public void handleImageResult(String serverResponse, String hash) {
        try {
            String pictureName = new JSONObject(serverResponse).getString("filename");
            Tools.add2Log("Successful photography, image name:\n"
                    + pictureName + "\n"
                    + "Image HASH: \n"
                    + hash, this);
            handleImageUpload(pictureName, hash);
        } catch (JSONException e) {
            Log.e("HandleImageResult", "Error when handle image result ", e);
        }
    }

    public void handleImageUpload(String pictureUrl, String fileHash) throws JSONException {
        Block block = new Block(address, blockListener.getLastBlockHash(), blockListener.getLastBlockNum() + 1);

        block.setTimestamp(Tools.getTimestamp());
        block.setPictureHash(fileHash);
        block.setPictureUrl(pictureUrl);

        blockHandler = new BlockHandler(this, block, blockListener, udpHelper, NONCE_DIFFICULTY);

        //计算nonce并广播区块
        blockHandler.startMining();
    }
}
