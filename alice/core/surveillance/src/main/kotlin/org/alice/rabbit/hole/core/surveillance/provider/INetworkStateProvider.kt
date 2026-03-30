package org.alice.rabbit.hole.core.surveillance.provider

interface INetworkStateProvider {
    fun hasActiveNetwork(): Boolean
    fun hasVpnNetwork(): Boolean
    fun hasTetheredInterfaces(): Boolean
    fun displayCount(): Int
    fun usbDeviceCount(): Int
    fun isOemUnlockEnabled(): Boolean
}
