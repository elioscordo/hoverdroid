package com.fragment;

import android.os.Bundle;
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

public class LeversFragment extends Fragment implements View.OnTouchListener, TaskObserved {
    public static final String EVENT_JOYSTICK_COMMAND = "EVENT_SET_TARGET";
    public static final boolean DEBUG = false;
    View canvas;
    View draggableSpeed;
    View draggableSteer;

    boolean isSpeedBeingDragged;
    boolean isSteerBeingDragged;

    TextView logger;
    float dX;
    float dY;

    float x0;
    float y0;

    TaskObserver observer;

    private TextView debugTextView;

    private int sensitivitySteer = 5;
    private int sensitivitySpeed = 20;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        canvas = inflater.inflate(R.layout.fragment_levers, container, false);
        draggableSpeed = canvas.findViewById(R.id.lever_speed);
        draggableSpeed.setOnTouchListener(this);
        draggableSteer = canvas.findViewById(R.id.lever_steer);
        draggableSteer.setOnTouchListener(this);
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
        if (view == this.draggableSpeed) {
            return this.onSpeedTouch(view, event);
        }
        if (view == this.draggableSteer) {
            return this.onSteerTouch(view, event);
        }
        return false;
    }

    public boolean onSpeedTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isSpeedBeingDragged = true;
                this.y0 = draggableSpeed.getY();
                dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                isSpeedBeingDragged = true;
                float y = event.getRawY() + dY;
                if (y > 0 && y < canvas.getHeight() - draggableSpeed.getHeight()) {
                    view.setY(y);
                }
                sendCommand();
                break;

            case MotionEvent.ACTION_UP:
                isSpeedBeingDragged = false;
                if (!isSteerBeingDragged) {
                    resetLevers();
                }
                break;

            default:
                return false;
        }
        return true;
    }

    public void resetLevers() {
        draggableSpeed.setY(y0);
        draggableSteer.setX(x0);
        sendCommand();
        observer.onResults(
                EVENT_JOYSTICK_COMMAND,
                HoverboardCommand.zeroCommand()
        );
    }

    public boolean onSteerTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isSteerBeingDragged = true;
                this.x0 = draggableSteer.getX();
                dX = view.getX() - event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                isSteerBeingDragged = true;
                float x = event.getRawX() + dX;
                if (x > 0 && x < canvas.getWidth() - draggableSpeed.getWidth()) {
                    view.setX(x);
                }
                sendCommand();
                break;
            case MotionEvent.ACTION_UP:
                isSteerBeingDragged = false;
                if (!isSpeedBeingDragged) {
                    resetLevers();

                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void sendCommand(){
        if (y0 <= 0) {
            y0 = draggableSpeed.getY();
        }
        if (x0 <= 0) {
            x0 = draggableSpeed.getX();
        }
        float x = (draggableSteer.getX() - x0);
        float baseX = canvas.getWidth() / 2;
        float baseY = y0;
        x = 100 * x / baseX;
        float y = (draggableSpeed.getY() - y0);
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
