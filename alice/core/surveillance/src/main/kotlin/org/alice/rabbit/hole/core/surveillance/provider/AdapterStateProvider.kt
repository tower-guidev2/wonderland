package org.alice.rabbit.hole.core.surveillance.provider

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.aware.WifiAwareManager
import android.nfc.NfcAdapter
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityManager

class AdapterStateProvider(private val context: Context) : IAdapterStateProvider {

    private companion object {
        const val BLUETOOTH_STATE_BLE_ON = 15
    }

    override fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter?.state == android.bluetooth.BluetoothAdapter.STATE_ON
    }

    override fun isBluetoothLowEnergyEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return bluetoothManager?.adapter?.state == BLUETOOTH_STATE_BLE_ON
    }

    override fun isNfcEnabled(): Boolean =
        NfcAdapter.getDefaultAdapter(context)?.isEnabled ?: false

    override fun isWifiBackgroundScanEnabled(): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return false
        return wifiManager.isScanAlwaysAvailable
    }

    override fun isWifiAwareAvailable(): Boolean {
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as? WifiAwareManager ?: return false
        return wifiAwareManager.isAvailable
    }

    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return locationManager.isLocationEnabled
    }

    override fun isAccessibilityEnabled(): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        return accessibilityManager.isEnabled
    }

    override fun simState(): Int {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return TelephonyManager.SIM_STATE_ABSENT
        return telephonyManager.simState
    }
}
