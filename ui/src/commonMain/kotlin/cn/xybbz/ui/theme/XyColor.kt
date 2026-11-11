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

package cn.xybbz.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 平台基础调色板。
 *
 * 移动端是小屏近距离、高对比的使用场景，桌面端是大窗口远距离，两者直接共用同一组色值
 * 会出现桌面端过于鲜艳、层级不够的问题。因此这里只保留语义契约，具体色值交给
 * androidMain / iosMain / jvmMain 各自实现。
 *
 * @param primary 主色，用于按钮、选中态、高亮
 * @param onSurface 主文字色
 * @param onSurfaceVariant 次级文字色
 * @param surface 页面背景色
 * @param surfaceContainerLowest 次级背景色，用于卡片、容器
 */
@Immutable
data class XyPalette(
    val primary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surface: Color,
    val surfaceContainerLowest: Color,
)

/**
 * 移动端浅色调色板。
 *
 * 保持原有品牌色不变，避免影响 Android / iOS 已确认的视觉效果。
 */
internal fun mobileLightPalette(): XyPalette = XyPalette(
    primary = Color(0xFF0470E6),
    onSurface = Color(0xFF1E1715),
    onSurfaceVariant = Color(0xFF8C8C8C),
    surface = Color(0xFFF3F3F3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
)

/**
 * 移动端深色调色板。
 *
 * 保持原有品牌色不变，避免影响 Android / iOS 已确认的视觉效果。
 */
internal fun mobileDarkPalette(): XyPalette = XyPalette(
    primary = Color(0xFF1478C8),
    onSurface = Color(0xFFEBEEF1),
    onSurfaceVariant = Color(0xFFA7A7A7),
    surface = Color(0xFF1A1A1A),
    surfaceContainerLowest = Color(0xFF2F2F2F),
)

/**
 * 当前平台浅色调色板。
 */
internal expect fun platformLightPalette(): XyPalette

/**
 * 当前平台深色调色板。
 */
internal expect fun platformDarkPalette(): XyPalette