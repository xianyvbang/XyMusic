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

