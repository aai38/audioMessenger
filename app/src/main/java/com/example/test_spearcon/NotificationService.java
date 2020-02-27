package com.example.test_spearcon;


import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class NotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String key = sbn.getKey();
        int id = sbn.getId();
        long time = sbn.getPostTime();
        String packageName = sbn.getPackageName();
        CharSequence ticker = sbn.getNotification().tickerText;



        Log.d(TAG, "Notification Key " + key);
        Log.d(TAG, "Notification Id " + id);
        Log.d(TAG, "Notification postTime " + time);
        Log.d(TAG, "Notification From : " + packageName);
        Log.d(TAG, "Notification TikerText :" + ticker);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
}

