package com.github.chagall.notificationlistenerexample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.provider.Settings;
import android.speech.RecognizerIntent;
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
import android.widget.Toast;
import android.widget.ToggleButton;


import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.media.AudioManager.*;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private ImageView interceptedNotificationImageView;
    private static TextView view;
    private ImageChangeBroadcastReceiver imageChangeBroadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;
    private Button play;

    public static TextToSpeech t1;
    public static MicrophoneListener micro;
    public boolean hasRecorded;
    public String fileName;
    public static SoundPool sp;
    private static int earcon;

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
            updateOurText( NotificationListenerExampleService.getMessageToRead());

        });


        //Button zum Testen von Spracheingabe (später entfernen)
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    micro.startRecording();

                    hasRecorded = true;
                } else {
                    if(hasRecorded) {

                        view.setText(micro.result);
                        micro.result = "";
                    }
                    // The toggle is disabled
                }
            }
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
        imageChangeBroadcastReceiver = new ImageChangeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.github.chagall.notificationlistenerexample");
        registerReceiver(imageChangeBroadcastReceiver,intentFilter);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(STREAM_MUSIC, 15, 0);
        t1 = new TextToSpeech(getApplicationContext(), (status) -> {
            if(status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.GERMAN);
            }
        });

        sp = new SoundPool(2, STREAM_MUSIC, 0);
        earcon = sp.load(this, R.raw.earcon1, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(imageChangeBroadcastReceiver);

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



    public static void updateOurText(String text) {

        view.setText(text);
        File file = new File("../../../../../res/raw/earcon1.mp3");
        ///Users/linaxu/Documents/audiomessenger/notification-listener-service-example-master/app/src/main/java/com/github/chagall/notificationlistenerexample/MainActivity.java
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
                sp.play(earcon, 1,1,0,0,1);
            }
            @Override
            public void onDone(String utteranceId) {
                // Speaking stopped.

            }
            @Override
            public void onError(String utteranceId) {
                // There was an error.
            }
        });


        //wait until the answer keyword has been spoken or timeout has been reached
        /*micro.startRecording();
        while(t1.isSpeaking()) {
            if(micro.result.equals("antworte")) {
                break;
            }
        }
        long currentTime = 0;
        long time = System.currentTimeMillis();
        while(currentTime < 3000) {
            currentTime = System.currentTimeMillis() - time;
            if(micro.result.equals("antworte")) {
                micro.stopRecording();
                break;
            }
        }
        micro.startRecording();
        while(micro.result.equals("")) {

        }*/
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
     * Image Change Broadcast Receiver.
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     * */
    public class ImageChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            changeInterceptedNotificationImage(receivedNotificationCode);
        }
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




}
