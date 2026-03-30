package org.alice.rabbit.hole.core.surveillance.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAirGapSurveillance : IAirGapSurveillance {

    private val mutableStatus: MutableStateFlow<AirGapStatus> = MutableStateFlow(AirGapStatus.Secure)
    override val status: StateFlow<AirGapStatus> = mutableStatus.asStateFlow()

    private val mutableViolations: MutableSharedFlow<AirGapViolation> = MutableSharedFlow()
    override val violations: Flow<AirGapViolation> = mutableViolations.asSharedFlow()

    suspend fun emitViolation(violation: AirGapViolation) {
        mutableStatus.value = AirGapStatus.Compromised(violation)
        mutableViolations.emit(violation)
    }

    fun reset() {
        mutableStatus.value = AirGapStatus.Secure
    }
}
