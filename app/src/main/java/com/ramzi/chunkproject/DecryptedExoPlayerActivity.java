package com.ramzi.chunkproject;

import android.app.Dialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.Toast;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.*;
import com.ramzi.chunkproject.encryption.CipherCommon;
import com.ramzi.chunkproject.file.GatheringFilePiecesAsync;
import com.ramzi.chunkproject.player.MediaFileCallback;
import com.ramzi.chunkproject.player.encryptionsource.EncryptedFileDataSourceFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;

import static com.ramzi.chunkproject.utils.HelperUtils.SECOUND_TO_SPLIT;


public class DecryptedExoPlayerActivity extends AppCompatActivity implements MediaFileCallback {

    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    private PlayerView mExoPlayerView;
    private boolean mExoPlayerFullscreen = false;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private Dialog mFullScreenDialog;

    private int mResumeWindow;
    private long mResumePosition;


    ConcatenatingMediaSource mediaSourcess;
    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;
    SimpleExoPlayer player;

    //    private static final String ENCRYPTED_FILE_NAME = "0.mp4.enc";
//    private static final String ENCRYPTED_FILE_NAME2 = "1.mp4.enc";
//    private static final String ENCRYPTED_FILE_NAME3 = "2.mp4.enc";
//    private static final String ENCRYPTED_FILE_NAME4 = "3.mp4.enc";
//    private File mEncryptedFile;
//    private File mEncryptedFile2;
//    private File mEncryptedFile3;
//    private File mEncryptedFile4;
    SeekBar seekBar;

