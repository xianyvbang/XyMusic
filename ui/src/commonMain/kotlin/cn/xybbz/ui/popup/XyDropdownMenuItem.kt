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

package cn.xybbz.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    // 右侧等宽读数，如步进量「+0.5s」；为空时只渲染标题
    trailingText: String? = null,
    // 标题颜色，默认取正文色；强调条目由调用方传主色 / 危险色
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    // 标题字重，强调条目传 SemiBold
    textWeight: FontWeight = FontWeight.Normal,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    backgroundColor: Color = Color.Transparent,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    itemHeight: Dp? = null,
    // 条目圆角；为空时回落主题圆角
    corner: Dp? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    // 标题占满剩余宽度，右侧读数才能贴齐条目右缘
                    modifier = Modifier.weight(1f),
                    // 统一到 12.5sp，桌面端 34dp 行高下更紧凑
                    fontSize = 12.5.sp,
                    fontWeight = textWeight,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (trailingText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trailingText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        },
        onClick = onClick,
        modifier = (if (itemHeight == null) modifier else modifier.height(itemHeight)).padding(
            horizontal = 5.dp
        ).clip(RoundedCornerShape(corner ?: XyTheme.dimens.corner)).background(backgroundColor),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    )

}
