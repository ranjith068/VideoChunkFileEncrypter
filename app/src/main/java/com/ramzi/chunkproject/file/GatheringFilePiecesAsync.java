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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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

    public GatheringFilePiecesAsync(Context context,File chunkFileDir, MediaFileCallback mediaFileCallback) {
        this.chunkFileDir = chunkFileDir;
        this.context=context;
        this.mediaFileCallback=mediaFileCallback;
    }

    @Override
    protected Void doInBackground(Void... voids) {

//        Properties prop = new Properties();
//        FileInputStream fis = null;
        File propertyFile = null;
        propertyFile = new File(chunkFileDir.getAbsoluteFile(), chunkFileDir.getName() + ".cfg.enc");
//            fis = new FileInputStream(propertyFile);
        if(propertyFile!=null&&propertyFile.exists()) {
//            try {
//                prop.load(fis);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            PropertyData prop = null;
            try {
                String propertyData=CipherDecryption.PropertyFileDecrypt(propertyFile,context);
                Log.d("TAG",propertyData);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                prop=gson.fromJson(propertyData, PropertyData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                filename = prop.fileName;
                filecount = prop.fileCount;
                videoLength = prop.videoLength;
                String fileExtention = prop.extention;
                Log.d("i gat",fileExtention);

                String secertKey= (new StringBuilder()).append(HelperUtils.getInstance().secretToken(context)).append(prop.filekey).toString();
                Log.d("decrypt olakz",secertKey);
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
                mediaSource = new ConcatenatingMediaSource(mediaSourcesToLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mediaSource != null) {
            mediaFileCallback.onMediaFileRecieve(mediaSource, filename, videoLength,filecount);
        } else {
            mediaFileCallback.onMediaFileRecieve(false);

        }
    }

    public class PropertyData{
        @SerializedName("file_name")
        public String fileName;
        @SerializedName("f_ext")
        public String extention;
        @SerializedName("v_length")
        public Long videoLength;
        @SerializedName("part_count")
        public int fileCount;
        @SerializedName("f_key")
        public String filekey="";

    }
}
