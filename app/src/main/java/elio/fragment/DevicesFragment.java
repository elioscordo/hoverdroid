package elio.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.Locale;

import elio.core.SerialDevice;
import elio.core.TaskObserved;
import elio.core.TaskObserver;
import elio.hoverdroid.R;
import elio.serial.CustomProber;

public class DevicesFragment extends ListFragment implements TaskObserved {
    public final static String EVENT_DEVICE_SELECTED = "EVENT_DEVICE_SELECTED";
    private TaskObserver observer;

    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }

    static class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }

    private final ArrayList<ListItem> listItems = new ArrayList<>();
    private ArrayAdapter<ListItem> listAdapter;
    private int baudRate = 115200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.observer = (TaskObserver)getActivity();
        listAdapter = new ArrayAdapter<ListItem>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                ListItem item = listItems.get(position);
                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);
                if(item.driver == null)
                    text1.setText("<no driver>");
                else
                    text1.setText("Hoverboard found!");
                text2.setText("Drive! Save the planet!");
                return view;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        setEmptyText("No Hoverboard Found");
        ((TextView) getListView().getEmptyView()).setTextSize(20);

        ((TextView) getListView().getEmptyView()).setTextColor(
                getResources().getColor(R.color.colorHigh)
        );

        setListAdapter(listAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    void refresh() {
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        listItems.clear();
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++)
                    listItems.add(new ListItem(device, port, driver));
            } else {
                listItems.add(new ListItem(device, 0, null));
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ListItem item = listItems.get(position);
        if(item.driver == null) {
            Toast.makeText(getActivity(), "no driver", Toast.LENGTH_SHORT).show();
        } else {
            SerialDevice payload = new SerialDevice(
                item.device.getDeviceId(),
                item.port,
                baudRate
            );
            observer.onResults(EVENT_DEVICE_SELECTED, payload);
        }
    }

}
