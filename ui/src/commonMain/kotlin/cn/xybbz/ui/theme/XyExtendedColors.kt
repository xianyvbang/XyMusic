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
 * Material3 ColorScheme 之外的扩展颜色语义。
 *
 * 侧边栏、状态提示、封面占位渐变这些色位 Material3 没有定义，桌面端却大量使用。
 * 统一收敛到这里后，各端按同一套语义取值，同时允许平台给出不同色值，
 * 避免页面里散落硬编码颜色导致主题切换时不跟随。
 */
@Immutable
data class XyExtendedColors(
    /** 侧边栏背景色 */
    val sidebarBackground: Color,
    /** 侧边栏一级导航选中背景色 */
    val sidebarNavigationSelectedBackground: Color,
    /** 侧边栏歌单选中背景色 */
    val sidebarPlaylistSelectedBackground: Color,
    /** 成功状态色 */
    val success: Color,
    /** 成功状态容器色 */
    val successContainer: Color,
    /** 警告状态色 */
    val warning: Color,
    /** 警告状态容器色 */
    val warningContainer: Color,
    /** 错误状态色 */
    val danger: Color,
    /** 收藏爱心色 */
    val favorite: Color,
    /** 音乐封面占位渐变起点色 */
    val mediaCoverGradientStart: Color,
    /** 音乐封面占位渐变终点色 */
    val mediaCoverGradientEnd: Color,
    /** 艺术家封面占位渐变起点色 */
    val artistCoverGradientStart: Color,
    /** 艺术家封面占位渐变终点色 */
    val artistCoverGradientEnd: Color,
)

/**
 * 移动端扩展颜色。
 *
 * 沿用原有色值，保证 Android / iOS 视觉不变。
 */
internal fun mobileExtendedColors(isDarkTheme: Boolean): XyExtendedColors {
    val palette = if (isDarkTheme) mobileDarkPalette() else mobileLightPalette()
    return XyExtendedColors(
        sidebarBackground = palette.surfaceContainerLowest,
        sidebarNavigationSelectedBackground = palette.primary.copy(alpha = 0.10f),
        sidebarPlaylistSelectedBackground = palette.primary.copy(alpha = 0.08f),
        success = Color(0xFF16824A),
        successContainer = Color(0xFFE8F6EE),
        warning = Color(0xFFA86200),
        warningContainer = Color(0xFFFFF4DF),
        danger = Color(0xFFB3261E),
        favorite = Color(0xFFE94157),
        mediaCoverGradientStart = Color(0xFF3B82F6),
        mediaCoverGradientEnd = Color(0xFF8B5CF6),
        artistCoverGradientStart = Color(0xFF10B981),
        artistCoverGradientEnd = Color(0xFF06B6D4),
    )
}

/**
 * 当前平台扩展颜色。
 *
 * @param isDarkTheme 是否深色主题，状态容器色需要随主题明暗切换
 */
internal expect fun platformExtendedColors(isDarkTheme: Boolean): XyExtendedColors