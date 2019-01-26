package com.ramzi.chunkproject.player.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by oliveboard on 26/1/19.
 *
 * @auther Ramesh M Nair
 */
public class BrightnessUtils {

    public static final int MAX_BRIGHTNESS = 255;
    public static final int MIN_BRIGHTNESS = 0;
    public static void set(Context context, int brightness){
        ContentResolver cResolver = context.getContentResolver();
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    public static int get(Context context) {
        ContentResolver cResolver = context.getContentResolver();
        try {
            return Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }
}
