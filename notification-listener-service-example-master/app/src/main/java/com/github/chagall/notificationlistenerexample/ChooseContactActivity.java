package com.github.chagall.notificationlistenerexample;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import static android.media.AudioManager.STREAM_MUSIC;

public class ChooseContactActivity extends AppCompatActivity {

    private ImageButton back;
    private ListView listView;
    private ArrayList<String> contacts;
    public static int errorEarcon;
    public static int feedbackEarcon;
    public static SoundPool sp;
    private ChooseContactAdapter dataAdapter;
    private String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
        message = getIntent().getStringExtra("msg");
        sp = new SoundPool(10, STREAM_MUSIC, 0);
        errorEarcon = sp.load(this, R.raw.earcon4,1);
        feedbackEarcon = sp.load(this, R.raw.earcon6, 1);
        back = findViewById(R.id.back);
        back.setOnClickListener((View view) -> {

            Intent backIntent = new Intent(this, MainActivity.class);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(backIntent);
            sp.play(errorEarcon,0.3f,0.3f,0,0,1.5f);
        });

        listView=(ListView)findViewById(R.id.contactlist);
        contacts = new ArrayList<>();
        getContacts();

    }

    private void getContacts(){
        contacts = new ArrayList<>();
        HashMap <Long, String> contactList = TelegramListener.getContactList();
        for (Long id: contactList.keySet()) {

            contacts.add( contactList.get(id));
        }


        dataAdapter = new ChooseContactAdapter(ChooseContactActivity.this, contacts, message, contactList);
        listView.setAdapter(dataAdapter);


    }
}

