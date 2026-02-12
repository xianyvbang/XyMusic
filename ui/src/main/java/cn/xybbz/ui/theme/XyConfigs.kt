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

@file:Suppress("UNUSED")

package cn.xybbz.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * @param isDarkTheme isDarkTheme
 */
@Stable
class XyConfigs(
    isDarkTheme: Boolean,
    isDynamic: Boolean
) {
    val isDarkTheme by mutableStateOf(isDarkTheme, structuralEqualityPolicy())

    val isDynamic by mutableStateOf(isDynamic, structuralEqualityPolicy())
}

fun xyConfigs(
    isDarkTheme: Boolean = true,
    isDynamic: Boolean = false
): XyConfigs = XyConfigs(
    isDarkTheme = isDarkTheme,
    isDynamic = isDynamic
)