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

    /*@Override
    public void onReceive(Context context, final Intent intent) {
        System.out.println("*** Action: " + intent.getAction() + " hasInternetConnection: "+ hasInternetConnection());
        ma.notifyInternetConnection();
    }*/

    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*int status = NetworkUtil.getConnectivityStatusString(context);
        Log.i("NETWORK", "status: "+status);
        Log.i("NETWORK", "intent: "+intent.getAction());
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Log.i("NETWORK", "if case: not connected");
            } else {
                Log.i("NETWORK", "else case: connected");
            }
        }*/
        if(isOnline(context)){
            ma.notifyInternetConnection();
            Log.i("NETWORK", "online");
        } else {
            ma.notifyInternetConnection();
            Log.i("NETWORK", "not online");
        }
    }

    private boolean hasInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager)ma.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isConnected();
        return connected;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

}
