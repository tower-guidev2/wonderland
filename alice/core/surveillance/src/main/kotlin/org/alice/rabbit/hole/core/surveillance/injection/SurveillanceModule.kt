package org.alice.rabbit.hole.core.surveillance.injection

import androidx.work.WorkManager
import org.alice.rabbit.hole.core.surveillance.AirGapNetworkMonitor
import org.alice.rabbit.hole.core.surveillance.AirGapSurveillance
import org.alice.rabbit.hole.core.surveillance.BuildConfig
import org.alice.rabbit.hole.core.surveillance.api.IAirGapSurveillance
import org.alice.rabbit.hole.core.surveillance.provider.AdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.BuildPropertyProvider
import org.alice.rabbit.hole.core.surveillance.provider.IAdapterStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.IBuildPropertyProvider
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider
import org.alice.rabbit.hole.core.surveillance.provider.NetworkStateProvider
import org.alice.rabbit.hole.core.surveillance.provider.SettingsProvider
import org.alice.rabbit.hole.core.surveillance.worker.IViolationHandler
import org.alice.rabbit.hole.core.surveillance.worker.IWorkScheduler
import org.alice.rabbit.hole.core.surveillance.worker.WorkScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val surveillanceModule = module {

    single<ISettingsProvider> { SettingsProvider(androidContext().contentResolver) }
    single<IAdapterStateProvider> { AdapterStateProvider(androidContext()) }
    single<INetworkStateProvider> { NetworkStateProvider(androidContext()) }
    single<IBuildPropertyProvider> { BuildPropertyProvider() }

    single { AirGapNetworkMonitor(androidContext()) }

    single {
        AirGapSurveillance(
            context = androidContext(),
            applicationScope = get(),
            isDebugBuild = BuildConfig.DEBUG,
            networkMonitor = get(),
        )
    } bind IAirGapSurveillance::class

    single<IViolationHandler> {
        val surveillance = get<AirGapSurveillance>()
        IViolationHandler { violation -> surveillance.setCompromised(violation) }
    }

    single<IWorkScheduler> { WorkScheduler(WorkManager.getInstance(androidContext())) }

    single {
        SurveillanceWorkerFactory(
            settingsProvider = get(),
            adapterStateProvider = get(),
            networkStateProvider = get(),
            buildPropertyProvider = get(),
            violationHandler = get(),
        )
    }
}
