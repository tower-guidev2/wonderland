package org.alice.poc.airgap.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.alice.poc.airgap.composables.theme.AirGapColors
import org.alice.poc.airgap.composables.theme.AirGapTheme
import org.alice.poc.airgap.domain.AirGapScreenState
import org.alice.poc.airgap.domain.SensorName
import org.alice.poc.airgap.domain.SensorStatus

private val ERROR_BANNER_PADDING = 16.dp
private val DIVIDER_THICKNESS = 0.5.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AirGapScreen(
    state: AirGapScreenState,
    onRefreshRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Air-Gap Verification") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (state.hasViolation)
                        AirGapColors.HardViolation
                    else
                        AirGapColors.Safe,
                    titleContentColor = AirGapColors.ErrorText,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onRefreshRequested) {
                Text("Refresh")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 0.dp),
        ) {
            if (state.errorMessage != null) {
                item(key = "error") {
                    ErrorBanner(message = state.errorMessage)
                }
            }

            items(
                items = state.sensorStatuses,
                key = { it.sensorName.name },
            ) { sensorStatus ->
                SensorListItem(sensorStatus = sensorStatus)
                HorizontalDivider(thickness = DIVIDER_THICKNESS)
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AirGapColors.ErrorBackground)
            .padding(ERROR_BANNER_PADDING),
    ) {
        Text(
            text = message,
            color = AirGapColors.ErrorText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun createPreviewStatuses(): List<SensorStatus> = listOf(
    SensorStatus(SensorName.AIRPLANE_MODE, false, "Enabled"),
    SensorStatus(SensorName.BLUETOOTH, false, "Disabled"),
    SensorStatus(SensorName.WIFI, true, "Enabled"),
    SensorStatus(SensorName.NFC, false, "Disabled"),
    SensorStatus(SensorName.LOCATION, true, "Enabled"),
    SensorStatus(SensorName.USB_POWER, true, "USB connected"),
    SensorStatus(SensorName.DEVICE_INTEGRITY, false, "Google Pixel (shiba), SDK 34"),
)

@Preview(
    name = "All Safe — Light",
    showBackground = true,
    device = "id:pixel_6",
)
@Composable
private fun PreviewAirGapScreenAllSafeLight() {
    AirGapTheme {
        AirGapScreen(
            state = AirGapScreenState(
                sensorStatuses = listOf(
                    SensorStatus(SensorName.AIRPLANE_MODE, false, "Enabled"),
                    SensorStatus(SensorName.BLUETOOTH, false, "Disabled"),
                    SensorStatus(SensorName.WIFI, false, "Disabled"),
                    SensorStatus(SensorName.NFC, false, "Disabled"),
                ),
                errorMessage = null,
                isBluetoothPermissionGranted = true,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(
    name = "All Safe — Dark",
    showBackground = true,
    device = "id:pixel_6",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAirGapScreenAllSafeDark() {
    AirGapTheme(isDarkTheme = true) {
        AirGapScreen(
            state = AirGapScreenState(
                sensorStatuses = listOf(
                    SensorStatus(SensorName.AIRPLANE_MODE, false, "Enabled"),
                    SensorStatus(SensorName.BLUETOOTH, false, "Disabled"),
                    SensorStatus(SensorName.WIFI, false, "Disabled"),
                    SensorStatus(SensorName.NFC, false, "Disabled"),
                ),
                errorMessage = null,
                isBluetoothPermissionGranted = true,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(
    name = "Violations — Light",
    showBackground = true,
    device = "id:pixel_6",
)
@Composable
private fun PreviewAirGapScreenViolationsLight() {
    AirGapTheme {
        AirGapScreen(
            state = AirGapScreenState(
                sensorStatuses = createPreviewStatuses(),
                errorMessage = null,
                isBluetoothPermissionGranted = true,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(
    name = "Violations — Dark",
    showBackground = true,
    device = "id:pixel_6",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAirGapScreenViolationsDark() {
    AirGapTheme(isDarkTheme = true) {
        AirGapScreen(
            state = AirGapScreenState(
                sensorStatuses = createPreviewStatuses(),
                errorMessage = null,
                isBluetoothPermissionGranted = true,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(
    name = "Error State — Light",
    showBackground = true,
    device = "id:pixel_6",
)
@Composable
private fun PreviewAirGapScreenErrorLight() {
    AirGapTheme {
        AirGapScreen(
            state = AirGapScreenState(
                sensorStatuses = createPreviewStatuses(),
                errorMessage = "Sensor check failed: SecurityException — BLUETOOTH_CONNECT permission denied",
                isBluetoothPermissionGranted = false,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(
    name = "Error State — Dark",
    showBackground = true,
    device = "id:pixel_6",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewAirGapScreenErrorDark() {
    AirGapTheme(isDarkTheme = true) {
        AirGapScreen(
            state = AirGapScreenState(
                sensorStatuses = createPreviewStatuses(),
                errorMessage = "Sensor check failed: SecurityException — BLUETOOTH_CONNECT permission denied",
                isBluetoothPermissionGranted = false,
            ),
            onRefreshRequested = {},
        )
    }
}
