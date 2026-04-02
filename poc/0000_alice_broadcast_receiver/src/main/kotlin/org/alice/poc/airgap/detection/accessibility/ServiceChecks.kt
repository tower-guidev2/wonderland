package org.alice.poc.airgap.detection.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object ServiceChecks {

    private const val SETTING_DISABLED = 0

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkMasterToggle(context),
        checkEnabledString(context),
        checkServiceList(context),
    )

    private fun checkMasterToggle(context: Context): CheckResult {
        val masterEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            SETTING_DISABLED,
        )
        return if (masterEnabled == SETTING_DISABLED)
            CheckResult(SurfaceName.ACCESSIBILITY_MASTER, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.ACCESSIBILITY_MASTER, Either.Left(ViolationDetail("Enabled")))
    }

    private fun checkEnabledString(context: Context): CheckResult {
        val rawServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        )
        return if (rawServices.isNullOrBlank())
            CheckResult(SurfaceName.ACCESSIBILITY_ENABLED_STRING, Either.Right(SafeDetail("Empty")))
        else
            CheckResult(SurfaceName.ACCESSIBILITY_ENABLED_STRING, Either.Left(ViolationDetail("Services: $rawServices")))
    }

    private fun checkServiceList(context: Context): CheckResult {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        )
        return if (enabledServices.isEmpty())
            CheckResult(SurfaceName.ACCESSIBILITY_SERVICE_LIST, Either.Right(SafeDetail("None active")))
        else
            CheckResult(SurfaceName.ACCESSIBILITY_SERVICE_LIST, Either.Left(ViolationDetail("${enabledServices.size} service(s) active")))
    }
}
