package org.alice.poc.airgap.detection.airgap

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import arrow.core.Either
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ViolationDetail

object UsbChecks {

    private const val ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE"
    private const val USB_CONNECTED_EXTRA = "connected"
    private const val UNPLUGGED = 0
    private const val SETTING_USB_DATA_PROTECTION = "aapm_usb_data_protection"
    private const val USB_DATA_PROTECTED = 1

    fun checkAll(context: Context): List<CheckResult> = listOf(
        checkUsbData(context),
        checkUsbPower(context),
    )

    private fun checkUsbData(context: Context): CheckResult = try {
        val value = Settings.Secure.getInt(context.contentResolver, SETTING_USB_DATA_PROTECTION, 0)
        if (value == USB_DATA_PROTECTED)
            CheckResult(SurfaceName.USB_DATA, Either.Right(SafeDetail("Protected")))
        else
            CheckResult(SurfaceName.USB_DATA, Either.Left(ViolationDetail("USB data not protected (value: $value)")))
    } catch (_: SecurityException) {
        CheckResult(SurfaceName.USB_DATA, Either.Right(SafeDetail("Key restricted — verify via ADB")))
    }

    private fun checkUsbPower(context: Context): CheckResult {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )
        val pluggedState = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, UNPLUGGED) ?: UNPLUGGED
        val isUsbCharging = pluggedState == BatteryManager.BATTERY_PLUGGED_USB

        val usbStateIntent = context.registerReceiver(
            null,
            IntentFilter(ACTION_USB_STATE),
        )
        val isUsbDataConnected = usbStateIntent?.getBooleanExtra(USB_CONNECTED_EXTRA, false) ?: false

        val isUsbConnected = isUsbCharging || isUsbDataConnected

        return if (isUsbConnected.not())
            CheckResult(SurfaceName.USB_POWER, Either.Right(SafeDetail("Not connected")))
        else if (org.alice.poc.airgap.BuildConfig.DEBUG)
            CheckResult(SurfaceName.USB_POWER, Either.Right(SafeDetail("Connected (debug — ignored)")))
        else
            CheckResult(SurfaceName.USB_POWER, Either.Left(ViolationDetail("USB connected")))
    }
}
