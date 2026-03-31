package org.alice.rabbit.hole.core.surveillance

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation

object IntentToViolationMapper {

    private const val BLUETOOTH_STATE_TURNING_ON = 11
    private const val BLUETOOTH_STATE_ON = 12
    private const val BLUETOOTH_STATE_BLE_TURNING_ON = 14
    private const val BLUETOOTH_STATE_BLE_ON = 15

    private const val NFC_STATE_TURNING_ON = 2
    private const val NFC_STATE_ON = 3

    private const val WIFI_STATE_ENABLING = 2
    private const val WIFI_STATE_ENABLED = 3

    private const val WIFI_P2P_STATE_ENABLED = 2

    private const val ACTION_AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE"
    private const val ACTION_BLUETOOTH_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED"
    private const val ACTION_NFC_STATE_CHANGED = "android.nfc.action.ADAPTER_STATE_CHANGED"
    private const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    private const val ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED"
    private const val ACTION_WIFI_P2P_STATE_CHANGED = "android.net.wifi.p2p.STATE_CHANGED"
    private const val ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED"
    private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
    private const val ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED"
    private const val ACTION_WIFI_AWARE_STATE_CHANGED = "android.net.wifi.aware.action.WIFI_AWARE_STATE_CHANGED"

    private const val EXTRA_BLUETOOTH_STATE = "android.bluetooth.adapter.extra.STATE"
    private const val EXTRA_NFC_STATE = "android.nfc.extra.ADAPTER_STATE"
    private const val EXTRA_WIFI_STATE = "wifi_state"
    private const val EXTRA_WIFI_P2P_STATE = "wifi_p2p_state"
    private const val EXTRA_SIM_STATE = "ss"
    private const val EXTRA_TETHER_ARRAY = "tetherArray"
    private const val EXTRA_USB_CONNECTED = "connected"
    private const val EXTRA_PLUGGED = "plugged"
    private const val EXTRA_WIFI_AWARE_AVAILABLE = "wifi_aware_available"
    private const val BATTERY_PLUGGED_USB = 2
    private const val SIM_STATE_VALUE_ABSENT = "ABSENT"

    fun map(data: BroadcastData): AirGapViolation? {
        return when (data.action) {
            ACTION_AIRPLANE_MODE -> mapAirplaneMode(data)
            ACTION_BLUETOOTH_STATE_CHANGED -> mapBluetooth(data)
            ACTION_NFC_STATE_CHANGED -> mapNfc(data)
            ACTION_SIM_STATE_CHANGED -> mapSim(data)
            ACTION_WIFI_STATE_CHANGED -> mapWifi(data)
            ACTION_WIFI_P2P_STATE_CHANGED -> mapWifiDirect(data)
            ACTION_TETHER_STATE_CHANGED -> mapTethering(data)
            ACTION_USB_STATE -> mapUsb(data)
            ACTION_POWER_CONNECTED -> mapPowerConnected(data)
            ACTION_WIFI_AWARE_STATE_CHANGED -> mapWifiAware(data)
            else -> null
        }
    }

    private fun mapAirplaneMode(data: BroadcastData): AirGapViolation? {
        val enabled = data.booleanExtras["state"] ?: return AirGapViolation.AirplaneModeDisabled
        return if (enabled.not()) AirGapViolation.AirplaneModeDisabled else null
    }

    private fun mapBluetooth(data: BroadcastData): AirGapViolation? {
        return when (data.intExtras[EXTRA_BLUETOOTH_STATE]) {
            BLUETOOTH_STATE_ON, BLUETOOTH_STATE_TURNING_ON -> AirGapViolation.BluetoothEnabled
            BLUETOOTH_STATE_BLE_ON, BLUETOOTH_STATE_BLE_TURNING_ON -> AirGapViolation.BluetoothLowEnergyEnabled
            else -> null
        }
    }

    private fun mapNfc(data: BroadcastData): AirGapViolation? {
        return when (data.intExtras[EXTRA_NFC_STATE]) {
            NFC_STATE_ON, NFC_STATE_TURNING_ON -> AirGapViolation.NfcEnabled
            else -> null
        }
    }

    private fun mapSim(data: BroadcastData): AirGapViolation? {
        val state = data.stringExtras[EXTRA_SIM_STATE]
        return if (state == SIM_STATE_VALUE_ABSENT) null else AirGapViolation.SimPresent
    }

    private fun mapWifi(data: BroadcastData): AirGapViolation? {
        return when (data.intExtras[EXTRA_WIFI_STATE]) {
            WIFI_STATE_ENABLED, WIFI_STATE_ENABLING -> AirGapViolation.WifiEnabled
            else -> null
        }
    }

    private fun mapWifiDirect(data: BroadcastData): AirGapViolation? {
        return when (data.intExtras[EXTRA_WIFI_P2P_STATE]) {
            WIFI_P2P_STATE_ENABLED -> AirGapViolation.WifiDirectEnabled
            else -> null
        }
    }

    private fun mapTethering(data: BroadcastData): AirGapViolation? {
        val tetheredInterfaces = data.stringArrayExtras[EXTRA_TETHER_ARRAY]
        return if (tetheredInterfaces.isNullOrEmpty().not()) AirGapViolation.TetheringActive else null
    }

    private fun mapUsb(data: BroadcastData): AirGapViolation? {
        val connected = data.booleanExtras[EXTRA_USB_CONNECTED] ?: return null
        return if (connected) AirGapViolation.UsbPowerConnected else null
    }

    private fun mapPowerConnected(data: BroadcastData): AirGapViolation? {
        val pluggedType = data.intExtras[EXTRA_PLUGGED] ?: return null
        return if (pluggedType == BATTERY_PLUGGED_USB) AirGapViolation.UsbPowerConnected else null
    }

    private fun mapWifiAware(data: BroadcastData): AirGapViolation? {
        val available = data.booleanExtras[EXTRA_WIFI_AWARE_AVAILABLE] ?: return null
        return if (available) AirGapViolation.WifiAwareEnabled else null
    }
}
