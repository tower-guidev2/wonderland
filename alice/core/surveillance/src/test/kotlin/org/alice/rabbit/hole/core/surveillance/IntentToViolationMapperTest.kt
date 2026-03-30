package org.alice.rabbit.hole.core.surveillance

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.junit.Test

class IntentToViolationMapperTest {

    @Test
    fun airplaneModeDisabledProducesViolation() {
        val data = BroadcastData(action = "android.intent.action.AIRPLANE_MODE", booleanExtras = mapOf("state" to false))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.AirplaneModeDisabled)
    }

    @Test
    fun airplaneModeEnabledProducesNull() {
        val data = BroadcastData(action = "android.intent.action.AIRPLANE_MODE", booleanExtras = mapOf("state" to true))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun bluetoothStateOnProducesViolation() {
        val data = BroadcastData(action = "android.bluetooth.adapter.action.STATE_CHANGED", intExtras = mapOf("android.bluetooth.adapter.extra.STATE" to 12))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.BluetoothEnabled)
    }

    @Test
    fun bluetoothStateTurningOnProducesViolation() {
        val data = BroadcastData(action = "android.bluetooth.adapter.action.STATE_CHANGED", intExtras = mapOf("android.bluetooth.adapter.extra.STATE" to 11))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.BluetoothEnabled)
    }

    @Test
    fun bluetoothBleOnProducesViolation() {
        val data = BroadcastData(action = "android.bluetooth.adapter.action.STATE_CHANGED", intExtras = mapOf("android.bluetooth.adapter.extra.STATE" to 15))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.BluetoothLowEnergyEnabled)
    }

    @Test
    fun bluetoothBleTurningOnProducesViolation() {
        val data = BroadcastData(action = "android.bluetooth.adapter.action.STATE_CHANGED", intExtras = mapOf("android.bluetooth.adapter.extra.STATE" to 14))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.BluetoothLowEnergyEnabled)
    }

    @Test
    fun bluetoothStateOffProducesNull() {
        val data = BroadcastData(action = "android.bluetooth.adapter.action.STATE_CHANGED", intExtras = mapOf("android.bluetooth.adapter.extra.STATE" to 10))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun nfcStateOnProducesViolation() {
        val data = BroadcastData(action = "android.nfc.action.ADAPTER_STATE_CHANGED", intExtras = mapOf("android.nfc.extra.ADAPTER_STATE" to 3))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.NfcEnabled)
    }

    @Test
    fun nfcStateTurningOnProducesViolation() {
        val data = BroadcastData(action = "android.nfc.action.ADAPTER_STATE_CHANGED", intExtras = mapOf("android.nfc.extra.ADAPTER_STATE" to 2))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.NfcEnabled)
    }

    @Test
    fun nfcStateOffProducesNull() {
        val data = BroadcastData(action = "android.nfc.action.ADAPTER_STATE_CHANGED", intExtras = mapOf("android.nfc.extra.ADAPTER_STATE" to 1))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun simReadyProducesViolation() {
        val data = BroadcastData(action = "android.intent.action.SIM_STATE_CHANGED", stringExtras = mapOf("ss" to "READY"))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.SimPresent)
    }

    @Test
    fun simAbsentProducesNull() {
        val data = BroadcastData(action = "android.intent.action.SIM_STATE_CHANGED", stringExtras = mapOf("ss" to "ABSENT"))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun simNullExtraProducesViolation() {
        val data = BroadcastData(action = "android.intent.action.SIM_STATE_CHANGED", stringExtras = mapOf("ss" to null))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.SimPresent)
    }

    @Test
    fun wifiEnabledProducesViolation() {
        val data = BroadcastData(action = "android.net.wifi.WIFI_STATE_CHANGED", intExtras = mapOf("wifi_state" to 3))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.WifiEnabled)
    }

    @Test
    fun wifiEnablingProducesViolation() {
        val data = BroadcastData(action = "android.net.wifi.WIFI_STATE_CHANGED", intExtras = mapOf("wifi_state" to 2))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.WifiEnabled)
    }

    @Test
    fun wifiDisabledProducesNull() {
        val data = BroadcastData(action = "android.net.wifi.WIFI_STATE_CHANGED", intExtras = mapOf("wifi_state" to 1))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun wifiDirectEnabledProducesViolation() {
        val data = BroadcastData(action = "android.net.wifi.p2p.STATE_CHANGED", intExtras = mapOf("wifi_p2p_state" to 2))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.WifiDirectEnabled)
    }

    @Test
    fun wifiDirectDisabledProducesNull() {
        val data = BroadcastData(action = "android.net.wifi.p2p.STATE_CHANGED", intExtras = mapOf("wifi_p2p_state" to 1))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun tetheringActiveProducesViolation() {
        val data = BroadcastData(action = "android.net.conn.TETHER_STATE_CHANGED", stringArrayExtras = mapOf("tetherArray" to arrayOf("wlan0")))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.TetheringActive)
    }

    @Test
    fun tetheringEmptyProducesNull() {
        val data = BroadcastData(action = "android.net.conn.TETHER_STATE_CHANGED", stringArrayExtras = mapOf("tetherArray" to emptyArray()))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun usbConnectedProducesViolation() {
        val data = BroadcastData(action = "android.hardware.usb.action.USB_STATE", booleanExtras = mapOf("connected" to true))
        assertThat(IntentToViolationMapper.map(data)).isEqualTo(AirGapViolation.UsbPowerConnected)
    }

    @Test
    fun usbDisconnectedProducesNull() {
        val data = BroadcastData(action = "android.hardware.usb.action.USB_STATE", booleanExtras = mapOf("connected" to false))
        assertThat(IntentToViolationMapper.map(data)).isNull()
    }

    @Test
    fun nullActionProducesNull() {
        assertThat(IntentToViolationMapper.map(BroadcastData(action = null))).isNull()
    }

    @Test
    fun unknownActionProducesNull() {
        assertThat(IntentToViolationMapper.map(BroadcastData(action = "com.example.UNKNOWN"))).isNull()
    }
}
