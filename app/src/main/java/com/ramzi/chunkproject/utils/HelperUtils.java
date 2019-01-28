package com.ramzi.chunkproject.utils;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by voltella on 21/1/19.
 *
 * @auther Ramesh M Nair
 */
public class HelperUtils {

    /**Currently splitting as 1 min you can increase as per the usage
      * SECOUND_TO_SPLIT_TIMESTAMP and SECOUND_TO_SPLIT should change equally based on the type*/
    public static long SECOUND_TO_SPLIT=60000;//in millesecound 1 min = 60000
    public static String SECOUND_TO_SPLIT_TIMESTAMP="00:01:00";//if you set SECOUND_TO_SPLIT to 2 min =120000 SECOUND_TO_SPLIT_TIMESTAMP should be 00:02:00
    private static HelperUtils instance;
    public static HelperUtils getInstance() {
        if (instance == null) {
            instance = new HelperUtils();
        }
        return instance;
    }

    /**
     * return fileextention
     * */
    public static String getFileExtention(String filePath)
    {
        if(filePath.substring(filePath.lastIndexOf(".")).contains("avi"))
        {
            return ".mp4";
        }
        return filePath.substring(filePath.lastIndexOf("."));
    }

    /**
     * return directory to save the files
     * */
    public static String finalDestination(String directory)
    {
        File finalStorage =new File(Environment.getExternalStorageDirectory()+File.separator+Constants.CHUNKDIR+File.separator+directory);
        if(!finalStorage.exists())
        {
            finalStorage.mkdirs();
        }
        return finalStorage.getAbsolutePath();
    }

    /**
     * return timestamp
     * */
    public static String getStartTimeStamp(long millis)
    {

        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    /**
     * return duration in millesecound
     * */
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

//    public static long splitTime=60000;
//    public static int lastindex=34;
    public void randomCheck(int currentProgress,long totalLength,int lastindex)
    {

        long currentSeekingTime = (long) (currentProgress / 100.0f * totalLength);
        int index= (int) Math.floor(currentSeekingTime/SECOUND_TO_SPLIT);
        long currentseekValue=0;
        if((index+1)==lastindex)
        {
//            double d = totalLength/(double)splitTime;
//            Log.d("value","ddddd"+d);
//            BigDecimal bd = new BigDecimal( d - Math.floor( d ));
//            bd = bd.setScale(10,RoundingMode.HALF_DOWN);
//            Log.d( "Value>>>>zzzzz",Double.parseDouble(bd.toString())+">>" );
//            long kkp = (long) Double.parseDouble(bd.toString()) * 1000;
//            Log.d( "Value in last section", Double.parseDouble(bd.toString()) * 1000+">>" );

//            currentseekValue= (long) ((splitTime*Double.parseDouble(bd.toString())));
//            currentseekValue=totalLength-;
//            0.217950
//            currentseekValue=
            currentseekValue=currentSeekingTime-(SECOUND_TO_SPLIT * index);
        }
        else {
            currentseekValue = (SECOUND_TO_SPLIT * (index + 1)) - currentSeekingTime;
        }

        Log.d("Value>>>>"," datatata \nTotalTime :"+totalLength+"\n Current Time : "
                +currentSeekingTime+"\n index:"+index+"-----"+"indexseek "+currentseekValue);


    }

    /**
     * Get the device id as scret key
     * important this is just for sample purpose yoy can make you own logic for this
     * */
    public String secretToken(Context context)
    {
         String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
         if(androidId==null)
         {
             return Constants.BASIC_CIPHER_KEY;
         }
         return androidId;
    }
}
