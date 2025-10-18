/**
 * SaltUI
 * Copyright (C) 2024 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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