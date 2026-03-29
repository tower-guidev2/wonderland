package org.alice.rabbit.hole.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.alice.rabbit.hole.core.ui.R

val LexendDeca = FontFamily(
    Font(
        resId = R.font.lexend_deca_variable,
        weight = FontWeight.W100,
        style = FontStyle.Normal
    )
)

private val baseline = Typography()

private fun TextStyle.withLexendDeca(): TextStyle = copy(
    fontFamily = LexendDeca,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

val AliceTypography = Typography(
    displayLarge = baseline.displayLarge.withLexendDeca(),
    displayMedium = baseline.displayMedium.withLexendDeca(),
    displaySmall = baseline.displaySmall.withLexendDeca(),

    headlineLarge = baseline.headlineLarge.withLexendDeca(),
    headlineMedium = baseline.headlineMedium.withLexendDeca(),
    headlineSmall = baseline.headlineSmall.withLexendDeca(),

    titleLarge = baseline.titleLarge.withLexendDeca(),
    titleMedium = baseline.titleMedium.withLexendDeca(),
    titleSmall = baseline.titleSmall.withLexendDeca(),

    bodyLarge = baseline.bodyLarge.withLexendDeca(),
    bodyMedium = baseline.bodyMedium.withLexendDeca(),
    bodySmall = baseline.bodySmall.withLexendDeca(),

    labelLarge = baseline.labelLarge.withLexendDeca(),
    labelMedium = baseline.labelMedium.withLexendDeca(),
    labelSmall = baseline.labelSmall.withLexendDeca(),
)
