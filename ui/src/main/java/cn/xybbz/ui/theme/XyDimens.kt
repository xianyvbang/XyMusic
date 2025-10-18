/**
 * SaltUI
 * Copyright (C) 2023 Moriafly
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param corner corner
 * @param dialogCorner dialogCorner
 * @param outerHorizontalPadding outerHorizontalPadding
 * @param innerHorizontalPadding innerHorizontalPadding
 * @param outerVerticalPadding outerVerticalPadding
 * @param innerVerticalPadding innerVerticalPadding
 * @param contentPadding contentPadding
 * @param itemHeight 每个item的行高-只影响音乐列表
 */
@Stable
class XyDimens(
    corner: Dp,
    dialogCorner: Dp,
    outerHorizontalPadding: Dp,
    innerHorizontalPadding: Dp,
    outerVerticalPadding: Dp,
    innerVerticalPadding: Dp,
    contentPadding: Dp,
    snackBarPlayerHeight: Dp,
    itemHeight:Dp
) {
    val corner by mutableStateOf(corner, structuralEqualityPolicy())
    val dialogCorner by mutableStateOf(dialogCorner, structuralEqualityPolicy())
    val outerHorizontalPadding by mutableStateOf(outerHorizontalPadding, structuralEqualityPolicy())
    val innerHorizontalPadding by mutableStateOf(innerHorizontalPadding, structuralEqualityPolicy())
    val outerVerticalPadding by mutableStateOf(outerVerticalPadding, structuralEqualityPolicy())
    val innerVerticalPadding by mutableStateOf(innerVerticalPadding, structuralEqualityPolicy())
    val contentPadding by mutableStateOf(contentPadding, structuralEqualityPolicy())
    val snackBarPlayerHeight by mutableStateOf(snackBarPlayerHeight, structuralEqualityPolicy())
    val itemHeight by mutableStateOf(itemHeight, structuralEqualityPolicy())
}

fun xyDimens(
    corner: Dp = 12.dp,
    dialogCorner: Dp = 20.dp,
    outerHorizontalPadding: Dp = 16.dp,
    innerHorizontalPadding: Dp = 16.dp,
    outerVerticalPadding: Dp = 8.dp,
    innerVerticalPadding: Dp = 12.dp,
    contentPadding: Dp = 12.dp,
    snackBarPlayerHeight: Dp = 54.dp,
    itemHeight:Dp = 62.dp
): XyDimens = XyDimens(
    corner = corner,
    dialogCorner = dialogCorner,
    outerHorizontalPadding = outerHorizontalPadding,
    innerHorizontalPadding = innerHorizontalPadding,
    outerVerticalPadding = outerVerticalPadding,
    innerVerticalPadding = innerVerticalPadding,
    contentPadding = contentPadding,
    snackBarPlayerHeight = snackBarPlayerHeight,
    itemHeight = itemHeight
)

