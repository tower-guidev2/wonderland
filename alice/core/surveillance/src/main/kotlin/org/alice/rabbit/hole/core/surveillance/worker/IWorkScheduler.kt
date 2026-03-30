package org.alice.rabbit.hole.core.surveillance.worker

import androidx.work.ListenableWorker

interface IWorkScheduler {
    fun schedule(definition: ISurveillanceWorkDefinition, workerClass: Class<out ListenableWorker>)
}
