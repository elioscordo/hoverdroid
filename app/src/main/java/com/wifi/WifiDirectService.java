package com.wifi;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.core.TaskObserved;
import com.core.TaskObserver;
import com.wifi.listeners.SocketListener;
import com.wifi.utils.MessageUtils;

import java.util.List;

public class WifiDirectService extends Service implements TaskObserved, WifiP2pManager.ChannelListener
        , WifiP2pManager.ActionListener, WifiP2pManager.PeerListListener
        , WifiP2pManager.ConnectionInfoListener, SocketListener {

    private static final String TAG = "WifiService";

    public static final String EVENT_WIFI_PEERS_AVAILABLE = "EVENT_WIFI_PEERS_AVAILABLE";
    public static final String EVENT_WIFI_P2P_STATE_ENABLED = "EVENT_WIFI_P2P_STATE_ENABLED";
    public static final String EVENT_WIFI_P2P_STATE_DISABLED = "EVENT_WIFI_P2P_STATE_ENABLED";
    public static final String EVENT_WIFI_CONNECTION_SUCCESS = "EVENT_WIFI_CONNECTION_SUCCESS";
    public static final String EVENT_WIFI_CONNECTION_FAILURE = "EVENT_WIFI_CONNECTION_FAILURE";
    public static final String EVENT_WIFI_P2P_CONNECTION_CHANGED_ACTION = "EVENT_WIFI_P2P_CONNECTION_CHANGED_ACTION";

    public static final String EVENT_WIFI_CONNECTION_OWNER = "EVENT_WIFI_CONNECTION_OWNER";
    public static final String EVENT_WIFI_CONNECTION_CLIENT = "EVENT_WIFI_CONNECTION_CLIENT";
    public static final String EVENT_WIFI_ACTION_PEER_LIST = "EVENT_WIFI_ACTION_PEER_LIST";
    public static final String EVENT_WIFI_DISCOVERY_STARTED = "EVENT_WIFI_DISCOVERY_STARTED";
    public static final String EVENT_WIFI_DISCOVERY_STOPPED = "EVENT_WIFI_DISCOVERY_STOPPED";

    public static final String EVENT_WIFI_MESSAGE_RECEIVED = "EVENT_WIFI_MESSAGE_RECEIVED";

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


    public List<WifiP2pDevice> deviceList;

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onSuccess() {
        observer.onResults(EVENT_WIFI_CONNECTION_SUCCESS, null);
    }

    @Override
    public void onFailure(int i) {
        observer.onResults(EVENT_WIFI_CONNECTION_FAILURE, null);
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
        manager.connect(channel, config, this);;
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
            observer.onResults(EVENT_WIFI_CONNECTION_OWNER, null);
            transfer.setPort(PORT).startServer();

        } else if (info.groupFormed) {
            Log.e(TAG, "GroupFormed");
            transfer.setPort(PORT).setAddress(info.groupOwnerAddress.getHostAddress())
                    .startClient();
            observer.onResults(EVENT_WIFI_CONNECTION_CLIENT, null);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        // clean devices list
        if (!deviceList.isEmpty()) {
            deviceList.clear();
        }
        deviceList.addAll(peers.getDeviceList());
        observer.onResults(EVENT_WIFI_PEERS_AVAILABLE, deviceList);
    }

    @Override
    public void onReceiver(String response) {
        String messageDecode = MessageUtils.parseMessage(response)[3];
        observer.onResults(EVENT_WIFI_MESSAGE_RECEIVED, messageDecode);
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

    /** Create binder. */
    public class ServiceBinder extends Binder {
        public WifiDirectService getService() {
            return WifiDirectService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intentFilter = new IntentFilter();
        initIntentFilter();

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);


        channel = manager.initialize(getApplicationContext(), Looper.getMainLooper(), null);

        // register receiver
        receiver = new WifiP2PReceiver(manager, channel, this, this);

        // socket
        transfer = new SocketTransfer(getApplicationContext());
        transfer.addListener(this);

    }


    private void initIntentFilter(){
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

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(receiver, intentFilter);

        if (manager != null) {
            manager.discoverPeers(channel, this);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private class WifiP2PReceiver extends BroadcastReceiver {
        private WifiP2pManager manager;
        private WifiP2pManager.Channel channel;
        private WifiP2pManager.PeerListListener peerListListener;
        private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

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
                    observer.onResults(EVENT_WIFI_P2P_STATE_ENABLED, null);
                } else {
                    observer.onResults(EVENT_WIFI_P2P_STATE_DISABLED, null);
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
                    observer.onResults(EVENT_WIFI_P2P_CONNECTION_CHANGED_ACTION, null);
                    manager.requestConnectionInfo(channel, connectionInfoListener);
                }


            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Broadcast intent action indicating that the available peer list has changed.
                Log.e(TAG, "P2P PEERS CHANGED");
                observer.onResults(EVENT_WIFI_ACTION_PEER_LIST, null);
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
                    observer.onResults(EVENT_WIFI_DISCOVERY_STARTED, null);
                } else {
                    observer.onResults(EVENT_WIFI_DISCOVERY_STOPPED, null);
                }

            }

        }
    }



}