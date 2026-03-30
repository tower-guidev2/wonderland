package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.api.AirGapViolation
import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider

object StandardTierChecks {

    private const val SINGLE_DISPLAY_COUNT = 1

    fun execute(networkStateProvider: INetworkStateProvider): List<AirGapViolation> {
        val violations = mutableListOf<AirGapViolation>()

        if (networkStateProvider.hasActiveNetwork()) violations.add(AirGapViolation.NetworkInterfaceActive)
        if (networkStateProvider.hasVpnNetwork()) violations.add(AirGapViolation.VpnActive)
        if (networkStateProvider.hasTetheredInterfaces()) violations.add(AirGapViolation.TetheringActive)
        if (networkStateProvider.displayCount() > SINGLE_DISPLAY_COUNT) violations.add(AirGapViolation.DisplayMirroringActive)
        if (networkStateProvider.isOemUnlockEnabled()) violations.add(AirGapViolation.OemUnlockEnabled)

        return violations
    }
}
