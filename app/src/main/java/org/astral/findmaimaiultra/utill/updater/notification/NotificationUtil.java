package org.astral.findmaimaiultra.utill.updater.notification;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.ui.UpdateActivity;


public class NotificationUtil extends Service {

    private volatile static NotificationUtil INSTANCE;
    private Context mContext;
    private static final String TAG = "notification";

    public static NotificationUtil getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (NotificationUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NotificationUtil();
                }
            }
        }
        return INSTANCE;
    }

    public NotificationUtil() {
    }

    public NotificationUtil setContext(Context mContext) {
        Log.d(TAG, "setContext: " + mContext);
        this.mContext = mContext;
        return getINSTANCE();
    }

    public void startNotification() {
        Log.d(TAG, "startNotification: " + mContext);
        Intent notificationIntent = new Intent(mContext, this.getClass());
        mContext.startService(notificationIntent);
    }

    public void stopNotification() {
        Intent notificationIntent = new Intent(mContext, this.getClass());
        mContext.stopService(notificationIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Intent nfIntent = new Intent(this, UpdateActivity.class);
        nfIntent.setAction(Intent.ACTION_MAIN);
        nfIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        nfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE; // 或者使用 PendingIntent.FLAG_MUTABLE
        }

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, pendingIntentFlags))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("更新器后台运行通知")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("正在努力的帮你上传分数")
                .setWhen(System.currentTimeMillis());

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("updater_keep_alive_channel");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("updater_keep_alive_channel", "更新器后台运行通知", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(12001, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
