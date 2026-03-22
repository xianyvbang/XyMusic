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

package cn.xybbz.config.scope

import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.childScope
import kotlin.coroutines.CoroutineContext

abstract class IoScoped() : AutoCloseable {

    protected lateinit var scope: XyCloseableCoroutineScope

    fun createScope(coroutineContext: CoroutineContext? = null) {
        scope =
            coroutineContext?.childScope()
                ?: CoroutineScopeUtils.getIo(
                    this::javaClass.name
                )
    }

    override fun close() {
        if (::scope.isInitialized)
            scope.close()
    }
}