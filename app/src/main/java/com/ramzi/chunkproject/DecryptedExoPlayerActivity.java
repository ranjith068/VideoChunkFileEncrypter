package com.ramzi.chunkproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.google.android.exoplayer2.C;
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
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.*;
import com.ramzi.chunkproject.encryption.CipherCommon;
import com.ramzi.chunkproject.file.GatheringFilePiecesAsync;
import com.ramzi.chunkproject.player.MediaFileCallback;
//import com.ramzi.chunkproject.player.PlayerEventListener;
import com.ramzi.chunkproject.player.encryptionsource.EncryptedFileDataSourceFactory;
import com.ramzi.chunkproject.player.gestures.GestureListener;
import com.ramzi.chunkproject.utils.HelperUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.ramzi.chunkproject.player.gestures.GestureListener.ONE_FINGER;
import static com.ramzi.chunkproject.utils.HelperUtils.SECOUND_TO_SPLIT;


public class DecryptedExoPlayerActivity extends AppCompatActivity implements Player.EventListener, MediaFileCallback {

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

    Handler mainHandler = new Handler();
    private Timer timer;
    int seekposition=0;

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
    boolean isPlayer=false;
    boolean isSeeking=false;
    TextView timeText,seekStatus;
    String totalTimeTImestamp;
    int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.exoplayer_activity);
        seekBar = (SeekBar) findViewById(R.id.seek);
        timeText=(TextView)findViewById(R.id.time_text);
        seekStatus=(TextView)findViewById(R.id.seek_ststus);
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
                    isSeeking=true;
                    seekposition=progress;
                    seekToPart(progress,true);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking=true;
//                timer.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                seekToPart((int) (player.getDuration() / 100.0f * seekBar.getProgress()));
//                player.seekTo((int) (player.getDuration() / 100.0f * seekBar.getProgress()));
                seekposition=seekBar.getProgress();
//                seekToPart(seekBar.getProgress());
                isSeeking=false;
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

//        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
//        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(DecryptedExoPlayerActivity.this, R.drawable.ic_fullscreen_skrink));
//        mExoPlayerFullscreen = true;
//        mFullScreenDialog.show();
    }


    private void closeFullscreenDialog() {

//        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
//        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
//        mExoPlayerFullscreen = false;
//        mFullScreenDialog.dismiss();
//        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(DecryptedExoPlayerActivity.this, R.drawable.ic_fullscreen_expand));
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
        mExoPlayerView.setUseController(false);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            player.seekTo(mResumeWindow, mResumePosition);
        }

        mExoPlayerView.getPlayer().addListener(this);

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

//            initFullscreenDialog();
//            initFullscreenButton();
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            setUpGestureControls();

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

    private void setUpGestureControls() {
        mExoPlayerView.setOnTouchListener(new ExVidPlayerGestureListener(DecryptedExoPlayerActivity.this));
        mExoPlayerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mExoPlayerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mExoPlayerView.getHeight(); //height is ready
                Log.d("widthmovetest",mExoPlayerView.getWidth()+">>>>");
                screenWidth=mExoPlayerView.getWidth();
            }
        });
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
                long sec = (totalLength/1000) % 60;
                long min = ((totalLength/1000) / 60) % 60;
                long hour = ((totalLength/1000) / 60) / 60;
                String secd=""+(totalLength/1000) % 60;
                String mind=""+((totalLength/1000) / 60) % 60;
                String hourd=""+((totalLength/1000) / 60) / 60;
                if(sec<10)
                {
                    secd="0"+secd;
                }
                if(min<10)
                {
                    mind="0"+mind;
                }
                if(hour<10)
                {
                    hourd="0"+hourd;
                }
                totalTimeTImestamp=hourd+":"+mind+":"+secd;
//                changeTimeTextView(totalLength);
                lastindex=totalIndex;
                player.prepare(mediaSource);
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
//        long currentSeekingTime = (long) (currentProgress / 100.0f * totalLength);
//        int index = (int) Math.floor(currentSeekingTime / SECOUND_TO_SPLIT);
        int index=player.getCurrentWindowIndex();
        long currentIndexSeekValue = 0;
