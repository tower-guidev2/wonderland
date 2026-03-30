package org.alice.rabbit.hole.core.surveillance.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IAirGapSurveillance {
    val status: StateFlow<AirGapStatus>
    val violations: Flow<AirGapViolation>
}
