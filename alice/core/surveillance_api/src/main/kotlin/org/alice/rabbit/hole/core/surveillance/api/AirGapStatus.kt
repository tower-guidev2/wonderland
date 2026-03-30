package org.alice.rabbit.hole.core.surveillance.api

sealed interface AirGapStatus {
    data object Secure : AirGapStatus
    data class Compromised(val violation: AirGapViolation) : AirGapStatus
}
