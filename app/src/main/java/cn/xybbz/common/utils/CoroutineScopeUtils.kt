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

package cn.xybbz.common.utils

import android.util.Log
import cn.xybbz.config.scope.XyCloseableCoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 获得CoroutineScope
 */
object CoroutineScopeUtils {

    /**
     * 获得Main协程
     */
    fun getMain(name: String = "xy"): XyCloseableCoroutineScope {
        return XyCloseableCoroutineScope(
            CoroutineExceptionHandler(
                handler = { context, throwable ->
                    Log.e("CoroutineExceptionHandler", throwable.message, throwable)
                }
            ) + SupervisorJob() + Dispatchers.Main + CoroutineName(name))
    }

    /**
     * 获得IO协程
     */
    fun getIo(name: String = "xy"): XyCloseableCoroutineScope {
        return XyCloseableCoroutineScope(
            CoroutineExceptionHandler(
            handler = { context, throwable ->
                Log.e("CoroutineExceptionHandler", throwable.message, throwable)
            }
        ) + SupervisorJob() + Dispatchers.IO + CoroutineName(name))
    }

    /**
     * 获得Default协程
     */
    fun getDefault(name: String = "xy"): XyCloseableCoroutineScope {
        return XyCloseableCoroutineScope(
            CoroutineExceptionHandler(
            handler = { context, throwable ->
                Log.e("CoroutineExceptionHandler", throwable.message, throwable)
            }
        ) + SupervisorJob() + Dispatchers.Default + CoroutineName(name))
    }
}