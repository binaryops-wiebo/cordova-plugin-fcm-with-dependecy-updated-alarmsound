package com.gae.scaffolder.plugin;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import com.binaryops.pnwtsunamialert.R;

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
        player = MediaPlayer.create(this, R.raw.alarmurgent); //select music file
        player.setLooping(true); //set looping
//        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        player.start();
        return Service.START_NOT_STICKY;
    }

    public void stopAlarm() {
        player.stop();
    }

    public void onDestroy() {
        player.stop();
        player.release();
        stopSelf();
        super.onDestroy();
    }
}
