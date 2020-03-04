package com.example.test_spearcon;


import android.app.Notification;
import android.app.PendingIntent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.robj.notificationhelperlibrary.utils.NotificationUtils;

import models.Action;
import services.BaseNotificationListener;

import static android.content.ContentValues.TAG;
import static com.robj.notificationhelperlibrary.utils.NotificationUtils.getQuickReplyAction;

public class NotificationService extends BaseNotificationListener {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String key = sbn.getKey();
        int id = sbn.getId();
        long time = sbn.getPostTime();
        String packageName = sbn.getPackageName();
        CharSequence ticker = sbn.getNotification().tickerText;
        Notification notification = sbn.getNotification();

        Action action = NotificationUtils.getQuickReplyAction(notification, packageName);

        //TODO ask user, if he wants to answer, get Message from User, generate Text from Speech

        String message = "test";

        try {
            action.sendReply(getApplicationContext(), message);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "Notification Key " + key);
        Log.d(TAG, "Notification Id " + id);
        Log.d(TAG, "Notification postTime " + time);
        Log.d(TAG, "Notification From : " + packageName);
        Log.d(TAG, "Notification TikerText :" + ticker);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    protected boolean shouldAppBeAnnounced(StatusBarNotification sbn) {
        return false;
    }

    @Override
    protected void onNotificationPosted(StatusBarNotification sbn, String dismissKey) {

    }
}

