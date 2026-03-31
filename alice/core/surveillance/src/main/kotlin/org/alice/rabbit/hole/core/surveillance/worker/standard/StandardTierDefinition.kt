package org.alice.rabbit.hole.core.surveillance.worker.standard

import org.alice.rabbit.hole.core.surveillance.worker.ISurveillanceWorkDefinition
import org.alice.rabbit.hole.core.surveillance.worker.ScheduleUniqueName
import kotlin.time.Duration.Companion.hours

data object StandardTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_standard")
    override val repeatInterval = 1.hours
}
