package org.alice.rabbit.hole.core.surveillance.worker.standard

import android.content.Context
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider
import org.alice.rabbit.hole.core.surveillance.worker.BaseWorker
import org.alice.rabbit.hole.core.surveillance.worker.IViolationHandler

class StandardTierWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val networkStateProvider: INetworkStateProvider,
    private val violationHandler: IViolationHandler,
) : BaseWorker(context, workerParameters) {

    override suspend fun doActualWork(): Result {
        val violations = StandardTierChecks.execute(networkStateProvider)
        violations.forEach { violationHandler.handle(it) }
        return Result.success()
    }
}
