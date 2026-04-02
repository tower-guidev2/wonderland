package org.alice.poc.airgap.domain

data class AirGapScreenState(
    val checkResults: List<CheckResult>,
    val errorMessage: String?,
    val isBluetoothPermissionGranted: Boolean,
) {
    val hasViolation: Boolean get() = checkResults.any { it.isViolating }

    fun resultsForGroup(group: ValidatorGroup): List<CheckResult> =
        checkResults.filter { it.surface.validator == group }

    fun violationCountForGroup(group: ValidatorGroup): Int =
        resultsForGroup(group).count { it.isViolating }
}

object AirGapScreenStateDefaults {
    val INITIAL = AirGapScreenState(
        checkResults = emptyList(),
        errorMessage = null,
        isBluetoothPermissionGranted = false,
    )
}
