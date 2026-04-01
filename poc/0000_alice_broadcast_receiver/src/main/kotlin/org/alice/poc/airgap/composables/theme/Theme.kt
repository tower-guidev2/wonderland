package org.alice.poc.airgap.composables.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

@Composable
fun AirGapTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme)
        DarkColorScheme
    else
        LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
