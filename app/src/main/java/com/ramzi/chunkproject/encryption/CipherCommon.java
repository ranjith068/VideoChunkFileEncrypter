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


import android.util.Log;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Created by voltella on 22/1/19.
 *
 * Class contains common things used in encryption
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
