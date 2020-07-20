package com.gae.scaffolder.plugin;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class AlarmSoundService extends Service {
    MediaPlayer player;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        AlarmSoundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AlarmSoundService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onCreate() {

//        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int alarmfcm = this.getResources().getIdentifier( "alarmfcm", "raw", this.getPackageName());
//        int fcma = R.raw.alarmfcm;
        player = MediaPlayer.create(this, alarmfcm); //select music file
        player.setLooping(true); //set looping
        player.start();
        return Service.START_NOT_STICKY;
    }

    public void stopAlarm() {
        if (player != null){
            player.stop();
        }
    }

    public void onDestroy() {
        if (player != null){
            player.stop();
            player.release();
        }

        stopSelf();
        super.onDestroy();
    }
}
