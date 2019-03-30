package com.luyucheng.acgclub.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.luyucheng.acgclub.Application;
import com.luyucheng.acgclub.R;
import com.luyucheng.acgclub.glide.GlideApp;
import com.luyucheng.acgclub.util.FileUtil;
import com.luyucheng.acgclub.util.NetworkUtil;
import com.ortiz.touchview.TouchImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.concurrent.ExecutionException;

import cc.shinichi.wallpaperlib.SetWallpaper;

public class PictureActivity extends BaseActivity {

    private static final String TAG = "PictureActivity";
    private static final String APP_AUTHORITY = "com.luyucheng.acgclub.fileprovider";
    private String imageUrl;
    private LinearLayout control_layuout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTranslucent();
        setContentView(R.layout.activity_picture);
        imageUrl = getIntent().getStringExtra("IMGURL");
        control_layuout = findViewById(R.id.control_layuout);
        TouchImageView touchImageView = findViewById(R.id.iv_full);
        new Thread(() -> {
            try {
                Bitmap bitmap = GlideApp.with(PictureActivity.this)
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


    public void download(View view) {
        if (NetworkUtil.isNetworkAvailable(this))
            requestPermission(imageUrl, 1);
        else Toast.makeText(PictureActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
    }

    public void setWallpaper(View view) {
        if (NetworkUtil.isNetworkAvailable(this))
            requestPermission(imageUrl, 2);
        else Toast.makeText(PictureActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
    }


    public void share(View view) {
        if (NetworkUtil.isNetworkAvailable(this))
            requestPermission(imageUrl, 3);
        else Toast.makeText(PictureActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
        /** * 分享图片 */
//        new Thread(() -> {
//            try {
//                File file = GlideApp.with(PictureActivity.this)
//                        .asFile()
//                        .load(imageUrl)
//                        .submit().get();
//                runOnUiThread(() -> {
//                    Intent share_intent = new Intent();
//                    share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
//                    share_intent.setType("image/*");  //设置分享内容的类型
//                    share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                    //创建分享的Dialog
//                    share_intent = Intent.createChooser(share_intent, "分享到");
//                    startActivity(share_intent);
//                });
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//        }
    }

    private void requestPermission(String url, int flag) {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        baseTask(url, flag);
                    } else {
                        Toast.makeText(PictureActivity.this, "没有存储权限，没法进行下一步了", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void baseTask(final String url, int flag) {

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
                    if (flag == 1) {
                        appDir = new File(pictureFolder, "AcgClub/AcgClub下载");
                        fileName = System.currentTimeMillis() + ".jpg";
                    } else if (flag == 2) {
                        appDir = new File(pictureFolder, "AcgClub/AcgClub缓存");
                        fileName = System.currentTimeMillis() + "";
                    } else if (flag == 3) {
                        appDir = new File(pictureFolder, "AcgClub/AcgClub缓存");
                        fileName = System.currentTimeMillis() + "";
                    }
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }

                    destFile = new File(appDir, fileName);
                    FileUtil.copy(file, destFile);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return destFile;
            }

            @Override
            protected void onPostExecute(File destFile) {
                if (flag == 1) {
                    // 通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(new File(destFile.getPath()))));
                    Toast.makeText(PictureActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                } else if (flag == 2) {
                    SetWallpaper.setWallpaper(PictureActivity.this, // 上下文
                            destFile.getAbsolutePath(), // 图片绝对路径
                            APP_AUTHORITY);// authority（7.0 文件共享权限）
                } else if (flag == 3) {
                    Log.i(TAG, "onPostExecute: " + getUriFromFile(destFile));
                    Intent share_intent = new Intent();
                    share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
                    share_intent.setType("image/*");  //设置分享内容的类型
                    share_intent.putExtra(Intent.EXTRA_STREAM, getUriFromFile(destFile));
                    //创建分享的Dialog
                    share_intent = Intent.createChooser(share_intent, "分享到");
                    startActivity(share_intent);
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }
        }.execute();
    }

    /**
     * 获取本地文件的uri
     *
     * @param file
     * @return
     */
    public static Uri getUriFromFile(File file) {
        Uri imageUri = null;
        if (file != null && file.exists() && file.isFile()) {
//            imageUri = Uri.fromFile(file);
            imageUri = FileProvider.getUriForFile(Application.getInstance(), "com.luyucheng.acgclub.fileprovider", file);
        }
        return imageUri;
    }

    public void changeVisible(View view) {
        if (control_layuout.getVisibility() == View.VISIBLE)
            control_layuout.setVisibility(View.GONE);
        else
            control_layuout.setVisibility(View.VISIBLE);
    }
}
