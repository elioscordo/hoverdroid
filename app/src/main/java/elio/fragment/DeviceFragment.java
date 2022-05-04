package elio.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import elio.core.TaskObserved;
import elio.core.TaskObserver;
import elio.hoverdroid.R;


public class DeviceFragment  extends Fragment implements TaskObserved, View.OnClickListener {
    private TextView receiveText;
    private TaskObserver observer = null;
    public final static String EVENT_DEVICE_REFRESH = "EVENT_DEVICE_REFRESH";
    public final static String EVENT_DEVICE_REMOVE = "EVENT_DEVICE_REMOVE";
    private Button closeButton;
    private Button refreshButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        this.refreshButton = (Button)view.findViewById(R.id.device_refresh);
        this.refreshButton.setOnClickListener(this);
        this.closeButton = (Button)view.findViewById(R.id.device_close);
        this.closeButton.setOnClickListener(this);
        return view;
    }


    public void append(String line) {
        receiveText.append(line);
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.device_refresh) {
            observer.onResults(EVENT_DEVICE_REFRESH, null);
        }
        if (id == R.id.device_close) {
            observer.onResults(EVENT_DEVICE_REMOVE, null);
        }
    }
}
