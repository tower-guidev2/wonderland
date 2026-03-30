package org.alice.rabbit.hole.core.surveillance.provider

interface IAdapterStateProvider {
    fun isBluetoothEnabled(): Boolean
    fun isBluetoothLowEnergyEnabled(): Boolean
    fun isNfcEnabled(): Boolean
    fun isWifiBackgroundScanEnabled(): Boolean
    fun isWifiAwareAvailable(): Boolean
    fun isLocationEnabled(): Boolean
    fun isAccessibilityEnabled(): Boolean
    fun simState(): Int
}
