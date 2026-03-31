package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation

fun interface IViolationHandler {
    suspend fun handle(violation: AirGapViolation)
}
