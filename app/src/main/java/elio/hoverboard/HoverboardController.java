package elio.hoverboard;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;

public class HoverboardController implements Runnable, SerialInputOutputManager.Listener {
    static final int HEART_BEAT = 10;
    static final int WRITE_WAIT_MILLIS = 100;
    static final boolean DEBUG = false;

    private Handler handler;
    private UsbSerialPort port;
    private SerialInputOutputManager serialIOManager;

    private HoverboardCommand nextCommand;
    private HoverboardCommand targetCommand;
    private HoverboardMessage lastMessage;
    private Integer frame = null;
    private static HoverboardCommand zeroCommand;


    public HoverboardController(UsbSerialPort port) {
        this.handler = new Handler();
        this.nextCommand = new HoverboardCommand();
        this.port = port;
        serialIOManager = new SerialInputOutputManager(port, this);

    }


    public void write(){
        try {
            port.write(this.nextCommand.toBytes(), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.serialIOManager.start();
        this.handler = new Handler(Looper.getMainLooper());
        this.handler.postDelayed(this, HEART_BEAT);
    }

    public void stop() {
        handler.removeCallbacks(this);
        this.serialIOManager.stop();
    }

    @Override
    public void run() {
        if (this.getFrame() != null) {
            if (this.targetCommand  != null) {
                getNextCommand();
            }else {
                this.nextCommand = getZeroCommand();
            }
            nextCommand.setFrame(this.getFrame());
            this.write();
        }
        this.handler.postDelayed(this, HEART_BEAT);
        Log.v("HoveboardController", String.valueOf(SystemClock.elapsedRealtime()));
    }

    @Override
    public void onNewData(byte[] data) {
        this.lastMessage = HoverboardMessage.createMessage(data);
    }

    @Override
    public void onRunError(Exception e) {
        e.printStackTrace();
    }

    public void setTargetCommand(HoverboardCommand targetCommand) {
        this.targetCommand = targetCommand;
    }

    public void getNextCommand() {
        // Feedback here
        this.nextCommand = this.targetCommand;
    }

    public HoverboardCommand getTargetCommand() {
        return targetCommand;
    }

    public Integer getFrame() {
        if (frame == null) {
            if (this.lastMessage != null) {
                frame = this.lastMessage.getFrame();
            }
        }
        return frame;
    }

    public static HoverboardCommand getZeroCommand() {
        if (HoverboardController.zeroCommand == null) {
            HoverboardController.zeroCommand = new HoverboardCommand();
            HoverboardController.zeroCommand.setSteer(0);
            HoverboardController.zeroCommand.setSpeed(0);
        }
        return HoverboardController.zeroCommand;
    }
}
