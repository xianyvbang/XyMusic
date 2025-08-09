package cn.xybbz.ui.xy


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.noRippleClickable
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
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    brush: Brush? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
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
            .debounceClickable(enabled = enabled) {
                onClick?.invoke()
            },
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
        supportingContent = {
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
        },
        leadingContent = {
            if (index == null)
                XySmallImage(
                    model = imgUrl,
                    contentDescription = "${name}的图片"
                )
            else
                Text(text = index.toString(), style = MaterialTheme.typography.bodySmall)
        }, trailingContent = {
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


/**
 * 列表单项
 * @param [modifier] 修饰符
 * @param [name] 名称
 * @param [subordination] 次要内容
 * @param [img] 图片地址 默认封面 https://pixabay.com/zh/vectors/cd-music-audio-notes-mp3-sound-158817/
 * @param [media] 标签
 * @param [enabled] 是否启用
 * @param [backgroundColor] 背景颜色
 * @param [onClick] 点击方法
 */
@Composable
fun ItemTrailingContent(
    modifier: Modifier = Modifier,
    name: String,
    subordination: String?,
    img: Painter? = null,
    media: String? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = backgroundColor),
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) {
                onClick?.invoke()
            },
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
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                media?.let {
                    XyItemSub(media = media)
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
                contentDescription = "${name}的图片"
            )
        }, trailingContent = {
            trailingContent?.invoke()
        }
    )
}

/**
 * 列表单项
 * @param [modifier] 修饰符
 * @param [name] 名称
 * @param [subordination] 次要内容
 * @param [img] 图片地址 默认封面 https://pixabay.com/zh/vectors/cd-music-audio-notes-mp3-sound-158817/
 * @param [enabled] 是否启用
 * @param [backgroundColor] 背景颜色
 * @param [onClick] 点击方法
 * @param [trailingOnClick] 尾部点击方法
 */
@Composable
fun Item(
    modifier: Modifier = Modifier,
    name: String,
    subordination: String?,
    img: Painter? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailingOnClick: (() -> Unit)? = null
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = backgroundColor),
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) {
                onClick?.invoke()
            },
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
                contentDescription = "${name}的图片"
            )
        }, trailingContent = {
            IconButton(
                modifier = Modifier.offset(x = (10).dp),
                onClick = {
                    trailingOnClick?.invoke()
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "${name}的其他操作按钮"
                )
            }

        }
    )
}

@Composable
fun XyItemSub(modifier: Modifier = Modifier, media: String) {
    Box(
        modifier = Modifier
            .then(modifier)
            .border(
                border = BorderStroke(
                    1.dp,
                    Color(0xFFCADBFF)
                ), shape = RoundedCornerShape(8.dp)
            ),
    ) {

        Text(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
            text = media,
            lineHeight = 12.sp,
            fontSize = 10.sp,
            color = Color(0xFFCADBFF),
        )

    }
}


/**
 * 列表item 头部显示index
 * @param [modifier] 修饰符
 * @param [index] 索引
 * @param [name] 名称
 * @param [subordination] 次要内容
 * @param [media] 标签
 * @param [enabled] 是否启用
 * @param [backgroundColor] 背景颜色
 * @param [onClick] 点击方法
 * @param [trailingContent] 尾部内容
 */
@Composable
fun ItemIndex(
    modifier: Modifier = Modifier,
    index: Int,
    name: String,
    subordination: String?,
    favoriteState: Boolean,
    media: String?,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ItemTrailingContent(
        backgroundColor = backgroundColor,
        index = index,
        name = name,
        subordination = subordination,
        favoriteState = favoriteState,
        media = media,
        enabled = enabled,
        textColor = textColor,
        onClick = onClick,
        trailingContent = trailingContent
    )
}

/**
 * 列表item 尾部增加右箭头
 * @param [modifier] 修饰符
 * @param [name] 名称
 * @param [subordination] 次要内容
 * @param [imgUrl] 图片地址 默认封面 https://pixabay.com/zh/vectors/cd-music-audio-notes-mp3-sound-158817/
 * @param [toneQuality] 标签
 * @param [enabled] 是否启用
 * @param [backgroundColor] 背景颜色
 * @param [onClick] 点击方法
 * @param [trailingOnClick] 尾部点击方法
 */
