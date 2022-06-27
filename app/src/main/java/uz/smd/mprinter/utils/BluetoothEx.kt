package uz.smd.mprinter.utils

import android.bluetooth.BluetoothAdapter




/**
 * Created by Siddikov Mukhriddin on 6/27/22
 */
fun setBluetooth(enable: Boolean): Boolean {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val isEnabled = bluetoothAdapter.isEnabled
    if (enable && !isEnabled) {
        return bluetoothAdapter.enable()
    } else if (!enable && isEnabled) {
        return bluetoothAdapter.disable()
    }
    // No need to change bluetooth state
    return true
}