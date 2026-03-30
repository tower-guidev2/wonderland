package org.alice.rabbit.hole.core.surveillance.worker

import android.content.Context
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider

class StandardTierWorker(
    context: Context,
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
