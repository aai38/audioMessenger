package com.github.chagall.notificationlistenerexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.media.AudioManager.*;
import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity {

    public static MainActivity reference;
    private String[] keywords = {"antworten", "abhören", "schreibe", "alle", "abbruch"};
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private ImageView interceptedNotificationImageView;
    private static TextView view;
    private AlertDialog enableNotificationListenerAlertDialog;
    private Button play;
    private TextView serverConnectionTextView;

    public static TextToSpeech t1;
    public static TextToSpeech t2; //person
    public static TextToSpeech t3; //message
    public static MicrophoneListener micro;
    public boolean hasRecorded;
    public String fileName;
    public static SoundPool sp;
    private SoundPool spFavoriteOne;
    private SoundPool spFavoriteTwo;
    private SoundPool spFavoriteThree;

    //public static int messageReceivedEarcon;
    public static int answerModeActiveEarcon;
    public static int noNewMessageEarcon;
    public static int singleMessageEarcon;
    public static int multipleMessageEarcon;
    public static int errorEarcon;
    public static int feedbackEarcon;

    private boolean isNoMessage = false;
    private boolean isSingleMessage = false;
    private boolean isMultipleMessage = false;

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

    public SharedPreferences shared;
    public SharedPreferences.Editor editor;
    public static SharedPreferences sharedPreferences;
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
    private int number_cancel;
    private int number_write;
    private int number_hearall;
    private int number_hearone;
    private int number_error;
    private static int number_falseContact;
    private boolean firstTimeSlide = true;
    private boolean firstTimeWrite = true;
    private boolean firstTimeInfo = true;
    private boolean firstTimeAll = true;
    private boolean firstTimeHear = true;
    private ArrayList<String> favorites = new ArrayList();
    private int index;

    private Button tutorialBtn;

    private AudioManager am;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

    public static boolean waitForDialog = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        myReminder();

        activateButtons();

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


        // Record to the external cache directory for visibility
        fileName = getFilesDir()+"/speak.pcm";
        micro = new MicrophoneListener(fileName);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestAudioFocus();
        //initializeAudioFocus();
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
        sp = new SoundPool(10, STREAM_MUSIC, 0);
        //messageReceivedEarcon = sp.load(this, R.raw.earcon1, 1);
        answerModeActiveEarcon = sp.load(this, R.raw.earcon_answer_mode, 1);
        noNewMessageEarcon = sp.load(this, R.raw.earcon1, 1);
        singleMessageEarcon = sp.load(this, R.raw.earcon2, 1);
        multipleMessageEarcon = sp.load(this, R.raw.earcon3, 1);
        errorEarcon = sp.load(this, R.raw.earcon4,1);
        feedbackEarcon = sp.load(this, R.raw.earcon6, 1);


        shared = getPreferences(Context.MODE_PRIVATE);
        sharedPreferences = shared;

        calls_before = shared.getInt("calls", 0);
        answers_before = shared.getInt("answers", 0);
        speech_rate_answers = shared.getInt("rate_answers", 1);
        speech_rate_calls = shared.getInt("rate_calls", 1);
        speech_rate_calls = shared.getInt("rate_calls", 1);
        firstTimeSlide = sharedPreferences.getBoolean("firstTimeSlide", true);
        firstTimeWrite = sharedPreferences.getBoolean("firstTimeWrite", true);
        firstTimeInfo = sharedPreferences.getBoolean("firstTimeInfo", true);
        firstTimeAll = sharedPreferences.getBoolean("firstTimeAll", true);
        firstTimeHear = sharedPreferences.getBoolean("firstTimeHear", true);
        editor = shared.edit();


        t1.setSpeechRate(speech_rate_calls);
        editor.putInt("rate_calls", (calls_before/20)+1);


        t2.setSpeechRate(speech_rate_answers);
        editor.putInt("rate_answers", answers_before/20+1);
        editor.apply();


        //Code to get the JSON data

        String resultJSON = CustomAdapter.getData(getApplicationContext());

        if(resultJSON != null) {
            try {
                JSONArray jsonArray = new JSONArray(resultJSON);
                for (int i = 0; i <jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    String name = jObj.getString("name");
                    favorites.add(name);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //handle headset input
        HeadsetService.mA = this;
        startService(new Intent(this, HeadsetService.class));


        //Telegram

        TelegramListener.mainActivity = this;
        TelegramListener.initialize();
        Gson gson = new Gson();
        String json = shared.getString("failCalls", "");
        if(json != "") {
            Type type = new TypeToken<List<FailContactCalls>>(){}.getType();
            TelegramListener.failCalls  = gson.fromJson(json, type);
        }




    }

    public void activateButtons() {
        //tutorial
        tutorialBtn =(Button) findViewById(R.id.tutBtn);
        tutorialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDescription();
            }
        });

        //favorites
        favorite = findViewById(R.id.imageButton);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFavorites();
            }
        });

        //record
        ImageView button = (ImageView) findViewById(R.id.recordBtn);
        button.setOnClickListener( (View view) -> {
            if(!isBusy) {
                showToastFeedback(5);
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
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

        isActiveModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //tutorial-dialog

                //show the dialog only at the first time
                if(firstTimeSlide) {
                    ActiveDialog activeDialog = new ActiveDialog();
                    activeDialog.show(getSupportFragmentManager(), "TAG");
                    firstTimeSlide = false;
                    editor.putBoolean("firstTimeSlide", firstTimeSlide);
                    editor.apply();
                }
                if(isChecked){
                    showToastFeedback(7);
                } else {
                    showToastFeedback(8);
                }
            }
        });

        //mail
        mailButton = findViewById(R.id.mailButton);
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tutorial-dialog
                InformationDialogue informationDialogue = new InformationDialogue();
                informationDialogue.show(getSupportFragmentManager(), "TAG");
                //show the dialog only at the first time
                if(firstTimeInfo) {
                    InformationDialog informationDialog = new InformationDialog();
                    informationDialog.show(getSupportFragmentManager(), "TAG");
                    firstTimeInfo = false;
                    editor.putBoolean("firstTimeInfo", firstTimeInfo);
                    editor.apply();

                }

            }
        });

        //server connection
        serverConnectionTextView = (TextView)findViewById(R.id.serverConnectionTextView);
    }

    public void openDescription() {
        Intent intent = new Intent(this, TutorialComplete.class);
        startActivity(intent);
    }

    public void openFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }


    public void openChooseContact(String msg){
        Intent intent = new Intent(this, ChooseContactActivity.class);
        intent.putExtra("msg", msg);
        startActivity(intent);
    }

    public static void updateSwitchStatus() {
        isActiveMode = isActiveModeSwitch.isChecked();
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        calls_before = shared.getInt("calls", 0);
        answers_before = shared.getInt("answers", 0);
        number_cancel = shared.getInt("number_cancel", 0);
        number_hearall = shared.getInt("number_hearall", 0);
        number_write = shared.getInt("number_write", 0);
        number_hearone = shared.getInt("number_hearone", 0);
        number_error = shared.getInt("number_error", 0);
        number_falseContact = shared.getInt("number_falseContact", 0);


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
    private boolean isPermissionToWriteAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                isPermissionToWriteAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;

        }
        if (!permissionToRecordAccepted || !isPermissionToWriteAccepted) finish();

    }

    public void playMessage(String text, boolean answerAllowed, long chatID) {

        isNoMessage = false;
        isSingleMessage = false;
        isMultipleMessage = false;

        if(text.startsWith("Keine neuen Nachrichten")){
            isNoMessage = true;
            File file = new File("../../../../../res/raw/earcon1.mp3");
            int succ1 = t1.addEarcon("[earcon]", file.getAbsolutePath());//"", R.raw.earcon1);
            Bundle param = new android.os.Bundle();
            param.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, 3);
            param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "earcon");
            //param.putBundle(String.valueOf(TextToSpeech.Engine.KEY_PARAM_STREAM), String.valueOf(AudioManager.STREAM_MUSIC));
            int succ = t1.playEarcon("[earcon]",TextToSpeech.QUEUE_FLUSH, param, "earcon");
            System.out.println("Success: "+succ1+ " " + succ);

            t1.speak(text,TextToSpeech.QUEUE_ADD,null);
        } else { //split text in three pieces
            output = new String[3];
            if(text.startsWith("Nachricht von")) { //single message
                isSingleMessage = true;
                File file = new File("../../../../../res/raw/earcon2.mp3");
                int succ1 = t1.addEarcon("[earcon]", file.getAbsolutePath());
                Bundle param = new android.os.Bundle();
                param.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, 3);
                param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "earcon");
                int succ = t1.playEarcon("[earcon]",TextToSpeech.QUEUE_FLUSH, param, "earcon");
                System.out.println("Success: "+succ1+ " " + succ);

                output[0] = "Nachricht von ";
                //get "<person>" or "<person> in <group>"
                String withoutBeginning = text.replace("Nachricht von ","");
                String person = withoutBeginning.split("§")[0];
                output[1] = person;
                //get <msg>
                output[2] = withoutBeginning.split("§")[1];

                //its possible that output[1] contains "in <group>"
                String p;
                if(output[1].contains("in")){
                    p = output[1].split(" ")[0];
                } else {
                    p = output[1];
                }
                if(favorites.contains(p)) {
                    int index = 0;
                    for (int i = 0; i < favorites.size(); i++) {
                        if (favorites.get(i).equals(p)) {
                            index = i + 2;
                        }
                    }

                    if (index == 2) {
                        spFavoriteOne = new SoundPool(2, STREAM_MUSIC, 0);
                        favoriteOneEarcon = spFavoriteOne.load(getApplicationContext(), R.raw.earcon_fav1, 1);
                    } else if (index == 3) {
                        spFavoriteTwo = new SoundPool(2, STREAM_MUSIC, 0);
                        favoriteTwoEarcon = spFavoriteTwo.load(getApplicationContext(), R.raw.earcon_fav2, 1);
                    } else if (index == 4) {
                        spFavoriteThree = new SoundPool(2, STREAM_MUSIC, 0);
                        favoriteThreeEarcon = spFavoriteThree.load(getApplicationContext(), R.raw.earcon_fav3, 1);
                    }
                }

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

                HashMap<String, String> map2 = new HashMap<String, String>();
                map2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID1");

                HashMap<String, String> map3 = new HashMap<String, String>();
                map3.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID2");


                if(speech_rate_calls >= 4) {
                    sp.play(singleMessageEarcon, 1,1,0,0,speechSpeedValue);
                } else {
                    t1.speak(output[0],TextToSpeech.QUEUE_ADD, map2);
                }

                t2.speak(output[1], TextToSpeech.QUEUE_ADD, map);
                t3.speak(output[2], TextToSpeech.QUEUE_ADD, map3);
            } else if(text.startsWith("Nachrichten")){ //multiple message
                isMultipleMessage = true;
                File file = new File("../../../../../res/raw/earcon3.mp3");
                int succ1 = t1.addEarcon("[earcon]", file.getAbsolutePath());//"", R.raw.earcon1);
                Bundle param = new android.os.Bundle();
                param.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, 3);
                param.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "earcon");
                //param.putBundle(String.valueOf(TextToSpeech.Engine.KEY_PARAM_STREAM), String.valueOf(AudioManager.STREAM_MUSIC));
                int succ = t1.playEarcon("[earcon]",TextToSpeech.QUEUE_FLUSH, param, "earcon");
                System.out.println("Success: "+succ1+ " " + succ);

                if(text.startsWith("Nachrichten von")){
                    output[0] = "Nachrichten von ";
                    String withoutBeginning = text.replace("Nachrichten von ","");
                    String personList = withoutBeginning.replace(",", " ");
                    output[1] = personList; //<person1> <person2> ...
                    output[2] = ""; //no msg

                    //create person Array and search for favorites
                    String[] personArray = personList.split(" ");
                    for(String person : personArray){
                        if(favorites.contains(person)) {
                            int index = 0;
                            for (int i = 0; i < favorites.size(); i++) {
                                if (favorites.get(i).equals(person)) {
                                    index = i + 2;
                                }
                            }

                            if (index == 2) {
                                spFavoriteOne = new SoundPool(2, STREAM_MUSIC, 0);
                                favoriteOneEarcon = spFavoriteOne.load(getApplicationContext(), R.raw.earcon_fav1, 1);
                            } else if (index == 3) {
                                spFavoriteTwo = new SoundPool(2, STREAM_MUSIC, 0);
                                favoriteTwoEarcon = spFavoriteTwo.load(getApplicationContext(), R.raw.earcon_fav2, 1);
                            } else if (index == 4) {
                                spFavoriteThree = new SoundPool(2, STREAM_MUSIC, 0);
                                favoriteThreeEarcon = spFavoriteThree.load(getApplicationContext(), R.raw.earcon_fav3, 1);
                            }
                        }
                    }

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

                    HashMap<String, String> map2 = new HashMap<String, String>();
                    map2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID1");

                    HashMap<String, String> map3 = new HashMap<String, String>();
                    map3.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID2");

                    if(speech_rate_calls >= 4) {
                        sp.play(multipleMessageEarcon, 1,1,0,0,speechSpeedValue);
                    } else {
                        t1.speak(output[0],TextToSpeech.QUEUE_ADD, map2);
                    }

                    t2.speak(output[1], TextToSpeech.QUEUE_ADD, map);
                    t3.speak(output[2], TextToSpeech.QUEUE_ADD, map3);
                } else { //text.startsWith("Nachrichten in")
                    output[0] = "Nachrichten in ";
                    String withoutBeginning = text.replace("Nachrichten in ","");

                    output[0] += withoutBeginning.split("§")[0]; //"Nachrichten in <group>"

                    //get <person1> sagt: <msg1>. Und <person2> sagt: <msg2>. ...
                    String loop = text.replace(output[0]+"§", "");

                    //create hashmap to store <person> and <msg>
                    HashMap<String,String> personMessage = new HashMap<>();
                    String[] pm = loop.split("."); //get each person and msg

                    for (String pM : pm){
                        if(pM.startsWith("Und")){ //multiple one
                            pM.replace("Und", "");
                        }
                        String person = pM.split("sagt§")[0];
                        String message = pM.split("sagt§")[1];
                        personMessage.put(person, message);
                    }

                    boolean firstPlay = true;
                    for(String key: personMessage.keySet()){

                        output[1] = key; //person
                        output[2] = personMessage.get(key); //message


                        if(favorites.contains(output[1])) {
                            int index = 0;
                            for (int i = 0; i < favorites.size(); i++) {
                                if (favorites.get(i).equals(output[1])) {
                                    index = i + 2;
                                }
                            }

                            if (index == 2) {
                                spFavoriteOne = new SoundPool(2, STREAM_MUSIC, 0);
                                favoriteOneEarcon = spFavoriteOne.load(getApplicationContext(), R.raw.earcon_fav1, 1);
                            } else if (index == 3) {
                                spFavoriteTwo = new SoundPool(2, STREAM_MUSIC, 0);
                                favoriteTwoEarcon = spFavoriteTwo.load(getApplicationContext(), R.raw.earcon_fav2, 1);
                            } else if (index == 4) {
                                spFavoriteThree = new SoundPool(2, STREAM_MUSIC, 0);
                                favoriteThreeEarcon = spFavoriteThree.load(getApplicationContext(), R.raw.earcon_fav3, 1);
                            }
                        }


                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

                        HashMap<String, String> map2 = new HashMap<String, String>();
                        map2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID1");

                        HashMap<String, String> map3 = new HashMap<String, String>();
                        map3.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID2");

                        if(firstPlay){
                            firstPlay = false;
                            if(speech_rate_calls >= 4) {
                                sp.play(multipleMessageEarcon, 1,1,0,0,speechSpeedValue);
                            } else {
                                t1.speak(output[0],TextToSpeech.QUEUE_ADD, map2);
                            }

                            t2.speak(output[1]+ " sagt ", TextToSpeech.QUEUE_ADD, map);
                            t3.speak(output[2], TextToSpeech.QUEUE_ADD, map3);
                        } else {
                            t2.speak(" Und "+output[1]+"sagt ", TextToSpeech.QUEUE_ADD, map);
                            t3.speak(output[2], TextToSpeech.QUEUE_ADD, map3);
                        }

                        personMessage.remove(key);
                    }
                }
            }
            else {
                t1.speak(text,TextToSpeech.QUEUE_ADD,null);
            }

        }

        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId){
                if(isNoMessage){
                    sp.play(noNewMessageEarcon, 1,1,0,0,speechSpeedValue);
                } else if(isSingleMessage){
                    sp.play(singleMessageEarcon, 1,1,0,0,speechSpeedValue);
                } else if(isMultipleMessage){
                    sp.play(multipleMessageEarcon, 1,1,0,0,speechSpeedValue);
                }
                //sp.play(messageReceivedEarcon, 1,1,0,0,speechSpeedValue);

            }
            @Override
            public void onDone(String utteranceId) {
                if(favorites.contains(output[1])) {
                    if (index == 2) {
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



        while(t1.isSpeaking() || t2.isSpeaking() || t3.isSpeaking()) {
            //wait until message was played
        }

        if(answerAllowed) {
            sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
            handleUserCommands(true, chatID);
        }


    }


    public int listenToKeyword() {

        micro.startRecording(1800);

        while(micro.isRecording) {
            //wait until a keyword was spoken

            //the answer keyword was spoken
            if(checkKeyword(micro.result,0)) {
                showToastFeedback(1);
                answers_before++;
                editor.putInt("answers", answers_before);
                editor.apply();
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                micro.stopRecording();
                return 0;
            } else if(checkKeyword(micro.result, 1)) { // eine Nachricht abhören
                showToastFeedback(9);
                number_hearone++;
                editor.putInt("number_hearone", number_hearone);
                editor.apply();
                micro.stopRecording();
                if(firstTimeHear) {
                    PlayingMessagesDialog msgDialog = new PlayingMessagesDialog();
                    msgDialog.show(getSupportFragmentManager(), "TAG");
                    while (waitForDialog){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Do nothing and wait
                    }

                    firstTimeHear = false;
                    editor.putBoolean("firstTimeHear", firstTimeHear);
                    editor.apply();
                }
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                waitForDialog = true;
                return 1;
            } else if(checkKeyword(micro.result, 2)){ //"schreibe"
                showToastFeedback(2);
                number_write++;
                editor.putInt("number_write", number_write);
                editor.apply();
                micro.stopRecording();
                if(firstTimeWrite) {
                    WriteDialog writeDialog = new WriteDialog();
                    writeDialog.show(getSupportFragmentManager(), "TAG");
                    firstTimeWrite = false;
                    editor.putBoolean("firstTimeWrite", firstTimeWrite);
                    editor.apply();
                    while (waitForDialog){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Do nothing and wait
                    }


                }
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);

                waitForDialog = true;
                return 2;
            } else if(checkKeyword(micro.result, 3)){ //alle Nachrichten abhören
                showToastFeedback(0);
                number_hearall++;
                editor.putInt("number_hearall", number_hearall);
                editor.apply();
                micro.stopRecording();
                if(firstTimeAll) {
                    PlayingAllMessagesDialog allDialog = new PlayingAllMessagesDialog();
                    allDialog.show(getSupportFragmentManager(), "TAG");
                    firstTimeAll = false;
                    editor.putBoolean("firstTimeAll", firstTimeAll);
                    editor.apply();
                    while (waitForDialog){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Do nothing and wait
                    }


                }
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);

                waitForDialog = true;
                return 3;
            } else if(checkKeyword(micro.result, 4)) { //"abbruch"
                showToastFeedback(10);
                number_cancel++;
                editor.putInt("number_cancel", number_cancel);
                editor.apply();
                sp.play(errorEarcon, 0.3f, 0.3f, 0, 0, 1.5f);
                micro.stopRecording();
                return 4;
            }
        }

        //no keyword
        showToastFeedback(11);
        sp.play(errorEarcon, 0.3f, 0.3f, 0, 0, 1.5f);
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

            String msg = inputMessage();
            if(!msg.equals("")) {
                sendMessage(msg, chatID);
            }
            isBusy = false;
            TelegramListener.playNextMessage(false);

        } else if (keyword == 1) {
            //

            micro.startRecording(20000);
            while (micro.isRecording) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!(containsCancel(micro.result))){
                TelegramListener.playStoredMessagesFromContact(micro.result);
            } else {
                sp.play(errorEarcon,0.3f,0.3f,0,0,1.5f);
            }

        } else if(keyword == 2) {

            String msg = inputMessage();
            if(!msg.equals("")) {
                sendMessage(msg,0);
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



    private static double getMaxOfDoubles(ArrayList<Double> doubles){
        double max = 0;
        for (double d: doubles) {
            if(d > max) {
                max = d;
            }
        }
        return max;
    }




    public static boolean confirmationCheck() {


        sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
        micro.startRecording(2400);
        while(micro.isRecording){
            //wait until user has spoken his answer
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /*ArrayList<Double> yesValues = new ArrayList<>();
            yesValues.add(TelegramListener.similarity("ja",micro.result));
            yesValues.add(TelegramListener.similarity("ähm ja",micro.result));
            yesValues.add(TelegramListener.similarity("ja ähm",micro.result));
            yesValues.add(TelegramListener.similarity("jahr",micro.result));
            yesValues.add(TelegramListener.similarity("jap",micro.result));
            yesValues.add(TelegramListener.similarity("joa",micro.result));
            yesValues.add(TelegramListener.similarity("ihr",micro.result));
            yesValues.add(TelegramListener.similarity("wir",micro.result));
            yesValues.add(TelegramListener.similarity("richtig",micro.result));
            yesValues.add(TelegramListener.similarity("passt",micro.result));
            yesValues.add(TelegramListener.similarity("yes",micro.result));
            double yes = getMaxOfDoubles(yesValues);*/
            ArrayList<Double> noValues = new ArrayList<>();
            noValues.add(TelegramListener.similarity("nein",micro.result));
            noValues.add(TelegramListener.similarity("ähm nein",micro.result));
            noValues.add(TelegramListener.similarity("no",micro.result));
            noValues.add(TelegramListener.similarity("nope",micro.result));
            noValues.add(TelegramListener.similarity("ne",micro.result));
            noValues.add(TelegramListener.similarity("falsch",micro.result));
            noValues.add(TelegramListener.similarity("abbruch", micro.result));
            double no = getMaxOfDoubles(noValues);
            if(no >= 0.6) {
                micro.stopRecording();
                sp.play(errorEarcon, 0.3f, 0.3f, 0, 0, 1.5f);
                return false;
            }

        }
        if(micro.result.contains("abbruch") || micro.result.contains("nein") || micro.result.contains("stop") || micro.result.contains("falsch")){
            micro.stopRecording();
            sp.play(errorEarcon, 0.3f,0.3f,0,0,1.5f);
            return false;
        } else {
            micro.stopRecording();
            sp.play(feedbackEarcon, 0.3f,0.3f,0,0,1.5f);
            return true;
        }



    }

    public boolean checkKeyword(String phrase, int keywordIndex) {

        for (String s : phrase.split(" ")) {
            double d = TelegramListener.similarity(s,keywords[keywordIndex]);
            if(d > 0.7) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyMessage(String message) {
        if(micro.result.equals("")) {
            if(speech_rate_calls < 4) {
                t1.speak("Du hast keinen Text eingesprochen, versuche es noch einmal.",TextToSpeech.QUEUE_ADD,null);
            }
            sp.play(errorEarcon,0.3f,0.3f,0,0,1.5f);
            number_error++;
            editor.putInt("number_error", number_error);
            editor.apply();
            while(t1.isSpeaking()) {
                //wait until message was played
            }
            return false;
        } else {
            sp.play(feedbackEarcon, 0.3f,0.3f,0,0,1.5f);
            if(speech_rate_calls < 4) {
                t1.speak("Die Nachricht lautet", TextToSpeech.QUEUE_ADD, null);
            }
            t2.speak(message,TextToSpeech.QUEUE_ADD,null);
            while(t1.isSpeaking()) {
                //wait until message was played
            }
            if(confirmationCheck()) {
                showToastFeedback(13);
                return true;
            } else {
                showToastFeedback(14);
                if(speech_rate_calls < 4) {
                    t1.speak("Spreche die Nachricht nochmal ein.", TextToSpeech.QUEUE_ADD, null);
                }
                sp.play(errorEarcon,0.3f,0.3f,0,0,1.5f);
                number_falseContact++;
                editor.putInt("number_falseContact", number_falseContact);
                editor.apply();
                while(t1.isSpeaking()) {
                    //wait until message was played
                }
                sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
                return false;
            }
        }
    }

    public String inputMessage() {
        micro.startRecording(20000);
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

        if(containsCancel(micro.result)){
            sp.play(errorEarcon,0.3f,0.3f,0,0,1.5f);
            return "";
        }
        String msg = micro.result;
        if(!verifyMessage(msg)){
            sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
            return inputMessage();
        } else {
            return msg;
        }



    }

    private int countFails = 0;

    public long verifyContact(String contact, FailContactCalls failCalls) {

        if(contact.equals("")) {
            if(speech_rate_calls < 4) {
                t1.speak("Du hast keinen Kontakt eingesprochen, versuche es noch einmal.", TextToSpeech.QUEUE_ADD, null);
            }
            number_error++;
            editor.putInt("number_error", number_error);
            editor.apply();
            sp.play(errorEarcon, 0.3f,0.3f,0,0,1.5f);
            while(t1.isSpeaking()) {
                //wait until message was played
            }
            return 0;
        }
        long id = TelegramListener.checkContacts(contact);
        if(id == 0){
            showToastFeedback(12);
            sp.play(MainActivity.errorEarcon, 0.3f,0.3f,0,0,1.5f);
            if(failCalls.fail1.equals("")) {
                failCalls.fail1 = contact;
            } else if(failCalls.fail2.equals("")) {
                failCalls.fail2 = contact;
            } else if(failCalls.fail3.equals("")) {
                failCalls.fail3 = contact;
                if(speech_rate_calls < 4) {
                    t1.speak("Wähle den Kontakt bitte per Hand aus.", TextToSpeech.QUEUE_ADD, null);
                }
            }
            countFails++;
            if(countFails == 3) {
                countFails = 0;
                return -1;
            }
            if(speech_rate_calls < 4) {
                t1.speak("Deine Eingabe wurde nicht verstanden oder der Kontakt existiert nicht, versuche es noch einmal.", TextToSpeech.QUEUE_ADD, null);
            }
            number_falseContact++;
            editor.putInt("number_falseContact", number_falseContact);
            editor.apply();
            while(t1.isSpeaking()) {
                //wait until message was played
            }
            return 0;
        } else {
            sp.play(feedbackEarcon, 0.3f,0.3f,0,0,1.5f);
            if(speech_rate_calls < 4) {
                t1.speak("Die Nachricht wird geschickt an ", TextToSpeech.QUEUE_ADD, null);
            }
            t2.speak(TelegramListener.getContactById(id),TextToSpeech.QUEUE_ADD,null);


            while(t1.isSpeaking() || t2.isSpeaking() || t3.isSpeaking()) {
                //wait until message was played
            }
            if(confirmationCheck()) {
                showToastFeedback(13);
                countFails = 0;
                return id;
            } else {
                showToastFeedback(14);
                sp.play(MainActivity.errorEarcon, 0.3f,0.3f,0,0,1.5f);
                if(failCalls.fail1.equals("")) {
                    failCalls.fail1 = contact;
                } else if(failCalls.fail2.equals("")) {
                    failCalls.fail2 = contact;
                } else if(failCalls.fail3.equals("")) {
                    failCalls.fail3 = contact;
                    if(speech_rate_calls < 4) {
                        t1.speak("Wähle den Kontakt bitte per Hand aus.", TextToSpeech.QUEUE_ADD, null);
                    }
                }
                countFails++;
                if(countFails == 3) {
                    countFails = 0;
                    return -1;
                }
                if(speech_rate_calls < 4) {
                    t1.speak("Spreche den Kontakt nochmal ein.", TextToSpeech.QUEUE_ADD, null);
                }
                number_falseContact++;
                editor.putInt("number_falseContact", number_falseContact);
                editor.apply();
                while(t1.isSpeaking()) {
                    //wait until message was played
                }
                return 0;
            }
        }

    }

    public long chooseContact(FailContactCalls failCalls) {

        sp.play(answerModeActiveEarcon, 0.3f,0.3f,0,0,1.5f);
        micro.startRecording(6000);
        while(micro.isRecording){
            //wait until user has spoken his answer
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        micro.stopRecording();
        if(containsCancel(micro.result)){
            sp.play(errorEarcon,0.3f,0.3f,0,0,1.5f);
            return 0;
        }
        long id = verifyContact(micro.result, failCalls);
        if(id == 0) {
            id = chooseContact(failCalls);
        }

        return id;

    }

    //send text message with given string
    public void sendMessage (String message, long id){
        FailContactCalls failCalls = new FailContactCalls();
        if(id == 0) {
            if(speech_rate_calls < 4) {
                t1.speak("Spreche den Kontakt ein.", TextToSpeech.QUEUE_ADD, null);
            }
            while(t1.isSpeaking()) {
                //wait until message was played
            }

            id = chooseContact(failCalls);
            if(id == 0) {
                return;
            }
        }
        if(id == -1) {
            openChooseContact(message);
            TelegramListener.failCalls.add(failCalls);
        } else {
            TelegramListener.sendMessage(message,id);
            showToastFeedback(6);
        }

    }


    private boolean containsCancel(String text){
        //is "abbruch" in given string?
        if(text.contains("abbruch")){
            showToastFeedback(10);
            number_cancel++;
            editor.putInt("number_cancel", number_cancel);
            editor.apply();
            return true;
        } else {
            return false;
        }
    }



    public void showToastFeedback(int action){

        int duration = Toast.LENGTH_LONG;
        String text;
        switch (action) {
            case 0: //alle - PLAY
                text = "Alle Nachrichten...";
                break;
            case 1: //antworten - NEXT
                text = "Antworten...";
                break;
            case 2: //schreibe - PREVIOUS
                text = "Schreiben...";
                break;
            case 3:
                text = "Antworten nicht möglich. Keine letzte Nachricht vorhanden.";
                break;
            case 5: //record button clicked
                text = "Mikrofon hört zu...";
                break;
            case 6:
                text = "Nachricht gesendet.";
                break;
            case 7:
                text = "Aktiv Modus an.";
                break;
            case 8:
                text = "Aktiv Modus aus.";
                break;
            case 9:
                text = "Eine Nachricht abhören...";
                break;
            case 10:
                text = "Abbruch.";
                break;
            case 11:
                text = "Kein Schlüsselwort erkannt.";
                break;
            case 12:
                text = "Kontakt wurde nicht verstanden oder existiert nicht.";
                break;
            case 13:
                text = "Eingabe wurde bestätigt.";
                break;
            case 14:
                text = "Eingabe wurde abgelehnt.";
                break;
            default:
                text = "Momentan keine Aktion möglich.";
                break;
        }
        runOnUiThread(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show());
        //Toast.makeText(context, text, duration).show();
    }

    /*private void initializeAudioFocus(){
        String TAG = "AUDIOFOCUS";
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
                        //requestAudioFocus();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        //pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        //pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                        break;
                    default:
                        //
                }
            }
        };
    }*/

    public boolean requestAudioFocus(){
        String TAG = "AUDIOFOCUS";
        int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "Request granted");

            return true;
        } else {
            // FAILED
            Log.e(TAG,">>>>>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
            return false;
        }
    }

    public void showServerConnectionStatus(String status){
        //Log.i("main", "wuhu: "+status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverConnectionTextView.setText(status);
                serverConnectionTextView.invalidate();
            }
        });
    }

    public void myReminder() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 00);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        }

    }
}
