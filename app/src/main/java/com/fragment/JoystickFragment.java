package com.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;



import com.core.TaskObserved;
import com.core.TaskObserver;
import com.hoverboard.HoverboardCommand;
import com.hoverdroid.R;

public class JoystickFragment extends Fragment implements View.OnTouchListener, TaskObserved {
    public static final String EVENT_JOYSTICK_COMMAND = "EVENT_SET_TARGET";
    public static final boolean DEBUG = false;
    View canvas;
    View draggable;
    TextView logger;
    float dX;
    float dY;
    TaskObserver observer;
    float x0;
    float y0;
    private TextView debugTextView;

    private int sensitivitySteer = 5;
    private int sensitivitySpeed = 15;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        canvas = inflater.inflate(R.layout.fragment_joystick, container, false);
        draggable = canvas.findViewById(R.id.draggable);
        draggable.setOnTouchListener(this);
        logger = canvas.findViewById(R.id.logger);
        return canvas;
    }

    public HoverboardCommand createCommand(float dx, float dy) {
        HoverboardCommand command = new HoverboardCommand();
        command.setSpeed((int)dx * sensitivitySpeed);
        command.setSteer((int)dy * sensitivitySteer);
        return command;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        String logString = String.format(
                "parent: %s,%s  draggable %s,%s",
                canvas.getX(),
                canvas.getY(),
                view.getX(),
                view.getY()
        );
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.x0 = draggable.getX();
                this.y0 = draggable.getY();
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                // Log.v("Joystick ACTION_DOWN", logString );
                break;

            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX() + dX;
                float y = event.getRawY() + dY;
                if (y > 0 && y < canvas.getHeight() - draggable.getHeight()) {
                    view.setY(y);

                }
                if (x > 0 && x < canvas.getWidth() - draggable.getWidth()) {
                    view.setX(x);
                }
                sendCommand(view);
                // Log.v("Joystick ACTION_MOVE", logString );
                break;

            case MotionEvent.ACTION_UP:
                view.setX(x0);
                view.setY(y0);
                sendCommand(view);
                observer.onResults(
                        EVENT_JOYSTICK_COMMAND,
                        HoverboardCommand.zeroCommand()
                );
                // Log.v("Joystick ACTION_UP", logString);
                break;

            default:
                return false;
        }
        return true;
    }

    public void sendCommand(View view){
        float x = (view.getX() - x0);
        float baseX = canvas.getWidth() / 2;
        float baseY = y0;
        x = 100 * x / baseX;
        float y = (view.getY() - y0);
        y = 100 * - y / baseY;
        logger.setText(String.format("(%.1f,%.1f)", x, y));
        HoverboardCommand command = createCommand( x , y);
        observer.onResults(
                EVENT_JOYSTICK_COMMAND,
                command
        );
    }



    @Override
    public void setObserver(TaskObserver observer) {
        this.observer = observer;
    }
}
