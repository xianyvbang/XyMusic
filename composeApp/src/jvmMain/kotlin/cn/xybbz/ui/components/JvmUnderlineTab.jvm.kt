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

package cn.xybbz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.rememberXyTextHoverColor

/**
 * 下划线标签的下划线高度，艺术家详情章节导航与设置页顶部标签保持一致。
 */
private val JvmUnderlineTabIndicatorHeight = 2.dp

/**
 * JVM 桌面端通用下划线标签项。
 *
 * 艺术家详情页章节导航与设置页顶部标签共用：支持可选的右侧数量，
 * hover 时仅文字切换为主题色并显示小手光标，点击无水波纹背景，
 * 选中态展示主题色下划线。
 *
 * @param title 标签展示文案。
 * @param selected 是否为当前选中的标签。
 * @param onClick 标签点击回调。
 * @param modifier 外层修饰符。
 * @param count 标签右侧数量，小于等于 0 时不展示。
 * @param titleStyle 标签文字样式。
 * @param fontWeight 标签文字字重。
 * @param topPadding 标签顶部内边距。
 * @param bottomPadding 标签底部内边距。
 * @param underlineGap 文字与下划线之间的间距。
 */
@Composable
internal fun JvmUnderlineTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int = 0,
    titleStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
    fontWeight: FontWeight? = FontWeight.SemiBold,
    topPadding: Dp = XyTheme.dimens.outerVerticalPadding / 2,
    bottomPadding: Dp = 9.dp,
    underlineGap: Dp = 7.dp,
) {
    // 悬停交互源与文字颜色统一由通用文字按钮状态维护，移入时仅文字换色
    val colorState = rememberXyTextHoverColor(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(
        modifier = modifier
            // 以文字宽度作为列宽，让下划线与文字内容等宽而不是撑满整行
            .width(IntrinsicSize.Max)
            .jvmHoverDebounceClickable(
                interactionSource = colorState.interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(top = topPadding, bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    colorState.color
                },
                style = titleStyle,
                fontWeight = fontWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(start = 7.dp),
                    color = if (selected || colorState.hovered) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.6.sp,
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.height(underlineGap))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(JvmUnderlineTabIndicatorHeight)
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(JvmUnderlineTabIndicatorHeight)
                )
        )
    }
}
