package com.github.chagall.notificationlistenerexample;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.nfc.Tag;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.robj.notificationhelperlibrary.utils.NotificationUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.Inflater;

import models.Action;

import static android.content.ContentValues.TAG;

/**
 * MIT License
 *
 *  Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class NotificationListenerExampleService extends NotificationListenerService {

    public static ArrayList<ReceivedMessage> messages = new ArrayList<ReceivedMessage>();
    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
        public static final String TELEGRAM_PACK_NAME = "org.telegram.messenger";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);
        Notification not =  sbn.getNotification();
        if (sbn.getPackageName().equals(ApplicationPackageNames.TELEGRAM_PACK_NAME)) {
            String message = not.extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            String person = not.extras.getCharSequence(Notification.EXTRA_TITLE).toString();
            String[] splitted = new String[3];
            //regex evtl in " \\(" ändern
            if (person.contains(" (")) {
                splitted = person.split(" \\(");
            } else {
                splitted[0] = person;
            }

            if (messages.size() == 0) {
                ReceivedMessage rec = new ReceivedMessage(message,splitted[0]);
                messages.add(rec);
            } else {
                boolean newPerson = true;
                for (int i = 0; i < messages.size(); i++) {

                    if (messages.get(i).getPerson().equals(person)) {
                        ReceivedMessage received = new ReceivedMessage(messages.get(i).getMessageText() + ", "+ message, splitted[0]);
                        messages.set(i, received);
                        newPerson = false;
                    }
                }
                if(newPerson) {
                    ReceivedMessage rec = new ReceivedMessage(message, splitted[0]);
                    messages.add(rec);
                }
            }
            //String person = not.extras.getCharSequence(Notification.).toString();

            MainActivity.updateOurText(splitted[0]+", " +message);
        }






        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
            Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
            intent.putExtra("Notification Code", notificationCode);
            sendBroadcast(intent);
        }
    }

    public void answerOnNotification(StatusBarNotification sbn, Notification not) {
        Action action = NotificationUtils.getQuickReplyAction(not,sbn.getPackageName());
        if(action == null) {
            return;
        }
        try{
            action.sendReply(
                    getApplicationContext(),
                    "something");

        }catch(PendingIntent.CanceledException e){

        }
    }



    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
                        intent.putExtra("Notification Code", notificationCode);
                        sendBroadcast(intent);
                        break;
                    }
                }
            }
        }
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
            return(InterceptedNotificationCode.FACEBOOK_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
            return(InterceptedNotificationCode.INSTAGRAM_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }

    public static String getMessageToRead () {
        String persons = "";
        String totalMessage = "";

        if (messages.size() == 0) {
            return "Du hast keine neuen Nachrichten";
        } else if (messages.size() == 1) {
            persons = messages.get(0).getPerson();
            totalMessage = persons + ", " + messages.get(0).getMessageText();
        } else {
            for (ReceivedMessage message: messages) {
                persons+= " " + message.getPerson();
                totalMessage = persons;
            }
        }
        return totalMessage;
    }

    public String getMessageFromPerson (String person) {
        for (ReceivedMessage message: messages) {
            if (message.getPerson().equals(person)) {
                return message.getMessageText();

            }
        }
        return "Keine Nachricht von dieser Person vorhanden";
    }
}
