/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.config.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetWorkMonitor(application: Context) {

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isUnmeteredWifi = MutableStateFlow(false)
    val isUnmeteredWifi: StateFlow<Boolean> = _isUnmeteredWifi.asStateFlow()

    var isWifiOrUnmetered: Boolean = false
    var isTransportCellular: Boolean = false

    var onNetworkChange: ((Boolean) -> Unit)? = null


    private fun check(caps: NetworkCapabilities?) {
        if (caps == null) {
            _isUnmeteredWifi.value = false
            return
        }

        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isUnmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        isWifiOrUnmetered = isWifi || isUnmetered

        val isTransportCellular = caps.hasCapability(NetworkCapabilities.TRANSPORT_CELLULAR)
        this.isTransportCellular = isTransportCellular

        _isUnmeteredWifi.value = isWifi && isUnmetered

        onNetworkChange?.invoke(isWifi || isUnmetered || isTransportCellular)
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            Log.i("music","网络切换1")
            check(connectivityManager.getNetworkCapabilities(network))
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.i("music","网络切换2")
//            check(networkCapabilities)
        }

        override fun onLost(network: Network) {
            _isUnmeteredWifi.value = false
            onNetworkChange?.invoke(false)
        }
    }

    fun start(
        onNetworkChange: ((Boolean) -> Unit)
    ) {
        if (this.onNetworkChange == null) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            this.onNetworkChange = onNetworkChange
            connectivityManager.registerNetworkCallback(request, callback)
        }

    }

    fun stop() {
        if (this.onNetworkChange != null) {
            this.onNetworkChange = null
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) {

            }
        }
    }
}