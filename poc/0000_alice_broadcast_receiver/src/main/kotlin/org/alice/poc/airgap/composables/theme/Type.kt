@file:OptIn(ExperimentalTextApi::class)

package org.alice.poc.airgap.composables.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import org.alice.poc.airgap.R

private const val WEIGHT_REGULAR = 400
private const val WEIGHT_MEDIUM = 500
private const val WEIGHT_SEMI_BOLD = 600
private const val WEIGHT_BOLD = 700

private val LexendDecaRegular = FontFamily(
    Font(
        resId = R.font.lexend_deca_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(WEIGHT_REGULAR),
        ),
    ),
)

private val LexendDecaMedium = FontFamily(
    Font(
        resId = R.font.lexend_deca_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(WEIGHT_MEDIUM),
        ),
    ),
)

private val LexendDecaSemiBold = FontFamily(
    Font(
        resId = R.font.lexend_deca_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(WEIGHT_SEMI_BOLD),
        ),
    ),
)

private val LexendDecaBold = FontFamily(
    Font(
        resId = R.font.lexend_deca_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(WEIGHT_BOLD),
        ),
    ),
)

private val Baseline = Typography()

val AliceTypography = Typography(
    displayLarge = Baseline.displayLarge.copy(fontFamily = LexendDecaBold),
    displayMedium = Baseline.displayMedium.copy(fontFamily = LexendDecaBold),
    displaySmall = Baseline.displaySmall.copy(fontFamily = LexendDecaBold),
    headlineLarge = Baseline.headlineLarge.copy(fontFamily = LexendDecaSemiBold),
    headlineMedium = Baseline.headlineMedium.copy(fontFamily = LexendDecaSemiBold),
    headlineSmall = Baseline.headlineSmall.copy(fontFamily = LexendDecaSemiBold),
    titleLarge = Baseline.titleLarge.copy(fontFamily = LexendDecaSemiBold),
    titleMedium = Baseline.titleMedium.copy(fontFamily = LexendDecaMedium),
    titleSmall = Baseline.titleSmall.copy(fontFamily = LexendDecaMedium),
    bodyLarge = Baseline.bodyLarge.copy(fontFamily = LexendDecaRegular),
    bodyMedium = Baseline.bodyMedium.copy(fontFamily = LexendDecaRegular),
    bodySmall = Baseline.bodySmall.copy(fontFamily = LexendDecaRegular),
    labelLarge = Baseline.labelLarge.copy(fontFamily = LexendDecaMedium),
    labelMedium = Baseline.labelMedium.copy(fontFamily = LexendDecaMedium),
    labelSmall = Baseline.labelSmall.copy(fontFamily = LexendDecaMedium),
)
