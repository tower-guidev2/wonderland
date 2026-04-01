@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection

import android.accessibilityservice.AccessibilityServiceInfo
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.net.wifi.aware.WifiAwareManager
import android.nfc.NfcAdapter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityManager
import arrow.core.Either
import org.alice.poc.airgap.domain.SensorName
import org.alice.poc.airgap.domain.SensorStatus
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.concurrent.CancellationException

object SensorChecker {

    private const val SETTING_BLE_SCAN_ALWAYS = "ble_scan_always_enabled"
    private const val SETTING_ADB_WIRELESS = "adb_wifi_enabled"
    private const val SETTING_OEM_UNLOCK = "oem_unlock_allowed"

    private const val SETTING_ENABLED = 1
    private const val SETTING_DISABLED = 0

    private const val EXPECTED_MANUFACTURER = "Google"
    private const val EXPECTED_BRAND = "google"
    private const val SINGLE_DISPLAY_COUNT = 1

    private val KNOWN_PIXEL_CODENAMES = setOf(
        "oriole",
        "raven",
        "bluejay",
        "panther",
        "cheetah",
        "lynx",
        "tangorpro",
        "felix",
        "shiba",
        "husky",
        "akita",
        "tokay",
        "caiman",
        "komodo",
        "comet",
        "tegu",
        "frankel",
        "blazer",
        "mustang",
        "rango",
        "stallion",
    )

    private const val ATTESTATION_EXTENSION_OID = "1.3.6.1.4.1.11129.2.1.17"
    private const val STRONGBOX_SECURITY_LEVEL = 2
    private const val VERIFIED_BOOT_SELF_SIGNED = 1
    private const val ATTESTATION_CHALLENGE_SIZE = 32
    private const val ELLIPTIC_CURVE_NAME = "secp256r1"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val ATTESTATION_KEY_ALIAS_PREFIX = "airgap_attestation_"
    private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
    private const val USB_CONNECTED_EXTRA = "connected"

    fun checkAll(context: Context): List<SensorStatus> = SensorName.entries.map { sensorName ->
        val result = checkSensor(context, sensorName)
        SensorStatus(
            sensorName = sensorName,
            isViolating = result.isLeft(),
            detail = result.fold(ifLeft = { it }, ifRight = { it }),
        )
    }

    private fun checkSensor(
        context: Context,
        sensorName: SensorName,
    ): Either<String, String> = when (sensorName) {
        SensorName.AIRPLANE_MODE -> checkAirplaneMode(context)
        SensorName.BLUETOOTH -> checkBluetooth(context)
        SensorName.BLUETOOTH_LOW_ENERGY -> checkBluetoothLowEnergy(context)
        SensorName.NFC -> checkNfc(context)
        SensorName.SIM -> checkSim(context)
        SensorName.WIFI -> checkWifi(context)
        SensorName.WIFI_DIRECT -> checkWifiDirect(context)
        SensorName.WIFI_AWARE -> checkWifiAware(context)
        SensorName.NETWORK_INTERFACE -> checkNetworkInterface(context)
        SensorName.VPN -> checkVpn(context)
        SensorName.TETHERING -> checkTethering(context)
        SensorName.WIFI_BACKGROUND_SCAN -> checkWifiBackgroundScan(context)
        SensorName.BLUETOOTH_BACKGROUND_SCAN -> checkBluetoothBackgroundScan(context)
        SensorName.LOCATION -> checkLocation(context)
        SensorName.DEVELOPER_OPTIONS -> checkDeveloperOptions(context)
        SensorName.ADB -> checkAdb(context)
        SensorName.ADB_WIRELESS -> checkAdbWireless(context)
        SensorName.ACCESSIBILITY_SERVICE -> checkAccessibilityService(context)
        SensorName.DISPLAY_MIRRORING -> checkDisplayMirroring(context)
        SensorName.OEM_UNLOCK -> checkOemUnlock(context)
        SensorName.DEVICE_INTEGRITY -> checkDeviceIntegrity()
        SensorName.ATTESTATION -> checkAttestation()
        SensorName.USB_POWER -> checkUsbPower(context)
    }

