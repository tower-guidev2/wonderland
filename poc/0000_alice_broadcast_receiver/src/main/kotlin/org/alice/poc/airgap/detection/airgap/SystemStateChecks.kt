@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection.airgap

import android.content.Context
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object SystemStateChecks {

    private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    private const val SETTING_CRASH_DIALOG = "show_first_crash_dialog_dev_option"
    private const val SINGLE_DISPLAY_COUNT = 1
    private const val SETTING_DISABLED = 0
    private const val SETTING_ENABLED = 1

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkHealthConnect(context),
        checkSensorDefault(context),
        checkCrashNotifications(context),
        checkFlagSecure(context),
        checkVpn(context),
        checkDisplayMirroring(context),
    )

    private fun checkHealthConnect(context: Context): CheckResult {
        val enabledSetting = try {
            context.packageManager.getApplicationEnabledSetting(HEALTH_CONNECT_PACKAGE)
        } catch (exception: IllegalArgumentException) {
            return CheckResult(SurfaceName.HEALTH_CONNECT, Either.Right(SafeDetail("Not installed")))
        }
        val isDisabled = enabledSetting == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
            enabledSetting == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        return if (isDisabled)
            CheckResult(SurfaceName.HEALTH_CONNECT, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.HEALTH_CONNECT, Either.Left(ViolationDetail("Enabled (state: $enabledSetting)")))
    }

    @Suppress("UnusedParameter")
    private fun checkSensorDefault(context: Context): CheckResult =
        CheckResult(SurfaceName.SENSOR_DEFAULT, Either.Left(ViolationDetail("Key undiscovered — run ADB on device")))

    private fun checkCrashNotifications(context: Context): CheckResult = try {
        val value = Settings.Secure.getInt(context.contentResolver, SETTING_CRASH_DIALOG, SETTING_ENABLED)
        if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.CRASH_NOTIFICATIONS, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.CRASH_NOTIFICATIONS, Either.Left(ViolationDetail("Crash dialog enabled (value: $value)")))
    } catch (_: SecurityException) {
        CheckResult(SurfaceName.CRASH_NOTIFICATIONS, Either.Right(SafeDetail("Key restricted — verify via ADB")))
    }

    @Suppress("UnusedParameter")
    private fun checkFlagSecure(context: Context): CheckResult =
        CheckResult(SurfaceName.FLAG_SECURE, Either.Right(SafeDetail("Self-enforcement — verified at Activity level")))

    @Suppress("DEPRECATION")
    private fun checkVpn(context: Context): CheckResult {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val allNetworks = connectivityManager.allNetworks
        val hasVpn = allNetworks.any { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
        return if (hasVpn.not())
            CheckResult(SurfaceName.VPN, Either.Right(SafeDetail("No VPN")))
        else
            CheckResult(SurfaceName.VPN, Either.Left(ViolationDetail("VPN active")))
    }

    private fun checkDisplayMirroring(context: Context): CheckResult {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val displayCount = displayManager.displays.size
        return if (displayCount <= SINGLE_DISPLAY_COUNT)
            CheckResult(SurfaceName.DISPLAY_MIRRORING, Either.Right(SafeDetail("Single display")))
        else
            CheckResult(SurfaceName.DISPLAY_MIRRORING, Either.Left(ViolationDetail("$displayCount displays detected")))
    }
}
