package org.alice.poc.airgap.detection

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NfcAdapter

object AirGapBroadcastReceiver {

    private const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    private const val ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED"
    private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"

    fun createIntentFilter(): IntentFilter = IntentFilter().also { filter ->
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        filter.addAction(ACTION_SIM_STATE_CHANGED)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        filter.addAction(ACTION_TETHER_STATE_CHANGED)
        filter.addAction(ACTION_USB_STATE)
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
    }

    fun createReceiver(onBroadcastReceived: () -> Unit): BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                onBroadcastReceived()
            }
        }
}
