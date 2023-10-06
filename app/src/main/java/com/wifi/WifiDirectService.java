package com.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.content.BroadcastReceiver;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.core.TaskObserved;
import com.core.TaskObserver;
import com.wifi.listeners.SocketListener;
import com.wifi.listeners.WifiP2PReceiver;
import com.wifi.utils.Constants;
import com.wifi.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectService extends Service implements TaskObserved, WifiP2pManager.ChannelListener
        , WifiP2pManager.PeerListListener
        , WifiP2pManager.ConnectionInfoListener, SocketListener,  WifiP2pManager.ActionListener {

    private static final String TAG = "WifiService";


    /** Server port. */
    private static final int PORT = 8888;
    /** Binder. */
    private IBinder binder = new ServiceBinder();
    private IntentFilter intentFilter;

    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;
    private WifiP2PReceiver receiver;
    private SocketTransfer transfer;
    private TaskObserver observer;


    public List<WifiP2pDevice> deviceList = new ArrayList<WifiP2pDevice>();

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intentFilter = new IntentFilter();
        initIntentFilter();

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);


        channel = manager.initialize(getApplicationContext(), Looper.getMainLooper(), null);

        // register receiver
        receiver = new WifiP2PReceiver(manager,channel,this,this);

        receiver.setObserver(observer);
        // socket
        transfer = new SocketTransfer(getApplicationContext());
        transfer.addListener(this);

    }

    @Override
    public void onChannelDisconnected() {

    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;
        manager.connect(channel, config, this);
    }

    /** Remove conncetion. */
    public void removeConnection() {
        manager.removeGroup(channel, this);
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.e(TAG, "ConnectionInfoAvailable");

        if (info.groupFormed && info.isGroupOwner) {
            Log.e(TAG, "GroupFormed, isGroupeOwner");
            observer.onResults(Constants.EVENT_WIFI_CONNECTION_OWNER, null);
            transfer.setPort(PORT).startServer();

        } else if (info.groupFormed) {
            Log.e(TAG, "GroupFormed");
            transfer.setPort(PORT).setAddress(info.groupOwnerAddress.getHostAddress())
                    .startClient();
            observer.onResults(Constants.EVENT_WIFI_CONNECTION_CLIENT, null);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // clean devices list
        if (!deviceList.isEmpty()) {
            deviceList.clear();
        }
        deviceList.addAll(peers.getDeviceList());
        observer.onResults(Constants.EVENT_WIFI_PEERS_AVAILABLE, deviceList);
    }

    @Override
    public void onReceiver(String response) {
        String messageDecode = MessageUtils.parseMessage(response)[3];
        observer.onResults(Constants.EVENT_WIFI_MESSAGE_RECEIVED, messageDecode);
    }

    /** Start message receiver. */
    public void startMessageReceiver() {
        transfer.startMessageReceiver();
    }

    /** Stop message receiver. */
    public void stopMessageReceiver() {
        transfer.stopMessageReceiver();
    }

    public void sendMessage(String message) {
        transfer.sendMessage(message);
    }

    @Override
    public void onSuccess() {
       observer.onResults(Constants.EVENT_WIFI_CONNECTION_SUCCESS, null);
    }

    @Override
    public void onFailure(int reason) {
        observer.onResults(Constants.EVENT_WIFI_CONNECTION_FAILURE, reason);
    }

    /** Create binder. */
    public class ServiceBinder extends Binder {
        public WifiDirectService getService() {
            return WifiDirectService.this;
        }
    }



    private void initIntentFilter() {
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Indicating that peer discovery has either started or stopped.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    public void initWifiDirect() {
        receiver.setObserver(this.observer);
        registerReceiver(receiver, intentFilter);
        manager.discoverPeers(channel, this);

    }
    @SuppressLint("MissingPermission")
    public void discoverPeers() {
        manager.requestPeers(channel, this);
    }





}