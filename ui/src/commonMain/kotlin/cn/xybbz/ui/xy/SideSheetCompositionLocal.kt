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

package cn.xybbz.ui.xy

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 标记当前组合树是否需要给 JVM 通用 LazyColumn 显示桌面滚动条。
 *
 * 默认是 false；JVM 桌面入口会统一提供 true，让内部通用列表按桌面端习惯补充滚动条。
 */
val LocalModalSideSheetContent = staticCompositionLocalOf { false }
