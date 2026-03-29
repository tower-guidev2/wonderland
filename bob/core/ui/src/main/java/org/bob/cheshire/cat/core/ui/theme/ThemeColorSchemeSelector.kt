package org.bob.cheshire.cat.core.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal fun selectColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    contrast: ThemeContrast,
    dynamicColorScheme: @Composable () -> ColorScheme
): ColorScheme {

    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return dynamicColorScheme()
    }

    return when (contrast) {
        ThemeContrast.Neutral -> if (darkTheme) darkScheme else lightScheme
        ThemeContrast.Medium -> if (darkTheme) mediumContrastDarkColorScheme else mediumContrastLightColorScheme
        ThemeContrast.High -> if (darkTheme) highContrastDarkColorScheme else highContrastLightColorScheme
    }
}
