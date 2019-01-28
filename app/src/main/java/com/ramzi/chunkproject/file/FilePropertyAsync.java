/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2019 Ramesh M Nair
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
 * Going for setting the property file
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

        /**
         * filename-name of the file
         * partcount- total part split
         * video_length- total duration of the video
         * fileextention-extention of file
         * filekey-secert key for decrypting the files
         * */
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
            encrypted= CipherEncryption.Encrypt(propertyFile,context,true);
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
