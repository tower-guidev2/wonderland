package org.alice.poc.airgap.domain

data class AirGapScreenState(
    val sensorStatuses: List<SensorStatus>,
    val errorMessage: String?,
    val isBluetoothPermissionGranted: Boolean,
) {
    val hasViolation: Boolean get() = sensorStatuses.any { it.isViolating }
}

object AirGapScreenStateDefaults {
    val INITIAL = AirGapScreenState(
        sensorStatuses = emptyList(),
        errorMessage = null,
        isBluetoothPermissionGranted = false,
    )
}