    long totalLength;
    int lastindex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.exoplayer_activity);
        seekBar = (SeekBar) findViewById(R.id.seek);

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }

        byte[] key = CipherCommon.PBKDF2("kolmklja".toCharArray(), CipherCommon.salt);
        mSecretKeySpec = new SecretKeySpec(key, CipherCommon.AES_ALGORITHM);
        mIvParameterSpec = new IvParameterSpec(CipherCommon.iv);

        try {
            mCipher = Cipher.getInstance(CipherCommon.AES_TRANSFORMATION);
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mEncryptedFile = new File(Environment.getExternalStorageDirectory(), ENCRYPTED_FILE_NAME);
//        mEncryptedFile2 = new File(Environment.getExternalStorageDirectory(), ENCRYPTED_FILE_NAME2);
//        mEncryptedFile3 = new File(Environment.getExternalStorageDirectory(), ENCRYPTED_FILE_NAME3);
//        mEncryptedFile4 = new File(Environment.getExternalStorageDirectory(), ENCRYPTED_FILE_NAME4);

        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
//                    updateTime((long) (progress / 100.0f * player.getDuration()));
                    seekToPart(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                timer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                player.seekTo((int) (player.getDuration() / 100.0f * seekBar.getProgress()));
            }
        });

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);

        super.onSaveInstanceState(outState);
    }


    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }


    private void openFullscreenDialog() {

        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(DecryptedExoPlayerActivity.this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }


    private void closeFullscreenDialog() {

        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(DecryptedExoPlayerActivity.this, R.drawable.ic_fullscreen_expand));
    }


    private void initFullscreenButton() {

        PlaybackControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }


    private void initExoPlayer() {


//        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();

        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mExoPlayerView.setPlayer(player);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            player.seekTo(mResumeWindow, mResumePosition);
        }

    /*    player.prepare(mediaSourcess);
        player.setPlayWhenReady(true);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Toast.makeText(getApplicationContext(),"Changing...",1).show();
                player.seekTo(3,10000);
            }
        }, 5000);*/

    }


    @Override
    protected void onResume() {

        super.onResume();

        if (mExoPlayerView == null) {

            mExoPlayerView = (PlayerView) findViewById(R.id.exoplayer);
            initFullscreenDialog();
            initFullscreenButton();
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

//                Log.d("Playing location", "Data" + mEncryptedFile.getAbsolutePath());
//                Uri uri = Uri.fromFile(mEncryptedFile);
//                Uri uri2 = Uri.fromFile(mEncryptedFile2);
//                Uri uri3 = Uri.fromFile(mEncryptedFile3);
//                Uri uri4 = Uri.fromFile(mEncryptedFile4);
//            DefaultBandwidthMeter  defaultBandwidthMeter = new DefaultBandwidthMeter();
//
//            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
//                    Util.getUserAgent(this, "mediaPlayerSample"),defaultBandwidthMeter);
//            MediaSource videoSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
//            MediaSource videoSource2 = new ExtractorMediaSource(uri2, dataSourceFactory, extractorsFactory, null, null);
//            MediaSource videoSource3 = new ExtractorMediaSource(uri3, dataSourceFactory, extractorsFactory, null, null);
//
//            MediaSource videoSource4 = new ExtractorMediaSource(uri4, dataSourceFactory, extractorsFactory, null, null);

               /* MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory)
                        .createMediaSource(uri);
                MediaSource videoSource2 = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory)
                        .createMediaSource(uri2);
                MediaSource videoSource3 = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory)
                        .createMediaSource(uri3);
                MediaSource videoSource4 = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory)
                        .createMediaSource(uri4);
//            player.prepare(videoSource);

//            player.prepare(new MergingMediaSource(videoSource,videoSource2));
                MediaSource[] mediaSourcesToLoad = new MediaSource[4];
                mediaSourcesToLoad[0] = videoSource;
                mediaSourcesToLoad[1] = videoSource2;
                mediaSourcesToLoad[2] = videoSource3;
                mediaSourcesToLoad[3] = videoSource4;
                 mediaSourcess = new ConcatenatingMediaSource(mediaSourcesToLoad);*/
            File chunkFileDirectory = new File(getIntent().getExtras().getString("file_dir"));
            new GatheringFilePiecesAsync(chunkFileDirectory, DecryptedExoPlayerActivity.this, dataSourceFactory, extractorsFactory).execute();


        } else {
            initExoPlayer();
        }


        if (mExoPlayerFullscreen) {
            ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
            mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(DecryptedExoPlayerActivity.this, R.drawable.ic_fullscreen_skrink));
            mFullScreenDialog.show();
        }
    }


    @Override
    protected void onPause() {

        super.onPause();

        if (mExoPlayerView != null && mExoPlayerView.getPlayer() != null) {

            mResumeWindow = mExoPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, mExoPlayerView.getPlayer().getContentPosition());
            mExoPlayerView.getPlayer().release();

        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss();
    }

    @Override
    public void onMediaFileRecieve(ConcatenatingMediaSource mediaSource, String filename, long totalTime,int totalIndex) {
        if (mediaSource != null) {
            Toast.makeText(getApplicationContext(), "Media player is playing....", Toast.LENGTH_SHORT).show();
            initExoPlayer();

            if (mExoPlayerView != null && player != null) {
                totalLength = totalTime;
                lastindex=totalIndex;
                player.prepare(mediaSource);
                player.setPlayWhenReady(true);
            }
        }

    }

    @Override
    public void onMediaFileRecieve(boolean status) {
        Toast.makeText(getApplicationContext(), "Gathering files failed", Toast.LENGTH_SHORT).show();

    }


    public void seekToPart(int currentProgress) {

        long currentSeekingTime = (long) (currentProgress / 100.0f * totalLength);
        int index = (int) Math.floor(currentSeekingTime / SECOUND_TO_SPLIT);
        long currentIndexSeekValue = 0;
        if ((index + 1) == lastindex) {

            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);
        } else {
            currentIndexSeekValue = (SECOUND_TO_SPLIT * (index + 1)) - currentSeekingTime;
        }

        Log.d("Value>>>>", " datatata \nTotalTime :" + totalLength + "\n Current Time : "
                + currentSeekingTime + "\n index:" + index + "-----" + "indexseek " + currentIndexSeekValue);
        player.seekTo(index, currentIndexSeekValue);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
//            exoPlayer.seekTo(0);
        }
    }
}
