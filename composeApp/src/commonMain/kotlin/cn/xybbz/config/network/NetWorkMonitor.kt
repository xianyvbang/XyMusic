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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class NetWorkMonitor {

    private val listeners = mutableListOf<OnNetworkChangeListener>()
    private val _isUnmeteredWifi = MutableStateFlow(false)
    private var started = false

    val isUnmeteredWifi: StateFlow<Boolean> = _isUnmeteredWifi.asStateFlow()

    fun addListener(listener: OnNetworkChangeListener) {
        listeners.add(listener)
    }

    fun start() {
        if (started) {
            return
        }
        started = true

        try {
            onStart()
        } catch (error: Throwable) {
            started = false
            throw error
        }
    }

    fun stop() {
        if (!started) {
            listeners.clear()
            return
        }
        started = false

        try {
            onStop()
        } finally {
            listeners.clear()
        }
    }

    protected abstract fun onStart()

    protected abstract fun onStop()

    protected fun publishNetworkState(
        isUnmeteredWifi: Boolean,
        force: Boolean = false
    ) {
        if (!started) {
            return
        }

        val changed = force || _isUnmeteredWifi.value != isUnmeteredWifi
        _isUnmeteredWifi.value = isUnmeteredWifi
        if (!changed) {
            return
        }

        for (listener in listeners.toList()) {
            listener.onNetworkChange(isUnmeteredWifi)
        }
    }
}
