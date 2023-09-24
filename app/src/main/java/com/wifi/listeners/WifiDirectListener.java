package com.wifi.listeners;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;


import com.core.TaskObserved;
import com.core.TaskObserver;

public class WifiDirectListener implements TaskObserved, PeerListListener, WifiP2pManager.ActionListener,  WifiP2pManager.ConnectionInfoListener {
    private static final String TAG = "WifiDirectListener ";
    public static final String EVENT_WIFI_PEERS_AVAILABLE = "EVENT_WIFI_PEERS_AVAILABLE";
    public static final String EVENT_WIFI_CONNECTION_SUCCESS = "EVENT_WIFI_CONNECTION_SUCCESS";
    public static final String EVENT_WIFI_CONNECTION_FAILURE = "EVENT_WIFI_CONNECTION_FAILURE";
    public static final String EVENT_WIFI_CONNECTION_OWNER = "EVENT_WIFI_CONNECTION_OWNER";
    public static final String EVENT_WIFI_CONNECTION_CLIENT = "EVENT_WIFI_CONNECTION_CLIENT";

    TaskObserver observer;
    public WifiP2pDeviceList peerList;

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        observer.onResults(EVENT_WIFI_PEERS_AVAILABLE, peerList);
        this.peerList = peerList;
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onSuccess() {
        observer.onResults(EVENT_WIFI_CONNECTION_SUCCESS, null);
    }

    @Override
    public void onFailure(int reason) {
        observer.onResults(EVENT_WIFI_CONNECTION_FAILURE, reason);
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.e(TAG, "ConnectionInfoAvailable");

        if (info.groupFormed && info.isGroupOwner) {
            Log.e(TAG, String.format("GroupFormed, isGroupeOwner %s", info.toString()));
            observer.onResults(EVENT_WIFI_CONNECTION_OWNER, info);
            // set port and start server
        } else if (info.groupFormed) {
            Log.e(TAG, String.format("GroupFormed %s", info.toString()));
            observer.onResults(EVENT_WIFI_CONNECTION_CLIENT, info);
        }

    }
}
