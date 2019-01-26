package com.ramzi.chunkproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import static com.ramzi.chunkproject.BuildConfig.DEBUG;
import static com.ramzi.chunkproject.player.animation.AnimationUtils.Type.SCALE_AND_ALPHA;
import static com.ramzi.chunkproject.player.animation.AnimationUtils.animateView;
//import static org.schabi.newpipe.util.AnimationUtils.Type.SCALE_AND_ALPHA;

/**
 * Created by oliveboard on 26/1/19.
 *
 * @auther Ramesh M Nair
 */
public class ProgressbarVolumeTest extends AppCompatActivity {
    public static final String TAG="test";
    RelativeLayout rootview,brView;
    ImageView brIV;
    ProgressBar brPG;
    int maxGestureLength;
    GestureDetector gestureDetector;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightnessvoulumetest);
        rootview=findViewById(R.id.rootview);
        brView=findViewById(R.id.brightnessRelativeLayout);
        brIV=findViewById(R.id.brightnessImageView);
        brPG=findViewById(R.id.brightnessProgressBar);
        rootview.addOnLayoutChangeListener((view, l, t, r, b, ol, ot, or, ob) -> {
            if (l != ol || t != ot || r != or || b != ob) {
                // Use smaller value to be consistent between screen orientations
                // (and to make usage easier)
                int width = r - l, height = b - t;
                maxGestureLength = (int) (Math.min(width, height) * MAX_GESTURE_LENGTH);

                if (DEBUG) Log.d(TAG, "maxGestureLength = " + maxGestureLength);

//                volumeProgressBar.setMax(maxGestureLength);
                brPG.setMax(maxGestureLength);

//                setInitialGestureValues();
            }
        });
        PlayerGestureListener listener = new PlayerGestureListener();
        gestureDetector = new GestureDetector(getApplicationContext(), listener);
        rootview.setOnTouchListener(listener);
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
        private boolean isMoving;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            if (DEBUG) Log.d(TAG, "onDoubleTap() called with: e = [" + e + "]" + "rawXy = " + e.getRawX() + ", " + e.getRawY() + ", xy = " + e.getX() + ", " + e.getY());
//
//            if (e.getX() > playerImpl.getRootView().getWidth() * 2 / 3) {
//                playerImpl.onFastForward();
//            } else if (e.getX() < playerImpl.getRootView().getWidth() / 3) {
//                playerImpl.onFastRewind();
//            } else {
//                playerImpl.getPlayPauseButton().performClick();
//            }

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
//            if (DEBUG) Log.d(TAG, "onSingleTapConfirmed() called with: e = [" + e + "]");
//            if (playerImpl.getCurrentState() == BasePlayer.STATE_BLOCKED) return true;
//
//            if (playerImpl.isControlsVisible()) {
//                playerImpl.hideControls(150, 0);
//            } else {
//                playerImpl.showControlsThenHide();
//                showSystemUi();
//            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
//            if (DEBUG) Log.d(TAG, "onDown() called with: e = [" + e + "]");

            return super.onDown(e);
        }

        private static final int MOVEMENT_THRESHOLD = 40;

//        private final boolean isVolumeGestureEnabled = PlayerHelper.isVolumeGestureEnabled(getApplicationContext());
//        private final boolean isBrightnessGestureEnabled = PlayerHelper.isBrightnessGestureEnabled(getApplicationContext());

        private final boolean isVolumeGestureEnabled = true;
        private final boolean isBrightnessGestureEnabled = true;
        private static final int SWIPE_THRESHOLD = 100;

