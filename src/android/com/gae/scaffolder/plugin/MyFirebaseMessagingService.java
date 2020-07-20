package com.gae.scaffolder.plugin;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import com.binaryops.pnwtsunamialert.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.xmlpull.v1.XmlPullParserException;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMPlugin";


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);
        FCMPlugin.sendTokenRefresh(token);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");
        
        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
        }
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("wasTapped", false);
        
        if(remoteMessage.getNotification() != null){
            data.put("title", remoteMessage.getNotification().getTitle());
            data.put("body", remoteMessage.getNotification().getBody());
        }

        for (String key : remoteMessage.getData().keySet()) {
                Object value = remoteMessage.getData().get(key);
                Log.d(TAG, "\tKey: " + key + " Value: " + value);
                data.put(key, value);
        }
        
        Log.d(TAG, "\tNotification Data: " + data.toString());


        String soundAlarm = data.get("soundAlarm").toString();
        if ("true".equalsIgnoreCase(soundAlarm)){
            Log.d(TAG, "\tStarting Alarm" );
            Intent intent = new Intent(this, AlarmSoundService.class);
            startService(intent);
        } else {
            Log.d(TAG, "\t Skipping Alarm");
        }

        this.createNotification(data);

        FCMPlugin.sendPushPayload(data);
    }
    // [END receive_message]

    public int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }

    private void createNotification(Map<String, Object> data){
        // Create an explicit intent for an Activity in your app
        Intent notification_intent = new Intent(this, FCMPluginActivity.class);
        notification_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notification_intent, 0);

        String notifTitle = data.get("notificationTitle").toString();
        String notifShort = data.get("notificationShortText").toString();
        String notifLong = data.get("notificationLongText").toString();

        String channelID= this.getChannelID();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notifTitle)
                .setContentText(notifShort)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notifLong))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        int notificationId = createID();
        notificationManager.notify(notificationId, builder.build());
    }

    private String  getChannelID() {

        String channelID ="alert";
        String preferenceTag = "preference";
        Context context = this.getBaseContext();

        int configXmlResourceId = context.getResources().getIdentifier("config", "xml", context.getPackageName());
        XmlResourceParser xrp =  context.getResources().getXml(configXmlResourceId);

        //
        // walk the config.xml tree and save all <preference> tags we want
        //
        try{
            xrp.next();
            while(xrp.getEventType() != XmlResourceParser.END_DOCUMENT){
                if(preferenceTag.equals(xrp.getName())){
                    String key = xrp.getAttributeValue(null, "name");
                    if("alertChannelID".equalsIgnoreCase(key)){
                        channelID = xrp.getAttributeValue(null, "value");
                        break;
                    }
                }
                xrp.next();
            }
        } catch(XmlPullParserException ex){
            Log.e(TAG, ex.toString());
        }  catch(IOException ex){
            Log.e(TAG, ex.toString());
        }

        return channelID;

    }
}
