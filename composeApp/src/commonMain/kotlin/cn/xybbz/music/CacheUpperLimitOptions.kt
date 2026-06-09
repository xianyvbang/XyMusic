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

package cn.xybbz.music

import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import org.jetbrains.compose.resources.StringResource

data class CacheUpperLimitOption(
    val limit: CacheUpperLimitEnum,
    /** 缓存上限的普通展示文案，容量类固定文本直接使用该字段。 */
    val message: String,
    /** 缓存上限的资源展示文案，需要跟随语言切换的选项使用该字段。 */
    val messageResource: StringResource? = null,
)

expect fun cacheUpperLimitOptions(): List<CacheUpperLimitOption>