@Composable
fun ItemTrailingArrowRight(
    modifier: Modifier = Modifier,
    name: String,
    subordination: String?,
    imgUrl: String? = null,
    toneQuality: String? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onClick: (() -> Unit)? = null,
    trailingOnClick: (() -> Unit)? = null
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = backgroundColor),
        modifier = modifier
            .height(XyTheme.dimens.itemHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable(enabled = enabled) {
                onClick?.invoke()
            },
        headlineContent = {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                        textAlign = TextAlign.Center
                    )
                }
            }


        },
        leadingContent = {
            XySmallImage(
                model = imgUrl,
                contentDescription = "${name}的图片"
            )
        }, trailingContent = {
            IconButton(onClick = composeClick {
                trailingOnClick?.invoke()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "查看${name}中的信息"
                )
            }


        }
    )
}


/**
 * 列表item 尾部增加右箭头
 * @param [modifier] 修饰符
 * @param [name] 名称
 * @param [subordination] 次要内容
 * @param [img] 本地图片信息
 * @param [toneQuality] 标签
 * @param [enabled] 是否启用
 * @param [backgroundColor] 背景颜色
 * @param [onClick] 点击方法
 * @param [trailingOnClick] 尾部点击方法
 */
@Composable
fun ItemTrailingArrowRight(
    modifier: Modifier = Modifier,
    trailingModifier: Modifier = Modifier,
    name: String,
    subordination: String?,
    img: Painter? = null,
    toneQuality: String? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    onClick: (() -> Unit)? = null,
    trailingOnClick: (() -> Unit)? = null
) {

    ItemTrailingArrowRight(
        modifier = modifier,
        name = name,
        subordination = subordination,
        img = img,
        toneQuality = toneQuality,
        enabled = enabled,
        backgroundColor = backgroundColor,
        textColor = textColor,
        shadowElevation = shadowElevation,
        onClick = onClick,
        trailingContent = {
            IconButton(
                modifier = trailingModifier,
                onClick = composeClick {
                    trailingOnClick?.invoke()
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "查看${name}中的信息"
                )
            }
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
                contentDescription = "${name}的图片"
            )
        }, trailingContent = trailingContent
    )
}

@Composable
fun ItemTitleCenter(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall
        )
    }

}

