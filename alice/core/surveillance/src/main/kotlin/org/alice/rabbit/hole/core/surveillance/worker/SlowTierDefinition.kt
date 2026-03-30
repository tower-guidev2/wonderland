package org.alice.rabbit.hole.core.surveillance.worker

import kotlin.time.Duration.Companion.hours

data object SlowTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_slow")
    override val repeatInterval = 6.hours
}
