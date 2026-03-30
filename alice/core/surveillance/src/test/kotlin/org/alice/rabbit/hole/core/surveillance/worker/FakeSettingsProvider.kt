package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.provider.ISettingsProvider

class FakeSettingsProvider(
    private val globalInts: Map<String, Int> = emptyMap(),
) : ISettingsProvider {
    override fun readGlobalInt(name: String, defaultValue: Int): Int =
        globalInts.getOrDefault(name, defaultValue)
}
