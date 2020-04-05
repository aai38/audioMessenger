package com.github.chagall.notificationlistenerexample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.io.File;
import java.util.Locale;

import HelperClasses.NotificationBroadcastReceiver;

import static android.media.AudioManager.*;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity {

    private String[] keywords = {"antworten"};
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private ImageView interceptedNotificationImageView;
    private static TextView view;
    public static NotificationBroadcastReceiver broadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;
    private Button play;

    public static TextToSpeech t1;
    public static MicrophoneListener micro;
    public boolean hasRecorded;
    public String fileName;
    public static SoundPool sp;
    private static int messageReceivedEarcon;
    private static int answerModeActiveEarcon;
    public static Thread messageThread;
    public boolean waitForAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);



        // Record to the external cache directory for visibility
        fileName = getFilesDir()+"/speak.pcm";
        micro = new MicrophoneListener(fileName);

        Button button = (Button) findViewById(R.id.buttonEverything);
        button.setOnClickListener( (View view) -> {
            updateOurText( NotificationListenerExampleService.getMessageToRead(), false);

        });




        // Here we get a reference to the image we will modify when a notification is received
        interceptedNotificationImageView
                = (ImageView) this.findViewById(R.id.intercepted_notification_logo);
        view = (TextView) this.findViewById(R.id.image_change_explanation);
        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        broadcastReceiver = new NotificationBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.github.chagall.notificationlistenerexample");
        registerReceiver(broadcastReceiver,intentFilter);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(STREAM_MUSIC, 15, 0);
        t1 = new TextToSpeech(getApplicationContext(), (status) -> {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.GERMAN);
            }
        });

        sp = new SoundPool(2, STREAM_MUSIC, 0);
        messageReceivedEarcon = sp.load(this, R.raw.earcon1, 1);
        answerModeActiveEarcon = sp.load(this, R.raw.earcon_answer_mode, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(broadcastReceiver);

    }

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    public void playMessage(String text, boolean isSingleMsgMode) {

        File file = new File("../../../../../res/raw/earcon1.mp3");
        int succ1 = t1.addEarcon("[earcon]", file.getAbsolutePath());//"", R.raw.earcon1);
        Bundle param = new android.os.Bundle();
        param.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, 3);
        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "earcon");
        //param.putBundle(String.valueOf(TextToSpeech.Engine.KEY_PARAM_STREAM), String.valueOf(AudioManager.STREAM_MUSIC));
        int succ = t1.playEarcon("[earcon]",TextToSpeech.QUEUE_FLUSH, param, "earcon");
        System.out.println("Success: "+succ1+ " " + succ);

        t1.speak(text,TextToSpeech.QUEUE_ADD,null);
        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId){
                sp.play(messageReceivedEarcon, 1,1,0,0,1);

            }
            @Override
            public void onDone(String utteranceId) {
                //listen if user wants to answer


                // Speaking stopped.
            }
            @Override
            public void onError(String utteranceId) {
                // There was an error.
            }
        });
        while(t1.isSpeaking()) {
            //wait until message was played
        }

        if(isSingleMsgMode) {
            reactOnMessage();
        }


    }

    private void setTextFromOtherThread(String s) {
        view.post(new Runnable() {
            public void run() {
                view.setText(s);
            }
        });
    }

    public void reactOnMessage() {
        micro.startRecording(3000);
        boolean answer = false;
        setTextFromOtherThread("Warte 3s auf Schlüsselwort (\"Antworten\")...");
        while(micro.isRecording) {
            //wait until a keyword was spoken

            //the answer keyword was spoken
            if(checkKeyword(micro.result,0)) {
                answer = true;
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);

                break;
            }
        }
        micro.stopRecording();

        if(answer) {
            setTextFromOtherThread("Antworten-Schlüsselwort erkannt!\nSpreche nun deine Nachricht ein...");
            micro.startRecording(5000);
            while(micro.isRecording){
                //wait until user has spoken his answer
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            micro.stopRecording();
            setTextFromOtherThread("Sende Antwort: "+micro.result);
            broadcastReceiver.isAnswer = true;
            Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
            intent.putExtra("Answer", micro.result);

            sendBroadcast(intent);

        } else {
            setTextFromOtherThread("Kein Schlüsselwort erkannt.");
        }

    }

    public void updateOurText(String text, boolean isSingleMsgMode) {
        view.setText(text);
        messageThread = new Thread(new Runnable() {
            public void run() {
                playMessage(text, isSingleMsgMode);
                Thread.currentThread().interrupt();
            }
        }, "Message Thread");
        messageThread.start();





    }

    /**
     * Change Intercepted Notification Image
     * Changes the MainActivity image based on which notification was intercepted
     * @param notificationCode The intercepted notification code
     */
    private void changeInterceptedNotificationImage(int notificationCode){
        switch(notificationCode){
            case NotificationListenerExampleService.InterceptedNotificationCode.FACEBOOK_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.facebook_logo);
                break;
            case NotificationListenerExampleService.InterceptedNotificationCode.INSTAGRAM_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.instagram_logo);
                break;
            case NotificationListenerExampleService.InterceptedNotificationCode.WHATSAPP_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.whatsapp_logo);
                break;
            case NotificationListenerExampleService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.other_notification_logo);
                break;
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }




    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

    public boolean checkKeyword(String phrase, int keywordIndex) {

        for (String s : phrase.split(" ")) {
            if(s.equals(keywords[keywordIndex])) {
                return true;
            }
        }
        return false;
    }




}
