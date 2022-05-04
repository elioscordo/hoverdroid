package elio.hoverboard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import elio.serial.Constants;
import elio.serial.CustomProber;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;

import elio.core.SerialDevice;
/*
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$PATH"

export ANDROID_HOME="/Users/elio/Library/Android/sdk"

export JAVA_HOME="/Applications/Android Studio.app/Contents/jre/jdk/Contents/Home"

export NDKROOT="/Users/elio/Library/Android/sdk/ndk/21.4.7075529"
export NDK_ROOT="/Users/elio/Library/Android/sdk/ndk/21.4.7075529"
 */
public class HoverboardManager {
    private boolean isStarted = false;
    private enum Connected { False, Pending, True }
    private Connected connected = Connected.False;

    private final BroadcastReceiver broadcastReceiver;
    private UsbSerialPort usbSerialPort;

    private Activity activity;
    private String status;
    private SerialDevice serialDevice;
    private HoverboardController controller;

    public HoverboardManager(
            Activity activity,
            SerialDevice device
    ) {
        this.activity = activity;
        this.serialDevice = device;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(Constants.INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    start();
                }
            }
        };
    }

    public void start() {
        if (!isStarted) {
            this.activity.runOnUiThread(this::connect);
        }
    }

    public void stop(){
        this.controller.stop();
        try {
            this.usbSerialPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isStarted = false;
    }

    public boolean isConnected() {
        return this.connected == Connected.True;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == serialDevice.getDeviceId())
                device = v;
        if(device == null) {
            setStatus("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            setStatus("connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < serialDevice.getPortNum()) {
            setStatus("connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(serialDevice.getPortNum());
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this.activity, 0, new Intent(Constants.INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                setStatus("connection failed: permission denied");
            else
                setStatus("connection failed: open failed");
            return;
        }
        connected = Connected.Pending;
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(
                    serialDevice.getBaudRate(),
                    UsbSerialPort.DATABITS_8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
            );
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    public void onSerialConnect() {
        setStatus("connected");
        connected = Connected.True;
        this.controller = new HoverboardController(this.usbSerialPort);
        this.controller.start();
    }

    public void onSerialConnectError(Exception e) {
        setStatus("connection failed: " + e.getMessage());
    }

    public void setTargetCommand(HoverboardCommand command) {
        this.controller.setTargetCommand(command);
    }

}
