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

package com.ramzi.chunkproject.encryption;

import android.content.Context;
import android.util.Log;
import com.ramzi.chunkproject.utils.Constants;
import com.ramzi.chunkproject.utils.HelperUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

import static com.ramzi.chunkproject.ChunkMainActivity.TAG;

/**
 * Created by voltella on 17/1/19.
 *
 * Class used to decryption
 *
 * @auther Ramesh M Nair
 */


public class CipherDecryption {

    /**
     *For now only property file is decrypting
     * @param chunkFileDir which give the file to be encrypted
     * @param context Context
     *
     * */
    public static String PropertyFileDecrypt(File chunkFileDir, Context context) throws IOException {

        String fileName=chunkFileDir.getAbsolutePath();
        String jsonFormat="{";
        String secetKey=(new StringBuilder()).append(HelperUtils.getInstance().secretToken(context)).toString();

        byte[] key = CipherCommon.PBKDF2(secetKey.toCharArray(), CipherCommon.salt);
        SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, CipherCommon.AES_ALGORITHM);
        IvParameterSpec mIvParameterSpec = new IvParameterSpec(CipherCommon.iv);
        Cipher mCipher = null;
        try {
            mCipher = Cipher.getInstance(CipherCommon.AES_TRANSFORMATION);
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileInputStream encFile = null;
        try {
            encFile = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        CipherInputStream cipherInputStream = new CipherInputStream(encFile, mCipher);
        try {
            cipherInputStream.skip(CipherCommon.salt.length + CipherCommon.iv.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[8192];
        int c;
        int actualSize = 0;

        while((c = cipherInputStream.read(buffer)) > 0)
        {
//            plainFile.write(buffer, 0, c);
//            Log.d("datatata",new String(buffer));

            /*Getting the property files and converting to json without creating a file */

            String[] lines = new String(buffer).split(System.getProperty("line.separator"));
            for(int i=0;i<lines.length;i++)
            {
                Log.d(TAG,lines[i]);
                if(lines[i].contains("video_length="))
                {
                    jsonFormat=jsonFormat+"\"v_length\":"+lines[i].replaceAll("video_length=","")+",";
                }
                if(lines[i].contains("part_count="))
                {
                    jsonFormat=jsonFormat+"\"part_count\":"+lines[i].replaceAll("part_count=","")+",";
                }
                if(lines[i].contains("filename="))
                {
                    jsonFormat=jsonFormat+"\"file_name\":"+"\""+lines[i].replaceAll("filename=","")+"\""+",";
                }
                if(lines[i].contains("filekey="))
                {
                    jsonFormat=jsonFormat+"\"f_key\":"+"\""+lines[i].replaceAll("filekey=","")+"\""+",";
                }
                if(lines[i].contains("fileextention="))
                {
                    jsonFormat=jsonFormat+"\"f_ext\":"+"\""+lines[i].replaceAll("fileextention=","")+"\"";
                }


            }
            jsonFormat=jsonFormat+"}";
            Log.d("dadadda",jsonFormat);
            actualSize += c;
//            progress = (int)(actualSize * 100.0 / fileSize + 0.5);
//            System.out.print("Progress: " + actualSize + " / " + fileSize + " - " + progress + "%\r");
        }
        try {
            cipherInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            encFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

       return  jsonFormat;
    }




}
