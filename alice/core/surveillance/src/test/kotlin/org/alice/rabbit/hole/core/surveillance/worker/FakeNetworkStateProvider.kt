package org.alice.rabbit.hole.core.surveillance.worker

import org.alice.rabbit.hole.core.surveillance.provider.INetworkStateProvider

class FakeNetworkStateProvider(
    private val activeNetwork: Boolean = false,
    private val vpnNetwork: Boolean = false,
    private val tetheredInterfaces: Boolean = false,
    private val displayCount: Int = 1,
    private val usbDeviceCount: Int = 0,
    private val oemUnlockEnabled: Boolean = false,
) : INetworkStateProvider {
    override fun hasActiveNetwork(): Boolean = activeNetwork
    override fun hasVpnNetwork(): Boolean = vpnNetwork
    override fun hasTetheredInterfaces(): Boolean = tetheredInterfaces
    override fun displayCount(): Int = displayCount
    override fun usbDeviceCount(): Int = usbDeviceCount
    override fun isOemUnlockEnabled(): Boolean = oemUnlockEnabled
}
