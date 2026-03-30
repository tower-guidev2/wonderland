package org.alice.rabbit.hole.core.surveillance.api

sealed class AirGapViolation(val severity: Severity) {

    enum class Severity { HARD, SOFT }

    data object AirplaneModeDisabled : AirGapViolation(Severity.HARD)
    data object BluetoothEnabled : AirGapViolation(Severity.HARD)
    data object BluetoothLowEnergyEnabled : AirGapViolation(Severity.HARD)
    data object NfcEnabled : AirGapViolation(Severity.HARD)
    data object SimPresent : AirGapViolation(Severity.HARD)
    data object WifiEnabled : AirGapViolation(Severity.HARD)
    data object WifiDirectEnabled : AirGapViolation(Severity.HARD)
    data object WifiAwareEnabled : AirGapViolation(Severity.HARD)
    data object NetworkInterfaceActive : AirGapViolation(Severity.HARD)
    data object VpnActive : AirGapViolation(Severity.HARD)
    data object TetheringActive : AirGapViolation(Severity.HARD)
    data object WifiBackgroundScanEnabled : AirGapViolation(Severity.HARD)
    data object BluetoothBackgroundScanEnabled : AirGapViolation(Severity.HARD)
    data object LocationEnabled : AirGapViolation(Severity.HARD)
    data object DeveloperOptionsEnabled : AirGapViolation(Severity.HARD)
    data object AdbEnabled : AirGapViolation(Severity.HARD)
    data object AdbWirelessEnabled : AirGapViolation(Severity.HARD)
    data object AccessibilityServiceActive : AirGapViolation(Severity.HARD)
    data object DisplayMirroringActive : AirGapViolation(Severity.HARD)
    data object OemUnlockEnabled : AirGapViolation(Severity.HARD)
    data class DeviceIntegrityFailed(val reason: String) : AirGapViolation(Severity.HARD)
    data class AttestationFailed(val failedChecks: List<String>) : AirGapViolation(Severity.HARD)
    data object UsbPowerConnected : AirGapViolation(Severity.SOFT)
}
