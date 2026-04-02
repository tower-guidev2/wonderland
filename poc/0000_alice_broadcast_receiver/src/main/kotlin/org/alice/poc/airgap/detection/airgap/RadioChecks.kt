@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection.airgap

import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.aware.WifiAwareManager
import android.nfc.NfcAdapter
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object RadioChecks {

    private const val SETTING_BLE_SCAN_ALWAYS = "ble_scan_always_enabled"
    private const val SETTING_ENABLED = 1
    private const val SETTING_DISABLED = 0

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkAirplaneMode(context),
        checkBluetooth(context),
        checkBluetoothLowEnergy(context),
        checkBluetoothBackgroundScan(context),
        checkNfc(context),
        checkWifi(context),
        checkWifiDirect(context),
        checkWifiAware(context),
        checkWifiBackgroundScan(context),
        checkUwb(context),
        checkSatellite(context),
        checkThread(context),
        checkSim(context),
        checkEsimProfiles(context),
        checkEsimToggle(context),
        checkNetworkInterface(context),
    )

    private fun checkAirplaneMode(context: Context): CheckResult {
        val value = readGlobalInt(context, Settings.Global.AIRPLANE_MODE_ON)
        return if (value == SETTING_ENABLED)
            CheckResult(SurfaceName.AIRPLANE_MODE, Either.Right(SafeDetail("Enabled")))
        else
            CheckResult(SurfaceName.AIRPLANE_MODE, Either.Left(ViolationDetail("Disabled")))
    }

    private fun checkBluetooth(context: Context): CheckResult {
        val value = readGlobalInt(context, Settings.Global.BLUETOOTH_ON)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.BLUETOOTH, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.BLUETOOTH, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkBluetoothLowEnergy(context: Context): CheckResult {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val adapter = bluetoothManager?.adapter
        val isLeEnabled = adapter?.isEnabled == true
        val isBleScanAlways = readGlobalInt(context, SETTING_BLE_SCAN_ALWAYS) == SETTING_ENABLED
        return if (isLeEnabled.not() && isBleScanAlways.not())
            CheckResult(SurfaceName.BLUETOOTH_LOW_ENERGY, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.BLUETOOTH_LOW_ENERGY, Either.Left(ViolationDetail("Enabled (LE: $isLeEnabled, scan-always: $isBleScanAlways)")))
    }

    private fun checkBluetoothBackgroundScan(context: Context): CheckResult {
        val value = readGlobalInt(context, SETTING_BLE_SCAN_ALWAYS)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.BLUETOOTH_BACKGROUND_SCAN, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.BLUETOOTH_BACKGROUND_SCAN, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkNfc(context: Context): CheckResult {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return if (adapter == null)
            CheckResult(SurfaceName.NFC, Either.Right(SafeDetail("Not available")))
        else if (adapter.isEnabled.not())
            CheckResult(SurfaceName.NFC, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.NFC, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkWifi(context: Context): CheckResult {
        val value = readGlobalInt(context, Settings.Global.WIFI_ON)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.WIFI, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.WIFI, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkWifiDirect(context: Context): CheckResult {
        val wifiValue = readGlobalInt(context, Settings.Global.WIFI_ON)
        return if (wifiValue == SETTING_DISABLED)
            CheckResult(SurfaceName.WIFI_DIRECT, Either.Right(SafeDetail("Disabled (Wi-Fi off)")))
        else
            CheckResult(SurfaceName.WIFI_DIRECT, Either.Left(ViolationDetail("Potentially active (Wi-Fi on)")))
    }

    private fun checkWifiAware(context: Context): CheckResult {
        val wifiAwareManager = context.getSystemService(WifiAwareManager::class.java)
        return if (wifiAwareManager == null)
            CheckResult(SurfaceName.WIFI_AWARE, Either.Right(SafeDetail("Not available")))
        else if (wifiAwareManager.isAvailable.not())
            CheckResult(SurfaceName.WIFI_AWARE, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.WIFI_AWARE, Either.Left(ViolationDetail("Available")))
    }

    @Suppress("DEPRECATION")
    private fun checkWifiBackgroundScan(context: Context): CheckResult {
        val wifiManager = context.getSystemService(WifiManager::class.java)
        return if (wifiManager.isScanAlwaysAvailable.not())
            CheckResult(SurfaceName.WIFI_BACKGROUND_SCAN, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.WIFI_BACKGROUND_SCAN, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkUwb(context: Context): CheckResult {
        val hasUwb = context.packageManager.hasSystemFeature("android.hardware.uwb")
        return if (hasUwb.not())
            CheckResult(SurfaceName.UWB, Either.Right(SafeDetail("Not available")))
        else
            CheckResult(SurfaceName.UWB, Either.Left(ViolationDetail("Hardware present — verify disabled in settings")))
    }

    private fun checkSatellite(context: Context): CheckResult {
        val hasSatellite = context.packageManager.hasSystemFeature("android.hardware.telephony.satellite")
        return if (hasSatellite.not())
            CheckResult(SurfaceName.SATELLITE, Either.Right(SafeDetail("Not available")))
        else
            CheckResult(SurfaceName.SATELLITE, Either.Left(ViolationDetail("Hardware present — verify disabled in settings")))
    }

    private fun checkThread(context: Context): CheckResult {
        val hasThread = context.packageManager.hasSystemFeature("android.hardware.thread_network")
        return if (hasThread.not())
            CheckResult(SurfaceName.THREAD, Either.Right(SafeDetail("Not available")))
        else
            CheckResult(SurfaceName.THREAD, Either.Left(ViolationDetail("Hardware present — verify disabled in settings")))
    }

    private fun checkSim(context: Context): CheckResult {
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        val simState = telephonyManager.simState
        return if (simState == TelephonyManager.SIM_STATE_ABSENT)
            CheckResult(SurfaceName.SIM, Either.Right(SafeDetail("Absent")))
        else
            CheckResult(SurfaceName.SIM, Either.Left(ViolationDetail("Present (state: $simState)")))
    }

    private fun checkEsimProfiles(context: Context): CheckResult {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        val activeSubscriptions = try {
            subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
        } catch (exception: SecurityException) {
            return CheckResult(SurfaceName.ESIM_PROFILES, Either.Right(SafeDetail("Cannot read — permission denied (expected)")))
        }
        return if (activeSubscriptions.isEmpty())
            CheckResult(SurfaceName.ESIM_PROFILES, Either.Right(SafeDetail("None active")))
        else
            CheckResult(SurfaceName.ESIM_PROFILES, Either.Left(ViolationDetail("${activeSubscriptions.size} active profile(s)")))
    }

    private fun checkEsimToggle(context: Context): CheckResult =
        CheckResult(SurfaceName.ESIM_TOGGLE, Either.Left(ViolationDetail("Key undiscovered — run ADB on device")))

    private fun checkNetworkInterface(context: Context): CheckResult {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        return if (activeNetwork == null)
            CheckResult(SurfaceName.NETWORK_INTERFACE, Either.Right(SafeDetail("None active")))
        else
            CheckResult(SurfaceName.NETWORK_INTERFACE, Either.Left(ViolationDetail("Active network detected")))
    }

    private fun readGlobalInt(
        context: Context,
        name: String,
    ): Int = Settings.Global.getInt(context.contentResolver, name, SETTING_DISABLED)
}
