package org.alice.rabbit.hole.core.surveillance.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import org.alice.rabbit.hole.core.surveillance.AirGapSurveillance
import org.alice.rabbit.hole.core.surveillance.api.IAirGapSurveillance
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider
import org.alice.rabbit.hole.core.surveillance.worker.FastTierChecks
import org.alice.rabbit.hole.core.surveillance.worker.FastTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.FastTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.IWorkScheduler
import org.alice.rabbit.hole.core.surveillance.worker.SlowTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.SlowTierWorker
import org.alice.rabbit.hole.core.surveillance.worker.StandardTierDefinition
import org.alice.rabbit.hole.core.surveillance.worker.StandardTierWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AirGapInitializer : Initializer<IAirGapSurveillance>, KoinComponent {

    override fun create(context: Context): IAirGapSurveillance {
        val settingsProvider: ISettingsProvider = get()
        val adapterStateProvider: IAdapterStateProvider = get()
        val surveillance: AirGapSurveillance = get<IAirGapSurveillance>() as AirGapSurveillance

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
        listOf(WorkManagerInitializer::class.java)
}
