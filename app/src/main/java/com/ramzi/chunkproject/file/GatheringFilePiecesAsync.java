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
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ramzi.chunkproject.encryption.CipherCommon;
import com.ramzi.chunkproject.encryption.CipherDecryption;
import com.ramzi.chunkproject.player.MediaFileCallback;
import com.ramzi.chunkproject.player.encryptionsource.EncryptedFileDataSourceFactory;
import com.ramzi.chunkproject.utils.Constants;
import com.ramzi.chunkproject.utils.HelperUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Properties;

import static com.ramzi.chunkproject.ChunkMainActivity.TAG;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public class GatheringFilePiecesAsync extends AsyncTask<Void, Void, Void> {

    ConcatenatingMediaSource mediaSource;
    //    MediaFileCallback mediaFileCallback;
    File chunkFileDir;
    //    DataSource.Factory dataSourceFactory;
//    ExtractorsFactory extractorsFactory;
    String filename;
    long videoLength;
    int filecount;
    Context context;


    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;
    private MediaFileCallback mediaFileCallback;

    public GatheringFilePiecesAsync(Context context, File chunkFileDir, MediaFileCallback mediaFileCallback) {
        this.chunkFileDir = chunkFileDir;
        this.context = context;
        this.mediaFileCallback = mediaFileCallback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        /*Check for cfg files*/
        final String[] cfgFileName = {null};
        chunkFileDir.list((dir, name) -> {
            if (name.toLowerCase().endsWith(".cfg.enc")) {
                cfgFileName[0] = name;

                return true;
            } else {
                return false;
            }
        });

        if (cfgFileName[0] != null) {
            File propertyFile = null;
            propertyFile = new File(chunkFileDir.getAbsoluteFile(), cfgFileName[0]);
            Log.d(TAG, "Yesssss before" + propertyFile.getAbsolutePath());

            if (propertyFile != null && propertyFile.exists()) {

                PropertyData prop = null;
                try {
                    String propertyData = CipherDecryption.PropertyFileDecrypt(propertyFile, context);
                    Log.d("TAG", propertyData);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    prop = gson.fromJson(propertyData, PropertyData.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    filename = prop.fileName;
                    filecount = prop.fileCount;
                    videoLength = prop.videoLength;
                    String fileExtention = prop.extention;
                    /**
                     * Setting exoplayer with cipher data for real time decryption of files
                     * */
                    String secertKey = (new StringBuilder()).append(HelperUtils.getInstance().secretToken(context)).append(prop.filekey).toString();
                    byte[] key = CipherCommon.PBKDF2(secertKey.toCharArray(), CipherCommon.salt);
                    mSecretKeySpec = new SecretKeySpec(key, CipherCommon.AES_ALGORITHM);
                    mIvParameterSpec = new IvParameterSpec(CipherCommon.iv);

                    try {
                        mCipher = Cipher.getInstance(CipherCommon.AES_TRANSFORMATION);
                        mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                    DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter);
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                    MediaSource[] mediaSourcesToLoad = new MediaSource[filecount];
                    for (int i = 0; i < filecount; i++) {
                        Uri uri = Uri.fromFile(new File(chunkFileDir.getAbsoluteFile(), i + fileExtention + ".enc"));
                        mediaSourcesToLoad[i] = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory)
                                .createMediaSource(uri);

                    }
                    /**
                     * concatenating media files
                     * */
                    mediaSource = new ConcatenatingMediaSource(mediaSourcesToLoad);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("TAG", "PROPERTYFILE NOT FOUND");
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mediaSource != null) {
            mediaFileCallback.onMediaFileRecieve(mediaSource, filename, videoLength, filecount);
        } else {
            mediaFileCallback.onMediaFileRecieve(false);

        }
    }

    /**
     * Gson model for property file
     */
    public class PropertyData {
        @SerializedName("file_name")
        public String fileName;
        @SerializedName("f_ext")
        public String extention;
        @SerializedName("v_length")
        public Long videoLength;
        @SerializedName("part_count")
        public int fileCount;
        @SerializedName("f_key")
        public String filekey = "";

    }
}
