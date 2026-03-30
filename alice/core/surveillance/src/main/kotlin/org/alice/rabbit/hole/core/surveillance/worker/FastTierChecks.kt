package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider

object FastTierChecks {

    internal const val SETTING_AIRPLANE_MODE = "airplane_mode_on"
    internal const val SETTING_WIFI = "wifi_on"
    internal const val SETTING_BLUETOOTH = "bluetooth_on"
    internal const val SETTING_NFC = "nfc_on"
    internal const val SETTING_BLUETOOTH_LOW_ENERGY_SCAN = "ble_scan_always_enabled"
    internal const val SETTING_ADB = "adb_enabled"
    internal const val SETTING_ADB_WIRELESS = "adb_wifi_enabled"
    internal const val SETTING_DEVELOPER_OPTIONS = "development_settings_enabled"

    internal const val SETTING_ENABLED = 1
    internal const val SETTING_DISABLED = 0

    private const val SIM_STATE_ABSENT = 1

    fun execute(settingsProvider: ISettingsProvider, adapterStateProvider: IAdapterStateProvider): List<AirGapViolation> {
        val violations = mutableListOf<AirGapViolation>()

        if (settingsProvider.readGlobalInt(SETTING_AIRPLANE_MODE) == SETTING_DISABLED) violations.add(AirGapViolation.AirplaneModeDisabled)
        if (settingsProvider.readGlobalInt(SETTING_WIFI) == SETTING_ENABLED) violations.add(AirGapViolation.WifiEnabled)
        if (settingsProvider.readGlobalInt(SETTING_BLUETOOTH) == SETTING_ENABLED) violations.add(AirGapViolation.BluetoothEnabled)
        if (settingsProvider.readGlobalInt(SETTING_NFC) == SETTING_ENABLED) violations.add(AirGapViolation.NfcEnabled)
        if (settingsProvider.readGlobalInt(SETTING_BLUETOOTH_LOW_ENERGY_SCAN) == SETTING_ENABLED) violations.add(AirGapViolation.BluetoothBackgroundScanEnabled)
        if (settingsProvider.readGlobalInt(SETTING_ADB) == SETTING_ENABLED) violations.add(AirGapViolation.AdbEnabled)
        if (settingsProvider.readGlobalInt(SETTING_ADB_WIRELESS) == SETTING_ENABLED) violations.add(AirGapViolation.AdbWirelessEnabled)
        if (settingsProvider.readGlobalInt(SETTING_DEVELOPER_OPTIONS) == SETTING_ENABLED) violations.add(AirGapViolation.DeveloperOptionsEnabled)

        if (adapterStateProvider.isBluetoothLowEnergyEnabled()) violations.add(AirGapViolation.BluetoothLowEnergyEnabled)
        if (adapterStateProvider.isNfcEnabled()) violations.add(AirGapViolation.NfcEnabled)
        if (adapterStateProvider.isWifiBackgroundScanEnabled()) violations.add(AirGapViolation.WifiBackgroundScanEnabled)
        if (adapterStateProvider.isWifiAwareAvailable()) violations.add(AirGapViolation.WifiAwareEnabled)
        if (adapterStateProvider.isLocationEnabled()) violations.add(AirGapViolation.LocationEnabled)
        if (adapterStateProvider.isAccessibilityEnabled()) violations.add(AirGapViolation.AccessibilityServiceActive)
        if ((adapterStateProvider.simState() == SIM_STATE_ABSENT).not()) violations.add(AirGapViolation.SimPresent)

        return violations
    }
}
