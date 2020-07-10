package com.gae.scaffolder.plugin;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public class FCMPluginActivity extends Activity {
    public static final String CHANNEL_ID = "urgent_alert";
    private static String TAG = "FCMPlugin";
    AlarmSoundService alarmSoundService;
    boolean mBound;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AlarmSoundService.LocalBinder binder = (AlarmSoundService.LocalBinder) service;
            alarmSoundService = binder.getService();
            alarmSoundService.stopAlarm();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /*
     * this activity will be started if the user touches a notification that we own. 
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application. 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "==> FCMPluginActivity onCreate");
		
		Map<String, Object> data = new HashMap<String, Object>();
        if (getIntent().getExtras() != null) {
			Log.d(TAG, "==> USER TAPPED NOTIFICATION");
			data.put("wasTapped", true);
			for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "\tKey: " + key + " Value: " + value);
				data.put(key, value);
            }
        }

        // Bind to LocalService
        Intent intent = new Intent(this, AlarmSoundService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
		
		FCMPlugin.sendPushPayload(data);

        finish();

        cancelAlarm();

        forceMainActivityReload();
    }

    private void cancelAlarm(){
        if (mBound) {
            Log.d(TAG, "==> FCMPluginActivity bound to AlarmService cancelling alarm");
            alarmSoundService.stopAlarm();

        } else {
            Log.d(TAG, "==> FCMPluginActivity NOT bound to AlarmService unable to cancel alarm");
        }

    }


    private void forceMainActivityReload() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
		Log.d(TAG, "==> FCMPluginActivity onResume");
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "==> FCMPluginActivity onStart");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "==> FCMPluginActivity onStop");
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "==> FCMPluginActivity onDestroy");
        unbindService(connection);
    }

}