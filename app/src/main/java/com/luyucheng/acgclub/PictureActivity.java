package com.luyucheng.acgclub;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.jaeger.library.StatusBarUtil;
import com.ortiz.touchview.TouchImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.concurrent.ExecutionException;

import cc.shinichi.wallpaperlib.SetWallpaper;

public class PictureActivity extends AppCompatActivity {

    private static final String TAG = "PictureActivity";
    private static final String APP_AUTHORITY = "com.luyucheng.acgclub.fileprovider";
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucent();
        setContentView(R.layout.activity_picture);
        imageUrl = getIntent().getStringExtra("IMGURL");
        TouchImageView touchImageView = findViewById(R.id.iv_full);
        new Thread(() -> {
            try {
                Bitmap bitmap = Glide.with(PictureActivity.this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit().get();
                runOnUiThread(() -> touchImageView.setImageBitmap(bitmap));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void setTranslucent() {
        StatusBarUtil.setTranslucent(this);
    }

    public void download(View view) {
        requestPermission(imageUrl, true);
    }

    public void setWallpaper(View view) {
        requestPermission(imageUrl, false);
    }

    private void requestPermission(String url, boolean isDownload) {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        baseTask(url, isDownload);
                    } else {
                        Toast.makeText(PictureActivity.this, "没有存储权限，没法进行下一步了", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void baseTask(final String url, boolean isDownload) {

        new AsyncTask<Void, Integer, File>() {

            @Override
            protected File doInBackground(Void... params) {
                File file = null;
                File destFile = null;
                try {
                    FutureTarget<File> future = Glide
                            .with(PictureActivity.this)
                            .load(url)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

                    file = future.get();
                    // 首先保存图片
                    File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
                    File appDir = null;
                    String fileName = "test";
                    if (isDownload) {
                        appDir = new File(pictureFolder, "AcgClub/AcgClub下载");
                        fileName = System.currentTimeMillis() + ".jpg";
                    } else {
                        appDir = new File(pictureFolder, "AcgClub/AcgClub缓存");
                        fileName = System.currentTimeMillis() + "";
                    }
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }

                    destFile = new File(appDir, fileName);
                    com.luyucheng.acgclub.FileUtil.copy(file, destFile);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return destFile;
            }

            @Override
            protected void onPostExecute(File destFile) {
                if (isDownload) {
                    // 通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(new File(destFile.getPath()))));
                    Toast.makeText(PictureActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                } else {
                    SetWallpaper.setWallpaper(PictureActivity.this, // 上下文
                            destFile.getAbsolutePath(), // 图片绝对路径
                            APP_AUTHORITY);// authority（7.0 文件共享权限）
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }
        }.execute();
    }
}
