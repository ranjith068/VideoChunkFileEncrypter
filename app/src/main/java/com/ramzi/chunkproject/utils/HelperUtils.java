package com.ramzi.chunkproject.utils;

import android.os.Environment;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by oliveboard on 21/1/19.
 *
 * @auther Ramesh M Nair
 */
public class HelperUtils {

    public static long SECOUND_TO_SPLIT=60000;
    public static String SECOUND_TO_SPLIT_TIMESTAMP="00:01:00";
    private static HelperUtils instance;
    public static HelperUtils getInstance() {
        if (instance == null) {
            instance = new HelperUtils();
        }
        return instance;
    }


    public static String getFileExtention(String filePath)
    {
        return filePath.substring(filePath.lastIndexOf("."));
    }


    public static String finalDestination(String directory)
    {
        File finalStorage =new File(Environment.getExternalStorageDirectory()+File.separator+directory);
        if(!finalStorage.exists())
        {
            finalStorage.mkdirs();
        }
        return finalStorage.getAbsolutePath();
    }


    public static String getStartTimeStamp(long millis)
    {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
