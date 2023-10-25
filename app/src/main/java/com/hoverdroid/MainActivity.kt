package com.hoverdroid


import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.core.NearbyUtils
import com.core.SerialDevice
import com.core.TaskObserver
import com.fragment.*
import com.google.android.gms.nearby.connection.Payload
import com.hoverboard.HoverboardCommand
import com.hoverboard.HoverboardManager



class MainActivity() : BaseTalkieActivity(),
    FragmentManager.OnBackStackChangedListener, TaskObserver
    {

        private var joystickFragment: JoystickFragment? = null;
        private var leversFragment: LeversFragment? = null;
        private var wifiFragment:  WifiDirectFragment? = null;
        private var debugFragment:  DebugFragment? = null;

        val CONTROLLER_JOYSTICK = "Joystick";
        val CONTROLLER_LEVERS = "Levers"
        val CONTROLLER_WIFI = "wifi"
        val CONTROLLER_DEBUG = "debug"

        private val controllers = arrayListOf(
            CONTROLLER_JOYSTICK,
            CONTROLLER_LEVERS,
            CONTROLLER_DEBUG
        )


        private var devicesFragment: DevicesFragment? = null;
        private var deviceFragment: DeviceFragment? = null;
        private var headerFragment:HeaderFragment? = null;

        private var hoverboardManager:HoverboardManager? = null;
        private val PERMISSION_REQUEST_CODE = 1

        private var isConnected:Boolean = false;

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            joystickFragment = JoystickFragment()
            joystickFragment!!.setObserver(this)

            leversFragment = LeversFragment()
            leversFragment!!.setObserver(this)

            wifiFragment = WifiDirectFragment()
            wifiFragment!!.setObserver(this)

            debugFragment = DebugFragment()
            debugFragment!!.setObserver(this)


            devicesFragment = DevicesFragment()
            devicesFragment!!.setObserver(this)

            headerFragment = HeaderFragment()
            headerFragment!!.setObserver(this)


            setContentView(R.layout.activity_main)

            supportFragmentManager.addOnBackStackChangedListener(this)

            if (savedInstanceState == null) supportFragmentManager.beginTransaction()
                .add(R.id.fragment, devicesFragment!!, "devices").commit() else onBackStackChanged()

            if (savedInstanceState == null) supportFragmentManager.beginTransaction()
                .add(R.id.header, headerFragment!!, "header").commit() else onBackStackChanged()

            headerFragment!!.setAvailableHeaders(controllers);
            headerFragment!!.setHeader(CONTROLLER_JOYSTICK)
        }


        override fun onResults(path: String, arg: Any?) {
            when (path) {
                JoystickFragment.EVENT_JOYSTICK_COMMAND -> {
                    if (arg is HoverboardCommand) {
                        if (hoverboardManager != null && hoverboardManager!!.isConnected) {
                            hoverboardManager!!.setTargetCommand(arg)
                        }
                        if (isConnected) {
                            send(Payload.fromBytes(arg.toBytes()));
                        }
                    }
                };
                DevicesFragment.EVENT_DEVICE_SELECTED -> {
                    if (arg is SerialDevice) {
                        this.hoverboardManager = HoverboardManager(this, arg)
                        this.hoverboardManager!!.start()
                        this.deviceFragment = DeviceFragment()
                        this.deviceFragment!!.setObserver(this)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment, deviceFragment!!, "terminal").addToBackStack(
                                null
                            )
                            .commit()
                    }
                }
                DeviceFragment.EVENT_DEVICE_REFRESH -> {
                    this.hoverboardManager!!.stop()
                    this.hoverboardManager!!.start()
                }
                DeviceFragment.EVENT_DEVICE_REMOVE -> {
                    this.hoverboardManager!!.stop()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, devicesFragment!!, "devices").commit()
                }
                HeaderFragment.EVENT_SET_HEADER -> {
                    if (arg is String) {
                        when (arg) {
                            CONTROLLER_JOYSTICK -> {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.controller, joystickFragment!!, "controller")
                                    .commit()
                            }
                            CONTROLLER_LEVERS -> {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.controller, leversFragment!!, "controller")
                                    .commit()
                            }
                            CONTROLLER_WIFI -> {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.controller, wifiFragment!!, "controller").commit()
                            }
                            CONTROLLER_DEBUG -> {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.controller, debugFragment!!, "controller")
                                    .commit()
                            }
                        }
                    }
                }
            }
        }

        override fun onBackStackChanged() {

        }

        override fun onSupportNavigateUp(): Boolean {
            return true
        }

        override fun onStateChanged(
            oldState: State,
            newState: State
        ) {
            super.onStateChanged(oldState, newState)
            debugFragment!!.log("NearBy connection from $oldState to $newState")
            if (newState == State.CONNECTED){
                isConnected = true
            }
        }

        override fun onReceive(endpoint:Endpoint, payload:Payload)
        {
            super.onReceive(endpoint, payload);
            if (payload.type == Payload.Type.BYTES) {
                //debugFragment!!.log(payload.asBytes().toString())
                if (hoverboardManager != null && hoverboardManager!!.isConnected) {
                    hoverboardManager!!.setTargetCommand(HoverboardCommand.commandFromBytes(payload.asBytes()))
                }
            }
        }
    }


