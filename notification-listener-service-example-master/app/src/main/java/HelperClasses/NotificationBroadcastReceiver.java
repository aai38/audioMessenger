package HelperClasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.chagall.notificationlistenerexample.MainActivity;
import com.github.chagall.notificationlistenerexample.NotificationListenerExampleService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private MainActivity main;
    private NotificationListenerExampleService not;
    public boolean isAnswer = false;

    public NotificationBroadcastReceiver(MainActivity main){
        this.main = main;

    }

    public void setNotificationListener(NotificationListenerExampleService not) {
        this.not = not;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //if(!isAnswer) {
            main.updateOurText(intent.getStringExtra("Message"));
        /*} else {
            not.answerOnNotification(intent.getStringExtra("Answer"));
        }*/

    }
}
