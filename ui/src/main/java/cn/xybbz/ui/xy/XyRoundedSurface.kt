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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyRoundedSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable () -> Unit
) {

    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = content
    )
}

@Composable
fun RoundedSurfaceColumnPadding(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentPaddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding
    ),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                contentPaddingValues
            ),
        color = color,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = {
            Column(
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    )
}


@Composable
fun RoundedSurfaceColumnPadding(
    modifier: Modifier = Modifier,
    brush: Brush,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                paddingValues
            )
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(brush),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/**
 * Column with rounded radius
 *
 * @param modifier The modifier to be applied to the column.
 * @param color The background color of the column.
 * @param content The content of the column.
 */
@Composable
fun RoundedSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(XyTheme.dimens.outerHorizontalPadding, XyTheme.dimens.outerVerticalPadding)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}