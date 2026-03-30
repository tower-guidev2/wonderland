package org.alice.rabbit.hole.core.surveillance.worker

import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WorkScheduler(private val workManager: WorkManager) : IWorkScheduler {

    override fun schedule(definition: ISurveillanceWorkDefinition, workerClass: Class<out ListenableWorker>) {
        val request = PeriodicWorkRequest.Builder(
            workerClass,
            definition.repeatInterval.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        ).build()
        workManager.enqueueUniquePeriodicWork(
            definition.uniqueName.value,
            definition.existingWorkPolicy,
            request,
        )
    }
}
