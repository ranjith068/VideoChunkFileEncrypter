package com.ramzi.chunkproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.*;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.ramzi.chunkproject.encryption.CipherCommon;
import com.ramzi.chunkproject.file.GatheringFilePiecesAsync;
import com.ramzi.chunkproject.player.MediaFileCallback;
import com.ramzi.chunkproject.player.animation.PlayIconDrawable;
import com.ramzi.chunkproject.player.encryptionsource.EncryptedFileDataSourceFactory;
import com.ramzi.chunkproject.player.gestures.GestureListener;
import com.ramzi.chunkproject.utils.HelperUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.ramzi.chunkproject.utils.HelperUtils.SECOUND_TO_SPLIT;

//import com.ramzi.chunkproject.player.PlayerEventListener;


public class DecryptedExoPlayerBackupActivity extends AppCompatActivity implements Player.EventListener, MediaFileCallback {


    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;

    SimpleExoPlayer player;
    private PlayerView mExoPlayerView;


    AppCompatSeekBar seekBar;
    TextView timeText, seekStatus;
    ImageView playPauseImageView;
    PlayIconDrawable play;
    RelativeLayout playerControlLayer;

    long totalLength;
    boolean isPlayer = false;
    boolean isSeeking = false;
    String totalTimeStamp;

