@file:Suppress("SpellCheckingInspection")

package org.alice.poc.airgap.detection.accessibility

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object FeatureChecks {

    private const val SETTING_DISABLED = 0
    private const val DEFAULT_FONT_SCALE = 1.0F

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkMagnification(context),
        checkColourInversion(context),
        checkColourCorrection(context),
        checkHighTextContrast(context),
        checkCaptions(context),
        checkLiveCaptions(context),
        checkFontScale(context),
        checkMonoAudio(context),
        checkTouchExploration(context),
    )

    private fun checkMagnification(context: Context): CheckResult {
        val value = readSecureInt(context, "accessibility_display_magnification_enabled")
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.MAGNIFICATION, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.MAGNIFICATION, Either.Left(ViolationDetail("On")))
    }

    private fun checkColourInversion(context: Context): CheckResult {
        val value = readSecureInt(context, "accessibility_display_inversion_enabled")
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.COLOUR_INVERSION, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.COLOUR_INVERSION, Either.Left(ViolationDetail("On")))
    }

    private fun checkColourCorrection(context: Context): CheckResult {
        val value = readSecureInt(context, "accessibility_display_daltonizer_enabled")
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.COLOUR_CORRECTION, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.COLOUR_CORRECTION, Either.Left(ViolationDetail("On")))
    }

    private fun checkHighTextContrast(context: Context): CheckResult {
        val value = readSecureInt(context, "high_text_contrast_enabled")
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.HIGH_TEXT_CONTRAST, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.HIGH_TEXT_CONTRAST, Either.Left(ViolationDetail("On")))
    }

    private fun checkCaptions(context: Context): CheckResult {
        val value = readSecureInt(context, "accessibility_captioning_enabled")
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.CAPTIONS, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.CAPTIONS, Either.Left(ViolationDetail("On")))
    }

    private fun checkLiveCaptions(context: Context): CheckResult {
        val value = readSecureInt(context, "odi_captions_enabled")
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.LIVE_CAPTIONS, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.LIVE_CAPTIONS, Either.Left(ViolationDetail("On")))
    }

    private fun checkFontScale(context: Context): CheckResult {
        val scale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, DEFAULT_FONT_SCALE)
        return if (scale == DEFAULT_FONT_SCALE)
            CheckResult(SurfaceName.FONT_SCALE, Either.Right(SafeDetail("Default (1.0)")))
        else
            CheckResult(SurfaceName.FONT_SCALE, Either.Left(ViolationDetail("Scale: $scale")))
    }

    private fun checkMonoAudio(context: Context): CheckResult {
        val value = Settings.System.getInt(context.contentResolver, "master_mono", SETTING_DISABLED)
        return if (value == SETTING_DISABLED)
            CheckResult(SurfaceName.MONO_AUDIO, Either.Right(SafeDetail("Stereo")))
        else
            CheckResult(SurfaceName.MONO_AUDIO, Either.Left(ViolationDetail("Mono")))
    }

    private fun checkTouchExploration(context: Context): CheckResult {
        val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        return if (accessibilityManager.isTouchExplorationEnabled.not())
            CheckResult(SurfaceName.TOUCH_EXPLORATION, Either.Right(SafeDetail("Off")))
        else
            CheckResult(SurfaceName.TOUCH_EXPLORATION, Either.Left(ViolationDetail("On")))
    }

    private fun readSecureInt(
        context: Context,
        name: String,
    ): Int = Settings.Secure.getInt(context.contentResolver, name, SETTING_DISABLED)
}
