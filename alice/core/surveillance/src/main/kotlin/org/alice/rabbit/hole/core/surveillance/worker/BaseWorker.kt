package org.alice.rabbit.hole.core.surveillance.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

abstract class BaseWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    abstract suspend fun doActualWork(): Result

    override suspend fun doWork(): Result {
        return if (isStopped) {
            Result.retry()
        } else {
            doActualWork()
        }
    }
}
