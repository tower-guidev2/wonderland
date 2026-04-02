package org.alice.poc.airgap.domain

enum class ValidatorGroup(
    val displayLabel: String,
) {
    AIR_GAP("Air Gap"),
    ACCESSIBILITY("Accessibility"),
    INTEGRITY("Integrity"),
    EXPLOIT("Exploit"),
}
