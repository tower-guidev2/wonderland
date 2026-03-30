package org.alice.rabbit.hole.core.surveillance.provider

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings

class NetworkStateProvider(private val context: Context) : INetworkStateProvider {

    override fun hasActiveNetwork(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return connectivityManager.activeNetwork != null
    }

    override fun hasVpnNetwork(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        }
    }

    override fun hasTetheredInterfaces(): Boolean {
        return Settings.Global.getString(context.contentResolver, "tethering_on")?.toIntOrNull() == 1
    }

    override fun displayCount(): Int {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return 1
        return displayManager.displays.size
    }

    override fun usbDeviceCount(): Int {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return 0
        return usbManager.deviceList.size
    }

    override fun isOemUnlockEnabled(): Boolean =
        Settings.Global.getInt(context.contentResolver, "oem_unlock_allowed", 0) == 1
}
