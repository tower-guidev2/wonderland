package org.alice.rabbit.hole.core.surveillance.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import org.alice.rabbit.hole.core.surveillance.AirGapSurveillance
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider
import org.alice.rabbit.hole.core.surveillance.worker.IWorkScheduler
import org.alice.rabbit.hole.core.surveillance.worker.fast.FastTierChecks
import org.alice.rabbit.hole.core.surveillance.worker.fast.FastTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.fast.FastTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.slow.SlowTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.slow.SlowTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.standard.StandardTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.standard.StandardTierWorker
import org.koin.androix.startup.KoinInitializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AirGapInitializer : Initializer<AirGapSurveillance>, KoinComponent {

    override fun create(context: Context): AirGapSurveillance {
        val settingsProvider: ISettingsProvider = get()
        val adapterStateProvider: IAdapterStateProvider = get()
        val surveillance: AirGapSurveillance = get()

        val initialViolations = FastTierChecks.execute(settingsProvider, adapterStateProvider)
        if (initialViolations.isNotEmpty()) {
            surveillance.setCompromised(initialViolations.first())
        }

        val workScheduler: IWorkScheduler = get()
        workScheduler.schedule(FastTierDefinition, FastTierWorker::class.java)
        workScheduler.schedule(StandardTierDefinition, StandardTierWorker::class.java)
        workScheduler.schedule(SlowTierDefinition, SlowTierWorker::class.java)

        surveillance.startCollecting()

        return surveillance
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(KoinInitializer::class.java, WorkManagerInitializer::class.java)
}
