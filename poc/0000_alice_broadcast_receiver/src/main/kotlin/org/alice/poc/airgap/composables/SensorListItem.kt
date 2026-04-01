package org.alice.poc.airgap.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.alice.poc.airgap.composables.theme.AirGapColors
import org.alice.poc.airgap.composables.theme.AirGapTheme
import org.alice.poc.airgap.domain.SensorName
import org.alice.poc.airgap.domain.SensorStatus
import org.alice.poc.airgap.domain.ViolationSeverity

private val INDICATOR_SIZE = 12.dp
private val ITEM_PADDING = 16.dp
private val INDICATOR_SPACING = 12.dp
private val BADGE_HORIZONTAL_PADDING = 8.dp
private val BADGE_VERTICAL_PADDING = 2.dp
private val BADGE_CORNER_RADIUS = 4.dp

@Composable
internal fun SensorListItem(
    sensorStatus: SensorStatus,
    modifier: Modifier = Modifier,
) {
    val indicatorColor = resolveIndicatorColor(sensorStatus)
    val backgroundColor = if (sensorStatus.isViolating)
        AirGapColors.ViolationBackground
    else
        AirGapColors.SafeBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(ITEM_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(INDICATOR_SPACING),
    ) {
        Box(
            modifier = Modifier
                .size(INDICATOR_SIZE)
                .clip(CircleShape)
                .background(indicatorColor),
        )

        Column(modifier = Modifier.weight(1.0F)) {
            Text(
                text = sensorStatus.sensorName.displayLabel,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = sensorStatus.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (sensorStatus.isViolating) {
            SeverityBadge(severity = sensorStatus.sensorName.severity)
        }
    }
}

@Composable
private fun SeverityBadge(severity: ViolationSeverity) {
    val badgeColor = when (severity) {
        ViolationSeverity.HARD -> AirGapColors.HardViolation
        ViolationSeverity.SOFT -> AirGapColors.SoftViolation
    }
    Text(
        text = severity.name,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(
                color = badgeColor,
                shape = RoundedCornerShape(BADGE_CORNER_RADIUS),
            )
            .padding(
                horizontal = BADGE_HORIZONTAL_PADDING,
                vertical = BADGE_VERTICAL_PADDING,
            ),
    )
}

private fun resolveIndicatorColor(sensorStatus: SensorStatus): Color =
    if (sensorStatus.isViolating.not())
        AirGapColors.Safe
    else
        when (sensorStatus.sensorName.severity) {
            ViolationSeverity.HARD -> AirGapColors.HardViolation
            ViolationSeverity.SOFT -> AirGapColors.SoftViolation
        }

@Preview(
    name = "Safe Sensor — Light",
    showBackground = true,
    device = "id:pixel_6",
)
@Composable
private fun PreviewSensorListItemSafeLight() {
    AirGapTheme {
        SensorListItem(
            sensorStatus = SensorStatus(
                sensorName = SensorName.AIRPLANE_MODE,
                isViolating = false,
                detail = "Enabled",
            ),
        )
    }
}

@Preview(
    name = "Safe Sensor — Dark",
    showBackground = true,
    device = "id:pixel_6",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSensorListItemSafeDark() {
    AirGapTheme(isDarkTheme = true) {
        SensorListItem(
            sensorStatus = SensorStatus(
                sensorName = SensorName.AIRPLANE_MODE,
                isViolating = false,
                detail = "Enabled",
            ),
        )
    }
}

@Preview(
    name = "Hard Violation — Light",
    showBackground = true,
    device = "id:pixel_6",
)
@Composable
private fun PreviewSensorListItemHardViolationLight() {
    AirGapTheme {
        SensorListItem(
            sensorStatus = SensorStatus(
                sensorName = SensorName.BLUETOOTH,
                isViolating = true,
                detail = "Enabled",
            ),
        )
    }
}

@Preview(
    name = "Hard Violation — Dark",
    showBackground = true,
    device = "id:pixel_6",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSensorListItemHardViolationDark() {
    AirGapTheme(isDarkTheme = true) {
        SensorListItem(
            sensorStatus = SensorStatus(
                sensorName = SensorName.BLUETOOTH,
                isViolating = true,
                detail = "Enabled",
            ),
        )
    }
}

@Preview(
    name = "Soft Violation — Light",
    showBackground = true,
    device = "id:pixel_6",
)
@Composable
private fun PreviewSensorListItemSoftViolationLight() {
    AirGapTheme {
        SensorListItem(
            sensorStatus = SensorStatus(
                sensorName = SensorName.USB_POWER,
                isViolating = true,
                detail = "USB connected",
            ),
        )
    }
}

@Preview(
    name = "Soft Violation — Dark",
    showBackground = true,
    device = "id:pixel_6",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewSensorListItemSoftViolationDark() {
    AirGapTheme(isDarkTheme = true) {
        SensorListItem(
            sensorStatus = SensorStatus(
                sensorName = SensorName.USB_POWER,
                isViolating = true,
                detail = "USB connected",
            ),
        )
    }
}
