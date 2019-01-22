package com.ramzi.chunkproject.encryption;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public class EncryptionAsync extends AsyncTask<Void,Void,Void> {

    public EncryptionCallback encryptionCallback;
    public String filePath;
    public boolean encryptionStatus=false;
    int filePart;
    long nextChunkStartTime;

    public EncryptionAsync(String filePath,int filePart,long nextChunkStartTime,EncryptionCallback encryptionCallback)
    {
        this.filePath=filePath;
        this.encryptionCallback=encryptionCallback;
        this.nextChunkStartTime=nextChunkStartTime;
        this.filePart=filePart;

    }
    @Override
    protected Void doInBackground(Void... voids) {

        File chunkFile=new File(filePath);
        if(chunkFile.exists())
        {
            encryptionStatus=CipherEncryption.Encrypt(filePath);


        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        encryptionCallback.encryptionResult(encryptionStatus,filePart,nextChunkStartTime);
    }
}
