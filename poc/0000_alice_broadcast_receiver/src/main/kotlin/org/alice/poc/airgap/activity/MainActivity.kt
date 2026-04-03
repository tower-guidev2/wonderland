package org.alice.poc.airgap.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.alice.poc.airgap.composables.AirGapScreen
import org.alice.poc.airgap.composables.theme.AirGapTheme
import org.alice.poc.airgap.viewmodel.AirGapViewModel

class MainActivity : ComponentActivity() {

    private val bluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        viewModelInstance?.updateBluetoothPermission(isGranted)
    }

    private var viewModelInstance: AirGapViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AirGapTheme {
                val airGapViewModel: AirGapViewModel = viewModel()
                viewModelInstance = airGapViewModel

                val state by airGapViewModel.state.collectAsState()

                AirGapScreen(
                    state = state,
                    onRefreshRequested = { airGapViewModel.refreshAll() },
                )
            }
        }

        requestBluetoothPermissionIfNeeded()
    }

    private fun requestBluetoothPermissionIfNeeded() {
        val isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

        if (isGranted)
            viewModelInstance?.updateBluetoothPermission(true)
        else
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
}
