@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection.integrity

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.concurrent.CancellationException

object DeviceIntegrityVerifier {

    private const val EXPECTED_MANUFACTURER = "Google"
    private const val EXPECTED_BRAND = "google"

    private val KNOWN_PIXEL_CODENAMES = setOf(
        "oriole", "raven", "bluejay",
        "panther", "cheetah", "lynx", "tangorpro", "felix",
        "shiba", "husky", "akita",
        "tokay", "caiman", "komodo", "comet",
        "tegu", "frankel", "blazer", "mustang", "rango", "stallion",
    )

    private const val ATTESTATION_EXTENSION_OID = "1.3.6.1.4.1.11129.2.1.17"
    private const val STRONGBOX_SECURITY_LEVEL = 2
    private const val VERIFIED_BOOT_SELF_SIGNED = 1
    private const val ATTESTATION_CHALLENGE_SIZE = 32
    private const val ELLIPTIC_CURVE_NAME = "secp256r1"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val ATTESTATION_KEY_ALIAS_PREFIX = "airgap_attestation_"

    fun validate(): List<CheckResult> = listOf(
        checkDeviceIntegrity(),
        checkAttestation(),
    )

    private fun checkDeviceIntegrity(): CheckResult {
        val failures = mutableListOf<String>()

        if (Build.MANUFACTURER != EXPECTED_MANUFACTURER)
            failures.add("manufacturer: ${Build.MANUFACTURER}")
        if (Build.BRAND != EXPECTED_BRAND)
            failures.add("brand: ${Build.BRAND}")
        if (KNOWN_PIXEL_CODENAMES.contains(Build.DEVICE).not())
            failures.add("device: ${Build.DEVICE}")

        return if (failures.isEmpty())
            CheckResult(SurfaceName.DEVICE_INTEGRITY, Either.Right(SafeDetail("Google Pixel (${Build.DEVICE}), SDK ${Build.VERSION.SDK_INT}")))
        else
            CheckResult(SurfaceName.DEVICE_INTEGRITY, Either.Left(ViolationDetail(failures.joinToString(", "))))
    }

    private fun checkAttestation(): CheckResult =
        Either.catch {
            performStrongBoxAttestation()
        }.fold(
            ifLeft = { throwable ->
                if (throwable is CancellationException)
                    throw throwable
                CheckResult(SurfaceName.ATTESTATION, Either.Left(ViolationDetail("Attestation failed: ${throwable.message}")))
            },
            ifRight = { result -> result },
        )

    private fun performStrongBoxAttestation(): CheckResult {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        val alias = "$ATTESTATION_KEY_ALIAS_PREFIX${System.nanoTime()}"
        val challenge = SecureRandom().let { random ->
            ByteArray(ATTESTATION_CHALLENGE_SIZE).also { random.nextBytes(it) }
        }

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            KEYSTORE_PROVIDER,
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN,
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(ELLIPTIC_CURVE_NAME))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setAttestationChallenge(challenge)
            .setIsStrongBoxBacked(true)
            .build()

        keyPairGenerator.initialize(parameterSpec)
        keyPairGenerator.generateKeyPair()

        val certificateChain = keyStore.getCertificateChain(alias)
        if (certificateChain == null || certificateChain.isEmpty()) {
            keyStore.deleteEntry(alias)
            return CheckResult(SurfaceName.ATTESTATION, Either.Left(ViolationDetail("No certificate chain")))
        }

        val attestationCertificate = certificateChain[0] as X509Certificate
        val attestationExtension = attestationCertificate.getExtensionValue(ATTESTATION_EXTENSION_OID)
        if (attestationExtension == null) {
            keyStore.deleteEntry(alias)
            return CheckResult(SurfaceName.ATTESTATION, Either.Left(ViolationDetail("No attestation extension")))
        }

        val failures = mutableListOf<String>()
        val extensionData = parseAttestationExtension(attestationExtension, challenge)

        if (extensionData.isStrongBox.not())
            failures.add("not StrongBox-backed")
        if (extensionData.isDeviceLocked.not())
            failures.add("bootloader unlocked")
        if (extensionData.isVerifiedBootSelfSigned.not())
            failures.add("not self-signed boot (not GrapheneOS)")
        if (extensionData.isChallengeValid.not())
            failures.add("challenge mismatch")

        keyStore.deleteEntry(alias)

        return if (failures.isEmpty())
            CheckResult(SurfaceName.ATTESTATION, Either.Right(SafeDetail("StrongBox verified, GrapheneOS confirmed")))
        else
            CheckResult(SurfaceName.ATTESTATION, Either.Left(ViolationDetail(failures.joinToString(", "))))
    }

    private fun parseAttestationExtension(
        extensionBytes: ByteArray,
        expectedChallenge: ByteArray,
    ): AttestationResult {
        val octetString = Asn1Parser.parseOctetString(extensionBytes)
        val sequence = Asn1Parser.parseSequence(octetString)
        return AttestationResult(
            isStrongBox = sequence.attestationSecurityLevel == STRONGBOX_SECURITY_LEVEL,
            isDeviceLocked = sequence.isDeviceLocked,
            isVerifiedBootSelfSigned = sequence.verifiedBootState == VERIFIED_BOOT_SELF_SIGNED,
            isChallengeValid = sequence.attestationChallenge.contentEquals(expectedChallenge),
        )
    }

    private class AttestationResult(
        val isStrongBox: Boolean,
        val isDeviceLocked: Boolean,
        val isVerifiedBootSelfSigned: Boolean,
        val isChallengeValid: Boolean,
    )
}
