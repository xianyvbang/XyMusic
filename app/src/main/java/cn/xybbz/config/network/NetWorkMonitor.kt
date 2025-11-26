package cn.xybbz.config.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetWorkMonitor(application: Context) {

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isUnmeteredWifi = MutableStateFlow(false)
    val isUnmeteredWifi: StateFlow<Boolean> = _isUnmeteredWifi.asStateFlow()

    var onNetworkChange: ((Boolean) -> Unit)? = null


    private fun check(caps: NetworkCapabilities?) {
        if (caps == null) {
            _isUnmeteredWifi.value = false
            return
        }

        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isUnmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        _isUnmeteredWifi.value = isWifi && isUnmetered

        onNetworkChange?.invoke(isWifi && isUnmetered)
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            check(connectivityManager.getNetworkCapabilities(network))
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            check(networkCapabilities)
        }

        override fun onLost(network: Network) {
            _isUnmeteredWifi.value = false
            onNetworkChange?.invoke(false)
        }
    }

    fun start(onNetworkChange: ((Boolean) -> Unit)) {
        if (this.onNetworkChange == null) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            this.onNetworkChange = onNetworkChange
            connectivityManager.registerNetworkCallback(request, callback)
        }

    }

    fun stop() {
        if (this.onNetworkChange != null){
            this.onNetworkChange = null
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) {

            }
        }
    }
}