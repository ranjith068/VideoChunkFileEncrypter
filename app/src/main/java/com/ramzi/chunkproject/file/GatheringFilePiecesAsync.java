package com.ramzi.chunkproject.file;

import android.net.Uri;
import android.os.AsyncTask;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.ramzi.chunkproject.player.MediaFileCallback;

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

    public GatheringFilePiecesAsync(File chunkFileDir, MediaFileCallback mediaFileCallback, DataSource.Factory dataSourceFactory, ExtractorsFactory extractorsFactory) {
        this.chunkFileDir = chunkFileDir;
        this.mediaFileCallback = mediaFileCallback;
        this.extractorsFactory = extractorsFactory;
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            File propertyFile = new File(chunkFileDir.getAbsoluteFile(), chunkFileDir.getName() + ".cfg");
            fis = new FileInputStream(propertyFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            filename = prop.getProperty("filename");
             filecount = Integer.parseInt(prop.getProperty("part_count"));
            videoLength = Long.parseLong(prop.getProperty("video_length"));
            String fileExtention = prop.getProperty("fileextention");
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
}
