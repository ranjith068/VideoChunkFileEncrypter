package com.ramzi.chunkproject.encryption;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public interface EncryptionCallback {

    public void encryptionResult(boolean status,int part,long nextChunkFileStartTime);
}
