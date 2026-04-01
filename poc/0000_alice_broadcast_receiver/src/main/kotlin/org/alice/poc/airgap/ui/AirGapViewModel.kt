package org.alice.poc.airgap.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.alice.poc.airgap.detection.AirGapBroadcastReceiver
import org.alice.poc.airgap.detection.SensorChecker
import org.alice.poc.airgap.domain.AirGapScreenState
import org.alice.poc.airgap.domain.AirGapScreenStateDefaults

class AirGapViewModel(application: Application) : AndroidViewModel(application) {

    private val mutableState = MutableStateFlow(AirGapScreenStateDefaults.INITIAL)
    val state: StateFlow<AirGapScreenState> = mutableState.asStateFlow()

    private val receiver: BroadcastReceiver = AirGapBroadcastReceiver.createReceiver {
        refreshSensorStatuses()
    }

    init {
        registerReceiver()
        refreshSensorStatuses()
    }

    fun refreshSensorStatuses() {
        val context = getApplication<Application>()
        try {
            val statuses = SensorChecker.checkAll(context)
            mutableState.update { current ->
                current.copy(
                    sensorStatuses = statuses,
                    errorMessage = null,
                )
            }
        } catch (exception: Exception) {
            if (exception is java.util.concurrent.CancellationException)
                throw exception
            mutableState.update { current ->
                current.copy(errorMessage = "Sensor check failed: ${exception.message}")
            }
        }
    }

    fun updateBluetoothPermission(isGranted: Boolean) {
        mutableState.update { current ->
            current.copy(isBluetoothPermissionGranted = isGranted)
        }
        if (isGranted)
            refreshSensorStatuses()
    }

    private fun registerReceiver() {
        val context = getApplication<Application>()
        val filter = AirGapBroadcastReceiver.createIntentFilter()
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(receiver)
        } catch (ignored: IllegalArgumentException) {
        }
    }
}
