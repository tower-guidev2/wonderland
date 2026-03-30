package org.alice.rabbit.hole.core.surveillance.worker

import android.content.Context
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.integrity.DeviceIntegrityVerifier
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider

class SlowTierWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val buildPropertyProvider: IBuildPropertyProvider,
    private val violationHandler: IViolationHandler,
) : BaseWorker(context, workerParameters) {

    override suspend fun doActualWork(): Result {
        val buildViolation = DeviceIntegrityVerifier.verifyBuildProperties(buildPropertyProvider)
        if (buildViolation != null) {
            violationHandler.handle(buildViolation)
        }
        return Result.success()
    }
}
