package com.example.wuzhongcheng.notificationbar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MediaNotificationService extends Service {

    private static final String TAG = MediaNotificationService.class.getSimpleName();
    private Context mContext;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private boolean isCancel;
    private String mMusicTitle;
    private static final int LAST_MUSIC = 10000;
    private static final int NEXT_MUSIC = 10001;
    private static final int PLAY_MUSIC = 10002;
    private static final int CANCEL_NOTIFY = 10003;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        if (null != mRemoteViews) {
            mRemoteViews.removeAllViews(R.layout.custom_notification_dark);
            mRemoteViews = null;
        }
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification_dark);
        setImgToPause();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int operation = intent.getIntExtra("operation", -1);
        boolean isFromNotification = intent.getBooleanExtra("isFromNotification", false);
        boolean isUpdate = intent.getBooleanExtra("isUpdate", false);
        createNotification(); 
        if (isUpdate) {
            updateRemoteViews();
            startForeground(100, mBuilder.build());
            return START_STICKY;
        }

        switch (operation) {
            case NEXT_MUSIC:
                playNextMusic();
                break;
            case LAST_MUSIC:
                playBackMusic();
                break;
            case PLAY_MUSIC:
                playMusic();
                break;
            case CANCEL_NOTIFY:
                hideNotification();
                return START_STICKY;
            default:
                break;
        }
        startForeground(100, mBuilder.build());
        return START_STICKY;
    }

    private void createNotification() {
        isCancel = false;
        if (null == mNotificationChannel) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(mNotificationChannel);
            }
        }
        if (null == mBuilder) {
            mBuilder = new NotificationCompat.Builder(mContext,"default")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContent(mRemoteViews)
                    .setOnlyAlertOnce(false)
                    .setAutoCancel(false)
                    .setOngoing(true);
        }

        Intent mainIntent = new Intent(mContext, MainActivity.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(mContext, 0, mainIntent, 0));

        Intent nextMusicIntent = new Intent(this, MediaNotificationService.class);
        nextMusicIntent.putExtra("operation", NEXT_MUSIC);
        nextMusicIntent.putExtra("isFromNotification", true);
        PendingIntent nextMusicPi = PendingIntent.getService(mContext, 1, nextMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(nextMusicPi);
        mRemoteViews.setOnClickPendingIntent(R.id.tv_note, nextMusicPi);

        Intent playMusicIntent = new Intent(this, MediaNotificationService.class);
        playMusicIntent.putExtra("operation", PLAY_MUSIC);
        playMusicIntent.putExtra("isFromNotification", true);
        PendingIntent playMusicPi = PendingIntent.getService(mContext, 2, playMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(playMusicPi);
        mRemoteViews.setOnClickPendingIntent(R.id.tv_addSchedule, playMusicPi);

        Intent lastMusicIntent = new Intent(this, MediaNotificationService.class);
        lastMusicIntent.putExtra("operation", LAST_MUSIC);
        lastMusicIntent.putExtra("isFromNotification", true);
        PendingIntent lastMusicPi = PendingIntent.getService(mContext, 3, lastMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(lastMusicPi);
        mRemoteViews.setOnClickPendingIntent(R.id.tv_inbox, lastMusicPi);

        Intent cancelNotifyIntent = new Intent(this, MediaNotificationService.class);
        cancelNotifyIntent.putExtra("operation", CANCEL_NOTIFY);
        cancelNotifyIntent.putExtra("isFromNotification", true);
        PendingIntent cancelNotifyPi = PendingIntent.getService(mContext, 4, cancelNotifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(cancelNotifyPi);
        mRemoteViews.setOnClickPendingIntent(R.id.cancel_notify_btn, cancelNotifyPi);
    }

    public void hideNotification() {
        isCancel = true;
//        if (MediaPlayerService.mIsPlaying) {
//            MediaPlayerService.stopPlay(mContext);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.deleteNotificationChannel("default");
            mNotificationChannel = null;
        } else {
            stopForeground(true);
        }
    }

    private void playNextMusic() {
        Toast.makeText(mContext,"play next music",Toast.LENGTH_SHORT).show();
    }

    private void playBackMusic() {
        Toast.makeText(mContext,"play back music",Toast.LENGTH_SHORT).show();
    }

    private void playMusic() {
        Intent updateIntent = new Intent(this, MediaNotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        startService(updateIntent);
    }

    private void setImgToPause() {
        mRemoteViews.setImageViewResource(R.id.tv_addSchedule,R.drawable.stopmusic_btn);
    }

    private void updateRemoteViews() {
//        if(!TextUtils.equals(newTitle, mMusicTitle)) {
//            mRemoteViews.setTextViewText(R.id.music_title, newTitle);
//            mMusicTitle = newTitle;
//        }
        mRemoteViews.setImageViewResource(R.id.tv_addSchedule, R.drawable.window_pause);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
