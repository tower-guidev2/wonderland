package org.alice.rabbit.hole.core.surveillance.injection

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider
import org.alice.rabbit.hole.core.surveillance.worker.IViolationHandler
import org.alice.rabbit.hole.core.surveillance.worker.fast.FastTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.slow.SlowTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.standard.StandardTierWorker

class SurveillanceWorkerFactory(
    private val settingsProvider: ISettingsProvider,
    private val adapterStateProvider: IAdapterStateProvider,
    private val networkStateProvider: INetworkStateProvider,
    private val buildPropertyProvider: IBuildPropertyProvider,
    private val violationHandler: IViolationHandler,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            FastTierWorker::class.java.name -> FastTierWorker(appContext, workerParameters, settingsProvider, adapterStateProvider, violationHandler)
            StandardTierWorker::class.java.name -> StandardTierWorker(appContext, workerParameters, networkStateProvider, violationHandler)
            SlowTierWorker::class.java.name -> SlowTierWorker(appContext, workerParameters, buildPropertyProvider, violationHandler)
            else -> null
        }
    }
}
