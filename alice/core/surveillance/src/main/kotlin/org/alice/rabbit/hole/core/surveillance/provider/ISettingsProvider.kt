package org.alice.rabbit.hole.core.surveillance.provider

interface ISettingsProvider {
    fun readGlobalInt(name: String, defaultValue: Int = 0): Int
}
