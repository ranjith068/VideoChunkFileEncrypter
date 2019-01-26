package com.ramzi.chunkproject.encryption;

import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by voltella on 17/1/19.
 *
 * @auther Ramesh M Nair
 */
public class CipherEncryption {

    public static boolean Encrypt(String fileName)
    {

        byte[] key = CipherCommon.PBKDF2("kolmklja".toCharArray(), CipherCommon.salt);
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
