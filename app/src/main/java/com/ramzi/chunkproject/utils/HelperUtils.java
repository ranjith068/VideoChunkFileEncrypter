package com.ramzi.chunkproject.utils;

import android.os.Environment;
import android.util.Log;

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
        File finalStorage =new File(Environment.getExternalStorageDirectory()+File.separator+"CHUNKPROJECT"+File.separator+directory);
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


    public static long getTheDurationInMillsecound(String duration)
    {
        Log.d("getDuration","BEFORE"+duration);

        String newDuration=duration.replace(" ","").replace(".",":");
        Log.d("getDuration","DurationcalString"+newDuration);
        String []timeSplitter=newDuration.split(":");
        Log.d("getDuration","length"+timeSplitter.length);
        if(timeSplitter.length==5)
        {
            long hour=TimeUnit.HOURS.toMillis(Long.parseLong(timeSplitter[1]));
            long minitues=TimeUnit.MINUTES.toMillis(Long.parseLong(timeSplitter[2]));
            long secound=TimeUnit.SECONDS.toMillis(Long.parseLong(timeSplitter[3]));
            long millis=TimeUnit.MILLISECONDS.toMillis(Long.parseLong(timeSplitter[4]));

            long totalMill=hour+minitues+secound+millis;
            Log.d("Converted",totalMill+">>444444totalVideoTime FFMPEG");
            return totalMill;
        }
//        else if(timeSplitter.length==4)7384006
//        {
//            long hour=TimeUnit.HOURS.toMillis(Long.parseLong(timeSplitter[0]));
//            long minitues=TimeUnit.MINUTES.toMillis(Long.parseLong(timeSplitter[1]));
//            long secound=TimeUnit.MINUTES.toMillis(Long.parseLong(timeSplitter[2]));
//            long totalMill=hour+minitues+secound;
//            Log.d("Converted",totalMill+">>33333totalVideoTime FFMPEG");
//            return totalMill;
//        }
        else
        {
            return 0;
        }


    }
}
