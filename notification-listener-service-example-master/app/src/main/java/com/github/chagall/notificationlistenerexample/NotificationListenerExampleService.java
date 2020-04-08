package com.github.chagall.notificationlistenerexample;

import android.content.Intent;
import android.content.IntentFilter;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.robj.notificationhelperlibrary.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import HelperClasses.NotificationBroadcastReceiver;
import models.Action;

/**
 * MIT License
 * <p>
 * Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * <p>
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
    public static StatusBarNotification currentSBN;
    public static Queue<StatusBarNotification> notifications = new LinkedList<>();
    String lastMessage = "";

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
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public boolean isFirst = true;
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(ApplicationPackageNames.TELEGRAM_PACK_NAME)) {
            Notification not = sbn.getNotification();
            String message = not.extras.getCharSequence(Notification.EXTRA_TEXT).toString();

            if (isFirst) {
                isFirst = false;
                return;
            }
            currentSBN = sbn;
            lastMessage = message;
            if (!MainActivity.notificationActive) {
                MainActivity.notificationActive = true;
                MainActivity.broadcastReceiver.setNotificationListener(this);
                Log.d("HIER", "nachricht kommt");
                int notificationCode = matchNotificationCode(sbn);


                if (message.contains(":")) {
                    return;
                }

                Log.d("MESSAGE", message);
                String person = not.extras.getCharSequence(Notification.EXTRA_TITLE).toString();
                Log.d("person", person);
                String[] splitted = new String[3];
                //regex evtl in " \\(" ändern
                if (person.contains(" (")) {
                    splitted = person.split(" \\(");
                } else {
                    splitted[0] = person;
                }

                if (messages.size() == 0) {
                    ReceivedMessage rec = new ReceivedMessage(message, person);
                    messages.add(rec);
                } else {
                    boolean newPerson = true;
                    for (int i = 0; i < messages.size(); i++) {
                        if (messages.get(i).getPerson().equals(splitted[0])) {
                            if (!(messages.get(i).getMessageText().equals(message))) {
                                Log.d("before message", messages.get(i).getMessageText());
                                ReceivedMessage received = new ReceivedMessage(messages.get(i).getMessageText() + message, splitted[0]);
                                messages.remove(i);
                                messages.add(i, received);
                                Log.d("after message", messages.get(i).getMessageText());

                            }
                            newPerson = false;
                        }
                    }
                    if (newPerson) {
                        ReceivedMessage rec = new ReceivedMessage(message, splitted[0]);
                        messages.add(rec);
                    }
                }
                //String person = not.extras.getCharSequence(Notification.).toString();

                //MainActivity.updateText(splitted[0]+", " +message);

                MainActivity.broadcastReceiver.isAnswer = false;
                Intent intent = new Intent("com.github.chagall.notificationlistenerexample");
                Log.d("SPLIT", splitted[0]);
                if (splitted[0].contains(":")) { //group message
                    String[] groupContact = splitted[0].split(":");
                    //Log.d("GROUPCONTACT", groupContact[1]);
                    Log.d("MESSAGE", messages.get(0).getMessageText());
                    intent.putExtra("Message", "Nachricht von" + groupContact[1] + " in " + groupContact[0] + ": " + messages.get(0).getMessageText());
                } else { //single person
                    intent.putExtra("Message", "Nachricht von " + splitted[0] + ": " + message);
                }

                sendBroadcast(intent);
            } else {
                System.out.println("notification buffered");
                notifications.add(sbn);

            }
            isFirst = true;
        }


    }

    public void answerOnNotification(String answer) {
        MainActivity.messageThread = null;
        Action action = NotificationUtils.getQuickReplyAction(currentSBN.getNotification(), currentSBN.getPackageName());
        if (action != null) {
            try {

                action.sendReply(
                        getApplicationContext(),
                        answer);

            } catch (PendingIntent.CanceledException e) {

            }
        }

        if (!notifications.isEmpty()) {

            onNotificationPosted(notifications.remove());
        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //int notificationCode = matchNotificationCode(sbn);
        //Log.d("REMOVED", sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
        if (!(sbn.equals(null))) {
            String message = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            String person = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
            String[] splitted = new String[3];
            //regex evtl in " \\(" ändern
            if (person.contains(" (")) {
                splitted = person.split(" \\(");
            } else {
                splitted[0] = person;
            }
            if (sbn.getPackageName().equals(ApplicationPackageNames.TELEGRAM_PACK_NAME)) {
                //search for the right notification in messages
                for (ReceivedMessage m : messages) {
                    if (m.getPerson().equals(splitted[0]) && m.getMessageText().contains(message)) {
                        messages.remove(m);
                    }
                }
            }
        }

        /*if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

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
        }*/
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if (packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)) {
            return (InterceptedNotificationCode.FACEBOOK_CODE);
        } else if (packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)) {
            return (InterceptedNotificationCode.INSTAGRAM_CODE);
        } else if (packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)) {
            return (InterceptedNotificationCode.WHATSAPP_CODE);
        } else {
            return (InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }

    public static String getMessageToRead() {
        //check all active notifications

        String persons = "";
        String totalMessage = "";

        if (messages.size() == 0) {
            return "Keine neuen Nachrichten";
        } else if (messages.size() == 1) {
            //group message
            if (messages.get(0).getPerson().contains(":")) {
                String[] splitted = messages.get(0).getPerson().split(":");
                totalMessage = "Nachricht von " + splitted[1] + " in " + splitted[0] + ": " + messages.get(0).getMessageText();
                return totalMessage;
            } else { //single person
                persons = messages.get(0).getPerson();
                totalMessage = persons + ": " + messages.get(0).getMessageText();
                return "Nachricht von " + totalMessage;
            }
        } else {
            for (ReceivedMessage message : messages) {
                if (message.getPerson().contains("Telegram")) {
                    //messages.remove(message);
                    continue;
                }
                if (message.getPerson().contains(":")) { //group message
                    String[] splitted = message.getPerson().split(":");
                    persons += splitted[1] + " in " + splitted[0] + ",";
                } else {
                    persons += message.getPerson() + ",";
                }
            }
            //remove last ,
            if (persons != null && persons.length() > 0 && persons.charAt(persons.length() - 1) == ',') {
                persons = persons.substring(0, persons.length() - 1);
            }
            return "Nachrichten von" + persons;
        }
    }

    public String getMessageFromPerson(String person) {
        for (ReceivedMessage message : messages) {
            if (message.getPerson().equals(person)) {
                return message.getMessageText();

            }
        }
        return "Keine Nachricht von dieser Person vorhanden";
    }
}
