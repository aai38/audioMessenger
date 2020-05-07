package com.github.chagall.notificationlistenerexample;

import android.content.Context;
import android.content.SharedPreferences;
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

        private String fileName = "favorites2.json";
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

            //Code to get the JSON data
            /*String resultJSON = getData(context);
            if(resultJSON != null) {
                try {
                    JSONArray jsonArray = new JSONArray(resultJSON);
                    JSONObject jObj = jsonArray.getJSONObject(position);
                    favorite = jObj.getBoolean("favorite");
                    Log.e("favorite", favorite.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } */

            editor = sharedPrefs.edit();
            holder.checkBox.setChecked(sharedPrefs.getBoolean("CheckValue"+position, false));



            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editor.putBoolean("CheckValue"+position, isChecked);
                    editor.commit();
                    if(isChecked) {
                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("name", holder.name.getText());
                            jo.put("favorite", holder.checkBox.isChecked());
                            StringWriter out = new StringWriter();

                            saveData(context, jo.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    public String getData(Context context) {
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

