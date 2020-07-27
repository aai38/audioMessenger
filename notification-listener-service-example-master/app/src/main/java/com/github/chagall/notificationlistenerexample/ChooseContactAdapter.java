package com.github.chagall.notificationlistenerexample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class ChooseContactAdapter extends ArrayAdapter<String> implements View.OnClickListener {

        private Context context;
        public static ArrayList<String> modelArrayList;
        String message = "";
        HashMap <Long, String> contactList;


        public ChooseContactAdapter(Context context, ArrayList<String> modelArrayList, String msg, HashMap <Long, String> contactList) {
            super(context, R.layout.activity_choose_contact, modelArrayList);
            message = msg;
            this.context = context;
            this.modelArrayList = modelArrayList;
            this.contactList = contactList;
        }

        @Override
        public void onClick(View v) {

            int position=(Integer) v.getTag();
            Object object= getItem(position);
            String contact = (String) object;
        }

        private int lastposition = -1;

        @Override
        public int getViewTypeCount() {
            return getCount();
        }
        @Override
        public int getItemViewType(int position) {

            return position;
        }

        @Override
        public int getCount() {
            return modelArrayList.size();
        }


        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final View result;


            String contact = getItem(position);
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item, null, true);

                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
                holder.name = (TextView) convertView.findViewById(R.id.name);
             //   holder.name = convertView.findViewById(R.id.number);

                result = convertView;
                convertView.setTag(holder);
            }else {
                // the getTag returns the viewHolder object set as a tag to the view
                holder = (ViewHolder)convertView.getTag();
                result = convertView;
            }
            lastposition = position;

            holder.name.setText(contact);



            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    for (long id : contactList.keySet()) {
                        if(contact.equals(contactList.get(id))) {
                            TelegramListener.sendMessage(message,id);
                            TelegramListener.updateFailCalls(id);
                            break;
                        }

                    }

                    Intent backIntent = new Intent(context, MainActivity.class);
                    backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(backIntent);

                }
            });

            return convertView;
        }

        private class ViewHolder {

            protected CheckBox checkBox;
            private TextView name;

        }



    }

