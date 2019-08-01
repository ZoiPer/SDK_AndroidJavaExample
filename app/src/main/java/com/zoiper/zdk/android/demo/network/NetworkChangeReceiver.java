package com.zoiper.zdk.android.demo.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

/**
 * NetworkChangeReceiver
 *
 * @since 1.2.2019 г.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private final com.zoiper.zdk.Context zdkContext;

    public NetworkChangeReceiver(com.zoiper.zdk.Context zdkContext) {
        this.zdkContext = zdkContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        zdkContext.networkChanged();
    }

    public static IntentFilter intentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        return intentFilter;
    }
}
