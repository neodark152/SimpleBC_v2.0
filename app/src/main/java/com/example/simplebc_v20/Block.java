package com.example.simplebc_v20;

import org.json.JSONException;
import org.json.JSONObject;

public class Block {
    private final JSONObject block = new JSONObject();

    public Block(String address, String preHash, int chainNum) throws JSONException {
        block.put("pre_hash", preHash);
        block.put("timestamp", 0L);
        block.put("chain_num", chainNum);
        block.put("nonce", 0);
        block.put("address", address);
        block.put("p_url", "");
        block.put("p_hash", "");
        block.put("tail_hash", "");
    }

    public Block() { /* 空方法，仅用于实现一个空的block对象 */ }

    public void setPictureHash(String hash) {
        try {
            block.put("p_hash", hash);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPreHash(String pre_hash) {
        try {
            block.put("pre_hash", pre_hash);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimestamp(long timestamp) {
        try {
            block.put("timestamp", timestamp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setChainNumber(int chain_num) {
        try {
            block.put("chain_num", chain_num);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNonce(int nonce) {
        try {
            block.put("nonce", nonce);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAddress(String address) {
        try {
            block.put("address", address);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPictureUrl(String p_url) {
        try {
            block.put("p_url", p_url);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTailHash(String tail_hash) {
        try {
            block.put("tail_hash", tail_hash);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTailHash() {
        try {
            return block.getString("tail_hash");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public int getChainNum() {
        try {
            return block.getInt("chain_num");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBlockString() {
        return block.toString();
    }
}