//        seekBar.se
       /* if ((index + 1) == lastindex) {

            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);
        } else if(index==0)
        {
            currentIndexSeekValue = currentSeekingTime;
        }
        else{
//            currentIndexSeekValue = (SECOUND_TO_SPLIT * (index + 1)) - currentSeekingTime;
            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);

        }*/
        if(index==0)
        {
            currentIndexSeekValue = player.getContentPosition();
        }
        else{
//            currentIndexSeekValue = (SECOUND_TO_SPLIT * (index + 1)) - currentSeekingTime;
//            currentIndexSeekValue = player.getContentPosition() - (SECOUND_TO_SPLIT * index);
            currentIndexSeekValue = ((SECOUND_TO_SPLIT * index)+player.getContentPosition());


        }
        Log.d("Doollllll","currentseek"+currentIndexSeekValue);
        changeTimeTextView(currentIndexSeekValue);


    }

    public void seekToPart(int currentProgress,boolean shouldSeek) {
        seekToTime();
        long currentSeekingTime = (long) (currentProgress / 100.0f * totalLength);
//        changeTimeTextView(currentSeekingTime);
        int index = (int) Math.floor(currentSeekingTime / SECOUND_TO_SPLIT);
        long currentIndexSeekValue = 0;
       /* if ((index + 1) == lastindex) {

            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);
        } else if(index==0)
        {
            currentIndexSeekValue = currentSeekingTime;
        }
        else{
//            currentIndexSeekValue = (SECOUND_TO_SPLIT * (index + 1)) - currentSeekingTime;
            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);

        }*/
        if(index==0)
        {
            currentIndexSeekValue = currentSeekingTime;
        }
        else{
//            currentIndexSeekValue = (SECOUND_TO_SPLIT * (index + 1)) - currentSeekingTime;
            currentIndexSeekValue = currentSeekingTime - (SECOUND_TO_SPLIT * index);

        }

        Log.d("Value>>>>", currentProgress+" datatata \nTotalTime :" + totalLength + "\n Current Time : "
                + currentSeekingTime + "\n index:" + index + "-----" + "indexseek " + currentIndexSeekValue);
        if(shouldSeek) {
            player.seekTo(index, currentIndexSeekValue);
        }




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

    void updateSeekBar() {
        if(seekBar!=null) {
            Log.d(">>>","change>>>"+(int) (player.getCurrentPosition() * 1.0f / totalLength * 100));
            seekBar.setProgress((int) (player.getCurrentPosition() * 1.0f / totalLength * 100));
            seekBar.setSecondaryProgress(player.getBufferedPercentage());
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        isPlayer=playWhenReady;
        if (playWhenReady && playbackState == Player.STATE_READY) {

//            Log.v("trackcheckstate", " check" + playbackState + "  Ready " + playWhenReady + "  duration " + getDuration());


//            timer = new Timer(true);
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    updateSeekBar();
//                }
//            }, 0, 1000);
            mHandler = new Handler();
            mHandler.post(updateProgressAction);



        }

        if (playbackState == Player.STATE_ENDED) {


            player.setPlayWhenReady(false);
            player.seekTo(0);
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
        if (timer != null) {
            timer.cancel();
        }
    }
    private Handler mHandler;


    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            Log.d("getcurrent","lalu out>>"+player.getCurrentPosition());
            updateProgress();
        }
    };

    public void updateProgress()
    {
        seekToTime();

        if(isPlayer) {
            Log.d("getcurrent","lalu innnn>>"+player.getCurrentPosition());

            long delayMs = TimeUnit.SECONDS.toMillis(1);
            mHandler.postDelayed(updateProgressAction, delayMs);

            if(!isSeeking)
            {
//                sekkbar.setProgress((int) (player.getCurrentPosition() * 1.0f / player.getDuration() * 100));
                if(player.getCurrentWindowIndex()==0) {
                    Log.d("PAPAPAPA", (int) (player.getCurrentPosition() * 1.0f / totalLength * 100) + ">>>");
                    seekBar.setProgress((int) (player.getCurrentPosition() * 1.0f / totalLength * 100));
                }
                else
                {
                    long currentposition=player.getCurrentPosition()+(HelperUtils.SECOUND_TO_SPLIT *player.getCurrentWindowIndex());
                    Log.d("PAPAPAPA", (int) (currentposition * 1.0f /totalLength * 100) + ">>>");
                    seekBar.setProgress((int) (currentposition * 1.0f /totalLength * 100));

                }

//                int newIndex=player.getCurrentWindowIndex()+1;
//                Log.d("CurrentSeek",player.getCurrentPosition()+"<<<>>>"+(int) ((player.getCurrentPosition()*newIndex) * 1.0f / totalLength * 100)+">>>");
            }

//                seekBar.setProgress((int) (player.getCurrentPosition()*player.getCurrentWindowIndex() * 1.0f / totalLength * 100));
//                seekBar.setSecondaryProgress(player.getBufferedPercentage());            }
        }
    }

    public void changeTimeTextView(long milliSec)
    {
//       long hour= TimeUnit.MILLISECONDS.toHours(millesecound);
        long sec = (milliSec/1000) % 60;
        long min = ((milliSec/1000) / 60) % 60;
        long hour = ((milliSec/1000) / 60) / 60;
//        long sec = (totalLength/1000) % 60;
//        long min = ((totalLength/1000) / 60) % 60;
//        long hour = ((totalLength/1000) / 60) / 60;
        String secd=""+(milliSec/1000) % 60;
        String mind=""+((milliSec/1000) / 60) % 60;
        String hourd=""+((milliSec/1000) / 60) / 60;
        if(sec<10)
        {
            secd="0"+secd;
        }
        if(min<10)
        {
            mind="0"+mind;
        }
        if(hour<10)
        {
            hourd="0"+hourd;
        }
       String currentTImestamp=hourd+":"+mind+":"+secd;

        timeText.setText(currentTImestamp+"/"+totalTimeTImestamp);
//        timeText.setText((hour>10)?hour:"0"+hour);
//       timeText.setText(((milliSec/1000) / 60) / 60>0?((milliSec/1000) / 60) / 60:"0"+((milliSec/1000) / 60) / 60+":"+TimeUnit.MILLISECONDS.toMinutes(millesecound)+":"+TimeUnit.MILLISECONDS.toSeconds(millesecound));
    }



    private class ExVidPlayerGestureListener extends GestureListener {
        ExVidPlayerGestureListener(Context ctx) {
            super(ctx);
        }

        @Override public void onTap() {
           /* if (root.getVisibility() == View.VISIBLE) {
                root.setVisibility(View.GONE);
            } else {
                exVidPlayer.showControls();
            }*/
        }

        @Override public void onHorizontalScroll(MotionEvent event, float delta) {
//            1920swiping horizontaly1034.3951
//            1920swiping horizontaly-1671.9392
            Log.d("tendiz",screenWidth+"swiping horizontaly"+delta+">>>>motion"+event.getAction());

//            Log.d("tendiz","Scroll to incrise="+(screenWidth-delta)/31.25);
//            Log.d("tendiz","Scroll to incrise="+delta*31.25);
            long perscreen = SECOUND_TO_SPLIT / screenWidth;

            if(delta*perscreen<0)
            {
                seekStatus.setText("Last Seeks to Reverse>>> -"+TimeUnit.MILLISECONDS.toSeconds((long) (0-(delta*perscreen))));
                Log.d("tendiz",delta*perscreen+"Scroll to incrise= Minus"+(SECOUND_TO_SPLIT+(delta*perscreen)));

            }
            else
            {
                Log.d("tendiz",perscreen+"Scroll to incrise= Adding "+delta*perscreen);
                seekStatus.setText("Last Seeks to Forward>>> +"+TimeUnit.MILLISECONDS.toSeconds((long) (delta*perscreen)));

            }

        }

        @Override public void onVerticalScroll(MotionEvent event, float delta) {

            if (event.getPointerCount() == ONE_FINGER) {
//                updateBrightnessProgressBar(extractVerticalDeltaScale(-delta, pBarBrighness));
                Log.d("tendiz","GO FOR BRIGness");
            } else {
                Log.d("tendiz","GO FOR Volume");

//                updateVolumeProgressBar(extractVerticalDeltaScale(-delta, pBarVolume));
            }
        }

        @Override public void onSwipeRight() {
            Log.d("tendiz","Swipe right");

        }

        @Override public void onSwipeLeft() {
            Log.d("tendiz","Swipe left");

        }

        @Override public void onSwipeBottom() {
            Log.d("tendiz","Swipe left");

        }

        @Override public void onSwipeTop() {
            Log.d("tendiz","Swipe left");

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

}
