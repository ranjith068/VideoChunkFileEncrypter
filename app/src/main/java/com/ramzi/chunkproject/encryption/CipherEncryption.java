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

        final byte[] iv = {65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45};
        final byte[] salt = {65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45};

//      byte[] iv = generateSecureBytes(IV_LENGTH);
//      final byte[] key = { 0, 42, 2, 54, 4, 45, 6, 7, 65, 9, 54, 11, 12, 13, 60, 15, 65, 9, 54, 11, 12, 13, 60, 15 };
//    secureRandom.nextBytes(key);
//    secureRandom.nextBytes(iv);kolmklja
//    byte[] salt = generateSecureBytes(SALT_LENGTH);
        byte[] key = PBKDF2("kolmklja".toCharArray(), salt);
        SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, AES_ALGORITHM);
        IvParameterSpec mIvParameterSpec = new IvParameterSpec(iv);
        Cipher mCipher = null;
        try {
            mCipher = Cipher.getInstance(AES_TRANSFORMATION);
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
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


    private static byte[] PBKDF2(char[] password, byte[] salt) {
        try {
//            [C@17ec0b5
            Log.d("kko",password.length+"");
//        Use PBKDF2WithHmacSHA512 for java 8
//    SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKey secretKey = secretKeyFactory.generateSecret(spec);

            return secretKey.getEncoded();
        }
        catch(Exception error)
        {
            System.out.println("Error: " + error.getMessage());
            return null;
        }
    }

    private static int PBKDF2_ITERATIONS = 50000;
    private static int KEY_LENGTH = 256;
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";

}
