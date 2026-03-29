package org.alice.rabbit.hole.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal fun selectColorScheme(
    darkTheme: Boolean,
    contrast: ThemeContrast,
): ColorScheme = when (contrast) {
    ThemeContrast.Neutral -> if (darkTheme) darkScheme else lightScheme
    ThemeContrast.Medium -> if (darkTheme) mediumContrastDarkColorScheme else mediumContrastLightColorScheme
    ThemeContrast.High -> if (darkTheme) highContrastDarkColorScheme else highContrastLightColorScheme
}
