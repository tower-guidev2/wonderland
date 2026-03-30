package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.junit.Test

class FastTierChecksTest {

    private val secureSettings = mapOf(
        "airplane_mode_on" to 1, "wifi_on" to 0, "bluetooth_on" to 0,
        "nfc_on" to 0, "ble_scan_always_enabled" to 0, "adb_enabled" to 0,
        "adb_wifi_enabled" to 0, "development_settings_enabled" to 0,
    )

    @Test
    fun secureDeviceProducesNoViolations() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider())
        assertThat(violations).isEmpty()
    }

    @Test
    fun airplaneModeOffProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("airplane_mode_on" to 0))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.AirplaneModeDisabled)
    }

    @Test
    fun wifiOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("wifi_on" to 1))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.WifiEnabled)
    }

    @Test
    fun bluetoothOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("bluetooth_on" to 1))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.BluetoothEnabled)
    }

    @Test
    fun nfcOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("nfc_on" to 1))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.NfcEnabled)
    }

    @Test
    fun bleBackgroundScanProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("ble_scan_always_enabled" to 1))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.BluetoothBackgroundScanEnabled)
    }

    @Test
    fun adbEnabledProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("adb_enabled" to 1))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.AdbEnabled)
    }

    @Test
    fun developerOptionsProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + ("development_settings_enabled" to 1))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.DeveloperOptionsEnabled)
    }

    @Test
    fun locationEnabledViaAdapterProducesViolation() {
        assertThat(FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider(locationEnabled = true))).contains(AirGapViolation.LocationEnabled)
    }

    @Test
    fun simReadyProducesViolation() {
        assertThat(FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider(simState = 5))).contains(AirGapViolation.SimPresent)
    }

    @Test
    fun simAbsentProducesNoViolation() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider(simState = 1))
        assertThat(violations.filterIsInstance<AirGapViolation.SimPresent>()).isEmpty()
    }

    @Test
    fun multipleViolationsReturnedTogether() {
        val settings = FakeSettingsProvider(secureSettings + ("airplane_mode_on" to 0) + ("wifi_on" to 1))
        val violations = FastTierChecks.execute(settings, FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.AirplaneModeDisabled)
        assertThat(violations).contains(AirGapViolation.WifiEnabled)
    }
}
