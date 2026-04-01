package org.alice.poc.airgap.domain

data class SensorStatus(
    val sensorName: SensorName,
    val isViolating: Boolean,
    val detail: String,
)
