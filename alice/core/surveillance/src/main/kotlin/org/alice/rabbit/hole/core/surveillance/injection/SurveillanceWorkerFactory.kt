package org.alice.rabbit.hole.core.surveillance.injection

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider
import org.alice.rabbit.hole.core.surveillance.worker.FastTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.IViolationHandler
import org.alice.rabbit.hole.core.surveillance.worker.SlowTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.StandardTierWorker

class SurveillanceWorkerFactory(
    private val settingsProvider: ISettingsProvider,
    private val adapterStateProvider: IAdapterStateProvider,
    private val networkStateProvider: INetworkStateProvider,
    private val buildPropertyProvider: IBuildPropertyProvider,
    private val violationHandler: IViolationHandler,
) : WorkerFactory() {

    override fun createWorker(
        applicationContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            FastTierWorker::class.java.name -> FastTierWorker(applicationContext, workerParameters, settingsProvider, adapterStateProvider, violationHandler)
            StandardTierWorker::class.java.name -> StandardTierWorker(applicationContext, workerParameters, networkStateProvider, violationHandler)
            SlowTierWorker::class.java.name -> SlowTierWorker(applicationContext, workerParameters, buildPropertyProvider, violationHandler)
            else -> null
        }
    }
}
