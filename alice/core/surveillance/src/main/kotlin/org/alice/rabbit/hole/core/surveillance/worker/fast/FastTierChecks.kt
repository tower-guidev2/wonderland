package org.alice.rabbit.hole.core.surveillance.worker.fast

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
        return checkSettings(settingsProvider) + checkAdapters(adapterStateProvider)
    }

    private fun checkSettings(settingsProvider: ISettingsProvider): List<AirGapViolation> {
        val disabledMeansViolation = listOf(
            SETTING_AIRPLANE_MODE to AirGapViolation.AirplaneModeDisabled,
        )
        val enabledMeansViolation = listOf(
            SETTING_WIFI to AirGapViolation.WifiEnabled,
            SETTING_BLUETOOTH to AirGapViolation.BluetoothEnabled,
            SETTING_NFC to AirGapViolation.NfcEnabled,
            SETTING_BLUETOOTH_LOW_ENERGY_SCAN to AirGapViolation.BluetoothBackgroundScanEnabled,
            SETTING_ADB to AirGapViolation.AdbEnabled,
            SETTING_ADB_WIRELESS to AirGapViolation.AdbWirelessEnabled,
            SETTING_DEVELOPER_OPTIONS to AirGapViolation.DeveloperOptionsEnabled,
        )

        val violations = mutableListOf<AirGapViolation>()
        disabledMeansViolation.forEach { (setting, violation) ->
            if (settingsProvider.readGlobalInt(setting) == SETTING_DISABLED) violations.add(violation)
        }
        enabledMeansViolation.forEach { (setting, violation) ->
            if (settingsProvider.readGlobalInt(setting) == SETTING_ENABLED) violations.add(violation)
        }
        return violations
    }

    private fun checkAdapters(adapterStateProvider: IAdapterStateProvider): List<AirGapViolation> {
        val adapterChecks = listOf(
            adapterStateProvider::isBluetoothLowEnergyEnabled to AirGapViolation.BluetoothLowEnergyEnabled,
            adapterStateProvider::isNfcEnabled to AirGapViolation.NfcEnabled,
            adapterStateProvider::isWifiBackgroundScanEnabled to AirGapViolation.WifiBackgroundScanEnabled,
            adapterStateProvider::isWifiAwareAvailable to AirGapViolation.WifiAwareEnabled,
            adapterStateProvider::isLocationEnabled to AirGapViolation.LocationEnabled,
            adapterStateProvider::isAccessibilityEnabled to AirGapViolation.AccessibilityServiceActive,
        )

        val violations = mutableListOf<AirGapViolation>()
        adapterChecks.forEach { (check, violation) ->
            if (check()) violations.add(violation)
        }
        if ((adapterStateProvider.simState() == SIM_STATE_ABSENT).not()) violations.add(AirGapViolation.SimPresent)
        return violations
    }
}
