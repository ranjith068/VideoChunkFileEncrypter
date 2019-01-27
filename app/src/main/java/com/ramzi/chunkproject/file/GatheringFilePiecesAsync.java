package com.ramzi.chunkproject.file;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ramzi.chunkproject.encryption.CipherDecryption;
import com.ramzi.chunkproject.player.MediaFileCallback;
import com.ramzi.chunkproject.utils.Constants;

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
    MediaFileCallback mediaFileCallback;
    File chunkFileDir;
    DataSource.Factory dataSourceFactory;
    ExtractorsFactory extractorsFactory;
    String filename;
    long videoLength;
    int filecount;
    Context context;

    public GatheringFilePiecesAsync(Context context,File chunkFileDir, MediaFileCallback mediaFileCallback, DataSource.Factory dataSourceFactory, ExtractorsFactory extractorsFactory) {
        this.chunkFileDir = chunkFileDir;
        this.mediaFileCallback = mediaFileCallback;
        this.extractorsFactory = extractorsFactory;
        this.dataSourceFactory = dataSourceFactory;
        this.context=context;
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
                String propertyData=CipherDecryption.PropertyFileDecrypt(propertyFile.getAbsolutePath(),context);
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

    }
}
