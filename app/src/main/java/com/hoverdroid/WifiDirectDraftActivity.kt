package com.hoverdroid


import android.Manifest
import android.content.*
import android.net.wifi.p2p.WifiP2pConfig
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.core.PermissionManager
import com.core.SerialDevice
import com.core.TaskObserver
import com.fragment.*
import com.google.android.gms.nearby.connection.Strategy
import com.hoverboard.HoverboardCommand
import com.hoverboard.HoverboardManager
import com.wifi.WifiDirectService
import com.wifi.listeners.SocketListener
import com.wifi.utils.Constants
import com.wifi.utils.MessageUtils
import kotlinx.android.synthetic.main.fragment_joystick.*


class WifiDirectDraftActivity() : ConnectionsActivity(),
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

    public lateinit var  wifiDirectService: WifiDirectService;
    private var isBind:Boolean = false;

    private var wifiState: Int = 0
    private var wifiServiceState: Int = 0

    private val intentFilter = IntentFilter()
    private lateinit var permissionManager:PermissionManager;


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
        isBind = true
        initWifi()
    }
    fun initWifi(){
        if (permissionManager.checkFineLocationPermission(this)) {
            wifiDirectService.initWifiDirect();
        } else {
            permissionManager.requestFineLocationPermission(this)
        }

    }

    fun setPeers() {
        if (wifiDirectService.deviceList == null || wifiDirectService.deviceList.size == 0 ) {
            wifiDirectService.discoverPeers()
        }
        else {
            wifiFragment!!.setList(wifiDirectService.deviceList)
        }
    }
    override fun onServiceDisconnected(name: ComponentName?) {
        wifiDirectService.setObserver(null);
        wifiDirectService.stopMessageReceiver()
        isBind = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        headerFragment!!.setAvailableHeaders(controllers);
        headerFragment!!.setHeader(CONTROLLER_JOYSTICK)
        permissionManager = PermissionManager();
        permissionManager.setObserver(this);
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
                        }
                    }
                }
            }
            Constants.EVENT_WIFI_PEERS_AVAILABLE -> {
                wifiFragment?.addDebugMessage("Peers available %d".format(wifiDirectService.deviceList.size))
                wifiFragment?.setList(
                    wifiDirectService.deviceList
                )
            }
            Constants.EVENT_WIFI_CONNECTION_OWNER -> {

                // set port and start server
                wifiState = 1
                wifiFragment?.addDebugMessage("Connected as Owner")
            }
            Constants.EVENT_WIFI_CONNECTION_CLIENT -> {
                wifiFragment?.addDebugMessage("Connected as Client")
                wifiState = 2
            }
            WifiDirectFragment.EVENT_WIFI_DIRECT_CONNECT -> {
                wifiFragment?.addDebugMessage("Connection Attempt")
                if (arg is WifiP2pConfig) {
                    wifiDirectService.connectToDevice(arg)
                }
            }
            Constants.EVENT_WIFI_CONNECTION_SUCCESS -> {
                wifiServiceState = 1
            }
            Constants.EVENT_WIFI_CONNECTION_FAILURE -> {
                wifiFragment?.addDebugMessage("Connection Failure")
            }

            Constants.EVENT_WIFI_DISCOVERY_STARTED -> {
                wifiFragment?.addDebugMessage("Discovering Peers Start")
            }
            Constants.EVENT_WIFI_DISCOVERY_STOPPED -> {
                wifiFragment?.addDebugMessage("Discovering Peers Stopped")
            }

            PermissionManager.EVENT_PERMISSION_GRANTED -> {
                if (arg is Int) {
                    if (arg == PermissionManager.ACCESS_FINE_LOCATION_CODE) {
                        wifiDirectService.initWifiDirect();
                    }
                }
            }
        }
    }



    constructor(parcel: Parcel) : this() {
        PORT = parcel.readInt()
        wifiState = parcel.readInt()
    }


    override fun onBackStackChanged() {

    }

    override fun onSupportNavigateUp(): Boolean {
        return true
    }

    override fun getName(): String {
        return "name1"
    }

    override fun getServiceId(): String {
       return "serviceId1"
    }

    override fun getStrategy(): Strategy {
       return Strategy.P2P_STAR
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

    companion object CREATOR : Parcelable.Creator<WifiDirectDraftActivity> {
        override fun createFromParcel(parcel: Parcel): WifiDirectDraftActivity {
            return WifiDirectDraftActivity(parcel)
        }

        override fun newArray(size: Int): Array<WifiDirectDraftActivity?> {
            return arrayOfNulls(size)
        }
    }
}
