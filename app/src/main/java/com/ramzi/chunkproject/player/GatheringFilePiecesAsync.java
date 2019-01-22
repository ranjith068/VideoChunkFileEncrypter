package com.ramzi.chunkproject.player;

import android.os.AsyncTask;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public class GatheringFilePiecesAsync extends AsyncTask<Void,Void,Void> {

    ConcatenatingMediaSource mediaSource;
    MediaFileCallback mediaFileCallback;
    String filename;
    public GatheringFilePiecesAsync(String filename,MediaFileCallback mediaFileCallback)
    {
        this.filename=filename;
        this.mediaFileCallback=mediaFileCallback;
    }
    @Override
    protected Void doInBackground(Void... voids) {


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