@Composable
fun XyItemTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun XyItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun XyItemBigTitle(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun XyItemBig(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 1,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        color = color,
        style = MaterialTheme.typography.titleLarge,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun XyItemMedium(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 1,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun XyItemTitlePadding(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        text = text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun XyItemTitleNotHorizontalPadding(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier
            .padding(
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        text = text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun XyText(
    text: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = textColor
    )
}

@Composable
fun XyItemText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = Modifier
            .then(modifier),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color
    )
}

@Composable
fun XyItemTextButton(
    modifier: Modifier = Modifier,
    text: String,
    enable: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    XyItemText(
        modifier = Modifier
            .then(modifier)
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enable
            ) {
                onClick?.invoke()
            },
        text = text,
        color = if (enable) color else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun XyItemTextPadding(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    XyItemTextPadding(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        overflow = overflow,
        color = color,
        horizontal = XyTheme.dimens.innerVerticalPadding,
        vertical = XyTheme.dimens.innerVerticalPadding,
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
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = Modifier
            .then(modifier),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color
    )
}

@Composable
fun XyItemTextLargePadding(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        text = text,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun XyItemTextBig(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = Modifier
            .then(modifier),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun XyItemTextBigPadding(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun XyItemTextAlignEnd(modifier: Modifier = Modifier, text: String, maxLines: Int) {
    Text(
        modifier = Modifier
            .then(modifier),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        textAlign = TextAlign.End,
        maxLines = maxLines
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
fun XyItemTextButton(
    modifier: Modifier = Modifier,
    text: String,
    sub: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    XyItemText(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick?.invoke()
            },
        text = text,
        sub = sub
    )
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
fun XyItemTextPadding(
    modifier: Modifier = Modifier,
    text: String,
    sub: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable {
                onClick.invoke()
            }
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
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

        Text(
            text = sub,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun XyItemSelect(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            )
            .noRippleClickable {
                onClick()
            },
        color = color,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun XyItemTextCheckSelectHeightSmall(
    modifier: Modifier = Modifier,
    text: String,
    select: Boolean,
    onClick: (() -> Unit)? = null
) {
    XyRowHeightSmall(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable { onClick?.invoke() },
    ) {
        XyItemText(
            modifier = Modifier
                .weight(1f),
            text = text,
        )
        if (select)
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
        modifier = modifier.noRippleClickable {
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
fun XyItemTextIconRow(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    actionIcon: @Composable () -> Unit,
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
            icon?.let { Icon(imageVector = icon, contentDescription = text) }
            Text(text = text)
        }
        actionIcon()
    }
}

@Composable
fun XyItemSwitcher(
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

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            color = if (enabled) Color(0xFF0470E6) else Color(0xFF8C8C8C)
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
            .heightIn(min = 56.dp)
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
) {
    Column(
        modifier = modifier.debounceClickable(enabled = enabled) {
            onClick()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = text,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        XyItemText(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f
            )
        )
    }
}


@Composable
fun XyItemNotClickEffectTabButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    imageVector: ImageVector,
    enabled: Boolean = true,
) {

    Column(
        modifier = modifier.debounceClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onClick()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = text,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        XyItemText(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f
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
    imageVector: ImageVector,
    iconSize: Dp = XyTheme.dimens.itemHeight,
    enabled: Boolean = true,
    iconColor: Color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = 0.3f
    ),
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest
) {
    Column(
        modifier = modifier
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLowest
                )
                .debounceClickable(enabled = enabled) {
                    onClick.invoke()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
                tint = iconColor,
                modifier = Modifier
                    .size(iconSize)
                    .padding(
                        XyTheme.dimens.contentPadding
                    )

            )

        }
        Spacer(modifier = Modifier.height(2.dp))
        XyItemTextButton(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f
            ),
            onClick = {
                onClick()
            }
        )
    }
}

@Composable
fun XyItemTabBigButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    sub: String? = null,
    imageVector: ImageVector,
    iconSize: Dp = XyTheme.dimens.itemHeight,
    enabled: Boolean = true,
    iconColor: Color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = 0.3f
    ),
    brush: Brush
) {
    Column(
        modifier = modifier
            .width(iconSize)
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
        XyItemTextButton(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.3f
            ),
            onClick = {
                onClick()
            }
        )
        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding / 2))
        sub?.let {
            XyItemTextButton(
                text = sub,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.3f
                ),
                onClick = {
                    onClick()
                }
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
        }

    }
}


@Composable
fun XyItemTabButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    iconContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .background(color)
            .debounceClickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding / 2,
                vertical = XyTheme.dimens.outerVerticalPadding / 2
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        iconContent()
        Spacer(modifier = Modifier.height(2.dp))
        text()
    }
}

@Composable
fun XyItemTextHorizontal(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding
            ),
        text = text,
        style = MaterialTheme.typography.titleSmall,
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
        Spacer(modifier = Modifier.height(12.dp))
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
fun XyItemSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    /*@IntRange(from = 0)*/
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    XySlider(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource
    )
}


/**
 * 输入框
 *
 * @param text 文本
 * @param onChange 当文本改变时调用
 * @param modifier modifier
 * @param hint 提示
 * @param hintColor [hint] 文本颜色
 * @param readOnly 只读模式
 * @param paddingValues 边缘填充, 线条流畅的 IME
 * @param keyboardOptions 键盘选项
 * @param keyboardActions 键盘操作
 * @param visualTransformation 视觉信息
 * @param actionContent 行动内容
 */
@Composable
fun XyItemEdit(
    text: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
    hint: String? = null,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    readOnly: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.innerHorizontalPadding,
        vertical = XyTheme.dimens.innerVerticalPadding
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null
) {
    BasicTextField(
        value = text,
        onValueChange = onChange,
        modifier = modifier
            .padding(paddingValues),
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface),
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .background(color = backgroundColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = XyTheme.dimens.contentPadding)
                ) {
                    innerTextField()
                    if (hint != null && text.isEmpty()) {
                        Text(
                            text = hint,
                            color = hintColor
                        )
                    }
                }
                if (actionContent != null) {
                    actionContent()
                } else {
                    Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                }
            }
        }
    )
}