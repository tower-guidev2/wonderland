package org.alice.rabbit.hole.core.surveillance.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class AirGapViolationTest {

    @Test
    fun airplaneModeDisabledIsHardSeverity() {
        val violation = AirGapViolation.AirplaneModeDisabled
        assertThat(violation.severity).isEqualTo(AirGapViolation.Severity.HARD)
    }

    @Test
    fun usbPowerConnectedIsSoftSeverity() {
        val violation = AirGapViolation.UsbPowerConnected
        assertThat(violation.severity).isEqualTo(AirGapViolation.Severity.SOFT)
    }

    @Test
    fun deviceIntegrityFailedCarriesReason() {
        val violation = AirGapViolation.DeviceIntegrityFailed(reason = "manufacturer mismatch")
        assertThat(violation.reason).isEqualTo("manufacturer mismatch")
    }

    @Test
    fun attestationFailedCarriesFailedChecks() {
        val checks = listOf("bootloader unlocked", "wrong boot key")
        val violation = AirGapViolation.AttestationFailed(failedChecks = checks)
        assertThat(violation.failedChecks).isEqualTo(checks)
    }

    @Test
    fun allHardViolationsCountTwentyTwo() {
        val hardViolations = listOf(
            AirGapViolation.AirplaneModeDisabled,
            AirGapViolation.BluetoothEnabled,
            AirGapViolation.BluetoothLowEnergyEnabled,
            AirGapViolation.NfcEnabled,
            AirGapViolation.SimPresent,
            AirGapViolation.WifiEnabled,
            AirGapViolation.WifiDirectEnabled,
            AirGapViolation.WifiAwareEnabled,
            AirGapViolation.NetworkInterfaceActive,
            AirGapViolation.VpnActive,
            AirGapViolation.TetheringActive,
            AirGapViolation.WifiBackgroundScanEnabled,
            AirGapViolation.BluetoothBackgroundScanEnabled,
            AirGapViolation.LocationEnabled,
            AirGapViolation.DeveloperOptionsEnabled,
            AirGapViolation.AdbEnabled,
            AirGapViolation.AdbWirelessEnabled,
            AirGapViolation.AccessibilityServiceActive,
            AirGapViolation.DisplayMirroringActive,
            AirGapViolation.OemUnlockEnabled,
            AirGapViolation.DeviceIntegrityFailed(reason = "test"),
            AirGapViolation.AttestationFailed(failedChecks = listOf("test")),
        )

        assertThat(hardViolations.size).isEqualTo(22)
        hardViolations.forEach { violation ->
            assertThat(violation.severity).isEqualTo(AirGapViolation.Severity.HARD)
        }
    }
}
