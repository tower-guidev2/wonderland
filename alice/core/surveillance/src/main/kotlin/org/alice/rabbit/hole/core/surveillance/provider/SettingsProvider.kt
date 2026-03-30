package org.alice.rabbit.hole.core.surveillance.provider

import android.content.ContentResolver
import android.provider.Settings

class SettingsProvider(private val contentResolver: ContentResolver) : ISettingsProvider {
    override fun readGlobalInt(name: String, defaultValue: Int): Int =
        Settings.Global.getInt(contentResolver, name, defaultValue)
}
