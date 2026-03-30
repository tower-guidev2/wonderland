package org.alice.rabbit.hole.core.surveillance.worker

import kotlin.time.Duration.Companion.hours

data object StandardTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_standard")
    override val repeatInterval = 1.hours
}
