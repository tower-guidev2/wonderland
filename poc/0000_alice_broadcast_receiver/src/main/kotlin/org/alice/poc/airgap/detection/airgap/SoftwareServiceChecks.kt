@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection.airgap

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object SoftwareServiceChecks {

    private const val UPDATER_PACKAGE = "com.android.updater"
    private const val SETTING_CELL_BROADCAST_SMS = "cdma_cell_broadcast_sms"
    private const val SETTING_DISABLED = 0

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkSystemUpdater(context),
        checkPrivateDns(context),
        checkMasterSync(),
        checkHotspot(context),
        checkPrintServices(context),
        checkEmergencySos(context),
        checkEmergencyAlerts(context),
    )

    private fun checkSystemUpdater(context: Context): CheckResult {
        val enabledSetting = try {
            context.packageManager.getApplicationEnabledSetting(UPDATER_PACKAGE)
        } catch (exception: IllegalArgumentException) {
            return CheckResult(SurfaceName.SYSTEM_UPDATER, Either.Right(SafeDetail("Package not found")))
        }
        val isDisabled = enabledSetting == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
            enabledSetting == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        return if (isDisabled)
            CheckResult(SurfaceName.SYSTEM_UPDATER, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.SYSTEM_UPDATER, Either.Left(ViolationDetail("Enabled (state: $enabledSetting)")))
    }

    private fun checkPrivateDns(context: Context): CheckResult {
        val mode = Settings.Global.getString(context.contentResolver, "private_dns_mode") ?: "off"
        return if (mode == "off")
            CheckResult(SurfaceName.PRIVATE_DNS, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.PRIVATE_DNS, Either.Left(ViolationDetail("Mode: $mode")))
    }

    private fun checkMasterSync(): CheckResult = try {
        val isSyncEnabled = ContentResolver.getMasterSyncAutomatically()
        if (isSyncEnabled.not())
            CheckResult(SurfaceName.MASTER_SYNC, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.MASTER_SYNC, Either.Left(ViolationDetail("Enabled")))
    } catch (exception: SecurityException) {
        CheckResult(SurfaceName.MASTER_SYNC, Either.Right(SafeDetail("Permission denied (expected on GOS)")))
    }

    @Suppress("DEPRECATION")
    private fun checkHotspot(context: Context): CheckResult {
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
            CheckResult(SurfaceName.HOTSPOT, Either.Right(SafeDetail("Not tethering")))
        else
            CheckResult(SurfaceName.HOTSPOT, Either.Left(ViolationDetail("Tethering detected")))
    }

    @Suppress("UnusedParameter")
    private fun checkPrintServices(context: Context): CheckResult =
        CheckResult(SurfaceName.PRINT_SERVICES, Either.Right(SafeDetail("API restricted — verify manually in settings")))

    private fun checkEmergencySos(context: Context): CheckResult {
        val value = Settings.Secure.getInt(context.contentResolver, "emergency_sos_enabled", SETTING_DISABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.EMERGENCY_SOS, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.EMERGENCY_SOS, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkEmergencyAlerts(context: Context): CheckResult = try {
        val value = Settings.Global.getInt(context.contentResolver, SETTING_CELL_BROADCAST_SMS, SETTING_DISABLED)
        if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.EMERGENCY_ALERTS, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.EMERGENCY_ALERTS, Either.Left(ViolationDetail("Cell broadcast enabled (value: $value)")))
    } catch (_: SecurityException) {
        CheckResult(SurfaceName.EMERGENCY_ALERTS, Either.Right(SafeDetail("Key restricted — verify via ADB")))
    }
}
