package com.github.chagall.notificationlistenerexample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
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

public class CustomAdapter  extends ArrayAdapter<Contact> implements View.OnClickListener {

        private Context context;
        public static ArrayList<Contact> modelArrayList;

        private static String fileName = "favorites2.json";
        private Boolean favorite = false;
        private SharedPreferences.Editor editor;


        public CustomAdapter(Context context, ArrayList<Contact> modelArrayList) {
            super(context, R.layout.activity_favorites, modelArrayList);

            this.context = context;
            this.modelArrayList = modelArrayList;

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
            holder.checkBox.setChecked(sharedPrefs.getBoolean("CheckValue"+position, false));
            JSONArray jsonArray = new JSONArray();


            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int count_favorite = sharedPrefs.getInt("count_favorite", 0);
                    if(isChecked && count_favorite <3) {
                        new AlertDialog.Builder(context)
                                .setTitle("Add Favorite")
                                .setMessage("You want to add this contact to your favorites?")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        editor.putBoolean("CheckValue"+position, isChecked);
                                        editor.putInt("count_favorite", count_favorite+1);
                                        editor.commit();
                                        JSONObject jo = new JSONObject();
                                        try {
                                            jo.put("name", holder.name.getText());
                                            jsonArray.put(jo);
                                            StringWriter out = new StringWriter();

                                            saveData(context, jsonArray.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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

                    } else if (isChecked && count_favorite >= 3){
                        Toast.makeText(context, "You have already chosen your favorites", Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(false);
                    }
                    /*View tempview = (View) holder.checkBox.getTag(R.integer.btnplusview);
                    TextView tv = (TextView) tempview.findViewById(R.id.animal);
                    Integer pos = (Integer)  holder.checkBox.getTag();
                    Toast.makeText(context, "Checkbox "+pos+" clicked!", Toast.LENGTH_SHORT).show();

                    if(modelArrayList.get(pos).getSelected()){
                        modelArrayList.get(pos).setSelected(false);
                    }else {
                        modelArrayList.get(pos).setSelected(true);
                    }*/

                }
            });

            return convertView;
        }

        private class ViewHolder {

            protected CheckBox checkBox;
            private TextView name;
            private TextView number;

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

