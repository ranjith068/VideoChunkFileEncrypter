package com.ramzi.chunkproject.player;

import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Created by voltella on 23/1/19.
 *
 * @auther Ramesh M Nair
 */


public class PlayerEventListener implements Player.EventListener {



    @Override
    public void onTracksChanged(TrackGroupArray trackGroups,

                                TrackSelectionArray trackSelections) {

        Log.d("Moveee","track changed");
    }
    @Override
    public void onLoadingChanged(boolean isLoading) {}

    @Override
    public void onPlayerStateChanged(boolean playWhenReady,int playbackState) {
        Log.d("Moveee",playWhenReady+"55555555555"+playbackState);

    }

    @Override
    public void onSeekProcessed() {
        Log.d("Seeking","seeker.....");
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.d("Moveee","44444444444444444");

    }



    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.d("Moveee","11111111111111");
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
        Log.d("Moveee","33333333333333333");

//        Log.d("GetVin")

    }

//    public interface StateChecking
//    {
//        void
//    }
}

