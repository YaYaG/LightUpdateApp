package com.jackwang.updateapk;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 姓名: Jack
 * 时间： 2020-03-25
 * 描述：
 */
public class UpdateApp {
    private static volatile UpdateApp mUpdateApp;
    private UpdateListener mUpdateListener;
    private boolean autoInatall = true;
    private File mApkFile;

    private UpdateApp() {
    }

    public static UpdateApp getInstance() {
        if (mUpdateApp == null) {
            synchronized (UpdateApp.class) {
                if (mUpdateApp == null) {
                    mUpdateApp = new UpdateApp();
                }
            }
        }
        return mUpdateApp;
    }


    public UpdateApp setUpdateListener(UpdateListener updateListener) {
        mUpdateListener = updateListener;
        return getInstance();
    }

    public UpdateApp setAutoInatall(boolean autoInatall) {
        this.autoInatall = autoInatall;
        return getInstance();
    }

    public void downloadApp(final Activity activity, final String apkUrl, final int logo) {
        AndPermission.with(activity)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        download(activity, apkUrl, logo);
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(activity, permissions)) {
                            // 打开权限设置页
                            AndPermission.permissionSetting(activity).execute();
                        }
                    }
                })
                .start();
    }

    private void download(final Activity context, String apkUrl, @DrawableRes final int logo) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apkUrl)
                .get()
                .build();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = Environment.getExternalStorageDirectory() + File.separator + "download";
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("adbParent", "StemParent", NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                    builder.setChannelId("adbParent");
                }

                builder.setContentTitle("正在更新...") //设置通知标题
                        .setSmallIcon(logo)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), logo)) //设置通知的大图标
                        .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                        .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                        .setAutoCancel(false)//设置通知被点击一次是否自动取消
                        .setContentText("下载进度:" + "0%")
                        .setProgress(100, 0, false);

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File apkFile = new File(savePath, "app.apk");
                    fos = new FileOutputStream(apkFile);
                    long sum = 0;
                    int currentProgress = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        final int progress = (int) (sum * 1.0f / total * 100);
                        if (currentProgress != progress) {
                            currentProgress = progress;
                            builder.setProgress(100, progress, true);
                            builder.setContentText("下载进度:" + progress + "%");
                            notificationManager.notify(1, builder.build());
                            if (mUpdateListener != null) {
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mUpdateListener.progress(progress);
                                    }
                                });
                            }
                        }
                    }
                    fos.flush();
                    builder.setContentTitle("下载完成")
                            .setContentText("点击安装")
                            .setAutoCancel(true);//设置通知被点击一次是否自动取消

                    mApkFile = apkFile;
                    startInstallN(context, builder, notificationManager);
                    if (autoInatall) {
                        startInstall(context);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mUpdateListener != null) {
                                mUpdateListener.downFail(e);
                            }
                        }
                    });
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * 安装apk 点击通知
     */
    private void startInstallN(Context mContext,
                               NotificationCompat.Builder builder, NotificationManager notificationManager) {

        PendingIntent pi = PendingIntent.getActivity(mContext, 0, preInatall(mContext, mApkFile), 0);
        Notification notification = builder.setContentIntent(pi).build();
        notificationManager.notify(1, notification);
    }


    /**
     * 安装apk 自动
     */
    public void startInstall(Context mContext) {
        mContext.startActivity(preInatall(mContext, mApkFile));
    }

    private Intent preInatall(Context context, File file) {
        if (!file.exists()) {
            return null;
        }
        //安装
        Intent install = new Intent(Intent.ACTION_VIEW);
        Log.i("getApplication-",context.getApplicationContext().getPackageName());
        //判断是否是android 7.0及以上
        if (Build.VERSION.SDK_INT >= 24) {
            //7.0获取存储文件的uri
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //赋予临时权限
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //设置dataAndType
            install.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            if (file.exists()) {
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
        }

        return install;
    }
}
