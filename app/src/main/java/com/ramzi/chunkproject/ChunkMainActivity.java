package com.ramzi.chunkproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oliveboard on 18/1/19.
 *
 * @auther Ramesh M Nair
 */
public class ChunkMainActivity extends AppCompatActivity {
    TextView selectedVideotextView, statusTextView;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    public static final String TAG = "ChunkCreator";
    String outputDirectory = Environment.getExternalStorageDirectory().toString() + "/";
    String finalName = "ChunkClip";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedVideotextView = (TextView) findViewById(R.id.selected_file_tv);
        statusTextView = (TextView) findViewById(R.id.chunk_status_tv);
        loadFFMpegBinary();
        checkPermisson(ChunkMainActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    Toast.makeText(getApplicationContext(), "Permission Grandad", Toast.LENGTH_SHORT).show();
                } else {
                    //not granted
                    Toast.makeText(getApplicationContext(), "Permission Denied App wont work properly guys", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*Button clicks*/

    public void selectVideoFile(View v) {
        if (checkPermisson(ChunkMainActivity.this)) {
            new ChooserDialog().with(this)
                    .withFilter(false, false, "mp4", "mkv", "flv", "avi", "mpg", "mov", "wmv")
                    .withStartFile(Environment.getExternalStorageDirectory().toString())
                    .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                    .withChosenListener(new ChooserDialog.Result() {
                        @Override
                        public void onChoosePath(String path, File pathFile) {
                            selectedVideotextView.setText(path);
//                        Toast.makeText(ChunkMainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build()
                    .show();
        }
    }


    public void startChunkConversion(View v) {
        String urlPath;
        try {
             urlPath = URLEncoder.encode(selectedVideotextView.getText().toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            urlPath=selectedVideotextView.getText().toString();
        }
        File videoFile=new File(selectedVideotextView.getText().toString());
        if (videoFile.exists()) {
            long timeInMillisec = 0;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
                retriever.setDataSource(getApplicationContext(), Uri.fromFile(videoFile));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                 timeInMillisec = Long.parseLong(time);
                Log.d(TAG, "totalVideoTime" + time);

                retriever.release();
            } catch (Exception e) {
                 e.printStackTrace();
            }
//            timeInMillisec=2
            if (timeInMillisec > 30000) {

                String extension = selectedVideotextView.getText().toString().substring(selectedVideotextView.getText().toString().lastIndexOf("."));
                String fullOutput = outputDirectory + System.currentTimeMillis() + finalName + extension;
                Log.d(TAG, "Fulloutput" + fullOutput);
//                String silenamebase="\"["+selectedVideotextView.getText().toString()+"]\"";
                List<String> commandList = new LinkedList<>();
                commandList.add("-i");
                commandList.add(selectedVideotextView.getText().toString());
                commandList.add("-acodec");
                commandList.add("copy");
                commandList.add("-vcodec");
                commandList.add("copy");
                commandList.add("-ss");
                commandList.add("00:00:10");
                commandList.add("-t");
                commandList.add("00:00:20");
                commandList.add(fullOutput);
//                commandList.add("-i");
//                String cmd = "-i " + silenamebase + " -acodec copy -vcodec copy -ss 00:00:10 -t 00:00:20 " + fullOutput;

//                String[] command = cmd.split(" ");
                String[] command  = commandList.toArray(new String[commandList.size()]);
                if (command.length != 0) {
                    Log.d(TAG, "comment " + command);

                    execFFmpegBinary(command);
                } else {
                    Toast.makeText(ChunkMainActivity.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "File Not Found...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "video Less that 30 sec", Toast.LENGTH_SHORT).show();

        }

    }


    public void selectChunkFile(View v) {

    }


    public void startPlayChunk(View v) {

    }

    /////////////////////////////////////////


    public boolean checkPermisson(final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_CALENDAR)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setMessage("Write calendar permission is necessary to write event!!!");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        } else {
            return true;
        }
    }


    private void loadFFMpegBinary() {
        try {
            FFmpeg.getInstance(this).loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            FFmpeg.getInstance(this).execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    addTextViewToLayout("FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    addTextViewToLayout("SUCCESS with output : " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
//                    addTextViewToLayout("progress : "+s);
                    statusTextView.setText("Processing\n" + s);
                }

                @Override
                public void onStart() {
//                    outputLayout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + command);
                    statusTextView.setText("Processing...");
//                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
//                    progressDialog.dismiss();
                    statusTextView.setText("Completed");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void addTextViewToLayout(String text) {
//        TextView textView = new TextView(Home.this);
//        textView.setText(text);
//        outputLayout.addView(textView);
        Log.d("status",text);
    }

    private void showUnsupportedExceptionDialog() {
        new android.app.AlertDialog.Builder(ChunkMainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChunkMainActivity.this.finish();
                    }
                })
                .create()
                .show();

    }


}
