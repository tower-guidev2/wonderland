package org.alice.rabbit.hole.core.surveillance.worker.fast

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.worker.FakeAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.worker.FakeSettingsProvider
import org.junit.Test

class FastTierChecksTest {

    private val secureSettings = mapOf(
        FastTierChecks.SETTING_AIRPLANE_MODE to FastTierChecks.SETTING_ENABLED,
        FastTierChecks.SETTING_WIFI to FastTierChecks.SETTING_DISABLED,
        FastTierChecks.SETTING_BLUETOOTH to FastTierChecks.SETTING_DISABLED,
        FastTierChecks.SETTING_NFC to FastTierChecks.SETTING_DISABLED,
        FastTierChecks.SETTING_BLUETOOTH_LOW_ENERGY_SCAN to FastTierChecks.SETTING_DISABLED,
        FastTierChecks.SETTING_ADB to FastTierChecks.SETTING_DISABLED,
        FastTierChecks.SETTING_ADB_WIRELESS to FastTierChecks.SETTING_DISABLED,
        FastTierChecks.SETTING_DEVELOPER_OPTIONS to FastTierChecks.SETTING_DISABLED,
    )

    @Test
    fun secureDeviceProducesNoViolations() {
        val violations = FastTierChecks.execute(FakeSettingsProvider(secureSettings), FakeAdapterStateProvider())
        assertThat(violations).isEmpty()
    }

    @Test
    fun airplaneModeOffProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_AIRPLANE_MODE to FastTierChecks.SETTING_DISABLED))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.AirplaneModeDisabled)
    }

    @Test
    fun wifiOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_WIFI to FastTierChecks.SETTING_ENABLED))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.WifiEnabled)
    }

    @Test
    fun bluetoothOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_BLUETOOTH to FastTierChecks.SETTING_ENABLED))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.BluetoothEnabled)
    }

    @Test
    fun nfcOnProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_NFC to FastTierChecks.SETTING_ENABLED))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.NfcEnabled)
    }

    @Test
    fun bleBackgroundScanProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_BLUETOOTH_LOW_ENERGY_SCAN to FastTierChecks.SETTING_ENABLED))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.BluetoothBackgroundScanEnabled)
    }

    @Test
    fun adbEnabledProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_ADB to FastTierChecks.SETTING_ENABLED))
        assertThat(FastTierChecks.execute(settings, FakeAdapterStateProvider())).contains(AirGapViolation.AdbEnabled)
    }

    @Test
    fun developerOptionsProducesViolation() {
        val settings = FakeSettingsProvider(secureSettings + (FastTierChecks.SETTING_DEVELOPER_OPTIONS to FastTierChecks.SETTING_ENABLED))
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
        val overrides = secureSettings +
            (FastTierChecks.SETTING_AIRPLANE_MODE to FastTierChecks.SETTING_DISABLED) +
            (FastTierChecks.SETTING_WIFI to FastTierChecks.SETTING_ENABLED)
        val violations = FastTierChecks.execute(FakeSettingsProvider(overrides), FakeAdapterStateProvider())
        assertThat(violations).contains(AirGapViolation.AirplaneModeDisabled)
        assertThat(violations).contains(AirGapViolation.WifiEnabled)
    }
}
