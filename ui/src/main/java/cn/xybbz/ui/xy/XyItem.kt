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


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.R
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme

/**
 * 列表单项
 * @param [modifier] 修饰符
 * @param [name] 名称
 * @param [subordination] 次要内容
 * @param [imgUrl] 图片地址 默认封面
 * @param [media] 描述信息
 * @param [enabled] 是否启用
 * @param [backgroundColor] 背景颜色
 * @param [onClick] 点击方法
 */
@Composable
fun ItemTrailingContent(
    modifier: Modifier = Modifier,
    name: String,
    subordination: String?,
    favoriteState: Boolean,
    imgUrl: String? = null,
    media: String? = null,
    enabledPic: Boolean = true,
    ifDownload: Boolean,
    ifPlay: Boolean,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    picSize: Dp = 50.dp,
    brush: Brush? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {

    val density = LocalDensity.current
    val iconSizeDp = with(density) { 16.sp.toDp() }

    val inlineContentId = "inlineContentFavoriteId"
    val inlineIfDownloadId = "inlineContentDownloadId"

    val inlineContent = mapOf(
        Pair(
            inlineContentId,
            InlineTextContent(
                //设置宽高
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "已收藏",
                    modifier = Modifier
                        .size(iconSizeDp)
                        .padding(end = 2.dp),
                    tint = Color.Red
                )
            }),
        Pair(
            inlineIfDownloadId,
            InlineTextContent(
                //设置宽高
                Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DownloadDone,
                    contentDescription = "已下载",
                    modifier = Modifier
                        .size(iconSizeDp)
                        .padding(end = 2.dp),
                    tint = Color.Green
                )
            }
        )
    )

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = if (brush != null) Color.Transparent else if (ifPlay) Color(0x3B000000) else backgroundColor
        ),
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(
                brush
            )
            .combinedClickable(enabled = enabled, onClick = composeClick {
                onClick?.invoke()
            }, onLongClick = { onLongClick?.invoke() }),
        headlineContent = {
            XyText(
                text = name,
                color = if (ifPlay) Color(0xFFABE2FF) else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = if (!media.isNullOrBlank() || !subordination.isNullOrBlank()) {
            {
                XyTextSub(
                    text = buildAnnotatedString {
                        if (favoriteState)
                            appendInlineContent(inlineContentId, "[icon]")
                        if (ifDownload)
                            appendInlineContent(inlineIfDownloadId, "[icon]")
                        media?.let {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 10.sp,
                                    color = Color(0xFFCADBFF)
                                ), block = {
                                    append(media)
                                })
                            append(" ")
                        }
                        subordination?.let {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    fontStyle = MaterialTheme.typography.titleSmall.fontStyle,
                                    color = MaterialTheme.colorScheme.onSurface
                                ), block = {
                                    append(subordination)
                                }
                            )
                        }

                    },
                    inlineContent = inlineContent,
                )
            }
        } else null,
        leadingContent = if (enabledPic) {
            {
                XySmallImage(
                    model = imgUrl,
                    size = picSize,
                    contentDescription = "${name}${stringResource(R.string.image_suffix)}"
                )
            }
        } else null, trailingContent = {
            trailingContent?.invoke()
        }
    )
}

private fun Modifier.background(brush: Brush?): Modifier {
    return then(
        if (brush == null) {
            Modifier.background(Color.Transparent)
        } else {
            Modifier.background(brush)
        }
    )
}

@Composable
fun ItemTrailingArrowRight(
    modifier: Modifier = Modifier,
    name: String,
    subordination: String?,
    img: Painter? = null,
    toneQuality: String? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = backgroundColor),
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .fillMaxWidth()
            .debounceClickable(enabled = enabled) {
                onClick?.invoke()
            },
        shadowElevation = shadowElevation,
        headlineContent = {
            XyText(
                text = name,
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                toneQuality?.let {
                    SuggestionChip(
                        modifier = Modifier.height(15.dp),
                        border = SuggestionChipDefaults.suggestionChipBorder(true),
                        onClick = {}, enabled = false, label = {
                            Text(
                                text = toneQuality,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 10.sp,
                                maxLines = 1,
                                color = Color(0xfff26b1f)
                            )
                        })
                }
                subordination?.let {
                    XyTextSub(
                        text = subordination,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        leadingContent = {
            XySmallImage(
                model = img,
                contentDescription = "${name}${stringResource(R.string.image_suffix)}"
            )
        }, trailingContent = trailingContent
    )
}

@Composable
fun XyItem(
    modifier: Modifier = Modifier,
    text: String,
    sub: String?,
    fontWeight: FontWeight? = FontWeight.Bold,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        XyText(
            text = text,
            fontWeight = fontWeight,
            style = style
        )
        sub?.let {
            XyTextSub(
                text = sub,
                maxLines = 1,
            )
        }
    }
}

/**
 * 将次要信息和主要信息样式反转
 */
@Composable
fun XyItemReversal(
    modifier: Modifier = Modifier,
    text: String,
    sub: String?
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        XyTextSub(
            text = text,
        )
        sub?.let {
            XyText(
                text = sub,
                maxLines = 1,
            )
        }
    }
}


