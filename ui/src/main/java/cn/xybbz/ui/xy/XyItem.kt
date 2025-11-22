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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
    index: Int? = null,
    media: String? = null,
    enabledPic: Boolean = true,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    brush: Brush? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {

    val density = LocalDensity.current
    val iconSizeDp = with(density) { 16.sp.toDp() }

    val inlineContentId = "inlineContentId"

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
            })
    )

    ListItem(
        colors = ListItemDefaults.colors(containerColor = if (brush != null) Color.Transparent else backgroundColor),
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
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                color = textColor
            )
        },
        supportingContent = if (!media.isNullOrBlank() || !subordination.isNullOrBlank()) {
            {
                Text(
                    text = buildAnnotatedString {
                        if (favoriteState)
                            appendInlineContent(inlineContentId, "[icon]")
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
                                    fontStyle = MaterialTheme.typography.titleSmall.fontStyle,
                                    color = textColor
                                ), block = {
                                    append(subordination)
                                }
                            )
                        }

                    },
                    inlineContent = inlineContent,
                    fontStyle = MaterialTheme.typography.titleSmall.fontStyle
                )
            }
        } else null,
        leadingContent = if (enabledPic) {
            {
                if (index == null)
                    XySmallImage(
                        model = imgUrl,
                        contentDescription = "${name}${stringResource(R.string.image_suffix)}"
                    )
                else
                    Text(text = index.toString(), style = MaterialTheme.typography.bodySmall)
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
    textColor: Color = MaterialTheme.colorScheme.onSurface,
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
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = textColor
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
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        text = subordination,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = textColor
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
fun XyItemBig(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        color = color,
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
fun XyItemTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = FontWeight.Bold,
    fontSize: TextUnit = TextUnit.Unspecified,
    style: TextStyle = MaterialTheme.typography.labelSmall
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        style = style
    )
}

@Composable
fun XyItemMedium(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        color = color,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun XyItemTitlePadding(
    modifier: Modifier = Modifier,
    text: String,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.innerHorizontalPadding,
        vertical = XyTheme.dimens.innerVerticalPadding
    )
) {
    Text(
        modifier = modifier
            .padding(
                paddingValues
            ),
        text = text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun XyItemText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = Modifier
            .then(modifier),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        maxLines = maxLines
    )
}

@Composable
fun XyItemTextPadding(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    horizontal: Dp = XyTheme.dimens.innerVerticalPadding,
    vertical: Dp = XyTheme.dimens.innerVerticalPadding,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = horizontal,
                vertical = vertical
            ),
        text = text,
        maxLines = maxLines,
        overflow = overflow,
        color = color,
        style = MaterialTheme.typography.titleSmall,
    )
}


@Composable
fun XyItemTextLarge(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Text(
        modifier = Modifier
            .then(modifier),
        text = text,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

@Composable
fun XyItemText(
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
        Text(
            text = text,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
        )
        sub?.let {
            Text(
                text = sub,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }


    }
}


@Composable
fun XyItemTextPadding(modifier: Modifier = Modifier, text: String, sub: String) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding / 2
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )

        SelectionContainer {
            Text(
                text = sub,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun XyItemTextCheckSelectHeightSmall(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: (() -> Unit)? = null
) {
    XyRow(
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable { onClick?.invoke() }
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Unspecified),
    ) {
        XyItemText(
            modifier = Modifier
                .weight(1f),
            text = text,
        )
        if (selected)
            Icon(imageVector = Icons.Rounded.Check, contentDescription = "选中${text}")
    }
}

/**
 * 列表item 增加尾部选中显示对号Icon
 * 高度为正常二分之一
 */

@Composable
fun XyItemTextIconCheckSelectHeightSmall(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    onIfSelected: () -> Boolean = { false },
    onClick: () -> Unit,
) {
    XyRowHeightSmall(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable { onClick.invoke() },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (icon == null) {
                Spacer(modifier = Modifier.width(24.dp))
            }
            icon?.let { Icon(imageVector = icon, contentDescription = text) }
            XyItemText(text = text)
        }
        if (onIfSelected())
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "",
                modifier = Modifier
            )
    }
}

/**
 * 列表item 增加尾部选中显示对号Icon
 * 高度正常
 */
@Composable
fun XyItemTextIconCheckSelect(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    onIfSelected: () -> Boolean = { false },
    onClick: () -> Unit,
) {
    XyRow(
        modifier = modifier.debounceClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onClick()
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (icon == null) {
                Spacer(modifier = Modifier.width(24.dp))
            }
            icon?.let { Icon(imageVector = icon, contentDescription = text) }
            XyItemText(text = text)
        }
        if (onIfSelected())
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "",
                modifier = Modifier
            )
    }
}

@Composable
fun XyItemSwitcherNotTextColor(
    modifier: Modifier = Modifier,
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    text: String,
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .debounceClickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        XyItemText(
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

@Composable
fun XyItemSwitcherNotPadding(
    modifier: Modifier = Modifier,
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    text: String
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .heightIn(min = 28.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .debounceClickable(enabled = enabled) {
                onChange(!state)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {

        XyItemText(
            modifier = Modifier,
            text = text,
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        Switch(
            checked = state, onCheckedChange = onChange, enabled = enabled,
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = Color.Transparent,
                checkedThumbColor = MaterialTheme.colorScheme.surface
            )
        )
    }

}

@Composable
fun XyItemTabButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    imageVector: ImageVector,
    enabled: Boolean = true,
    iconColor: Color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = 0.3f
    ),
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest
) {
    Surface(
        enabled = enabled,
        modifier = modifier.padding(vertical = XyTheme.dimens.contentPadding),
        onClick = onClick,
        color = color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            XyRoundedSurface(color = color) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = text,
                    tint = iconColor,
                    modifier = Modifier.padding(
                        XyTheme.dimens.contentPadding
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            XyItemText(
                text = text,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.3f
                )
            )
        }

    }
}

@Composable
fun XyItemTabBigButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    sub: String? = null,
    imageVector: ImageVector,
    enabled: Boolean = true,
    iconColor: Color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = 0.3f
    ),
    brush: Brush
) {
    Column(
        modifier = modifier
            .background(brush, RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) {
                onClick.invoke()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {

        Icon(
            imageVector = imageVector,
            contentDescription = text,
            tint = iconColor,
            modifier = Modifier
                .padding(
                    top = XyTheme.dimens.outerVerticalPadding,
                    bottom = XyTheme.dimens.outerVerticalPadding / 2
                )
        )
        XyItemText(
            text = text,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding / 2))
        sub?.let {
            XyItemText(
                text = sub,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
        }

    }
}

@Composable
fun XyItemTextHorizontal(text: String, modifier: Modifier = Modifier) {
    XyItemText(
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding
            ),
        text = text
    )
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
            XyItemText(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            sub?.let {
                XyItemText(
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
