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

package com.ramzi.chunkproject.conversion;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.ramzi.chunkproject.ChunkMainActivity;
import com.ramzi.chunkproject.R;
import com.ramzi.chunkproject.conversion.interfaces.ConversionCallback;
import com.ramzi.chunkproject.encryption.EncryptionAsync;
import com.ramzi.chunkproject.encryption.EncryptionCallback;
import com.ramzi.chunkproject.file.FilePropertyAsync;
import com.ramzi.chunkproject.utils.HelperUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ramzi.chunkproject.utils.HelperUtils.SECOUND_TO_SPLIT;

/**
 * Created by voltella on 21/1/19.
 * <p>
 * FFMeg convertion here video file is split and converted to smaller pieces
 *
 * @auther Ramesh M Nair
 */
public class FFmpegConversion implements EncryptionCallback {

    /*initializing variables*/

    public ConversionCallback conversionCallback;

    public static final String TAG = "FFmpegConversion";
    Context context;
    List<String> commandList = new LinkedList<>();

    String input;
    int totalPart;
    String destinationDirectory;
    long videoLength;
    String secoundTosplit=String.format("%02d", ((SECOUND_TO_SPLIT / 1000) / 60) / 60) + ":" + String.format("%02d", ((SECOUND_TO_SPLIT / 1000) / 60) % 60) + ":" + String.format("%02d", (SECOUND_TO_SPLIT / 1000) % 60);



    public FFmpegConversion(ConversionCallback conversionCallback, Context context, String input, long videoLength, String destinationDirectory) {
        this.conversionCallback = conversionCallback;
        this.context = context;
        this.input = input;
        this.videoLength = videoLength;
        totalPart = (int) Math.round(videoLength / SECOUND_TO_SPLIT);
        this.destinationDirectory = destinationDirectory;


        Log.d(TAG, "Constuctor input :" + input + "\n" + "totalPart:" + totalPart + "\n destinationDirectory:" + destinationDirectory);

    }

    /*will use in upcomming versions this for splitting video thump nails*/

    public void splitimages() {
//        ffmpeg -i video.webm -vf fps=1 thumb%04d.jpg -hide_banner
//        ffmpeg -i video.webm -vf fps=1/5 thumb%04d.jpg -hide_banner
        commandList.clear();
        commandList.add("-i");
        commandList.add(input);
        commandList.add("-vf");
        commandList.add("fps=1");
        commandList.add(destinationDirectory + "/thumb%04d.jpg");
        commandList.add("-hide_banner");

        String[] command = commandList.toArray(new String[commandList.size()]);


        try {
            FFmpeg.getInstance(context).execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {

                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus("FAILED with output : " + s);
                    }
                }

                @Override
                public void onSuccess(String s) {

                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus("SUCCESS with output : " + s);
                    }

                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + s);
//                    addTextViewToLayout("progress : "+s);
//                    statusTextView.setText("Processing\n" + s);
                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus("Processing\n" + s);
                    }
                }

                @Override
                public void onStart() {
//                    outputLayout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + command);
                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus("Processing...");
                    }
