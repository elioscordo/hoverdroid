package com.wifi.listeners;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.core.TaskObserved;
import com.core.TaskObserver;
import com.wifi.utils.Constants;

// Old receiver from milan

public class WifiP2PReceiver extends BroadcastReceiver implements TaskObserved {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private TaskObserver observer;
    private static final String TAG = "WifiDirectListener ";


    public WifiP2PReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel
            , WifiP2pManager.PeerListListener peerListListener
            , WifiP2pManager.ConnectionInfoListener connectionInfoListener){
        this.manager = manager;
        this.channel = channel;
        this.peerListListener = peerListListener;
        this.connectionInfoListener = connectionInfoListener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
            Log.e(TAG, "STATE CHANGED");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            // is wifi enabled
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                observer.onResults(Constants.EVENT_WIFI_P2P_STATE_ENABLED, null);
            } else {
                observer.onResults(Constants.EVENT_WIFI_P2P_STATE_DISABLED, null);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Broadcast intent action indicating that the state of Wi-Fi p2p connectivity
            // has changed.
            Log.e(TAG, "P2P CONNECTION CHANGED");
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                observer.onResults(Constants.EVENT_WIFI_P2P_CONNECTION_CHANGED_ACTION, null);
                manager.requestConnectionInfo(channel, connectionInfoListener);
            }


        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Broadcast intent action indicating that the available peer list has changed.
            Log.e(TAG, "P2P PEERS CHANGED");
            observer.onResults(Constants.EVENT_WIFI_ACTION_PEER_LIST, null);
            if (manager != null) {
                manager.requestPeers(channel, peerListListener);
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Broadcast intent action indicating that this device details have changed.


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