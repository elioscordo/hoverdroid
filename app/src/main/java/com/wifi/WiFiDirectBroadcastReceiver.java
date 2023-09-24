package com.wifi;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.core.TaskObserved;
import com.core.TaskObserver;
import com.wifi.listeners.WifiDirectListener;

import java.util.ArrayList;
import java.util.List;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver implements TaskObserved {
    TaskObserver observer;
    Channel channel;
    WifiP2pManager manager;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    public WifiDirectListener wifiDirectListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    public static final String EVENT_WIFI_P2P_STATE_ENABLED = "EVENT_WIFI_P2P_STATE_ENABLED";
    public static final String EVENT_WIFI_P2P_STATE_DISABLED = "EVENT_WIFI_P2P_STATE_DISABLED";
    public static final String EVENT_WIFI_P2P_THIS_DEVICE_CHANGED_ACTION = "EVENT_WIFI_P2P_THIS_DEVICE_CHANGED_ACTION";

    private static final String TAG = "WiFiDirectBroadcastReceiver";

    public WiFiDirectBroadcastReceiver(Channel channel,
                                       WifiP2pManager manager) {
        this.channel = channel;
        this.manager = manager;
        wifiDirectListener = new WifiDirectListener();
    }
    private void requestPeers(){
        if (ActivityCompat.checkSelfPermission( (Activity)this.observer, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestPeers(channel, wifiDirectListener);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wi-Fi Direct mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                this.requestPeers();
                observer.onResults(EVENT_WIFI_P2P_STATE_ENABLED, null);
            } else {
                observer.onResults(EVENT_WIFI_P2P_STATE_DISABLED, null);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            requestPeers();
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.e(TAG, "P2P CONNECTION CHANGED");NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                Log.e(TAG, "networkInfo is Connected");
                manager.requestConnectionInfo(channel, wifiDirectListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            observer.onResults(
                    EVENT_WIFI_P2P_THIS_DEVICE_CHANGED_ACTION,
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
            );
        }
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
        wifiDirectListener.setObserver(observer);
    }
}