    int screenWidth;
    boolean gestureSeek = false;
    int gestureSeekIndex = 0;
    long gestureSeekPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.exoplayer_activity);
        seekBar = findViewById(R.id.seek);
        timeText = findViewById(R.id.time_text);
        seekStatus = findViewById(R.id.seek_ststus);
        mExoPlayerView = findViewById(R.id.exoplayer);
        playerControlLayer=findViewById(R.id.player_control);
        playPauseImageView = findViewById(R.id.pause_play_button);


        byte[] key = CipherCommon.PBKDF2("kolmklja".toCharArray(), CipherCommon.salt);
        mSecretKeySpec = new SecretKeySpec(key, CipherCommon.AES_ALGORITHM);
        mIvParameterSpec = new IvParameterSpec(CipherCommon.iv);

        try {
            mCipher = Cipher.getInstance(CipherCommon.AES_TRANSFORMATION);
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }


        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isSeeking = true;
                    seekToPart(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                isSeeking = false;
            }

        });


        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        setUpGestureControls();
        initExoPlayer();

        File chunkFileDirectory = new File(getIntent().getExtras().getString("file_dir"));
        new GatheringFilePiecesAsync(chunkFileDirectory, DecryptedExoPlayerBackupActivity.this, dataSourceFactory, extractorsFactory).execute();
        play = PlayIconDrawable.builder()
                .withColor(Color.WHITE)
                .withInterpolator(new FastOutSlowInInterpolator())
                .withDuration(300)
                .withInitialState(PlayIconDrawable.IconState.PAUSE)
                .withAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Log.d("Animation", "animationFinished");
                    }
                })
                .withStateListener(new PlayIconDrawable.StateListener() {
                    @Override
                    public void onStateChanged(PlayIconDrawable.IconState state) {
                        Log.d("IconState", "onStateChanged: " + state);

                    }
                })
                .into(playPauseImageView);

        playPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (play.getIconState() == PlayIconDrawable.IconState.PAUSE) {

                    pausePlayer();
                } else if (play.getIconState() == PlayIconDrawable.IconState.PLAY) {
                   startPlayer();

                }

            }
        });

    }


    private void initExoPlayer() {


        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();

        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mExoPlayerView.setPlayer(player);
        mExoPlayerView.setUseController(false);
        player.addListener(this);

    }


    @Override
    protected void onResume() {

        super.onResume();

        startPlayer();


    }

    private void setUpGestureControls() {
        mExoPlayerView.setOnTouchListener(new ExVidPlayerGestureListener(DecryptedExoPlayerBackupActivity.this));
        mExoPlayerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mExoPlayerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mExoPlayerView.getHeight(); //height is ready
                screenWidth = mExoPlayerView.getWidth();
            }
        });
    }


    @Override
    protected void onPause() {

        super.onPause();

        pausePlayer();
    }

    @Override
    public void onMediaFileRecieve(ConcatenatingMediaSource mediaSource, String filename, long totalTime, int totalIndex) {
        if (mediaSource != null) {
            if (mExoPlayerView != null && player != null) {
                totalLength = totalTime;
                totalTimeStamp = String.format("%02d", ((totalLength / 1000) / 60) / 60) + ":" + String.format("%02d", ((totalLength / 1000) / 60) % 60) + ":" + String.format("%02d", (totalLength/1000) % 60);
                player.prepare(mediaSource);
                play.setIconState(PlayIconDrawable.IconState.PAUSE);

                player.setPlayWhenReady(true);
                mExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
        }

    }

    @Override
    public void onMediaFileRecieve(boolean status) {
        Toast.makeText(getApplicationContext(), "Gathering files failed", Toast.LENGTH_SHORT).show();

    }

    public void seekToTime() {

        int index = player.getCurrentWindowIndex();
        long currentIndexSeekValue = 0;

        if (index == 0) {
            currentIndexSeekValue = player.getContentPosition();
        } else {
            currentIndexSeekValue = ((SECOUND_TO_SPLIT * index) + player.getContentPosition());
        }
        changeTimeTextView(currentIndexSeekValue);


    }

    public void seekToPart(int currentProgress) {

        seekToTime();
        long currentSeekingTime = (long) (currentProgress / 100.0f * totalLength);
        int index = (int) Math.floor(currentSeekingTime / SECOUND_TO_SPLIT);
        long currentIndexSeekValue = 0;

        if (index == 0) {
            currentIndexSeekValue = currentSeekingTime;
        } else {
            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);

        }

        Log.d("Value>>>>", currentProgress + " datatata \nTotalTime :" + totalLength + "\n Current Time : "
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

        if (mHandler != null) {
            mHandler.removeCallbacks(updateProgressAction);
        }
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        isPlayer = playWhenReady;
        if (playWhenReady && playbackState == Player.STATE_READY) {

            mHandler = new Handler();
            mHandler.post(updateProgressAction);

        }

        if (playbackState == Player.STATE_ENDED) {
            if (play != null) {
                play.setIconState(PlayIconDrawable.IconState.PLAY);
            }

            player.setPlayWhenReady(false);
            player.seekTo(0, 0);
            seekBar.setProgress(0);
            if (mHandler != null) {

                mHandler.removeCallbacks(updateProgressAction);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (player != null) {
            player.release();
        }

    }

    private Handler mHandler;


    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            Log.d("getcurrent", "lalu out>>" + player.getCurrentPosition());
            updateProgress();
        }
    };

    public void updateProgress() {
        seekToTime();

        if (isPlayer) {

            long delayMs = TimeUnit.SECONDS.toMillis(1);
            mHandler.postDelayed(updateProgressAction, delayMs);

            if (!isSeeking) {
                if (player.getCurrentWindowIndex() == 0) {
                    seekBar.setProgress((int) (player.getCurrentPosition() * 1.0f / totalLength * 100));
                } else {
                    long currentposition = player.getCurrentPosition() + (HelperUtils.SECOUND_TO_SPLIT * player.getCurrentWindowIndex());
                    seekBar.setProgress((int) (currentposition * 1.0f / totalLength * 100));

                }

            }

        }
    }

    public void changeTimeTextView(long milliSec) {
//       long hour= TimeUnit.MILLISECONDS.toHours(millesecound);
        long sec = (milliSec / 1000) % 60;
        long min = ((milliSec / 1000) / 60) % 60;
        long hour = ((milliSec / 1000) / 60) / 60;
//        long sec = (totalLength/1000) % 60;
//        long min = ((totalLength/1000) / 60) % 60;
//        long hour = ((totalLength/1000) / 60) / 60;
        String secd = "" + (milliSec / 1000) % 60;
        String mind = "" + ((milliSec / 1000) / 60) % 60;
        String hourd = "" + ((milliSec / 1000) / 60) / 60;
        if (sec < 10) {
            secd = "0" + secd;
        }
        if (min < 10) {
            mind = "0" + mind;
        }
        if (hour < 10) {
            hourd = "0" + hourd;
        }
        String currentTImestamp = hourd + ":" + mind + ":" + secd;

        timeText.setText(currentTImestamp + "/" + totalTimeStamp);
//        timeText.setText((hour>10)?hour:"0"+hour);
//       timeText.setText(((milliSec/1000) / 60) / 60>0?((milliSec/1000) / 60) / 60:"0"+((milliSec/1000) / 60) / 60+":"+TimeUnit.MILLISECONDS.toMinutes(millesecound)+":"+TimeUnit.MILLISECONDS.toSeconds(millesecound));
    }

    int currentIndex = 0;

    private class ExVidPlayerGestureListener extends GestureListener {
        ExVidPlayerGestureListener(Context ctx) {
            super(ctx);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
//            Log.d("index",currentIndex+"oyaaaaaaaaaaaaaaaaaaaaaaaa"+motionEvent.getAction());
//            if() {
            if (gestureSeek) {
                seekStatus.setVisibility(View.GONE);

                if (motionEvent.getAction() == 0) {
                    Log.d("get>>>", "yes got new index" + currentIndex);


                } else if (motionEvent.getAction() == 1) {
                    if (player != null) {
                        try {
                            Log.d("get>>>", gestureSeekPosition + "yes got ne2222w index" + gestureSeekIndex);

                            player.seekTo(gestureSeekIndex, gestureSeekPosition);
                        } catch (Exception e) {
                            Log.d("get>>>", "yes error");

                            player.seekTo(gestureSeekIndex, totalLength - (gestureSeekIndex * SECOUND_TO_SPLIT) - 1000);

                        }
                    }
//                    startPlayer();
                    play.setIconState(PlayIconDrawable.IconState.PAUSE);
                    player.setPlayWhenReady(true);
                    gestureSeek = false;
                }
            }
            return super.onTouch(view, motionEvent);
        }

        @Override
        public void onTap() {
            if (playerControlLayer.getVisibility() == View.VISIBLE) {
                playerControlLayer.setVisibility(View.GONE);
            } else {
                playerControlLayer.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onHorizontalScroll(MotionEvent event, float delta) {
            seekStatus.setVisibility(View.VISIBLE);
            gestureSeek = true;
            currentIndex = player.getCurrentWindowIndex();

            pausePlayer();
            Log.d("tendiz", screenWidth + "swiping horizontaly" + delta + ">>>>motion" + event.getAction());

            long perscreen = SECOUND_TO_SPLIT / screenWidth;


            if (delta * perscreen < 0) {
                seekStatus.setText("-" + TimeUnit.MILLISECONDS.toSeconds((long) (0 - (delta * perscreen))));
                Log.d("tendiz", delta * perscreen + "Scroll to incrise= Minus" + (SECOUND_TO_SPLIT + (delta * perscreen)));

                seekToGesture((long) (0 - (delta * perscreen)), false);

            } else {
                Log.d("tendiz", perscreen + "Scroll to incrise= Adding " + delta * perscreen);
                seekStatus.setText("+" + TimeUnit.MILLISECONDS.toSeconds((long) (delta * perscreen)));
                seekToGesture((long) (delta * perscreen), true);

            }

        }

        private void seekToGesture(long seekValue, boolean forward) {
            Log.d("gesturerchunk", "/////////////////////////////////////start////////////////////////////");


            long currentPosition;

            if (currentIndex == 0) {

                currentPosition = player.getCurrentPosition();
            } else {
                currentPosition = player.getCurrentPosition() + (HelperUtils.SECOUND_TO_SPLIT * currentIndex);


            }
            if (forward) {
                currentPosition = currentPosition + seekValue;
            } else {
                currentPosition = currentPosition - seekValue;
            }

            int index = (int) Math.floor(currentPosition / SECOUND_TO_SPLIT);

            long newIndexSeekValue;

            if (index == 0) {
                newIndexSeekValue = currentPosition;
            } else {
                newIndexSeekValue = currentPosition - (SECOUND_TO_SPLIT * index);

            }
            gestureSeekIndex = index;
            gestureSeekPosition = newIndexSeekValue;

        }

        @Override
        public void onVerticalScroll(MotionEvent event, float delta) {

            if (event.getPointerCount() == ONE_FINGER) {
//                updateBrightnessProgressBar(extractVerticalDeltaScale(-delta, pBarBrighness));
                Log.d("tendiz", "GO FOR BRIGness");
            } else {
                Log.d("tendiz", "GO FOR Volume");

//                updateVolumeProgressBar(extractVerticalDeltaScale(-delta, pBarVolume));
            }
        }

        @Override
        public void onSwipeRight() {
            Log.d("tendiz", "Swipe right");

        }

        @Override
        public void onSwipeLeft() {
            Log.d("tendiz", "Swipe left");

        }

        @Override
        public void onSwipeBottom() {
            Log.d("tendiz", "Swipe left");

        }

        @Override
        public void onSwipeTop() {
            Log.d("tendiz", "Swipe left");

        }
    }









    /*currentApiVersion = android.os.Build.VERSION.SDK_INT;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

// This work only for android 4.4+
if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
    {

        getWindow().getDecorView().setSystemUiVisibility(flags);

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        final View decorView = getWindow().getDecorView();
        decorView
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });
    }



    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }*/


    private void pausePlayer() {
        if (player != null) {
            if (play != null) {
                play.setIconState(PlayIconDrawable.IconState.PLAY);
            }

            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void startPlayer() {
        if (player != null) {
            if (play != null) {
                play.setIconState(PlayIconDrawable.IconState.PAUSE);
            }

            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }
}
