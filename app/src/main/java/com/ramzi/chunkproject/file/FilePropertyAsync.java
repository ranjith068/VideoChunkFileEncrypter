package com.ramzi.chunkproject.file;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.ramzi.chunkproject.encryption.CipherEncryption;
import com.ramzi.chunkproject.encryption.EncryptionCallback;
import com.ramzi.chunkproject.utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public class FilePropertyAsync extends AsyncTask<Void,Void,Void> {
    File chunkFileDir;
    int totalFileParts;
    long videoLength;
    EncryptionCallback encryptionCallback;
    boolean encrypted=false;
    String extention;
    Context context;
    public FilePropertyAsync(Context context,String chunkFileDirName, int totalFileParts, long videoLength, EncryptionCallback encryptionCallback, String extention)
    {
        chunkFileDir=new File(chunkFileDirName);
        this.totalFileParts=totalFileParts;
        this.videoLength=videoLength;
        this.encryptionCallback=encryptionCallback;
        this.extention=extention;
        this.context=context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        File propertyFile = null;

        if(chunkFileDir.exists()) {
            Properties prop = new Properties();
            prop.setProperty("filename", chunkFileDir.getName());
            prop.setProperty("part_count", (totalFileParts+1) + "");
            prop.setProperty("video_length", videoLength + "");
            prop.setProperty("fileextention",extention);
            prop.setProperty("filekey",chunkFileDir.getName().replaceAll(" ",""));
            propertyFile = new File(chunkFileDir.getAbsoluteFile(), chunkFileDir.getName() + ".cfg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(propertyFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                prop.store(fos, chunkFileDir.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(propertyFile!=null&&propertyFile.exists()) {
            encrypted= CipherEncryption.Encrypt(propertyFile.getAbsolutePath(),context,true);
        }
        if(encrypted)
        {
            if(propertyFile.exists())
            {
                propertyFile.delete();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("Overrr","Wrotted");

            encryptionCallback.propertyResult(encrypted);

    }
}
