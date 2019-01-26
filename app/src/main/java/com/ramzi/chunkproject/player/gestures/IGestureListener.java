package com.ramzi.chunkproject.player.gestures;

import android.view.MotionEvent;

/**
 * Created by Brajendr on 1/26/2017.
 */

public interface IGestureListener {
  void onTap();

  void onHorizontalScroll(MotionEvent event, float delta);

  void onVerticalScroll(MotionEvent event, float delta);

  void onSwipeRight();

  void onSwipeLeft();

  void onSwipeBottom();

  void onSwipeTop();

  void brightness(int value);
  void onScrollEnd();

}
