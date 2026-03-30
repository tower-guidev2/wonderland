package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation

class RecordingViolationHandler : IViolationHandler {
    val violations: MutableList<AirGapViolation> = mutableListOf()
    override suspend fun handle(violation: AirGapViolation) {
        violations.add(violation)
    }
}
