package uz.smd.mprinter

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import uz.smd.mprinter.async.*
import uz.smd.mprinter.connection.DeviceConnection
import uz.smd.mprinter.connection.bluetooth.BluetoothConnection
import uz.smd.mprinter.connection.bluetooth.BluetoothPrintersConnections
import uz.smd.mprinter.connection.tcp.TcpConnection
import uz.smd.mprinter.connection.usb.UsbConnection
import uz.smd.mprinter.connection.usb.UsbPrintersConnections
import uz.smd.mprinter.textparser.PrinterTextParserImg
import uz.smd.mprinter.utils.checkPermissionPhoto
import uz.smd.mprinter.utils.setBluetooth
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var button = findViewById<View>(R.id.button_bluetooth_browse) as Button
        button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermissionPhoto(Manifest.permission.BLUETOOTH_CONNECT){
                    setBluetooth(true)
                    browseBluetoothDevice()
                }
            }else{
                setBluetooth(true)
                browseBluetoothDevice()
            }
        }
        button = findViewById<View>(R.id.button_bluetooth) as Button
        button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermissionPhoto(Manifest.permission.BLUETOOTH_CONNECT){
                    setBluetooth(true)
                    printBluetooth()
                }
            }else{
                setBluetooth(true)
                printBluetooth()
            }

        }
        button = findViewById<View>(R.id.button_usb) as Button
        button.setOnClickListener { printUsb() }
        button = findViewById<View>(R.id.button_tcp) as Button
        button.setOnClickListener { printTcp() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_BLUETOOTH, PERMISSION_BLUETOOTH_ADMIN, PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN -> printBluetooth()
            }
        }
    }

    private var selectedDevice: BluetoothConnection? = null
    fun browseBluetoothDevice() {
        val bluetoothDevicesList: Array<BluetoothConnection>? =
            BluetoothPrintersConnections().list
        if (bluetoothDevicesList != null) {
            val items = arrayOfNulls<String>(bluetoothDevicesList.size + 1)
            items[0] = "Default printer"
            var i = 0
            for (device: BluetoothConnection in bluetoothDevicesList) {
                items[++i] = device.device.name
            }
            val alertDialog = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Bluetooth printer selection")
            alertDialog.setItems(
                items
            ) { dialogInterface, i ->
                val index = i - 1
                if (index == -1) {
                    selectedDevice = null
                } else {
                    selectedDevice = bluetoothDevicesList[index]
                }
                val button =
                    findViewById<View>(R.id.button_bluetooth_browse) as Button
                button.text = items.get(i)
            }
            val alert = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }
    }

    fun printBluetooth() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                PERMISSION_BLUETOOTH_ADMIN
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                PERMISSION_BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                PERMISSION_BLUETOOTH_SCAN
            )
        } else {
            AsyncBluetoothEscPosPrint(
                this,
                object : AsyncEscPosPrint.OnPrintFinished() {
                  override  fun onError(asyncEscPosPrinter: AsyncEscPosPrinter?, codeException: Int) {
                        Log.e(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                        )
                    }

                    override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                        Log.i(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                        )
                    }
                }
            )
                .execute(getAsyncEscPosPrinter(selectedDevice))
        }
    }

    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbManager: UsbManager? =
                        getSystemService(USB_SERVICE) as UsbManager?
                    val usbDevice: UsbDevice? =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            AsyncUsbEscPosPrint(
                                context,
                                object : AsyncEscPosPrint.OnPrintFinished() {
                                   override fun onError(
                                        asyncEscPosPrinter: AsyncEscPosPrinter?,
                                        codeException: Int
                                    ) {
                                        Log.e(
                                            "Async.OnPrintFinished",
                                            "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                                        )
                                    }

                                  override  fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                                        Log.i(
                                            "Async.OnPrintFinished",
                                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                                        )
                                    }
                                }
                            )
                                .execute(
                                    getAsyncEscPosPrinter(
                                        UsbConnection(
                                            usbManager,
                                            usbDevice
                                        )
                                    )
                                )
                        }
                    }
                }
            }
        }
    }

    fun printUsb() {
        val usbConnection: UsbConnection? = UsbPrintersConnections.selectFirstConnected(this)
        val usbManager: UsbManager? = this.getSystemService(USB_SERVICE) as UsbManager?
        if (usbConnection == null || usbManager == null) {
            AlertDialog.Builder(this)
                .setTitle("USB Connection")
                .setMessage("No USB printer found.")
                .show()
            return
        }
        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
        usbManager.requestPermission(usbConnection.device, permissionIntent)
    }

    /*==============================================================================================
    =========================================TCP PART===============================================
    ==============================================================================================*/
    fun printTcp() {
        val ipAddress = findViewById<View>(R.id.edittext_tcp_ip) as EditText
        val portAddress = findViewById<View>(R.id.edittext_tcp_port) as EditText
        try {
            AsyncTcpEscPosPrint(
                this,
                object : AsyncEscPosPrint.OnPrintFinished() {
                   override fun onError(asyncEscPosPrinter: AsyncEscPosPrinter?, codeException: Int) {
                        Log.e(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : An error occurred !"
                        )
                    }

                   override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                        Log.i(
                            "Async.OnPrintFinished",
                            "AsyncEscPosPrint.OnPrintFinished : Print is finished !"
                        )
                    }
                }
            )
                .execute(
                    getAsyncEscPosPrinter(
                        TcpConnection(
                            ipAddress.text.toString(), portAddress.text.toString().toInt()
                        )
                    )
                )
        } catch (e: NumberFormatException) {
            AlertDialog.Builder(this)
                .setTitle("Invalid TCP port address")
                .setMessage("Port field must be an integer.")
                .show()
            e.printStackTrace()
        }
    }
    /*==============================================================================================
    ===================================ESC/POS PRINTER PART=========================================
    ==============================================================================================*/
    /**
     * Asynchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    fun getAsyncEscPosPrinter(printerConnection: DeviceConnection?): AsyncEscPosPrinter {
        val format = SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss")
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
        return printer.addTextToPrint(
            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(
                printer,
                this.applicationContext.resources.getDrawableForDensity(
                    R.drawable.logo,
                    DisplayMetrics.DENSITY_MEDIUM
                )
            ).toString() + "</img>\n" +
                    "[L]\n" +
                    "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                    "[L]\n" +
                    "[C]<u type='double'>" + format.format(Date()).toString() + "</u>\n" +
                    "[C]\n" +
                    "[C]================================\n" +
                    "[L]\n" +
                    "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99€\n" +
                    "[L]  + Size : S\n" +
                    "[L]\n" +
                    "[L]<b>AWESOME HAT</b>[R]24.99€\n" +
                    "[L]  + Size : 57/58\n" +
                    "[L]\n" +
                    "[C]--------------------------------\n" +
                    "[R]TOTAL PRICE :[R]34.98€\n" +
                    "[R]TAX :[R]4.23€\n" +
                    "[L]\n" +
                    "[C]================================\n" +
                    "[L]\n" +
                    "[L]<u><font color='bg-black' size='tall'>Customer :</font></u>\n" +
                    "[L]Raymond DUPONT\n" +
                    "[L]5 rue des girafes\n" +
                    "[L]31547 PERPETES\n" +
                    "[L]Tel : +33801201456\n" +
                    "\n" +
                    "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                    "[L]\n" +
                    "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>\n"
        )
    }

    companion object {
        /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/
        val PERMISSION_BLUETOOTH = 1
        val PERMISSION_BLUETOOTH_ADMIN = 2
        val PERMISSION_BLUETOOTH_CONNECT = 3
        val PERMISSION_BLUETOOTH_SCAN = 4

        /*==============================================================================================
    ===========================================USB PART=============================================
    ==============================================================================================*/
        private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}