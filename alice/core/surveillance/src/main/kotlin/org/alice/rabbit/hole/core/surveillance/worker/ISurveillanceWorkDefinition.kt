package org.alice.rabbit.hole.core.surveillance.worker

import androidx.work.ExistingPeriodicWorkPolicy
import kotlin.time.Duration

interface ISurveillanceWorkDefinition {
    val uniqueName: ScheduleUniqueName
    val repeatInterval: Duration
    val existingWorkPolicy: ExistingPeriodicWorkPolicy
        get() = ExistingPeriodicWorkPolicy.KEEP
}
