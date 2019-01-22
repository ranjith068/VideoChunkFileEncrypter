package com.ramzi.chunkproject.player;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

/**
 * Created by voltella on 22/1/19.
 *
 * @auther Ramesh M Nair
 */
public interface MediaFileCallback {

    public void onMediaFileRecieve(ConcatenatingMediaSource mediaSource);
}
