package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider

object FastTierChecks {

    private const val SIM_STATE_ABSENT = 1

    fun execute(settingsProvider: ISettingsProvider, adapterStateProvider: IAdapterStateProvider): List<AirGapViolation> {
        val violations = mutableListOf<AirGapViolation>()

        if (settingsProvider.readGlobalInt("airplane_mode_on") == 0) violations.add(AirGapViolation.AirplaneModeDisabled)
        if (settingsProvider.readGlobalInt("wifi_on") == 1) violations.add(AirGapViolation.WifiEnabled)
        if (settingsProvider.readGlobalInt("bluetooth_on") == 1) violations.add(AirGapViolation.BluetoothEnabled)
        if (settingsProvider.readGlobalInt("nfc_on") == 1) violations.add(AirGapViolation.NfcEnabled)
        if (settingsProvider.readGlobalInt("ble_scan_always_enabled") == 1) violations.add(AirGapViolation.BluetoothBackgroundScanEnabled)
        if (settingsProvider.readGlobalInt("adb_enabled") == 1) violations.add(AirGapViolation.AdbEnabled)
        if (settingsProvider.readGlobalInt("adb_wifi_enabled") == 1) violations.add(AirGapViolation.AdbWirelessEnabled)
        if (settingsProvider.readGlobalInt("development_settings_enabled") == 1) violations.add(AirGapViolation.DeveloperOptionsEnabled)

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
