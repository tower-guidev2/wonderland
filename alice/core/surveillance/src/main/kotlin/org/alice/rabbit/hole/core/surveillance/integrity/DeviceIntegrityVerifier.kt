package org.alice.rabbit.hole.core.surveillance.integrity

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider

object DeviceIntegrityVerifier {

    private const val EXPECTED_MANUFACTURER = "Google"
    private const val EXPECTED_BRAND = "google"
    private const val MINIMUM_SDK_VERSION = 33

    // Source: https://grapheneos.org/articles/attestation-compatibility-guide
    private val knownPixelCodenames = setOf(
        "oriole", "raven", "bluejay",
        "panther", "cheetah", "lynx",
        "tangorpro", "felix",
        "shiba", "husky", "akita",
        "tokay",
        "caiman", "komodo", "comet",
    )

    fun verifyBuildProperties(provider: IBuildPropertyProvider): AirGapViolation? {
        if (provider.manufacturer() != EXPECTED_MANUFACTURER) {
            return AirGapViolation.DeviceIntegrityFailed(reason = "manufacturer is not $EXPECTED_MANUFACTURER")
        }
        if (provider.brand() != EXPECTED_BRAND) {
            return AirGapViolation.DeviceIntegrityFailed(reason = "brand is not $EXPECTED_BRAND")
        }
        if (provider.device() !in knownPixelCodenames) {
            return AirGapViolation.DeviceIntegrityFailed(reason = "device codename ${provider.device()} is not a known Pixel")
        }
        if (provider.sdkVersion() < MINIMUM_SDK_VERSION) {
            return AirGapViolation.DeviceIntegrityFailed(reason = "SDK version ${provider.sdkVersion()} is below minimum $MINIMUM_SDK_VERSION")
        }
        return null
    }
}
