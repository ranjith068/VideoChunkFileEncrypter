package com.ramzi.chunkproject.encryption;


import android.util.Log;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public class CipherCommon {
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";

    private static int PBKDF2_ITERATIONS = 50000;
    private static int KEY_LENGTH = 256;
    public static byte[] iv = {65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45};
    public static  byte[] salt = {65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45};


    public static byte[] PBKDF2(char[] password, byte[] salt) {
        try {
//            [C@17ec0b5
            Log.d("kko", password.length + "");
//        Use PBKDF2WithHmacSHA512 for java 8
//    SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKey secretKey = secretKeyFactory.generateSecret(spec);

            return secretKey.getEncoded();
        } catch (Exception error) {
            System.out.println("Error: " + error.getMessage());
            return null;
        }
    }
}
