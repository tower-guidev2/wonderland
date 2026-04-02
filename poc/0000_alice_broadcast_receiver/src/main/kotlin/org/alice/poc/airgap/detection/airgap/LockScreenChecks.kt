package org.alice.poc.airgap.detection.airgap

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object LockScreenChecks {

    private const val SETTING_DISABLED = 0
    private const val SETTING_ENABLED = 1

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkDeviceAdmin(context),
        checkTrustAgents(context),
        checkDeviceEncrypted(context),
        checkLockScreenNotifications(context),
        checkLockScreenSensitive(context),
        checkLockScreenMedia(context),
        checkAppPinning(context),
    )

    private fun checkDeviceAdmin(context: Context): CheckResult {
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val activeAdmins = devicePolicyManager.activeAdmins
        return if (activeAdmins.isNullOrEmpty())
            CheckResult(SurfaceName.DEVICE_ADMIN, Either.Right(SafeDetail("None")))
        else
            CheckResult(SurfaceName.DEVICE_ADMIN, Either.Left(ViolationDetail("${activeAdmins.size} admin(s) active")))
    }

    private fun checkTrustAgents(context: Context): CheckResult {
        val trustAgents = Settings.Secure.getString(context.contentResolver, "enabled_trust_agents")
        return if (trustAgents.isNullOrBlank())
            CheckResult(SurfaceName.TRUST_AGENTS, Either.Right(SafeDetail("None")))
        else
            CheckResult(SurfaceName.TRUST_AGENTS, Either.Left(ViolationDetail("Active: $trustAgents")))
    }

    private fun checkDeviceEncrypted(context: Context): CheckResult {
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val encryptionStatus = devicePolicyManager.storageEncryptionStatus
        return if (encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER)
            CheckResult(SurfaceName.DEVICE_ENCRYPTED, Either.Right(SafeDetail("Encrypted")))
        else
            CheckResult(SurfaceName.DEVICE_ENCRYPTED, Either.Left(ViolationDetail("Status: $encryptionStatus")))
    }

    private fun checkLockScreenNotifications(context: Context): CheckResult {
        val value = Settings.Secure.getInt(context.contentResolver, "lock_screen_show_notifications", SETTING_ENABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.LOCK_SCREEN_NOTIFICATIONS, Either.Right(SafeDetail("Hidden")))
        else
            CheckResult(SurfaceName.LOCK_SCREEN_NOTIFICATIONS, Either.Left(ViolationDetail("Visible")))
    }

    private fun checkLockScreenSensitive(context: Context): CheckResult {
        val value = Settings.Secure.getInt(context.contentResolver, "lock_screen_allow_private_notifications", SETTING_ENABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.LOCK_SCREEN_SENSITIVE, Either.Right(SafeDetail("Hidden")))
        else
            CheckResult(SurfaceName.LOCK_SCREEN_SENSITIVE, Either.Left(ViolationDetail("Visible")))
    }

    private fun checkLockScreenMedia(context: Context): CheckResult {
        val value = Settings.Secure.getInt(context.contentResolver, "lock_screen_show_media", SETTING_DISABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.LOCK_SCREEN_MEDIA, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.LOCK_SCREEN_MEDIA, Either.Left(ViolationDetail("On")))
    }

    private fun checkAppPinning(context: Context): CheckResult {
        val value = Settings.Secure.getInt(context.contentResolver, "lock_to_app_enabled", SETTING_DISABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.APP_PINNING, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.APP_PINNING, Either.Left(ViolationDetail("On")))
    }
}
