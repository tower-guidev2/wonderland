package org.alice.rabbit.hole.core.surveillance

data class BroadcastData(
    val action: String?,
    val booleanExtras: Map<String, Boolean> = emptyMap(),
    val intExtras: Map<String, Int> = emptyMap(),
    val stringExtras: Map<String, String?> = emptyMap(),
    val stringArrayExtras: Map<String, Array<String>> = emptyMap(),
)
