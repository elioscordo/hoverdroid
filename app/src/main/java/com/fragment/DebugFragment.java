package com.fragment;

import android.app.AlertDialog;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.core.TaskObserved;
import com.core.TaskObserver;
import com.hoverdroid.R;
import com.wifi.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class DebugFragment extends Fragment implements TaskObserved {
    public static final boolean DEBUG = false;

    public View canvas;

    public TextView debugTextView;
    public ArrayList<String> debugMessages = new ArrayList<>();

    TaskObserver observer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        canvas = inflater.inflate(R.layout.fragment_wifi, container, false);
        debugTextView = canvas.findViewById(R.id.debugArea);
        debugTextView.setSingleLine(false);
        debugTextView.setOnClickListener(debugOnClickListener);
        syncDebugView();
        return canvas;
    }

    private View.OnClickListener debugOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Options");
            builder.setItems(new String[]{"Advertise"}
                    , (dialog, which) -> {
                        if (which == 0) {

                        }
                    }
            );
            builder.create();
            builder.show();
        }
    };

    public void log(String line) {
        this.debugMessages.add(
                String.format("%s \n", line)
        );
        syncDebugView();
    }

    public void syncDebugView(){
        if (debugTextView != null){
            debugTextView.setText("");
            for (String debugMessage: debugMessages) {
                debugTextView.append(debugMessage);
            }
        }
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }
}
