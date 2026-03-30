package org.alice.rabbit.hole.core.surveillance.api

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeAirGapSurveillanceTest {

    @Test
    fun initialStatusIsSecure() = runTest {
        val surveillance = FakeAirGapSurveillance()
        assertThat(surveillance.status.value).isEqualTo(AirGapStatus.Secure)
    }

    @Test
    fun emitViolationUpdatesStatusToCompromised() = runTest {
        val surveillance = FakeAirGapSurveillance()
        surveillance.emitViolation(AirGapViolation.WifiEnabled)

        assertThat(surveillance.status.value).isInstanceOf<AirGapStatus.Compromised>()
        val compromised = surveillance.status.value as AirGapStatus.Compromised
        assertThat(compromised.violation).isEqualTo(AirGapViolation.WifiEnabled)
    }

    @Test
    fun violationsFlowEmitsViolation() = runTest {
        val surveillance = FakeAirGapSurveillance()

        surveillance.violations.test {
            surveillance.emitViolation(AirGapViolation.BluetoothEnabled)
            assertThat(awaitItem()).isEqualTo(AirGapViolation.BluetoothEnabled)
        }
    }

    @Test
    fun resetRestoresSecureStatus() = runTest {
        val surveillance = FakeAirGapSurveillance()
        surveillance.emitViolation(AirGapViolation.NfcEnabled)
        surveillance.reset()
        assertThat(surveillance.status.value).isEqualTo(AirGapStatus.Secure)
    }

    @Test
    fun statusReflectsLatestViolation() = runTest {
        val surveillance = FakeAirGapSurveillance()
        surveillance.emitViolation(AirGapViolation.SimPresent)
        surveillance.emitViolation(AirGapViolation.VpnActive)

        val compromised = surveillance.status.value as AirGapStatus.Compromised
        assertThat(compromised.violation).isEqualTo(AirGapViolation.VpnActive)
    }
}
