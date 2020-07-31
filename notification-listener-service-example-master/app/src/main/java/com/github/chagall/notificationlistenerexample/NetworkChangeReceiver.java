package com.github.chagall.notificationlistenerexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public static MainActivity ma;

    @Override
    public void onReceive(Context context, final Intent intent) {
        /*System.out.println("*** Action: " + intent.getAction());
        if(intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE") || intent.getAction().equalsIgnoreCase("android.net.wifi.WIFI_STATE_CHANGED")) {
            //Toast.makeText(context, "Connection changed to: "+hasInternetConnection(), Toast.LENGTH_SHORT).show();
            ma.notifyInternetConnection();
        }*/
        /*int status = NetworkUtil.getConnectivityStatusString(context);
        System.out.println("*** Action: " + intent.getAction());
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) { //|| "android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Toast.makeText(context, "Not connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("NICHT");
        }*/
        System.out.println("*** Action: " + intent.getAction() + " hasInternetConnection: "+ hasInternetConnection());
        ma.notifyInternetConnection();
    }

    private boolean hasInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager)ma.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isConnected();
        return connected;
    }

}
