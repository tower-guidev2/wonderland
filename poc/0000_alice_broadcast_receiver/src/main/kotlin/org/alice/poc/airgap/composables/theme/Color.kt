package org.alice.poc.airgap.composables.theme

import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF884A6A)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFD8E7)
val onPrimaryContainerLight = Color(0xFF6D3351)
val secondaryLight = Color(0xFF725762)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFDD9E7)
val onSecondaryContainerLight = Color(0xFF59404B)
val tertiaryLight = Color(0xFF7F553A)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFDBC7)
val onTertiaryContainerLight = Color(0xFF643E24)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFFFF8F8)
val onBackgroundLight = Color(0xFF21191D)
val surfaceLight = Color(0xFFFFF8F8)
val onSurfaceLight = Color(0xFF21191D)
val surfaceVariantLight = Color(0xFFF1DEE4)
val onSurfaceVariantLight = Color(0xFF504348)
val outlineLight = Color(0xFF827379)
val outlineVariantLight = Color(0xFFD4C2C8)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF372E31)
val inverseOnSurfaceLight = Color(0xFFFCEDF1)
val inversePrimaryLight = Color(0xFFFDB0D4)
val surfaceDimLight = Color(0xFFE5D6DA)
val surfaceBrightLight = Color(0xFFFFF8F8)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF0F4)
val surfaceContainerLight = Color(0xFFF9EAEE)
val surfaceContainerHighLight = Color(0xFFF4E4E9)
val surfaceContainerHighestLight = Color(0xFFEEDFE3)

val primaryDark = Color(0xFFFDB0D4)
val onPrimaryDark = Color(0xFF521D3A)
val primaryContainerDark = Color(0xFF6D3351)
val onPrimaryContainerDark = Color(0xFFFFD8E7)
val secondaryDark = Color(0xFFE0BDCB)
val onSecondaryDark = Color(0xFF412A34)
val secondaryContainerDark = Color(0xFF59404B)
val onSecondaryContainerDark = Color(0xFFFDD9E7)
val tertiaryDark = Color(0xFFF2BB99)
val onTertiaryDark = Color(0xFF4A2810)
val tertiaryContainerDark = Color(0xFF643E24)
val onTertiaryContainerDark = Color(0xFFFFDBC7)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF191114)
val onBackgroundDark = Color(0xFFEEDFE3)
val surfaceDark = Color(0xFF191114)
val onSurfaceDark = Color(0xFFEEDFE3)
val surfaceVariantDark = Color(0xFF504348)
val onSurfaceVariantDark = Color(0xFFD4C2C8)
val outlineDark = Color(0xFF9D8D92)
val outlineVariantDark = Color(0xFF504348)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFEEDFE3)
val inverseOnSurfaceDark = Color(0xFF372E31)
val inversePrimaryDark = Color(0xFF884A6A)
val surfaceDimDark = Color(0xFF191114)
val surfaceBrightDark = Color(0xFF40373A)
val surfaceContainerLowestDark = Color(0xFF130C0F)
val surfaceContainerLowDark = Color(0xFF21191D)
val surfaceContainerDark = Color(0xFF251D21)
val surfaceContainerHighDark = Color(0xFF30282B)
val surfaceContainerHighestDark = Color(0xFF3B3236)

data class AirGapStatusColors(
    val safe: Color,
    val hardViolation: Color,
    val softViolation: Color,
    val errorBackground: Color,
    val onError: Color,
    val safeBackground: Color,
    val violationBackground: Color,
)

val LightAirGapStatusColors = AirGapStatusColors(
    safe = Color(0xFF2E7D32),
    hardViolation = Color(0xFFC62828),
    softViolation = Color(0xFFF57F17),
    errorBackground = Color(0xFFB71C1C),
    onError = Color(0xFFFFFFFF),
    safeBackground = Color(0xFFE8F5E9),
    violationBackground = Color(0xFFFFEBEE),
)

val DarkAirGapStatusColors = AirGapStatusColors(
    safe = Color(0xFF66BB6A),
    hardViolation = Color(0xFFEF5350),
    softViolation = Color(0xFFFFCA28),
    errorBackground = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    safeBackground = Color(0xFF1B5E20),
    violationBackground = Color(0xFF2C1212),
)
