package org.alice.poc.airgap.detection.accessibility

import android.content.Context
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object ShortcutChecks {

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkAccessibilityShortcut(context),
        checkAccessibilityButton(context),
    )

    private fun checkAccessibilityShortcut(context: Context): CheckResult {
        val target = Settings.Secure.getString(context.contentResolver, "accessibility_shortcut_target_service")
        return if (target.isNullOrBlank())
            CheckResult(SurfaceName.ACCESSIBILITY_SHORTCUT, Either.Right(SafeDetail("None")))
        else
            CheckResult(SurfaceName.ACCESSIBILITY_SHORTCUT, Either.Left(ViolationDetail("Target: $target")))
    }

    private fun checkAccessibilityButton(context: Context): CheckResult {
        val targets = Settings.Secure.getString(context.contentResolver, "accessibility_button_targets")
        return if (targets.isNullOrBlank())
            CheckResult(SurfaceName.ACCESSIBILITY_BUTTON, Either.Right(SafeDetail("None")))
        else
            CheckResult(SurfaceName.ACCESSIBILITY_BUTTON, Either.Left(ViolationDetail("Targets: $targets")))
    }
}
