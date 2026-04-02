package org.alice.poc.airgap.detection.airgap

import android.content.Context
import android.location.LocationManager
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object LocationChecks {

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkLocation(context),
        checkSupl(context),
        checkPsds(context),
    )

    private fun checkLocation(context: Context): CheckResult {
        val locationManager = context.getSystemService(LocationManager::class.java)
        return if (locationManager.isLocationEnabled.not())
            CheckResult(SurfaceName.LOCATION, Either.Right(SafeDetail("Disabled")))
        else
            CheckResult(SurfaceName.LOCATION, Either.Left(ViolationDetail("Enabled")))
    }

    @Suppress("UnusedParameter")
    private fun checkSupl(context: Context): CheckResult =
        CheckResult(SurfaceName.SUPL, Either.Left(ViolationDetail("Key undiscovered — run ADB on device")))

    @Suppress("UnusedParameter")
    private fun checkPsds(context: Context): CheckResult =
        CheckResult(SurfaceName.PSDS, Either.Left(ViolationDetail("Key undiscovered — run ADB on device")))
}
