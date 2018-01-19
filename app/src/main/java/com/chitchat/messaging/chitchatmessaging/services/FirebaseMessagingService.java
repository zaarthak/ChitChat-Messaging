package com.chitchat.messaging.chitchatmessaging.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.chitchat.messaging.chitchatmessaging.R;
import com.chitchat.messaging.chitchatmessaging.utils.Constants;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service that generates a notification on receiving a message when the app is in foreground.
 *
 * NOTE : For notifications when the app is in background, Firebase functions have been used.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationMessage = remoteMessage.getNotification().getBody();

        String userId = remoteMessage.getData().get(Constants.INTENT_USER_ID_KEY);
        String userName = remoteMessage.getData().get(Constants.INTENT_USER_NAME_KEY);

        String clickAction = remoteMessage.getNotification().getClickAction();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setPriority(2)
                .setSound(defaultSoundUri)
                .setGroupSummary(true)
                .setOnlyAlertOnce(true);

        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra(Constants.INTENT_USER_ID_KEY, userId);
        resultIntent.putExtra(Constants.INTENT_USER_NAME_KEY, userName);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);

        // set an ID for the notification
        int mNotificationId = 1000;
        // get an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // builds a unique notification for each user
        mNotifyMgr.notify(userId, mNotificationId, mBuilder.build());
    }
}
