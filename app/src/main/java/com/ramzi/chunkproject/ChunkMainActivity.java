package com.ramzi.chunkproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

/**
 * Created by oliveboard on 18/1/19.
 *
 * @auther Ramesh M Nair
 */
public class ChunkMainActivity extends AppCompatActivity {
    TextView selectedVideotextView;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=1001;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedVideotextView=(TextView)findViewById(R.id.selected_file_tv);

        checkPermisson(getApplicationContext());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    Toast.makeText(getApplicationContext(),"Permission Grandad",Toast.LENGTH_SHORT).show();
                } else {
                    //not granted
                    Toast.makeText(getApplicationContext(),"Permission Denied App wont work properly guys",Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*Button clicks*/

    public void selectVideoFile(View v)
    {
        if(checkPermisson(getApplicationContext())) {
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


    public void startChunkConversion(View v)
    {

    }


    public void selectChunkFile(View v)
    {

    }


    public void startPlayChunk(View v)
    {

    }

    /////////////////////////////////////////



    public boolean checkPermisson(final Context context)
    {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_CALENDAR)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setMessage("Write calendar permission is necessary to write event!!!");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
            } else {
                ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return false;
        }
        else
        {
            return true;
        }
    }




}
