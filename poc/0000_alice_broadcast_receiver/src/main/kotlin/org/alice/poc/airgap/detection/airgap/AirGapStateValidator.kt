package org.alice.poc.airgap.detection.airgap

import android.content.Context
import org.alice.poc.airgap.domain.CheckResult

object AirGapStateValidator {

    fun validate(context: Context): List<CheckResult> =
        RadioChecks.checkAll(context) +
            LocationChecks.checkAll(context) +
            UsbChecks.checkAll(context) +
            SoftwareServiceChecks.checkAll(context) +
            AccountChecks.checkAll(context) +
            LockScreenChecks.checkAll(context) +
            DeveloperChecks.checkAll(context) +
            SystemStateChecks.checkAll(context)
}
