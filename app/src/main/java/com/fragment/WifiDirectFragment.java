package com.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.core.TaskObserved;
import com.core.TaskObserver;
import com.hoverboard.HoverboardCommand;
import com.hoverdroid.R;
import com.wifi.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectFragment extends Fragment implements TaskObserved {
    public static final boolean DEBUG = false;
    public static final String EVENT_WIFI_DIRECT_CONNECT = "EVENT_WIFI_DIRECT_CONNECT";
    View canvas;

    public ListView deviceListView;
    public TextView debugTextView;
    public ArrayList<String> debugMessages = new ArrayList<>();

    TaskObserver observer;
    private DeviceAdapter deviceAdapter;
    private List<WifiP2pDevice> deviceList = new ArrayList<>();
    private WifiP2pDevice selectedDevice;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        canvas = inflater.inflate(R.layout.fragment_wifi, container, false);
        deviceListView = canvas.findViewById(R.id.deviceList);
        deviceListView.setOnItemClickListener(new ListViewOnClickItem());
        deviceAdapter = new DeviceAdapter(getActivity(), this.deviceList);
        deviceListView.setAdapter(deviceAdapter);
        debugTextView = canvas.findViewById(R.id.debugArea);
        // debugTextView.setVisibility(View.INVISIBLE);
        debugTextView.setSingleLine(false);
        for (String debugMessage: debugMessages) {
            debugTextView.append(debugMessage);
        }
        return canvas;
    }

    public void setList(List<WifiP2pDevice> deviceList) {
        this.deviceList = deviceList;
        if (deviceAdapter != null) {
            deviceAdapter.refreshList(this.deviceList);
        }
    }

    public void addDebugMessage(String line) {
        String msg = String.format("%s \n", line);
        if (this.debugTextView != null){
            this.debugTextView.append(msg);
        }else {
            this.debugMessages.add(msg);
        }

    }

    private void dialogOptions(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Options");
        builder.setItems(new String[]{"Connect", "Send message"}
                , (dialog, which) -> {
                    if (which == 0) {
                        WifiP2pConfig config = new WifiP2pConfig();
                        selectedDevice = deviceList.get(position);
                        config.deviceAddress = selectedDevice.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;
                        config.groupOwnerIntent = 0;
                        observer.onResults(EVENT_WIFI_DIRECT_CONNECT,config);
                    }
                }
        );
        builder.create();
        builder.show();
    }
    /** ListView on click listener. */
    private class ListViewOnClickItem implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            dialogOptions(position);
        }
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }
}
