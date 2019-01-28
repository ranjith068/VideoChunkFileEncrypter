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
import com.ramzi.chunkproject.utils.HelperUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by voltella on 17/1/19.
 *   Encryption class
 * @auther Ramesh M Nair
 */
public class CipherEncryption {

    /***
     * @param chunkFileDir which give the file to be encrypted
     * @param context Context
     * @param isProperty check if property file or not
     * */
    public static boolean Encrypt(File chunkFileDir, Context context, boolean isProperty)
    {
        String fileName=chunkFileDir.getAbsolutePath();
        String secetKey=null;

        /**
         * Two type of key is used one for property and one for file pieces
         * */
        if(isProperty)
        {
             secetKey=(new StringBuilder()).append(HelperUtils.getInstance().secretToken(context)).toString();

        }
        else
        {
             secetKey=(new StringBuilder()).append(HelperUtils.getInstance().secretToken(context)).append(chunkFileDir.getParentFile().getName().replaceAll(" ","")).toString();
        }
        Log.d("encryption olakz",secetKey);

        byte[] key = CipherCommon.PBKDF2(secetKey.toCharArray(), CipherCommon.salt);
        SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, CipherCommon.AES_ALGORITHM);
        IvParameterSpec mIvParameterSpec = new IvParameterSpec(CipherCommon.iv);
        Cipher mCipher = null;
        try {
            mCipher = Cipher.getInstance(CipherCommon.AES_TRANSFORMATION);
            mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName+".enc");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, mCipher);

        byte buffer[] = new byte[1024 * 1024];
        int bytesRead;
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                Log.d("polakku", "reading from http...");

                cipherOutputStream.write(buffer, 0, bytesRead);

            }

            inputStream.close();
            cipherOutputStream.close();
            Log.d("Done","dunno>>>");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




}
