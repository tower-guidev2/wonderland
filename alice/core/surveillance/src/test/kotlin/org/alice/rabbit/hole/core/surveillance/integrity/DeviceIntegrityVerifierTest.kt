package org.alice.rabbit.hole.core.surveillance.integrity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider
import org.junit.Test

class DeviceIntegrityVerifierTest {

    @Test
    fun validPixelDeviceProducesNoViolation() {
        val provider = FakeBuildPropertyProvider(manufacturer = "Google", brand = "google", device = "husky", sdkVersion = 34)
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isNull()
    }

    @Test
    fun wrongManufacturerProducesViolation() {
        val provider = FakeBuildPropertyProvider(manufacturer = "Samsung", brand = "google", device = "husky", sdkVersion = 34)
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isEqualTo(AirGapViolation.DeviceIntegrityFailed(reason = "manufacturer is not Google"))
    }

    @Test
    fun wrongBrandProducesViolation() {
        val provider = FakeBuildPropertyProvider(manufacturer = "Google", brand = "samsung", device = "husky", sdkVersion = 34)
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isEqualTo(AirGapViolation.DeviceIntegrityFailed(reason = "brand is not google"))
    }

    @Test
    fun unknownDeviceCodenameProducesViolation() {
        val provider = FakeBuildPropertyProvider(
            manufacturer = "Google",
            brand = "google",
            device = "unknown_device",
            sdkVersion = 34,
        )
        val expected = AirGapViolation.DeviceIntegrityFailed(reason = "device codename unknown_device is not a known Pixel")
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isEqualTo(expected)
    }

    @Test
    fun sdkVersionBelowThirtyThreeProducesViolation() {
        val provider = FakeBuildPropertyProvider(manufacturer = "Google", brand = "google", device = "husky", sdkVersion = 32)
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isEqualTo(AirGapViolation.DeviceIntegrityFailed(reason = "SDK version 32 is below minimum 33"))
    }

    @Test
    fun pixel6ProCodenamePasses() {
        val provider = FakeBuildPropertyProvider(device = "raven", sdkVersion = 33)
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isNull()
    }

    @Test
    fun pixel9ProCodenamePasses() {
        val provider = FakeBuildPropertyProvider(device = "caiman", sdkVersion = 35)
        assertThat(DeviceIntegrityVerifier.verifyBuildProperties(provider)).isNull()
    }
}

class FakeBuildPropertyProvider(
    private val manufacturer: String = "Google",
    private val brand: String = "google",
    private val device: String = "husky",
    private val sdkVersion: Int = 34,
) : IBuildPropertyProvider {
    override fun manufacturer(): String = manufacturer
    override fun brand(): String = brand
    override fun device(): String = device
    override fun sdkVersion(): Int = sdkVersion
}
