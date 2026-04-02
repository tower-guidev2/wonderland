package org.alice.poc.airgap.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import arrow.core.Either
import kotlinx.coroutines.launch
import org.alice.poc.airgap.composables.theme.AirGapColors
import org.alice.poc.airgap.composables.theme.AirGapTheme
import org.alice.poc.airgap.domain.AirGapScreenState
import org.alice.poc.airgap.domain.CheckResult
import org.alice.poc.airgap.domain.SafeDetail
import org.alice.poc.airgap.domain.SurfaceName
import org.alice.poc.airgap.domain.ValidatorGroup
import org.alice.poc.airgap.domain.ViolationDetail

private val ERROR_BANNER_PADDING = 16.dp
private val DIVIDER_THICKNESS = 0.5.dp

private val TAB_ORDER = listOf(
    ValidatorGroup.AIR_GAP,
    ValidatorGroup.ACCESSIBILITY,
    ValidatorGroup.INTEGRITY,
    ValidatorGroup.EXPLOIT,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AirGapScreen(
    state: AirGapScreenState,
    onRefreshRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { TAB_ORDER.size })
    val coroutineScope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.errorMessage != null) {
                ErrorBanner(message = state.errorMessage)
            }

            PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage, edgePadding = 0.dp) {
                TAB_ORDER.forEachIndexed { index, group ->
                    val violationCount = state.violationCountForGroup(group)
                    val label = if (violationCount > 0)
                        "${group.displayLabel} ($violationCount)"
                    else
                        group.displayLabel
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val group = TAB_ORDER[page]
                val results = state.resultsForGroup(group)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 0.dp),
                ) {
                    items(
                        items = results,
                        key = { it.surface.name },
                    ) { checkResult ->
                        SurfaceListItem(checkResult = checkResult)
                        HorizontalDivider(thickness = DIVIDER_THICKNESS)
                    }
                }
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

private fun createPreviewResults(): List<CheckResult> = listOf(
    CheckResult(SurfaceName.AIRPLANE_MODE, Either.Right(SafeDetail("Enabled"))),
    CheckResult(SurfaceName.BLUETOOTH, Either.Left(ViolationDetail("Enabled"))),
    CheckResult(SurfaceName.WIFI, Either.Left(ViolationDetail("Enabled"))),
    CheckResult(SurfaceName.NFC, Either.Right(SafeDetail("Disabled"))),
    CheckResult(SurfaceName.LOCATION, Either.Left(ViolationDetail("Enabled"))),
    CheckResult(SurfaceName.USB_POWER, Either.Left(ViolationDetail("USB connected"))),
    CheckResult(SurfaceName.DEVICE_INTEGRITY, Either.Right(SafeDetail("Google Pixel (shiba), SDK 34"))),
    CheckResult(SurfaceName.ACCESSIBILITY_MASTER, Either.Right(SafeDetail("Disabled"))),
    CheckResult(SurfaceName.MAGNIFICATION, Either.Right(SafeDetail("Off"))),
    CheckResult(SurfaceName.ATTESTATION, Either.Right(SafeDetail("StrongBox verified"))),
    CheckResult(SurfaceName.PTRACE, Either.Left(ViolationDetail("Phase 2 — JNI required"))),
)

@Preview(name = "Tabs — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun PreviewAirGapScreenLight() {
    AirGapTheme {
        AirGapScreen(
            state = AirGapScreenState(
                checkResults = createPreviewResults(),
                errorMessage = null,
                isBluetoothPermissionGranted = true,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(name = "Tabs — Dark", showBackground = true, device = "id:pixel_6", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAirGapScreenDark() {
    AirGapTheme(isDarkTheme = true) {
        AirGapScreen(
            state = AirGapScreenState(
                checkResults = createPreviewResults(),
                errorMessage = null,
                isBluetoothPermissionGranted = true,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(name = "Error — Light", showBackground = true, device = "id:pixel_6")
@Composable
private fun PreviewAirGapScreenErrorLight() {
    AirGapTheme {
        AirGapScreen(
            state = AirGapScreenState(
                checkResults = createPreviewResults(),
                errorMessage = "Check failed: SecurityException — BLUETOOTH_CONNECT permission denied",
                isBluetoothPermissionGranted = false,
            ),
            onRefreshRequested = {},
        )
    }
}

@Preview(name = "Error — Dark", showBackground = true, device = "id:pixel_6", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAirGapScreenErrorDark() {
    AirGapTheme(isDarkTheme = true) {
        AirGapScreen(
            state = AirGapScreenState(
                checkResults = createPreviewResults(),
                errorMessage = "Check failed: SecurityException — BLUETOOTH_CONNECT permission denied",
                isBluetoothPermissionGranted = false,
            ),
            onRefreshRequested = {},
        )
    }
}
