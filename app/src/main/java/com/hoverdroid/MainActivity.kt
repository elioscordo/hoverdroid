package com.hoverdroid


import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.core.SerialDevice
import com.core.TaskObserver
import com.fragment.*
import com.hoverboard.HoverboardCommand
import com.hoverboard.HoverboardManager
import com.wifi.SocketTransfer
import com.wifi.WiFiDirectBroadcastReceiver
import com.wifi.WifiDirectService
import com.wifi.listeners.SocketListener
import com.wifi.listeners.WifiDirectListener
import com.wifi.utils.MessageUtils


class MainActivity() : AppCompatActivity(),
    FragmentManager.OnBackStackChangedListener, TaskObserver, SocketListener, ServiceConnection,
    Parcelable {

    private var joystickFragment: JoystickFragment? = null;
    private var leversFragment: LeversFragment? = null;
    private var wifiFragment:  WifiDirectFragment? = null;

    private val CONTROLLER_JOYSTICK = "Joystick";
    private val CONTROLLER_LEVERS = "Levers"
    private val CONTROLLER_WIFI = "wifi"

    private val controllers = arrayListOf(
        CONTROLLER_JOYSTICK,
        CONTROLLER_LEVERS,
        CONTROLLER_WIFI
    )


    private var devicesFragment: DevicesFragment? = null;
    private var deviceFragment: DeviceFragment? = null;
    private var headerFragment:HeaderFragment? = null;

    private var hoverboardManager:HoverboardManager? = null;
    private val PERMISSION_REQUEST_CODE = 1
    private var PORT: Int = 8888
    private val PERMISSIONS = arrayOf(
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private lateinit var  wifiDirectService: WifiDirectService;
    private var isBind:Boolean = false;

    private var wifiState: Int = 0

    private val intentFilter = IntentFilter()


    /** Start and bind service.  */
    fun startAndBindService() {
        val intent = Intent(this, WifiDirectService::class.java)
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    /** Stop and unbind service.  */
    fun stopAndUnbindService() {
        val intent = Intent(this, WifiDirectService::class.java)
        stopService(intent)
        unbindService(this)
    }



    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        wifiDirectService = (service as WifiDirectService.ServiceBinder).service
        wifiDirectService.setObserver(this);
        wifiDirectService.initWifiDirect();
        //wifiDirectService.startMessageReceiver()
        isBind = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        wifiDirectService.setObserver(null);
        wifiDirectService.stopMessageReceiver()
        isBind = false
    }


    fun setIntents() {
        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setIntents()

        ActivityCompat.requestPermissions(
            this, PERMISSIONS, PERMISSION_REQUEST_CODE
        )
        joystickFragment = JoystickFragment()
        joystickFragment!!.setObserver(this)

        leversFragment = LeversFragment()
        leversFragment!!.setObserver(this)

        wifiFragment = WifiDirectFragment()
        wifiFragment!!.setObserver(this)


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

        maybeEnableArButton()

        headerFragment!!.setAvailableHeaders(controllers);
        headerFragment!!.setHeader(CONTROLLER_JOYSTICK)

        // start
        startAndBindService()

    }

    override fun onResults(path: String, arg: Any?){
        when(path){
            JoystickFragment.EVENT_JOYSTICK_COMMAND -> {
                if (arg is HoverboardCommand) {
                    if (hoverboardManager != null && hoverboardManager!!.isConnected) {
                        hoverboardManager!!.setTargetCommand(arg)
                    }
                    if (wifiState !== 0) {
                        // create json object
                        val messageEncode: String = MessageUtils.createMessage(
                            "client",
                            arg.toString(),
                            MessageUtils.TYPE_MESSAGE,
                            arg.toString().length,
                            "text"
                        )
                        wifiDirectService.sendMessage(
                            messageEncode
                        )
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
                        .replace(R.id.fragment, deviceFragment!!, "terminal").addToBackStack(null)
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
                                .replace(R.id.controller, joystickFragment!!, "controller").commit()
                        }
                        CONTROLLER_LEVERS -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.controller, leversFragment!!, "controller").commit()
                        }
                        CONTROLLER_WIFI -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.controller, wifiFragment!!, "controller").commit()
                            refreshWifiFragment()
                        }
                    }
                }
            }
            WifiDirectService.EVENT_WIFI_PEERS_AVAILABLE -> {
                refreshWifiFragment()
            }
            WifiDirectService.EVENT_WIFI_CONNECTION_OWNER -> {

                // set port and start server
                wifiState = 1
                wifiFragment?.addDebugMessage("Connected as Owner")
            }
            WifiDirectListener.EVENT_WIFI_CONNECTION_CLIENT -> {
                    wifiFragment?.addDebugMessage("Connected as Client")
                    wifiState = 2
            }
            WifiDirectFragment.EVENT_WIFI_DIRECT_CONNECT -> {
                wifiFragment?.addDebugMessage("Connection Attempt")
            }
            WifiDirectListener.EVENT_WIFI_CONNECTION_SUCCESS -> {
                wifiFragment?.addDebugMessage("Connection Success")
            }
            WifiDirectListener.EVENT_WIFI_CONNECTION_FAILURE -> {
                wifiFragment?.addDebugMessage("Connection Failure")
            }
        }
    }


    private fun refreshWifiFragment() {
        if (wifiDirectService.deviceList != null) {
            wifiFragment?.addDebugMessage("Peers available %d".format(wifiDirectService.deviceList.size))
            wifiFragment?.setList(
                wifiDirectService.deviceList
            )
        }
    }
    private val locationPermissionRequest = registerForActivityResult(RequestMultiplePermissions(),
        ActivityResultCallback<Map<String?, Boolean?>> { result: Map<String?, Boolean?> ->
            val fineLocationGranted = result.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false
            )
            val coarseLocationGranted = result.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            )
            if (fineLocationGranted != null && fineLocationGranted) {
                // Precise location access granted.
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                // Only approximate location access granted.
            } else {
                // No location access granted.
            }
        }
    )

    constructor(parcel: Parcel) : this() {
        PORT = parcel.readInt()
        wifiState = parcel.readInt()
    }


    private fun wifiDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }

    }
    override fun onBackStackChanged() {

    }

    override fun onSupportNavigateUp(): Boolean {
        return true
    }

    fun getCurrentController(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.controller)
    }

    override fun onNewIntent(intent: Intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED" == intent.action) {
            val terminal =
                supportFragmentManager.findFragmentByTag("terminal") as DeviceFragment?
            terminal?.append("USB device detected")
        }
        super.onNewIntent(intent)
    }
    fun maybeEnableArButton() : Boolean {
        /*
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({
                maybeEnableArButton()
            }, 200)
        }
        val isSupported: Boolean
        isSupported = if (availability.isSupported) {
            true
        } else { // The device is unsupported or unknown.
            true
        }
        */
        return true
    }

    /** register the BroadcastReceiver with the intent values to be matched  */
    public override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        // stop service
        stopAndUnbindService()
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onReceiver(response: String?) {
        // create json object

        // create json object
        val messageDecode = MessageUtils.parseMessage(response)
        wifiFragment?.addDebugMessage(messageDecode[3])
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(PORT)
        parcel.writeInt(wifiState)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }
}

