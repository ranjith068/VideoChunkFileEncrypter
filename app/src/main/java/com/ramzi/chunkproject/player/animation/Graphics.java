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


import android.graphics.Path;
import android.support.annotation.NonNull;
/**
 * Created by voltella on 25/1/19.
 *
 * @auther Ramesh M Nair
 */
final class Graphics {
    private Graphics() {
    }

    static float animateValue(float start, float end, float fraction) {
        return start + (end - start) * fraction;
    }

    static void inRect(@NonNull Path into, @NonNull float[] pathData) {
        if (!into.isEmpty()) into.rewind();

        into.moveTo(pathData[0], pathData[1]); // top left
        into.lineTo(pathData[2], pathData[3]); // top right
        into.lineTo(pathData[4], pathData[5]); // bottom right
        into.lineTo(pathData[6], pathData[7]); // bottom left
    }

    static void animatePath(@NonNull float[] out, @NonNull float[] startPath,
                            @NonNull float[] endPath, float fraction) {
        if (startPath.length != endPath.length || out.length != startPath.length) {
            throw new IllegalStateException("Paths should be of the same size");
        }

        final int pathSize = startPath.length;

        for (int i = 0; i < pathSize; i++) {
            out[i] = animateValue(startPath[i], endPath[i], fraction);
        }
    }
}