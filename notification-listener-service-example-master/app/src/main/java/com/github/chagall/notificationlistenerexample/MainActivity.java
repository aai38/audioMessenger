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
import android.net.Uri;
import android.os.FileObserver;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.BadRequestException;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResult;

//import org.drinkless.td.libcore.telegram.Client;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import HelperClasses.NotificationBroadcastReceiver;

import static android.media.AudioManager.*;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity {

    private String[] keywords = {"antworten", "abhören", "schreibe", "alle", "abbruch"};
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
    private SoundPool spFavoriteOne;
    private SoundPool spFavoriteTwo;
    private SoundPool spFavoriteThree;

    public static int messageReceivedEarcon;
    public static int answerModeActiveEarcon;
    private int favoriteOneEarcon;
    private int favoriteTwoEarcon;
    private int favoriteThreeEarcon;
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
    public static boolean isBusy = false;

    public static boolean isActiveMode = true;
    public static Switch isActiveModeSwitch;
    private File testAudio;

    private String[] output;

    private ImageButton mailButton;
    private int answers_before;
    private int calls_before;
    private int speech_rate_answers;
    private int speech_rate_calls;

    private ArrayList<String> favorites = new ArrayList();
    private int index;

    private Button tutorialBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //load tutorial
        tutorialBtn =(Button) findViewById(R.id.tutBtn);
        tutorialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDescription();
            }
        });

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //initialization test
        Thread iniThread = new Thread(new Runnable() {
            public void run() {
                try {
                    initializationTest();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return;
            }
        }, "Initialization Thread");
        iniThread.start();

        //handle headset input
        startService(new Intent(this, HeadsetService.class));

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
            if(!isBusy) {
                activeListeningThread = null;
                activeListeningThread = new Thread(new Runnable() {
                    public void run() {
                        isBusy = true;
                        handleUserCommands(false,0);
                        isBusy = false;
                        TelegramListener.playNextMessage(true);
                        return;
                    }
                }, "Message Thread");
                activeListeningThread.start();
            }


        });

        // initiate a Switch
        isActiveModeSwitch = (Switch) findViewById(R.id.switch1);

        // check current state of a Switch (true or false).
        isActiveMode = isActiveModeSwitch.isChecked();


        //view = (TextView) this.findViewById(R.id.image_change_explanation);
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


        /*
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

         */
        shared = getPreferences(Context.MODE_PRIVATE);

        calls_before = shared.getInt("calls", 0);
        answers_before = shared.getInt("answers", 0);
        speech_rate_answers = shared.getInt("rate_answers", 1);
        speech_rate_calls = shared.getInt("rate_calls", 1);

        editor = shared.edit();


        t1.setSpeechRate(speech_rate_calls);
        editor.putInt("rate_calls", (calls_before/20)+1);




        t2.setSpeechRate(speech_rate_answers);
        editor.putInt("rate_answers", answers_before/20+1);
        editor.apply();




        mailButton = findViewById(R.id.mailButton);
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InformationDialogue informationDialogue = new InformationDialogue();
                informationDialogue.show(getSupportFragmentManager(), "TAG");
            }
        });

        //Code to get the JSON data

        String resultJSON = CustomAdapter.getData(getApplicationContext());

        if(resultJSON != null) {
            try {
                JSONArray jsonArray = new JSONArray(resultJSON);
                for (int i = 0; i <jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    String name = jObj.getString("name");
                    favorites.add(name);
                    Log.e("name", name);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.e("favorites", favorites.toString());



        //Telegram

        TelegramListener.mainActivity = this;
        TelegramListener.initialize();
    }

    public void openDescription() {
        Intent intent = new Intent(this, MyIntro.class);
        startActivity(intent);
    }

    public static void updateSwitchStatus() {
        isActiveMode = isActiveModeSwitch.isChecked();
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
        //unregisterReceiver(broadcastReceiver);

    }

    @Override
    protected void onStart() {
        super.onStart();
        editor = shared.edit();

        calls_before = shared.getInt("calls", 0);
        answers_before = shared.getInt("answers", 0);


        editor.putInt("calls", calls_before+1);
        editor.apply();

    }

    private File resourceToFile(int res) {
        File tempFile = null;
        try{
            InputStream inputStream = getResources().openRawResource(res);
            tempFile = File.createTempFile("pre", "suf");
            copyFile(inputStream, new FileOutputStream(tempFile));

        } catch (IOException e) {
            throw new RuntimeException("Can't create temp file ", e);
        }

       return tempFile;
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void initializationTest() throws FileNotFoundException {
        long time = System.currentTimeMillis();
        testAudio = resourceToFile(R.raw.testvoice);
        SpeechToText speechToText;
        IamAuthenticator authenticator;
        authenticator = new IamAuthenticator("N2UJ-ncPfcdKPi71q8ESL1yapZWy5Qh6FkbEZmsQTnr3");
        speechToText = new SpeechToText(authenticator);
        speechToText.setServiceUrl("https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/a0d543a7-e42e-45d9-b28f-ffaa0922d3c7");
        RecognizeOptions options;
        List<SpeechRecognitionResult> transcript;
        String res = "";
        Pattern pattern = Pattern.compile("\"transcript\": \"(.*)\"");
        Matcher matcher;
        options = new RecognizeOptions.Builder()
                .audio(testAudio)
                //.contentType("audio/l16;rate=16000;endianness=little-endian")
                .contentType(HttpMediaType.AUDIO_WAV)
                .model("de-DE_BroadbandModel")
                .build();
        String result = "";

        while(result.equals("")) {
            if(System.currentTimeMillis() - time > 5000){
                System.out.println("Initialization of speech to text api failed");
                break;
            }
            try{
                transcript = speechToText
                        .recognize(options)
                        .execute()
                        .getResult().getResults();

                for (SpeechRecognitionResult s : transcript) {
                    res = s.toString();
                    matcher = pattern.matcher(res);

                    while (matcher.find()) {
                        result += matcher.group(1);
                    }

                }

            }catch (BadRequestException e) {
                System.out.println("bad request");
            } catch (RuntimeException e) {
                System.out.println("internal error");
            }

        }




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

    public void playMessage(String text, boolean answerAllowed, long chatID) {
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
            output = new String[3]; //nachricht von, contact, message
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
                Log.e("favorites", favorites.toString());
                if(favorites.contains(output[1])) {
                    Log.e("favorite in", output[1] + favorites.get(0));
                    int index = 0;
                    for (int i = 0; i < favorites.size(); i++) {
                        if (favorites.get(i).equals(output[1])) {
                            index = i + 2;
                        }
                    }

                    if (index == 2) {
                        Log.e("index before start", ""+index);
                        spFavoriteOne = new SoundPool(2, STREAM_MUSIC, 0);
                        favoriteOneEarcon = spFavoriteOne.load(getApplicationContext(), R.raw.earcon2, 1);
                    } else if (index == 3) {
                        spFavoriteTwo = new SoundPool(2, STREAM_MUSIC, 0);
                        favoriteTwoEarcon = spFavoriteTwo.load(getApplicationContext(), R.raw.earcon3, 1);
                    } else if (index == 4) {
                        spFavoriteThree = new SoundPool(2, STREAM_MUSIC, 0);
                        favoriteThreeEarcon = spFavoriteThree.load(getApplicationContext(), R.raw.earcon4, 1);
                    }
                }



                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

                HashMap<String, String> map2 = new HashMap<String, String>();
                map2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID1");

                HashMap<String, String> map3 = new HashMap<String, String>();
                map3.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID2");


                if(speech_rate_calls >= 4) {
                    sp.play(messageReceivedEarcon, 1,1,0,0,speechSpeedValue);
                } else {
                    t1.speak(output[0],TextToSpeech.QUEUE_ADD, map2);
                }

                t2.speak(output[1], TextToSpeech.QUEUE_ADD, map);
                t3.speak(output[2], TextToSpeech.QUEUE_ADD, map3);
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
                if(favorites.contains(output[1])) {
                    Log.i("favorite in output", "");
                    if (index == 2) {
                        Log.i("index_instart", "" + 2);
                        spFavoriteOne.play(favoriteOneEarcon, 1, 1, 0 , 0 , speechSpeedValue);
                    } else if (index == 3) {
                        spFavoriteTwo.play(favoriteTwoEarcon, 1, 1, 0 , 0 , speechSpeedValue);
                    } else if (index == 4) {
                        spFavoriteThree.play(favoriteThreeEarcon, 1, 1, 0 , 0 , speechSpeedValue);
                    }
                }
            }
            @Override
            public void onError(String utteranceId) {
                // There was an error.
            }
        });

        t2.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {


            }

            @Override
            public void onDone(String s) {

            }

            @Override
            public void onError(String s) {

            }
        });



        while(t1.isSpeaking()) {
            //wait until message was played
        }

        if(answerAllowed) {
            handleUserCommands(true, chatID);
        }


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
        //
        //setTextFromOtherThread("Warte 3s auf Schlüsselwort ...");
        while(micro.isRecording) {
            //wait until a keyword was spoken

            //the answer keyword was spoken
            if(checkKeyword(micro.result,0)) {

                editor = shared.edit();
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
            } else if(checkKeyword(micro.result, 4)) { //"abbruch"
                sp.play(answerModeActiveEarcon, 0.3f, 0.3f, 0, 0, 1.5f);
                micro.stopRecording();
                return 4;
            }
        }
        //no keyword
        return -1;
    }





    public void handleUserCommands(boolean isReactionToNotification, long chatID) {
        int keyword = listenToKeyword();
        if(keyword != 4){ //everything else except "abbruch"
            reactToKeyword(keyword, isReactionToNotification, chatID);
        }
    }

    public void reactToKeyword(int keyword, boolean isReactionToNotification, long chatID){
        if(keyword == 0 && isReactionToNotification) {

            micro.startRecording(5000);
            while(micro.isRecording){
                //wait until user has spoken his answer
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(micro.result.equals("")) {
                updateOutput("Du hast keinen Text eingesprochen, Funktion wird abgebrochen.",false,0);
                return;
            }
            String point = micro.result.replaceAll("punkt", ".");
            String comma = point.replaceAll("komma", ",");
            String exclamationPoint = comma.replaceAll("ausrufezeichen", "!");
            String questionMark = exclamationPoint.replaceAll("fragezeichen", "?");
            micro.result = questionMark;
            if(!(containsCancel(micro.result))){
                TelegramListener.sendMessage(micro.result,"",chatID);
            }
            isBusy = false;
            TelegramListener.playNextMessage(false);

        } else if (keyword == 1) {
            //

            micro.startRecording(3000);
            while (micro.isRecording) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!(containsCancel(micro.result))){
                TelegramListener.playStoredMessagesFromContact(micro.result);
            }

        } else if(keyword == 2) {

            micro.startRecording(5000);
            while(micro.isRecording){
                //wait until user has spoken his answer
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(micro.result.equals("")) {
                updateOutput("Du hast keinen Text eingesprochen, Funktion wird abgebrochen.",false,0);
                return;
            }
            String point = micro.result.replaceAll("punkt", ".");
            String comma = point.replaceAll("komma", ",");
            String exclamationPoint = comma.replaceAll("ausrufezeichen", "!");
            String questionMark = exclamationPoint.replaceAll("fragezeichen", "?");
            micro.result = questionMark;

            if(!(containsCancel(micro.result))){
                sendMessage(micro.result);
            }

        } else if(keyword == 3) {
            TelegramListener.playAllStoredMessages();
        }
        else {
            if(isReactionToNotification) {
                isBusy = false;
                TelegramListener.playNextMessage(false);
            }
        }
    }

    public void updateOutput(String text, boolean answerAllowed, long chatID) {

        if(text != null) {
            messageThread = null;
            //setTextFromOtherThread(text);
            messageThread = new Thread(new Runnable() {
                public void run() {
                    isBusy = true;
                    playMessage(text, answerAllowed, chatID);
                    isBusy = false;
                    return;
                }
            }, "Message Thread");
            messageThread.start();
        }


    }

    public static boolean confirmationCheck() {


        micro.startRecording(3000);
        while(micro.isRecording){
            //wait until user has spoken his answer
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(micro.result.equals("ja")) {
                micro.stopRecording();
                return true;
            } else if(micro.result.equals("nein")){
                micro.stopRecording();
                return false;
            }
        }
        micro.stopRecording();
        return false;


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
        sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
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
        if(micro.result.equals("")) {
            updateOutput("Du hast keinen Kontakt eingesprochen, Funktion wird abgebrochen.",false,0);
            return;
        }

        TelegramListener.sendMessage(message,micro.result,0);

        /*Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        waIntent.setPackage("org.telegram.messenger");
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, message);//
            startActivity(Intent.createChooser(waIntent, "Share with"));
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Telegram is not installed", Toast.LENGTH_SHORT).show();
        }*/
        /*Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        //String url = "https://t.me/Naiggoo";
        //sendIntent.setData(Uri.parse(url));
        //sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setPackage("org.telegram.messenger");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, "https://t.me/Naiggoo");
        //sendIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        sendIntent.putExtra(Intent.EXTRA_STREAM, message);
        startActivity(sendIntent);*/
        /*try {
            Intent telegram = new Intent(Intent.ACTION_SEND);
            telegram.setData(Uri.parse("https://t.me/Naiggoo"));
                    telegram.setPackage("org.telegram.messenger");
                    telegram.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(telegram);
        } catch (Exception e) {
            Toast.makeText(this, "Telegram app is not installed", Toast.LENGTH_LONG).show();
        }*/

    }


    private boolean containsCancel(String text){
        //is "abbruch" in given string?
        if(text.contains("abbruch")){
            return true;
        } else {
            return false;
        }
    }
}
