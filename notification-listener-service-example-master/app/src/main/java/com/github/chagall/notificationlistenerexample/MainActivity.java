package com.github.chagall.notificationlistenerexample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.File;
import java.util.Locale;

import HelperClasses.NotificationBroadcastReceiver;

import static android.media.AudioManager.*;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity {

    private String[] keywords = {"antworten", "abhören", "schreibe", "alle"};
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private ImageView interceptedNotificationImageView;
    private static TextView view;
    public static NotificationBroadcastReceiver broadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;
    private Button play;

    public static TextToSpeech t1;
    public static TextToSpeech t2;
    public static TextToSpeech t3;
    public static MicrophoneListener micro;
    public boolean hasRecorded;
    public String fileName;
    public static SoundPool sp;
    private static int messageReceivedEarcon;
    private static int answerModeActiveEarcon;
    public static Thread messageThread;
    public static Thread activeListeningThread;
    public boolean waitForAnswer;
    private SeekBar speechSpeed;
    private SeekBar contactSpeed;
    private int speechSpeedValue = 1;
    public static boolean notificationActive = false;
    public static boolean isSamePerson = false;
    public String bufferedAnswer = "";

    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private ImageButton favorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        favorite = findViewById(R.id.imageButton);
        favorite.setOnClickListener((View view) -> {
            Intent activityIntent = new Intent(this, FavoritesActivity.class);
            startActivity(activityIntent);
        });

        // Record to the external cache directory for visibility
        fileName = getFilesDir()+"/speak.pcm";
        micro = new MicrophoneListener(fileName);

        ImageView button = (ImageView) findViewById(R.id.recordBtn);
        button.setOnClickListener( (View view) -> {

            activeListeningThread = null;
            activeListeningThread = new Thread(new Runnable() {
                public void run() {
                    handleUserCommands(false);
                    return;
                }
            }, "Message Thread");
            activeListeningThread.start();

        });

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

        t2 = new TextToSpeech(getApplicationContext(), (status) -> {
            if(status != TextToSpeech.ERROR) {
                t2.setLanguage(Locale.GERMAN);
            }
        });

        t3 = new TextToSpeech(getApplicationContext(), (status) -> {
            if(status != TextToSpeech.ERROR) {
                t3.setLanguage(Locale.GERMAN);
            }
        });
        sp = new SoundPool(2, STREAM_MUSIC, 0);
        messageReceivedEarcon = sp.load(this, R.raw.earcon1, 1);
        answerModeActiveEarcon = sp.load(this, R.raw.earcon_answer_mode, 1);

        this.speechSpeed = (SeekBar)findViewById(R.id.speechSpeed);
        this.speechSpeed.setMax(10);
        this.speechSpeed.setProgress(0);
        this.speechSpeed.incrementProgressBy(1);
        this.speechSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speechSpeedValue = progress;
                //1.0 normal, 0.5 half the normal speed
                t1.setSpeechRate(progress);
                //Log.d("SPEECH speed", "value:"+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        this.contactSpeed = (SeekBar)findViewById(R.id.contactSpeed);
        this.contactSpeed.setMax(10);
        this.contactSpeed.setProgress(0);
        this.contactSpeed.incrementProgressBy(1);
        this.contactSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                t2.setSpeechRate(progress);
                //Log.d("CONTACT speed", "value:"+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        shared = getPreferences(Context.MODE_PRIVATE);
        editor = shared.edit();

        int calls_before = shared.getInt("calls", 0);
        int answers_before = shared.getInt("answers", 0);


        editor.putInt("calls", calls_before+1);
        editor.putInt("answers", answers_before+1);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        finish();
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
        //Log.d("OUTPUT", text);
        File file = new File("../../../../../res/raw/earcon1.mp3");
        int succ1 = t1.addEarcon("[earcon]", file.getAbsolutePath());//"", R.raw.earcon1);
        Bundle param = new android.os.Bundle();
        param.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, 3);
        param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "earcon");
        //param.putBundle(String.valueOf(TextToSpeech.Engine.KEY_PARAM_STREAM), String.valueOf(AudioManager.STREAM_MUSIC));
        int succ = t1.playEarcon("[earcon]",TextToSpeech.QUEUE_FLUSH, param, "earcon");
        System.out.println("Success: "+succ1+ " " + succ);
        //t1.speak(text,TextToSpeech.QUEUE_ADD,null);
        if(text.contains("Keine neuen Nachrichten")){
            t1.speak(text,TextToSpeech.QUEUE_ADD,null);
        } else { //split text in three pieces
            String[] output = new String[3]; //nachricht von, contact, message
            if(text.contains("Nachricht")) {
                //output[0] = text.split("(?<=von)(?s)(.*$)")[0];
                String m = text.split(" ")[0]; //Nachricht(en)
                String f = text.split(" ")[1]; //von
                output[0] = m+" "+f;
                //output[1] = text.split("von")[1].split(":")[0];
                String contact = text.split(":")[0];
                output[1] = contact.substring(contact.lastIndexOf(" ")+1);
                output[2] = text.split(":")[1];

                //Log.d("0", output[0]);
                //Log.d("1", output[1]);
                //Log.d("2", output[2]);
                t1.speak(output[0],TextToSpeech.QUEUE_ADD,null);
                t2.speak(output[1], TextToSpeech.QUEUE_ADD, null);
                t3.speak(output[2], TextToSpeech.QUEUE_ADD, null);
            } else {
                t1.speak(text,TextToSpeech.QUEUE_ADD,null);
            }

        }

        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId){
                sp.play(messageReceivedEarcon, 1,1,0,0,speechSpeedValue);

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
        while(t1.isSpeaking()) {
            //wait until message was played
        }

        if(isSingleMsgMode && !isSamePerson) {
            handleUserCommands(true);
        }
        if(isSamePerson) {
            broadcastReceiver.isAnswer = true;
            Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
            intent.putExtra("Answer", "");

            sendBroadcast(intent);
        }
        notificationActive = false;

    }

    private void setTextFromOtherThread(String s) {
        view.post(new Runnable() {
            public void run() {
                view.setText(s);
            }
        });
    }

    public int listenToKeyword() {
        micro.startRecording(3000);
        setTextFromOtherThread("Warte 3s auf Schlüsselwort ...");
        while(micro.isRecording) {
            //wait until a keyword was spoken

            //the answer keyword was spoken
            if(checkKeyword(micro.result,0)) {

                editor = shared.edit();
                int answers_before = shared.getInt("answers", 0);
                editor.putInt("answers", answers_before+1);
                editor.apply();
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                micro.stopRecording();
                return 0;
            } else if(checkKeyword(micro.result, 1)) { // eine Nachricht abhören
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                micro.stopRecording();
                return 1;
            } else if(checkKeyword(micro.result, 2)){ //"schreibe"
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                micro.stopRecording();
                return 2;
            } else if(checkKeyword(micro.result, 3)){ //alle Nachrichten abhören
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                micro.stopRecording();
                return 3;
            }
        }
        //no keyword
        return -1;
    }





    public void handleUserCommands(boolean isReactionToNotification) {

        int keyword = listenToKeyword();

        if(keyword == 0 && isReactionToNotification) {
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

            Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
            String point = micro.result.replaceAll("punkt", ".");
            String comma = point.replaceAll("komma", ",");
            String exclamationPoint = comma.replaceAll("ausrufezeichen", "!");
            String questionMark = exclamationPoint.replaceAll("fragezeichen", "?");
            micro.result = questionMark;
            if(isSamePerson) {
                bufferedAnswer +=" "+ micro.result;
                intent.putExtra("Answer", "");
            } else {
                if(!bufferedAnswer.equals("")) {
                    intent.putExtra("Answer", bufferedAnswer + " " +micro.result);
                    setTextFromOtherThread("Sende Antwort: "+bufferedAnswer + " " +micro.result);
                    bufferedAnswer = "";
                } else {
                    intent.putExtra("Answer", micro.result);
                    setTextFromOtherThread("Sende Antwort: "+micro.result);
                }
            }

            broadcastReceiver.isAnswer = true;
            sendBroadcast(intent);


        } else if (keyword == 1) {
            setTextFromOtherThread("Abhören-Schlüsselwort erkannt!\nSpreche nun den Namen ein...");
            micro.startRecording(3000);
            while (micro.isRecording) {

            }
            String message = NotificationListenerExampleService.getMessageFromPerson(micro.result);

            t1.speak(message, TextToSpeech.QUEUE_ADD, null);
        } else if(keyword == 2) {
            setTextFromOtherThread("Schreibe-Schlüsselwort erkannt!\nSpreche nun deine Nachricht ein...");
            micro.startRecording(5000);
            while(micro.isRecording){
                //wait until user has spoken his answer
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            String point = micro.result.replaceAll("punkt", ".");
            String comma = point.replaceAll("komma", ",");
            String exclamationPoint = comma.replaceAll("ausrufezeichen", "!");
            String questionMark = exclamationPoint.replaceAll("fragezeichen", "?");
            micro.result = questionMark;
            if(isSamePerson) {
                bufferedAnswer +=" "+ micro.result;
            } else {
                if(!bufferedAnswer.equals("")) {

                    setTextFromOtherThread("Sende Nachricht: "+bufferedAnswer + " " +micro.result);
                    bufferedAnswer = "";
                } else {

                    setTextFromOtherThread("Sende Nachricht: "+micro.result);
                }
            }

            sendMessage(micro.result);


        } else if(keyword == 3) {
            updateOurText( NotificationListenerExampleService.getMessageToRead(), false);
        }
        else {
            setTextFromOtherThread("Kein Schlüsselwort erkannt.");
            if(isReactionToNotification) {
                broadcastReceiver.isAnswer = true;
                Intent intent = new  Intent("com.github.chagall.notificationlistenerexample");
                intent.putExtra("Answer", "");
                sendBroadcast(intent);
            }

        }





    }

    public void updateOurText(String text, boolean isSingleMsgMode) {
        if(text != null) {
            messageThread = null;
            view.setText(text);
            messageThread = new Thread(new Runnable() {
                public void run() {
                    playMessage(text, isSingleMsgMode);
                    return;
                }
            }, "Message Thread");
            messageThread.start();
        }


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

    //send text message with given string
    public void sendMessage (String message){
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        waIntent.setPackage("org.telegram.messenger");
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, message);//
            startActivity(Intent.createChooser(waIntent, "Share with"));
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Telegram is not installed", Toast.LENGTH_SHORT).show();
        }

    }


}
