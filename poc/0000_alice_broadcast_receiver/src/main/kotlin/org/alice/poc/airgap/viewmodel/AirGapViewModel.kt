package org.alice.poc.airgap.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.alice.poc.airgap.detection.AirGapBroadcastReceiver
import org.alice.poc.airgap.detection.accessibility.AccessibilityStateValidator
import org.alice.poc.airgap.detection.airgap.AirGapStateValidator
import org.alice.poc.airgap.detection.exploit.ExploitProtectionValidator
import org.alice.poc.airgap.detection.integrity.DeviceIntegrityVerifier
import org.alice.poc.airgap.domain.AirGapScreenState
import org.alice.poc.airgap.domain.AirGapScreenStateDefaults
import java.util.concurrent.CancellationException

class AirGapViewModel(application: Application) : AndroidViewModel(application) {

    private val mutableState = MutableStateFlow(AirGapScreenStateDefaults.INITIAL)
    val state: StateFlow<AirGapScreenState> = mutableState.asStateFlow()

    private val receiver: BroadcastReceiver = AirGapBroadcastReceiver.createReceiver {
        refreshAll()
    }

    private val accessibilityObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            refreshAll()
        }
    }

    init {
        registerReceiver()
        registerAccessibilityObserver()
        refreshAll()
    }

    fun refreshAll() {
        val context = getApplication<Application>()
        try {
            val results = AirGapStateValidator.validate(context) +
                AccessibilityStateValidator.validate(context) +
                DeviceIntegrityVerifier.validate() +
                ExploitProtectionValidator.validate()
            mutableState.update { current ->
                current.copy(
                    checkResults = results,
                    errorMessage = null,
                )
            }
        } catch (exception: Exception) {
            if (exception is CancellationException)
                throw exception
            mutableState.update { current ->
                current.copy(errorMessage = "Check failed: ${exception.message}")
            }
        }
    }

    fun updateBluetoothPermission(isGranted: Boolean) {
        mutableState.update { current ->
            current.copy(isBluetoothPermissionGranted = isGranted)
        }
        if (isGranted)
            refreshAll()
    }

    private fun registerReceiver() {
        val context = getApplication<Application>()
        val filter = AirGapBroadcastReceiver.createIntentFilter()
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    private fun registerAccessibilityObserver() {
        val context = getApplication<Application>()
        val uri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        context.contentResolver.registerContentObserver(uri, false, accessibilityObserver)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(receiver)
        } catch (ignored: IllegalArgumentException) {
        }
        getApplication<Application>().contentResolver.unregisterContentObserver(accessibilityObserver)
    }
}
