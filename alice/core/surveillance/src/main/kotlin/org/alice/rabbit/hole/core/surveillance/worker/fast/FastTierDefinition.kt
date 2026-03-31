package org.alice.rabbit.hole.core.surveillance.worker.fast

import org.alice.rabbit.hole.core.surveillance.worker.ISurveillanceWorkDefinition
import org.alice.rabbit.hole.core.surveillance.worker.ScheduleUniqueName
import kotlin.time.Duration.Companion.minutes

data object FastTierDefinition : ISurveillanceWorkDefinition {
    override val uniqueName = ScheduleUniqueName("surveillance_fast")
    override val repeatInterval = 15.minutes
}