//        private final int maxVolume = playerImpl.getAudioReactor().getMaxVolume();

        @Override
        public boolean onScroll(MotionEvent initialEvent, MotionEvent movingEvent, float distanceX, float distanceY) {
            float deltaY = movingEvent.getY() - initialEvent.getY();
            float deltaX = movingEvent.getX() - initialEvent.getX();

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                    onHorizontalScroll(movingEvent,deltaX);
                }
            }




            if (!isVolumeGestureEnabled && !isBrightnessGestureEnabled) return false;

            //noinspection PointlessBooleanExpression
            if (DEBUG && false) Log.d(TAG, "MainVideoPlayer.onScroll = " +
                    ", e1.getRaw = [" + initialEvent.getRawX() + ", " + initialEvent.getRawY() + "]" +
                    ", e2.getRaw = [" + movingEvent.getRawX() + ", " + movingEvent.getRawY() + "]" +
                    ", distanceXy = [" + distanceX + ", " + distanceY + "]");

            final boolean insideThreshold = Math.abs(movingEvent.getY() - initialEvent.getY()) <= MOVEMENT_THRESHOLD;
          /*  if (!isMoving && (insideThreshold || Math.abs(distanceX) > Math.abs(distanceY))
                    || playerImpl.getCurrentState() == BasePlayer.STATE_COMPLETED) {
                return false;
            }
*/
            if (!isMoving && (insideThreshold || Math.abs(distanceX) > Math.abs(distanceY))
                    || false) {
                return false;
            }

            isMoving = true;

            boolean acceptAnyArea = isVolumeGestureEnabled != isBrightnessGestureEnabled;
            boolean acceptVolumeArea = acceptAnyArea || initialEvent.getX() > rootview.getWidth() / 2;
            boolean acceptBrightnessArea = acceptAnyArea || !acceptVolumeArea;

            if (isVolumeGestureEnabled && acceptVolumeArea) {
                /*playerImpl.getVolumeProgressBar().incrementProgressBy((int) distanceY);
                float currentProgressPercent =
                        (float) playerImpl.getVolumeProgressBar().getProgress() / playerImpl.getMaxGestureLength();
                int currentVolume = (int) (maxVolume * currentProgressPercent);
                playerImpl.getAudioReactor().setVolume(currentVolume);

                if (DEBUG) Log.d(TAG, "onScroll().volumeControl, currentVolume = " + currentVolume);

                final int resId =
                        currentProgressPercent <= 0 ? R.drawable.ic_volume_off_white_72dp
                                : currentProgressPercent < 0.25 ? R.drawable.ic_volume_mute_white_72dp
                                : currentProgressPercent < 0.75 ? R.drawable.ic_volume_down_white_72dp
                                : R.drawable.ic_volume_up_white_72dp;

                playerImpl.getVolumeImageView().setImageDrawable(
                        AppCompatResources.getDrawable(getApplicationContext(), resId)
                );

                if (playerImpl.getVolumeRelativeLayout().getVisibility() != View.VISIBLE) {
                    animateView(playerImpl.getVolumeRelativeLayout(), SCALE_AND_ALPHA, true, 200);
                }
                if (playerImpl.getBrightnessRelativeLayout().getVisibility() == View.VISIBLE) {
                    playerImpl.getBrightnessRelativeLayout().setVisibility(View.GONE);
                }*/
            } else if (isBrightnessGestureEnabled && acceptBrightnessArea) {
                brPG.incrementProgressBy((int) distanceY);
                float currentProgressPercent =
                        (float) brPG.getProgress() / maxGestureLength;
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = currentProgressPercent;
                getWindow().setAttributes(layoutParams);

                if (DEBUG) Log.d(TAG, "onScroll().brightnessControl, currentBrightness = " + currentProgressPercent);

                final int resId =
                        currentProgressPercent < 0.25 ? R.drawable.ic_brightness_low_white_72dp
                                : currentProgressPercent < 0.75 ? R.drawable.ic_brightness_medium_white_72dp
                                : R.drawable.ic_brightness_high_white_72dp;

                brIV.setImageDrawable(
                        AppCompatResources.getDrawable(getApplicationContext(), resId)
                );

                if (brView.getVisibility() != View.VISIBLE) {
                    animateView(brView, SCALE_AND_ALPHA, true, 200);
                }
//                if (playerImpl.getVolumeRelativeLayout().getVisibility() == View.VISIBLE) {
//                    playerImpl.getVolumeRelativeLayout().setVisibility(View.GONE);
//                }
            }
            return true;
        }

        private void onHorizontalScroll(MotionEvent movingEvent, float deltaX) {
            Log.d("SCRooo",">>>>>>>>>>>>>>>>>>>>>>>>scrolling hotzontal");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (DEBUG && false) Log.d(TAG, "onTouch() called with: v = [" + v + "], event = [" + event + "]");
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP && isMoving) {
                isMoving = false;
                onScrollEnd();
            }
            return true;
        }
    }
    private final float MAX_GESTURE_LENGTH = 0.75f;


    private void onScrollEnd() {
        if (DEBUG) Log.d(TAG, "onScrollEnd() called");

//        if (playerImpl.getVolumeRelativeLayout().getVisibility() == View.VISIBLE) {
//            animateView(playerImpl.getVolumeRelativeLayout(), SCALE_AND_ALPHA, false, 200, 200);
//        }
        if (brView.getVisibility() == View.VISIBLE) {
            animateView(brView, SCALE_AND_ALPHA, false, 200, 200);
        }

//        if (playerImpl.isControlsVisible() && playerImpl.getCurrentState() == STATE_PLAYING) {
//            playerImpl.hideControls(DEFAULT_CONTROLS_DURATION, DEFAULT_CONTROLS_HIDE_TIME);
//        }
    }

}
