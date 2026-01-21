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


    val onNetworkChanges = mutableListOf<OnNetworkChangeListener>()


    private fun check(caps: NetworkCapabilities?) {
        if (caps == null) {
            _isUnmeteredWifi.value = false
            return
        }

        val isWifi = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val isUnmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)


        _isUnmeteredWifi.value = isWifi && isUnmetered

        for (listener in onNetworkChanges) {
            listener.onNetworkChange(isUnmetered)
        }
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
            check(networkCapabilities)
        }

        override fun onLost(network: Network) {
            _isUnmeteredWifi.value = false
            for (listener in onNetworkChanges) {
                listener.onNetworkChange(false)
            }
        }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

    }

    fun addListener(listener: OnNetworkChangeListener) {
        onNetworkChanges.add(listener)
    }

    fun stop() {
        try {
            onNetworkChanges.clear()
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: Exception) {

        }
    }
}