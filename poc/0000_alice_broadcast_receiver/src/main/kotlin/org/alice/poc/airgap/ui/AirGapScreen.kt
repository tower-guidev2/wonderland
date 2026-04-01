package org.alice.poc.airgap.ui

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
import androidx.compose.ui.unit.dp
import org.alice.poc.airgap.domain.AirGapScreenState
import org.alice.poc.airgap.ui.theme.AirGapColors

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
