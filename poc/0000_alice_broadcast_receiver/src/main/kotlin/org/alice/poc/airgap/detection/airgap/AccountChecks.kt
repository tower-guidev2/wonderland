package org.alice.poc.airgap.detection.airgap

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object AccountChecks {

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkAccounts(context),
        checkAutofill(context),
        checkAutoSync(),
    )

    private fun checkAccounts(context: Context): CheckResult {
        val accounts = AccountManager.get(context).accounts
        return if (accounts.isEmpty())
            CheckResult(SurfaceName.ACCOUNTS, Either.Right(SafeDetail("None")))
        else
            CheckResult(SurfaceName.ACCOUNTS, Either.Left(ViolationDetail("${accounts.size} account(s) present")))
    }

    private fun checkAutofill(context: Context): CheckResult {
        val autofillService = Settings.Secure.getString(context.contentResolver, "autofill_service")
        return if (autofillService.isNullOrBlank())
            CheckResult(SurfaceName.AUTOFILL, Either.Right(SafeDetail("None")))
        else
            CheckResult(SurfaceName.AUTOFILL, Either.Left(ViolationDetail("Service: $autofillService")))
    }

    private fun checkAutoSync(): CheckResult {
        val isSyncEnabled = ContentResolver.getMasterSyncAutomatically()
        return if (isSyncEnabled.not())
            CheckResult(SurfaceName.AUTO_SYNC, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.AUTO_SYNC, Either.Left(ViolationDetail("Enabled")))
    }
}
