/*******************************************************************************
 * Title:Common Shared Preference class
 * Authour:Voltella
 *******************************************************************************/

package com.ramzi.chunkproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {
    // private static final String APP_SHARED_PREFS =
    // "com.aydabtu.BroadcastSMS_preferences"; // Name of the file -.xml
    private SharedPreferences appSharedPrefs;
    private Editor prefsEditor;

    public AppPreferences(Context context, String Preferncename) {
        this.appSharedPrefs = context.getSharedPreferences(Preferncename,
                Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    /**
     * *
     * <p/>
     * getdata() get the value from the preference
     */
    public String getData(String key) {
        return appSharedPrefs.getString(key, "");
    }

    public void clearpref(Context prefcontect, String name) {
        this.appSharedPrefs = prefcontect.getSharedPreferences(
                name, 0);
        this.appSharedPrefs.edit().clear().commit();
    }

    public void clearData() {
        if(appSharedPrefs!=null) {
            appSharedPrefs.edit().clear().commit();
        }
//        editor.commit();
    }

    /**
     * *
     * <p/>
     * SaveData() save the value to the preference
     */
    public void SaveData(String Tag, String text) {
        prefsEditor.putString(Tag, text);
        prefsEditor.commit();
    }

    public int getIntData(String key) {
        // return appSharedPrefs.getString(key, "");
        return appSharedPrefs.getInt(key, 0);
    }

    public int getCompassData(String key) {
        // return appSharedPrefs.getString(key, "");
        return appSharedPrefs.getInt(key, -1);
    }

    /**
     * *
     * <p/>
     * SaveData() save the value to the preference
     */
    public void SaveIntData(String text, Integer Tag) {
        // prefsEditor.putString(Tag, text);
        prefsEditor.putInt(text, Tag);
        prefsEditor.commit();
    }
    public void saveFloatData(String text, Float Tag) {
        prefsEditor.putFloat(text, Tag);
        prefsEditor.commit();
    }
    public Float getFloat(String key) {
        return appSharedPrefs.getFloat(key,-100f);

    }

    public boolean getBoolean(String key) {
        return appSharedPrefs.getBoolean(key, false);

    }

    public boolean getTrueBoolean(String key) {
        return appSharedPrefs.getBoolean(key, true);

    }

    public void putLongData(String key, long value) {
        prefsEditor.putLong(key, value);
        prefsEditor.commit();
    }

    public long getLongData(String key) {
        return appSharedPrefs.getLong(key,0);

    }

    public void saveBoolean(String key, boolean b) {
        prefsEditor.putBoolean(key, b);
        prefsEditor.commit();
    }
}