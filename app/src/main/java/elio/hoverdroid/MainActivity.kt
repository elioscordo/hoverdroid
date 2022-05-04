package elio.hoverdroid


import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import elio.core.SerialDevice
import elio.core.TaskObserver
import elio.fragment.*
import elio.hoverboard.HoverboardCommand
import elio.hoverboard.HoverboardManager
import elio.hoverdroid.R
import java.util.*


class MainActivity : AppCompatActivity(),
    FragmentManager.OnBackStackChangedListener, TaskObserver  {

    private var joystickFragment: JoystickFragment? = null;
    private var leversFragment: LeversFragment? = null;

    private val CONTROLLER_JOYSTICK = "Joystick";
    private val CONTROLLER_LEVERS = "Levers"
    private val CONTROLLER_GYRO = "Gyro"

    private val controllers = arrayListOf(
        CONTROLLER_JOYSTICK,
        CONTROLLER_LEVERS
    )

    private var devicesFragment: DevicesFragment? = null;
    private var deviceFragment: DeviceFragment? = null;
    private var headerFragment:HeaderFragment? = null;

    private var hoverboardManager:HoverboardManager? = null;
    private val PERMISSION_REQUEST_CODE = 1
    private val PERMISSIONS = arrayOf(
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this, PERMISSIONS, PERMISSION_REQUEST_CODE)
        joystickFragment = JoystickFragment()
        joystickFragment!!.setObserver(this)

        leversFragment = LeversFragment()
        leversFragment!!.setObserver(this)


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
    }

    override fun onResults(path: String, arg: Any?){
        when(path){
            JoystickFragment.EVENT_JOYSTICK_COMMAND -> {
                if (arg is HoverboardCommand) {
                    if (hoverboardManager != null && hoverboardManager!!.isConnected) {
                        hoverboardManager!!.setTargetCommand(arg)
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
}
