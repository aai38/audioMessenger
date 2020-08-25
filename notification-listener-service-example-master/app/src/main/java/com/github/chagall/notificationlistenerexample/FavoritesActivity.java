package com.github.chagall.notificationlistenerexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoritesActivity extends AppCompatActivity {

    private ImageButton back;
    private ImageButton confirm;
    private ListView listView;
    private ArrayList<Contact> contacts;
    private CustomAdapter dataAdapter;
    private int dialogOpen;
    public SharedPreferences shared;
    public SharedPreferences.Editor editor;
    public static SharedPreferences sharedPreferences;
    private boolean firstTimeFav = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        confirm = findViewById(R.id.confirm);
        back = findViewById(R.id.backButtonNumber);
        back.setOnClickListener((View view) -> {
            Intent backIntent = new Intent(this, MainActivity.class);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(backIntent);
        });

        listView=(ListView)findViewById(R.id.list);
        contacts = new ArrayList<>();
        getContacts();

        //tutorial-dialog
        shared = getPreferences(Context.MODE_PRIVATE);
        sharedPreferences = shared;
        firstTimeFav = sharedPreferences.getBoolean("firstTimeFav", true);
        editor = shared.edit();


        //show the dialog only at the first time
        if(firstTimeFav) {
            FavoritesDialog favoritesDialog = new FavoritesDialog();
            favoritesDialog.show(getSupportFragmentManager(), "TAG");
            firstTimeFav = false;
            editor.putBoolean("firstTimeFav", firstTimeFav);
            editor.apply();
        }

    }

    private void getContacts(){
        contacts = new ArrayList<>();

        HashMap <Long, String> contactList = TelegramListener.getContactList();
        for (Long id: contactList.keySet()) {
            Contact contactsInfo = new Contact();
            contactsInfo.setName(contactList.get(id));
            contacts.add(contactsInfo);
        }

        Parcelable state = listView.onSaveInstanceState();
        dataAdapter = new CustomAdapter(FavoritesActivity.this, contacts,confirm);
        listView.onRestoreInstanceState(state);
        listView.setAdapter(dataAdapter);

    }
}

