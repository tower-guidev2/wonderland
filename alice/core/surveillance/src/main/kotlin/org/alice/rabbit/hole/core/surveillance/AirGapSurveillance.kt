package org.alice.rabbit.hole.core.surveillance

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NfcAdapter
import android.os.BatteryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.alice.rabbit.hole.core.surveillance.api.AirGapStatus
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.api.IAirGapSurveillance

private const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
private const val ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED"
private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"

class AirGapSurveillance(
    private val context: Context,
    private val applicationScope: CoroutineScope,
    private val isDebugBuild: Boolean,
    private val networkMonitor: AirGapNetworkMonitor,
) : IAirGapSurveillance {

    private val mutableStatus: MutableStateFlow<AirGapStatus> = MutableStateFlow(AirGapStatus.Secure)
    override val status: StateFlow<AirGapStatus> = mutableStatus.asStateFlow()

    private val mutableViolations: MutableSharedFlow<AirGapViolation> = MutableSharedFlow()
    override val violations: Flow<AirGapViolation> = mutableViolations.asSharedFlow()

    fun setCompromised(violation: AirGapViolation) {
        mutableStatus.value = AirGapStatus.Compromised(violation)
    }

    fun broadcastFlow(): Flow<AirGapViolation> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                val augmentedIntExtras = buildAugmentedIntExtras(receiverContext, intent)
                val augmentedBooleanExtras = buildAugmentedBooleanExtras(receiverContext, intent)
                val data = BroadcastData(
                    action = intent.action,
                    booleanExtras = augmentedBooleanExtras,
                    intExtras = augmentedIntExtras,
                    stringExtras = extractStringExtras(intent),
                    stringArrayExtras = extractStringArrayExtras(intent),
                )
                val violation = IntentToViolationMapper.map(data) ?: return
                if (isDebugBuild && violation == AirGapViolation.UsbPowerConnected) return
                trySend(violation)
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            addAction(ACTION_SIM_STATE_CHANGED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(ACTION_TETHER_STATE_CHANGED)
            addAction(ACTION_USB_STATE)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        }

        context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    fun startCollecting() {
        applicationScope.launch {
            broadcastFlow().collect { violation ->
                mutableStatus.value = AirGapStatus.Compromised(violation)
                mutableViolations.emit(violation)
            }
        }
        applicationScope.launch {
            networkMonitor.allNetworkFlow().collect { violation ->
                mutableStatus.value = AirGapStatus.Compromised(violation)
                mutableViolations.emit(violation)
            }
        }
        applicationScope.launch {
            networkMonitor.vpnNetworkFlow().collect { violation ->
                mutableStatus.value = AirGapStatus.Compromised(violation)
                mutableViolations.emit(violation)
            }
        }
    }

    private fun buildAugmentedBooleanExtras(receiverContext: Context, intent: Intent): Map<String, Boolean> {
        val extras = mutableMapOf<String, Boolean>()
        if (intent.hasExtra("state")) extras["state"] = intent.getBooleanExtra("state", false)
        if (intent.hasExtra("connected")) extras["connected"] = intent.getBooleanExtra("connected", false)
        if (intent.action == WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED) {
            val wifiAwareManager = receiverContext.getSystemService(Context.WIFI_AWARE_SERVICE) as? WifiAwareManager
            extras["wifi_aware_available"] = wifiAwareManager?.isAvailable ?: false
        }
        return extras
    }

    private fun buildAugmentedIntExtras(receiverContext: Context, intent: Intent): Map<String, Int> {
        val extras = mutableMapOf<String, Int>()
        val intKeys = listOf(
            BluetoothAdapter.EXTRA_STATE,
            NfcAdapter.EXTRA_ADAPTER_STATE,
            WifiManager.EXTRA_WIFI_STATE,
            WifiP2pManager.EXTRA_WIFI_STATE,
        )
        intKeys.forEach { key ->
            if (intent.hasExtra(key)) extras[key] = intent.getIntExtra(key, -1)
        }
        if (intent.action == Intent.ACTION_POWER_CONNECTED) {
            val batteryIntent = receiverContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            if (plugged >= 0) extras["plugged"] = plugged
        }
        return extras
    }

    private fun extractStringExtras(intent: Intent): Map<String, String?> {
        val extras = mutableMapOf<String, String?>()
        if (intent.hasExtra("ss")) extras["ss"] = intent.getStringExtra("ss")
        return extras
    }

    @Suppress("DEPRECATION")
    private fun extractStringArrayExtras(intent: Intent): Map<String, List<String>> {
        val extras = mutableMapOf<String, List<String>>()
        val tetherArray = intent.getStringArrayExtra("tetherArray")
        if (tetherArray != null) extras["tetherArray"] = tetherArray.toList()
        return extras
    }
}
