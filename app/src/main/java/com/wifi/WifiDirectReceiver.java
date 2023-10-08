package com.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.wifi.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class WifiDirectReceiver extends android.content.BroadcastReceiver implements TaskObserved {
    TaskObserver observer;
    Channel channel;
    WifiP2pManager manager;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private WifiP2pManager.ActionListener actionListener;
    private WifiP2pManager.PeerListListener peerListListener;
    public static final String EVENT_WIFI_P2P_STATE_ENABLED = "EVENT_WIFI_P2P_STATE_ENABLED";
    public static final String EVENT_WIFI_P2P_STATE_DISABLED = "EVENT_WIFI_P2P_STATE_DISABLED";
    public static final String EVENT_WIFI_P2P_THIS_DEVICE_CHANGED_ACTION = "EVENT_WIFI_P2P_THIS_DEVICE_CHANGED_ACTION";

    private static final String TAG = "WiFiDirectBroadcastReceiver";

    public WifiDirectReceiver(WifiP2pManager manager,
                              Channel channel,
                              WifiP2pManager.PeerListListener peerListListener,
                              WifiP2pManager.ConnectionInfoListener connectionInfoListener) {
        this.channel = channel;
        this.manager = manager;
        this.peerListListener = peerListListener;
        this.connectionInfoListener = connectionInfoListener;

    }

    @SuppressLint("MissingPermission")
    private void requestPeers() {
        manager.requestPeers(channel, peerListListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wi-Fi Direct mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                observer.onResults(EVENT_WIFI_P2P_STATE_ENABLED, null);
            } else {
                observer.onResults(EVENT_WIFI_P2P_STATE_DISABLED, null);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            requestPeers();
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.e(TAG, "P2P CONNECTION CHANGED");
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                Log.e(TAG, "networkInfo is Connected");
                manager.requestConnectionInfo(channel, connectionInfoListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            observer.onResults(
                    EVENT_WIFI_P2P_THIS_DEVICE_CHANGED_ACTION,
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
            );
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            // Broadcast intent action indicating that peer discovery has either started
            // or stopped.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                // started
                observer.onResults(Constants.EVENT_WIFI_DISCOVERY_STARTED, null);
            } else {
                observer.onResults(Constants.EVENT_WIFI_DISCOVERY_STOPPED, null);
            }

        }
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }
}
