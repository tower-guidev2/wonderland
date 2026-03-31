package org.alice.rabbit.hole.core.surveillance.worker.fast

import android.content.Context
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider
import org.alice.rabbit.hole.core.surveillance.worker.BaseWorker
import org.alice.rabbit.hole.core.surveillance.worker.IViolationHandler

class FastTierWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val settingsProvider: ISettingsProvider,
    private val adapterStateProvider: IAdapterStateProvider,
    private val violationHandler: IViolationHandler,
) : BaseWorker(context, workerParameters) {

    override suspend fun doActualWork(): Result {
        val violations = FastTierChecks.execute(settingsProvider, adapterStateProvider)
        violations.forEach { violationHandler.handle(it) }
        return Result.success()
    }
}
