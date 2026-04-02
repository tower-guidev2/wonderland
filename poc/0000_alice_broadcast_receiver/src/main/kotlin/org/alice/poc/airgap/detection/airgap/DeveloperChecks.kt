package org.alice.poc.airgap.detection.airgap

import android.content.Context
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object DeveloperChecks {

    private const val SETTING_ADB_WIRELESS = "adb_wifi_enabled"
    private const val SETTING_OEM_UNLOCK = "oem_unlock_allowed"
    private const val SETTING_DISABLED = 0

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkDeveloperOptions(context),
        checkAdb(context),
        checkAdbWireless(context),
        checkOemUnlock(context),
    )

    private fun checkDeveloperOptions(context: Context): CheckResult {
        val value = readGlobalInt(context, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.DEVELOPER_OPTIONS, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.DEVELOPER_OPTIONS, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkAdb(context: Context): CheckResult {
        val value = readGlobalInt(context, Settings.Global.ADB_ENABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.ADB, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.ADB, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkAdbWireless(context: Context): CheckResult {
        val value = readGlobalInt(context, SETTING_ADB_WIRELESS)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.ADB_WIRELESS, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.ADB_WIRELESS, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkOemUnlock(context: Context): CheckResult {
        val value = readGlobalInt(context, SETTING_OEM_UNLOCK)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.OEM_UNLOCK, Either.Right(SafeDetail("Locked")))
        else
            CheckResult(SurfaceName.OEM_UNLOCK, Either.Left(ViolationDetail("Unlocked")))
    }

    private fun readGlobalInt(
        context: Context,
        name: String,
    ): Int = Settings.Global.getInt(context.contentResolver, name, SETTING_DISABLED)
}
