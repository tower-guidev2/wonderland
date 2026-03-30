package org.alice.rabbit.hole.core.surveillance.worker

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.junit.Test

class StandardTierChecksTest {

    @Test
    fun secureDeviceProducesNoViolations() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider())).isEmpty()
    }

    @Test
    fun activeNetworkProducesViolation() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider(activeNetwork = true))).contains(AirGapViolation.NetworkInterfaceActive)
    }

    @Test
    fun vpnProducesViolation() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider(vpnNetwork = true))).contains(AirGapViolation.VpnActive)
    }

    @Test
    fun tetheringProducesViolation() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider(tetheredInterfaces = true))).contains(AirGapViolation.TetheringActive)
    }

    @Test
    fun multipleDisplaysProducesViolation() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider(displayCount = 2))).contains(AirGapViolation.DisplayMirroringActive)
    }

    @Test
    fun singleDisplayProducesNoViolation() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider(displayCount = 1)).filterIsInstance<AirGapViolation.DisplayMirroringActive>()).isEmpty()
    }

    @Test
    fun oemUnlockProducesViolation() {
        assertThat(StandardTierChecks.execute(FakeNetworkStateProvider(oemUnlockEnabled = true))).contains(AirGapViolation.OemUnlockEnabled)
    }
}
