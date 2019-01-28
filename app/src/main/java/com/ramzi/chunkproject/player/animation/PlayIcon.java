/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2019 Ramesh M Nair
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ramzi.chunkproject.player.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by voltella on 25/1/19.
 *
 * @auther Ramesh M Nair
 */

public interface PlayIcon {

    /**
     * Change icon without animation
     *
     * @param state new icon state
     */
    void setIconState(@NonNull PlayIconDrawable.IconState state);

    /**
     * Return current icon state
     *
     * @return icon state
     */
    @NonNull PlayIconDrawable.IconState getIconState();

    /**
     * Animate icon to given state.
     *
     * @param nextState new icon state
     */
    void animateToState(@NonNull PlayIconDrawable.IconState nextState);

    /**
     * Toggles the state of the icon Play-Pause and vice versa
     *
     * @param animated indicates whether this method should change the state with some animation
     */
    void toggle(boolean animated);

    /**
     * Sets the listener, which is being invoked every time the state's changed
     *
     * @param listener instance of state listener
     */
    void setStateListener(@Nullable PlayIconDrawable.StateListener listener);

    /**
     * Set color of icon
     *
     * @param color new icon color
     */
    void setColor(int color);

    /**
     * Set visibility of icon
     *
     * @param visible new value for visibility
     */
    void setVisible(boolean visible);

    /**
     * Set duration of transformation animations
     *
     * @param duration new animation duration
     */
    void setAnimationDuration(int duration);

    /**
     * Set interpolator for transformation animations
     *
     * @param interpolator new interpolator
     */
    void setInterpolator(@NonNull TimeInterpolator interpolator);

    /**
     * Set listener for {@code MaterialMenuDrawable} animation events
     *
     * @param listener new listener or null to remove any listener
     */
    void setAnimationListener(@Nullable Animator.AnimatorListener listener);

    /**
     * Allows one to manually control the transformation of the icon
     *
     * @param fraction determinate fraction which should be in range 0F..1F
     */
    void setCurrentFraction(float fraction);
}