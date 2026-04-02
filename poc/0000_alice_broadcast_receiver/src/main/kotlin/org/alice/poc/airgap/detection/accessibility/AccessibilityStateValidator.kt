package org.alice.poc.airgap.detection.accessibility

import android.content.Context
import org.alice.poc.airgap.domain.CheckResult

object AccessibilityStateValidator {

    fun validate(context: Context): List<CheckResult> =
        ServiceChecks.checkAll(context) +
            FeatureChecks.checkAll(context) +
            ShortcutChecks.checkAll(context)
}
