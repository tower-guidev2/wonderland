package org.alice.rabbit.hole.core.surveillance.worker.slow

import android.content.Context
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.integrity.DeviceIntegrityVerifier
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider
import org.alice.rabbit.hole.core.surveillance.worker.BaseWorker
import org.alice.rabbit.hole.core.surveillance.worker.IViolationHandler

class SlowTierWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val buildPropertyProvider: IBuildPropertyProvider,
    private val violationHandler: IViolationHandler,
) : BaseWorker(appContext, workerParameters) {

    override suspend fun doActualWork(): Result {
        val buildViolation = DeviceIntegrityVerifier.verifyBuildProperties(buildPropertyProvider)
        if (buildViolation != null) {
            violationHandler.handle(buildViolation)
        }
        return Result.success()
    }
}
