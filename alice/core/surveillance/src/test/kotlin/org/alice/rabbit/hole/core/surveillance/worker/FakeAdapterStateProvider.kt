package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider

class FakeAdapterStateProvider(
    private val bluetoothEnabled: Boolean = false,
    private val bluetoothLowEnergyEnabled: Boolean = false,
    private val nfcEnabled: Boolean = false,
    private val wifiBackgroundScanEnabled: Boolean = false,
    private val wifiAwareAvailable: Boolean = false,
    private val locationEnabled: Boolean = false,
    private val accessibilityEnabled: Boolean = false,
    private val simState: Int = 1,
) : IAdapterStateProvider {
    override fun isBluetoothEnabled(): Boolean = bluetoothEnabled
    override fun isBluetoothLowEnergyEnabled(): Boolean = bluetoothLowEnergyEnabled
    override fun isNfcEnabled(): Boolean = nfcEnabled
    override fun isWifiBackgroundScanEnabled(): Boolean = wifiBackgroundScanEnabled
    override fun isWifiAwareAvailable(): Boolean = wifiAwareAvailable
    override fun isLocationEnabled(): Boolean = locationEnabled
    override fun isAccessibilityEnabled(): Boolean = accessibilityEnabled
    override fun simState(): Int = simState
}
