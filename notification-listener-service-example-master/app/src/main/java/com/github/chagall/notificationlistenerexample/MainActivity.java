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

    public static TextToSpeech t1;
    public MicrophoneListener micro;
    public boolean hasRecorded;
    public String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.mp4";
        micro = new MicrophoneListener(fileName);


        //Button zum Testen von Spracheingabe (später entfernen)
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    micro.startRecording();
                    hasRecorded = true;
                } else {
                    if(hasRecorded) {
                        micro.stopRecording();
                        //micro.startPlaying();
                        try {
                            parseSpeechToText();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(imageChangeBroadcastReceiver);
        micro.stopRecording();
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

    public void parseSpeechToText() throws FileNotFoundException {
        IamAuthenticator authenticator = new IamAuthenticator("N2UJ-ncPfcdKPi71q8ESL1yapZWy5Qh6FkbEZmsQTnr3");
        SpeechToText speechToText = new SpeechToText(authenticator);
        speechToText.setServiceUrl("https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/a0d543a7-e42e-45d9-b28f-ffaa0922d3c7");
        SpeechToText service = new SpeechToText(authenticator);

        File audio = new File(fileName);
        RecognizeOptions options = new RecognizeOptions.Builder()
                .audio(audio)
                .contentType(HttpMediaType.AUDIO_BASIC)
                .build();
        SpeechRecognitionResults transcript = service.recognize(options).execute().getResult();
        view.setText(transcript.toString());
    }

    public static void updateOurText(String text) {

        view.setText("Test: "+ text);
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
