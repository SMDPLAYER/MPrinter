package uz.smd.mprinter.utils

import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.os.Handler


/**
 * Created by Siddikov Mukhriddin on 6/27/22
 */
fun setBluetooth(block:()->Unit) {
    val bluetoothAdapter = getDefaultAdapter()
    val isEnabled = bluetoothAdapter.isEnabled
    if (!isEnabled) {
        bluetoothAdapter.enable()
        Handler().postDelayed({
            block()
        }, 1000)

    }
    block()
}