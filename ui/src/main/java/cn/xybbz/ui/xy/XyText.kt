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

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * 主要信息使用的Text
 */
@Composable
fun XyText(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight? = FontWeight.Bold,
    maxLines: Int = 1,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        maxLines = maxLines,
        style = style,
        overflow = overflow,
        color = color
    )
}

/**
 * 次要信息使用的Text
 */
@Composable
fun XyTextSub(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    style: TextStyle = MaterialTheme.typography.bodySmall,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        modifier = modifier,
        text = text,
        inlineContent = inlineContent,
        style = style,
        overflow = overflow,
        textAlign = textAlign,
        color = color,
    )
}


/**
 * 次要信息使用的Text
 */
@Composable
fun XyTextSub(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        overflow = overflow,
        textAlign = textAlign,
        maxLines = maxLines,
        color = color
    )
}

@Composable
fun XyTextSubSmall(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    XyTextSub(
        modifier = Modifier
            .then(modifier),
        text = text,
        color = color,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * 页面标题Txt
 */
@Composable
fun XyScreenTitle(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight? = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        color = color,
        style = MaterialTheme.typography.titleLarge,
    )
}