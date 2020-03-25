package com.github.chagall.notificationlistenerexample;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.BasicAuthenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicrophoneListener {

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    public IamAuthenticator authenticator;

    public SpeechToText speechToText;


    public File audio;

    private static String fileName = null;

    public MicrophoneListener(String fileName){
        this.fileName = fileName;
        authenticator = new IamAuthenticator("N2UJ-ncPfcdKPi71q8ESL1yapZWy5Qh6FkbEZmsQTnr3");

        speechToText = new SpeechToText(authenticator);
        speechToText.setServiceUrl("https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/a0d543a7-e42e-45d9-b28f-ffaa0922d3c7");



        audio = new File(fileName);
    }

    public void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte


        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            System.out.println("Short writing to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording(TextView view) {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
            recordingThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        parseSpeechToText(view);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }, "SpeechToText Thread");
            recordingThread.start();
            while(recordingThread.isAlive()) {

            }
            recordingThread = null;
        }
    }

    public String result = "";
    public void parseSpeechToText(TextView view) throws FileNotFoundException {

        RecognizeOptions options = new RecognizeOptions.Builder()
                .audio(audio)
                .contentType(HttpMediaType.createAudioRaw(16000))

                .build();


        SpeechRecognitionResults transcript = speechToText
                .recognize(options)
                .execute()
                .getResult();
        result = transcript.toString();
        // uses capturing group for characters other than "," "]" and whitespace...
        Pattern pattern = Pattern.compile("\"transcript\": \"(.*)\"");
        Matcher matcher = pattern.matcher(result);
        while (matcher.find()) {
            result = matcher.group(1);
        }

    }

}
