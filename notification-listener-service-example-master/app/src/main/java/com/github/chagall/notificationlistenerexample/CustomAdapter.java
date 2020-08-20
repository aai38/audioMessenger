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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CustomAdapter extends ArrayAdapter<Contact> implements View.OnClickListener {

        private Context context;
        public static ArrayList<Contact> modelArrayList;

        private static String fileName = "favorites2.json";
        private Boolean favorite = false;
        private SharedPreferences.Editor editor;
        private JSONArray jsonArray;
        private String resultJSON;
        public static ArrayList<String> favNames;
        public ImageButton confirm;
        public boolean locked = false;


        public CustomAdapter(Context context, ArrayList<Contact> modelArrayList, ImageButton confirm) {
            super(context, R.layout.activity_favorites, modelArrayList);
            this.confirm = confirm;
            this.context = context;
            this.modelArrayList = modelArrayList;
            favNames = new ArrayList<>();

        }

        @Override
        public void onClick(View v) {

            int position=(Integer) v.getTag();
            Object object= getItem(position);
            Contact contact = (Contact) object;
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
            SharedPreferences sharedPrefs = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

            Contact contact = (Contact) getItem(position);
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

            holder.name.setText(contact.getName());


            editor = sharedPrefs.edit();
            Set<String> checked = sharedPrefs.getStringSet("checked",new HashSet<>());
            for (String s: checked) {
                if(s.equals(holder.name.getText().toString())){
                    holder.checkBox.setChecked(true);
                }
            }
            //holder.checkBox.setChecked(sharedPrefs.getBoolean("CheckValue"+position, false));
            locked = sharedPrefs.getBoolean("locked",false);



            confirm.setOnClickListener((View view) -> {
                if(locked) {
                    Toast.makeText(context, "Deine Favoriten wurden schon gespeichert!", Toast.LENGTH_SHORT).show();
                } else {
                    if(sharedPrefs.getInt("count_favorite", 0) >= 3) {

                        new AlertDialog.Builder(context)
                                .setTitle("Bestätige Favoriten")
                                .setMessage("Hast du alle deine Favoriten ausgewählt? Du kannst sie danach nicht mehr ändern!")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        jsonArray = new JSONArray();
                                        for (String name: favNames) {
                                            JSONObject jo = new JSONObject();
                                            try {
                                                jo.put("name", name);
                                                jsonArray.put(jo);
                                                saveData(context, jsonArray.toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        locked = true;
                                        editor.putBoolean("locked", true);

                                        editor.commit();
                                    }
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();



                    } else {
                        Toast.makeText(context, "Du musst insgesamt 3 Favoriten wählen.", Toast.LENGTH_SHORT).show();
                    }


                }

            });

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int count_favorite = sharedPrefs.getInt("count_favorite", 0);
                    if(isChecked && count_favorite <3) {
                        if(locked) {
                            Toast.makeText(context, "Deine Favoriten wurden schon gespeichert!", Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        } else {
                            new AlertDialog.Builder(context)
                                    .setTitle("Füge Favorit hinzu")
                                    .setMessage("Willst du diesen Kontakt zu deinen Favoriten hinzufügen?")

                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            //editor.putBoolean("CheckValue" + position, isChecked);
                                            Set<String> checked = sharedPrefs.getStringSet("checked",new HashSet<>());

                                            checked.add(holder.name.getText().toString());
                                            editor.putStringSet("checked", checked);
                                            editor.putInt("count_favorite", count_favorite + 1);
                                            editor.commit();
                                            favNames.add(holder.name.getText().toString());

                                        }
                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            buttonView.setChecked(false);
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    } else if (isChecked && count_favorite >= 3){
                        if(locked) {
                            Toast.makeText(context, "Deine Favoriten wurden schon gespeichert!", Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        } else {
                            Toast.makeText(context, "Du kannst nur 3 Favoriten wählen.", Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(false);
                        }

                    } else {
                        if(locked) {
                            Toast.makeText(context, "Deine Favoriten wurden schon gespeichert!", Toast.LENGTH_SHORT).show();
                            buttonView.setChecked(true);
                        } else {
                            //editor.putBoolean("CheckValue"+position, isChecked);
                            Set<String> checked = sharedPrefs.getStringSet("checked",new HashSet<>());
                            checked.remove(holder.name.getText().toString());
                            editor.putStringSet("checked", checked);
                            editor.putInt("count_favorite", count_favorite-1);
                            editor.commit();
                            favNames.remove(holder.name.getText().toString());
                        }

                    }


                }
            });

            return convertView;
        }

        private class ViewHolder {

            protected CheckBox checkBox;
            private TextView name;

        }



    public void saveData(Context context, String mJsonResponse) {
        try {
            FileWriter file = new FileWriter(context.getFilesDir().getPath() + "/" + fileName);
            file.write(mJsonResponse);
            file.flush();
            file.close();
        } catch (IOException e) {
            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
        }
    }

    public static String getData(Context context) {
        try {
            File f = new File(context.getFilesDir().getPath() + "/" + fileName);
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }

    }