    private fun checkAirplaneMode(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, Settings.Global.AIRPLANE_MODE_ON)
        return if (value == SETTING_ENABLED)
            Either.Right("Enabled")
        else
            Either.Left("Disabled")
    }

    private fun checkBluetooth(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, Settings.Global.BLUETOOTH_ON)
        return if (value == SETTING_DISABLED)
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkBluetoothLowEnergy(context: Context): Either<String, String> {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
        val isLeEnabled = adapter?.isEnabled == true
        val isBleScanAlways = readGlobalSetting(context, SETTING_BLE_SCAN_ALWAYS) == SETTING_ENABLED
        return if (isLeEnabled.not() && isBleScanAlways.not())
            Either.Right("Disabled")
        else
            Either.Left("Enabled (LE: $isLeEnabled, scan-always: $isBleScanAlways)")
    }

    private fun checkNfc(context: Context): Either<String, String> {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return if (adapter == null)
            Either.Right("Not available")
        else if (adapter.isEnabled.not())
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkSim(context: Context): Either<String, String> {
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        val simState = telephonyManager.simState
        return if (simState == TelephonyManager.SIM_STATE_ABSENT)
            Either.Right("Absent")
        else
            Either.Left("Present (state: $simState)")
    }

    private fun checkWifi(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, Settings.Global.WIFI_ON)
        return if (value == SETTING_DISABLED)
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkWifiDirect(context: Context): Either<String, String> {
        val wifiValue = readGlobalSetting(context, Settings.Global.WIFI_ON)
        return if (wifiValue == SETTING_DISABLED)
            Either.Right("Disabled (Wi-Fi off)")
        else
            Either.Left("Potentially active (Wi-Fi on)")
    }

    private fun checkWifiAware(context: Context): Either<String, String> {
        val wifiAwareManager = context.getSystemService(WifiAwareManager::class.java)
        return if (wifiAwareManager == null)
            Either.Right("Not available")
        else if (wifiAwareManager.isAvailable.not())
            Either.Right("Disabled")
        else
            Either.Left("Available")
    }

    private fun checkNetworkInterface(context: Context): Either<String, String> {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        return if (activeNetwork == null)
            Either.Right("None active")
        else
            Either.Left("Active network detected")
    }

    @Suppress("DEPRECATION")
    private fun checkVpn(context: Context): Either<String, String> {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val allNetworks = connectivityManager.allNetworks
        val hasVpn = allNetworks.any { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
        return if (hasVpn.not())
            Either.Right("No VPN")
        else
            Either.Left("VPN active")
    }

    @Suppress("DEPRECATION")
    private fun checkTethering(context: Context): Either<String, String> {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val allNetworks = connectivityManager.allNetworks
        val hasTethering = allNetworks.any { network ->
            val linkProperties = connectivityManager.getLinkProperties(network)
            val interfaceName = linkProperties?.interfaceName ?: ""
            interfaceName.startsWith("rndis") ||
                interfaceName.startsWith("swlan") ||
                interfaceName.startsWith("ap") ||
                interfaceName.startsWith("bt-pan")
        }
        return if (hasTethering.not())
            Either.Right("Not tethering")
        else
            Either.Left("Tethering detected")
    }

    @Suppress("DEPRECATION")
    private fun checkWifiBackgroundScan(context: Context): Either<String, String> {
        val wifiManager = context.getSystemService(WifiManager::class.java)
        return if (wifiManager.isScanAlwaysAvailable.not())
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkBluetoothBackgroundScan(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, SETTING_BLE_SCAN_ALWAYS)
        return if (value == SETTING_DISABLED)
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkLocation(context: Context): Either<String, String> {
        val locationManager = context.getSystemService(LocationManager::class.java)
        return if (locationManager.isLocationEnabled.not())
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkDeveloperOptions(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
        return if (value == SETTING_DISABLED)
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkAdb(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, Settings.Global.ADB_ENABLED)
        return if (value == SETTING_DISABLED)
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkAdbWireless(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, SETTING_ADB_WIRELESS)
        return if (value == SETTING_DISABLED)
            Either.Right("Disabled")
        else
            Either.Left("Enabled")
    }

    private fun checkAccessibilityService(context: Context): Either<String, String> {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        )
        return if (enabledServices.isEmpty())
            Either.Right("None active")
        else
            Either.Left("${enabledServices.size} service(s) active")
    }

    private fun checkDisplayMirroring(context: Context): Either<String, String> {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val displayCount = displayManager.displays.size
        return if (displayCount <= SINGLE_DISPLAY_COUNT)
            Either.Right("Single display")
        else
            Either.Left("$displayCount displays detected")
    }

    private fun checkOemUnlock(context: Context): Either<String, String> {
        val value = readGlobalSetting(context, SETTING_OEM_UNLOCK)
        return if (value == SETTING_DISABLED)
            Either.Right("Locked")
        else
            Either.Left("Unlocked")
    }

    private fun checkDeviceIntegrity(): Either<String, String> {
        val failures = mutableListOf<String>()

        if (Build.MANUFACTURER != EXPECTED_MANUFACTURER)
            failures.add("manufacturer: ${Build.MANUFACTURER}")
        if (Build.BRAND != EXPECTED_BRAND)
            failures.add("brand: ${Build.BRAND}")
        if (KNOWN_PIXEL_CODENAMES.contains(Build.DEVICE).not())
            failures.add("device: ${Build.DEVICE}")

        return if (failures.isEmpty())
            Either.Right("Google Pixel (${Build.DEVICE}), SDK ${Build.VERSION.SDK_INT}")
        else
            Either.Left(failures.joinToString(", "))
    }

    private fun checkAttestation(): Either<String, String> =
        Either.catch {
            performStrongBoxAttestation()
        }.fold(
            ifLeft = { throwable ->
                if (throwable is CancellationException)
                    throw throwable
                Either.Left("Attestation failed: ${throwable.message}")
            },
            ifRight = { result -> result },
        )

    private fun performStrongBoxAttestation(): Either<String, String> {
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
        if (certificateChain == null || certificateChain.isEmpty())
            return Either.Left("No certificate chain")

        val attestationCertificate = certificateChain[0] as X509Certificate
        val attestationExtension = attestationCertificate.getExtensionValue(ATTESTATION_EXTENSION_OID)
            ?: return Either.Left("No attestation extension")

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
            Either.Right("StrongBox verified, GrapheneOS confirmed")
        else
            Either.Left(failures.joinToString(", "))
    }

    private fun checkUsbPower(context: Context): Either<String, String> {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val pluggedState = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, SETTING_DISABLED)
            ?: SETTING_DISABLED
        val isUsbCharging = pluggedState == BatteryManager.BATTERY_PLUGGED_USB

        val usbStateIntent = context.registerReceiver(
            null,
            IntentFilter(ACTION_USB_STATE),
        )
        val isUsbDataConnected = usbStateIntent?.getBooleanExtra(USB_CONNECTED_EXTRA, false) ?: false

        val isUsbConnected = isUsbCharging || isUsbDataConnected

        return if (isUsbConnected.not())
            Either.Right("Not connected")
        else if (org.alice.poc.airgap.BuildConfig.DEBUG)
            Either.Right("Connected (debug — ignored)")
        else
            Either.Left("USB connected")
    }

    private fun readGlobalSetting(
        context: Context,
        name: String,
    ): Int = Settings.Global.getInt(context.contentResolver, name, SETTING_DISABLED)

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
