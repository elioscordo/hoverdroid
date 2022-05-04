package elio.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import elio.core.TaskObserved;
import elio.core.TaskObserver;
import elio.hoverdroid.R;

public class HeaderFragment extends Fragment implements TaskObserved, View.OnClickListener {
    private TextView headerText;
    private TaskObserver observer = null;
    public final static String EVENT_SET_HEADER = "EVENT_HEADER_SET";
    public ArrayList<String> availableHeaders;

    public String header;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);
        headerText = view.findViewById(R.id.header);
        headerText.setOnClickListener(this::onClick);
        if (header != null) {
            headerText.setText(header);
        }
        return view;
    }

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    public void setHeader(String header) {
        this.setHeader(header,true);
    }
    public void setHeader(String header, Boolean send) {
        if (this.availableHeaders.indexOf(header) == -1) {
            String logLine = String.format("Controller %s not available", header);
            Log.v("Controller Header", logLine);
        }

        this.header = header;
        if (headerText != null){
            headerText.setText(this.header);
        }
        if (send) {
            this.observer.onResults(
                    EVENT_SET_HEADER,
                    this.header
            );
        }
    }

    public void nextHeader() {
        int currentIndex =  this.availableHeaders.indexOf(header);
        if (currentIndex == -1) {
            Log.v("Controller Header", String.format("Current Controller %s not available", header));
        } else {
            int count = this.availableHeaders.size();
            String controller = this.availableHeaders.get((currentIndex+1) % count);
            this.setHeader(controller);
        }
    }

    @Override
    public void onClick(View view) {
        this.nextHeader();
    }

    public void setAvailableHeaders(ArrayList<String> availableHeaders) {
        this.availableHeaders = availableHeaders;
    }

}