//                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
//                    progressDialog.dismiss();
                    if (conversionCallback != null) {
//                        conversionCallback.conversionStatus("Completed,Going for encryption");

                        conversionCallback.conversionStatus("Completed,frame extraction");


                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }


    public void spliteTimeAndStart(int part, long startTime) {
      Log.d("Dynamicttm",""+secoundTosplit);


//        $ ffmpeg -i source.mkv -ss 01:02:37.754 -map_chapters -1 -c:v libx264-c:a copy -crf 18 -t 00:04:52.292 output.mkv

        /**
         *
         * *We are adding comment via list because if
         * we add it as string white space and special characters makes issue for safe execution this is best way
         */
        commandList.clear();
        commandList.add("-i");
        commandList.add(input);
        commandList.add("-ss");
        commandList.add(HelperUtils.getStartTimeStamp(startTime));
        commandList.add("-map_chapters");
        commandList.add("-1");
        commandList.add("-c:v");
        commandList.add("libx264");
        commandList.add("-preset");
        commandList.add("ultrafast");
        commandList.add("-c:a");
        commandList.add("copy");
//        commandList.add("-crf");
//        commandList.add("300");//18
        commandList.add("-t");
        commandList.add(secoundTosplit);
        commandList.add(destinationDirectory + "/" + part + HelperUtils.getFileExtention(input));

        String[] command = commandList.toArray(new String[commandList.size()]);
        if (command.length != 0) {
            Log.d(TAG, "cmd " + command.toString());

            execFFmpegBinary(command, part, totalPart, startTime, destinationDirectory + "/" + part + HelperUtils.getFileExtention(input));
        } else {
        }

        /*  commented following ffmpeg cmd for future releases

        commandList.add("-i");
        commandList.add(input);
        commandList.add("-acodec");
        commandList.add("copy");
        commandList.add("-vcodec");
        commandList.add("copy");
        commandList.add("-ss");
        commandList.add(HelperUtils.getStartTimeStamp(startTime));
        commandList.add("-t");
        commandList.add(HelperUtils.SECOUND_TO_SPLIT_TIMESTAMP);
        commandList.add(destinationDirectory+"/"+part+HelperUtils.getFileExtention(input));*/


        //ffmpeg -i input.mp4 -vcodec copy -acodec copy -copyinkf -ss 00:36:18 -to 00:39:50 output.mp4

       /* commandList.add("-i");
        commandList.add(input);
        commandList.add("-vcodec");
        commandList.add("copy");
        commandList.add("-acodec");
        commandList.add("copy");
        commandList.add("-copyinkf");
        commandList.add("-ss");
        commandList.add(HelperUtils.getStartTimeStamp(startTime));
        commandList.add("-to");
        commandList.add(HelperUtils.getStartTimeStamp((startTime+HelperUtils.SECOUND_TO_SPLIT)));
        commandList.add(destinationDirectory+"/"+part+HelperUtils.getFileExtention(input));*/


//        ffmpeg -i source.mp4 -ss 577.92 -t 11.98 -c copy -map 0 clip1.mp4
//        commandList.add("-i");
//        commandList.add(input);
//        commandList.add("-ss");
//        commandList.add(HelperUtils.getStartTimeStamp(startTime));
//        commandList.add("-t");
//        commandList.add(HelperUtils.SECOUND_TO_SPLIT_TIMESTAMP);
//        commandList.add("copy");
//        commandList.add("-map");
//        commandList.add("0");
//        commandList.add(destinationDirectory+"/"+part+HelperUtils.getFileExtention(input));


    }

    /**
     *cmd excecution of the video conversion
     * */
    private void execFFmpegBinary(final String[] command, final int part, final int totalPart, final long lastStartTime, final String fileoutPut) {
        try {
            FFmpeg.getInstance(context).execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {

                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus(part + "/" + totalPart + " " + "FAILED with output : " + s);
                    }
                }

                @Override
                public void onSuccess(String s) {

                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus(part + "/" + totalPart + " " + "SUCCESS with output : " + s);
                    }

                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + s);
//                    addTextViewToLayout("progress : "+s);
//                    statusTextView.setText("Processing\n" + s);
                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus(part + "/" + totalPart + " " + "Processing\n" + s);
                    }
                }

                @Override
                public void onStart() {
//                    outputLayout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + command);
                    if (conversionCallback != null) {
                        conversionCallback.conversionStatus(part + "/" + totalPart + " " + "Processing...");
                    }
//                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
//                    progressDialog.dismiss();
                    if (conversionCallback != null) {
//                        conversionCallback.conversionStatus("Completed,Going for encryption");

                        conversionCallback.conversionStatus("Completed,Going for encryption Please wait...");

                        new EncryptionAsync(context, fileoutPut, part, (lastStartTime + SECOUND_TO_SPLIT), FFmpegConversion.this).execute();

                    }

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }
    /**
     *status of video part encryption
     * */
    @Override
    public void encryptionResult(boolean status, int part, long nextChunkFileStartTime) {
        if (status) {
            if (new File(destinationDirectory + "/" + part + HelperUtils.getFileExtention(input)).exists()) {
                new File(destinationDirectory + "/" + part + HelperUtils.getFileExtention(input)).delete();
            }
            if (part < totalPart) {

                spliteTimeAndStart((part + 1), nextChunkFileStartTime);

            } else {
                conversionCallback.conversionStatus("All chunk files has been encrypted writing file property Please wait....");
                new FilePropertyAsync(context, destinationDirectory, totalPart, videoLength, FFmpegConversion.this, HelperUtils.getFileExtention(input)).execute();

            }
        } else {
            conversionCallback.conversionStatus("Encryption Failed.....:(");

        }
    }
    /**
     *status of video conversation
     * */
    @Override
    public void propertyResult(boolean status) {

        if (status) {
            conversionCallback.conversionStatus("ALL Process Completed");

        } else {
            conversionCallback.conversionStatus("ALL Process Completed with property file failed");

        }

    }
}
