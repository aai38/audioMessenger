package com.github.chagall.notificationlistenerexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter  extends ArrayAdapter<Contact> implements View.OnClickListener {

        private Context context;
        public static ArrayList<Contact> modelArrayList;


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
            if(contact.getNumber() != null) {
                holder.number.setText(contact.getNumber());
            }


            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!holder.checkBox.isChecked()) {
                        holder.checkBox.setSelected(true);
                    }
                    else {
                        holder.checkBox.setSelected(false);
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

    }

