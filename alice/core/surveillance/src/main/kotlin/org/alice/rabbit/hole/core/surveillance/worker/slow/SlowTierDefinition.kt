package org.alice.rabbit.hole.core.surveillance.worker.slow

import org.alice.rabbit.hole.core.surveillance.worker.ISurveillanceWorkDefinition
import org.alice.rabbit.hole.core.surveillance.worker.ScheduleUniqueName
import kotlin.time.Duration.Companion.hours

data object SlowTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_slow")
    override val repeatInterval = 6.hours
}
