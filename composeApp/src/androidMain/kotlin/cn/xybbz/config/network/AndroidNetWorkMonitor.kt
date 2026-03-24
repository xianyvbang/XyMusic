package cn.xybbz.config.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class AndroidNetWorkMonitor(
    context: Context
) : NetWorkMonitor() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            publishCurrentState()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            publishNetworkState(resolveState(networkCapabilities))
        }

        override fun onLost(network: Network) {
            publishCurrentState()
        }

        override fun onUnavailable() {
            publishNetworkState(false)
        }
    }

    override fun onStart() {
        publishCurrentState(force = true)
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    override fun onStop() {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
    }

    private fun publishCurrentState(force: Boolean = false) {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        publishNetworkState(resolveState(capabilities), force = force)
    }

    private fun resolveState(capabilities: NetworkCapabilities?): Boolean {
        if (capabilities == null) {
            return false
        }

        val hasInternet =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isUnmetered =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        return hasInternet && isUnmetered
    }
}
