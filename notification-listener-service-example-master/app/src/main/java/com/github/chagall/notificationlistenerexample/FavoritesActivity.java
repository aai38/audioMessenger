package com.github.chagall.notificationlistenerexample;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoritesActivity extends AppCompatActivity {

    private ImageButton back;
    private ListView listView;
    private ArrayList<Contact> contacts;
    private CustomAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        back = findViewById(R.id.backButton);
        back.setOnClickListener((View view) -> {
            Intent backIntent = new Intent(this, MainActivity.class);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(backIntent);
        });

        listView=(ListView)findViewById(R.id.list);
        contacts = new ArrayList<>();
        getContacts();

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
        dataAdapter = new CustomAdapter(FavoritesActivity.this, contacts);
        listView.onRestoreInstanceState(state);
        listView.setAdapter(dataAdapter);

    }
}

