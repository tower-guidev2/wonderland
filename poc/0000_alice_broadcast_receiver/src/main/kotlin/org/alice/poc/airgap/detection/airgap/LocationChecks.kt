package org.alice.poc.airgap.detection.airgap

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object LocationChecks {

    private const val SETTING_FORCE_DISABLE_SUPL = "force_disable_supl"
    private const val SETTING_PSDS_SERVER = "psds_server"
    private const val SUPL_DISABLED = 1
    private const val PSDS_GRAPHENEOS_SERVER = 2

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

    private fun checkSupl(context: Context): CheckResult = try {
        val value = Settings.Global.getInt(context.contentResolver, SETTING_FORCE_DISABLE_SUPL, 0)
        if (value == SUPL_DISABLED)
            CheckResult(SurfaceName.SUPL, Either.Right(SafeDetail("Force disabled")))
        else
            CheckResult(SurfaceName.SUPL, Either.Left(ViolationDetail("SUPL enabled (value: $value)")))
    } catch (_: SecurityException) {
        CheckResult(SurfaceName.SUPL, Either.Right(SafeDetail("Key restricted — verify via ADB")))
    }

    private fun checkPsds(context: Context): CheckResult = try {
        val value = Settings.Global.getInt(context.contentResolver, SETTING_PSDS_SERVER, 0)
        if (value == PSDS_GRAPHENEOS_SERVER)
            CheckResult(SurfaceName.PSDS, Either.Right(SafeDetail("GrapheneOS PSDS server")))
        else
            CheckResult(SurfaceName.PSDS, Either.Left(ViolationDetail("PSDS server: $value (expected $PSDS_GRAPHENEOS_SERVER)")))
    } catch (_: SecurityException) {
        CheckResult(SurfaceName.PSDS, Either.Right(SafeDetail("Key restricted — verify via ADB")))
    }
}