/**
 * 横向带图标的文本Item
 */
@Composable
fun XyItemIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    sub: String? = null,
    enabled: Boolean = true,
    middleContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    XyRow(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) { onClick?.invoke() },
        horizontalArrangement = Arrangement.Start,
        paddingValues = PaddingValues(
            vertical = XyTheme.dimens.innerVerticalPadding
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
            XyText(
                text = text,
                fontWeight = null,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            middleContent?.invoke()
            sub?.let {
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                XyText(
                    text = sub,
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/**
 * 带图标可选择的文本的Item
 */
@Composable
fun XyItemIconSelect(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector? = null,
    enabled: Boolean = true,
    enableLeading: Boolean = true,
    onIfSelected: () -> Boolean = { false },
    onClick: () -> Unit,
) {

    XyRow(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) { onClick.invoke() },
        paddingValues = PaddingValues(
            vertical = XyTheme.dimens.innerVerticalPadding
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (enableLeading) {
                imageVector?.let {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = text,
                        tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Spacer(modifier = Modifier.width(24.dp))

                Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
            }

            XyText(
                text = text,
                fontWeight = null,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) if (!onIfSelected()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
        if (onIfSelected()) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已选择",
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
            }
        }
    }
}

/**
 * 带切换按钮的 item
 */
@Composable
fun XyItemSwitcher(
    modifier: Modifier = Modifier,
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    text: String,
    paddingValue: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding
    )
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = XyTheme.dimens.itemHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .debounceClickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(
                paddingValue
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        XyText(
            modifier = Modifier.weight(1f),
            text = text
        )
        Switch(
            checked = state, onCheckedChange = onChange, enabled = enabled,
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = Color.Transparent,
                checkedThumbColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

/**
 * 竖向排列的item
 */
@Composable
fun XyItemLabel(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    sub: String? = null,
    imageVector: ImageVector,
    enabled: Boolean = true,
    iconColor: Color? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) {
                onClick.invoke()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {

        Icon(
            imageVector = imageVector,
            contentDescription = text,
            tint = iconColor
                ?: if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.3f
                ),
            modifier = Modifier
                .padding(
                    top = XyTheme.dimens.outerVerticalPadding,
                    bottom = XyTheme.dimens.outerVerticalPadding / 2
                )
        )
        XyTextSubSmall(
            text = text,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding / 2))
        sub?.let {
            XyTextSubSmall(
                text = sub,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
        }

    }
}

@Composable
fun XyItemOutSpacer() {
    Spacer(
        modifier = Modifier
            .height(XyTheme.dimens.outerVerticalPadding * 2)
    )
}


@Composable
fun XyItemSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String? = null,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconPainter?.let {
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(iconPaddingValues),
                    painter = iconPainter,
                    contentDescription = null,
                    colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
                )
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            }
            XyTextSubSmall(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            sub?.let {
                XyTextSubSmall(
                    text = sub,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(XyTheme.dimens.contentPadding))
        XySlider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource
        )
    }
}

@Composable
fun XyItemRadioButton(
    modifier: Modifier = Modifier,
    text: String,
    sub: String? = null,
    fontWeight: FontWeight? = FontWeight.Bold,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    selected: Boolean,
    enabled: Boolean = true,
    paddingValue: PaddingValues = PaddingValues(
        start = XyTheme.dimens.innerHorizontalPadding,
        end = XyTheme.dimens.innerHorizontalPadding / 2
    ),
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .debounceClickable(enabled = enabled) {
                onClick()
            }
            .padding(
                paddingValue
            )
    ) {
        XyItem(
            text = text,
            sub = sub,
            fontWeight = fontWeight,
            style = style,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = selected,
            onClick = {
                onClick()
            },
            enabled = enabled,
            modifier = Modifier
                .semantics {
                    contentDescription = text
                }
        )
    }
}