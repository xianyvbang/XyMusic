/*
 * SaltUI
 * Copyright (C) 2023 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UNUSED")

package cn.xybbz.ui.xy

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.R
import cn.xybbz.ui.Text
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.noRippleClickable
import cn.xybbz.ui.popup.PopupMenu
import cn.xybbz.ui.popup.PopupState
import cn.xybbz.ui.theme.XyTheme

/**
 * Build content interface title text.
 */
@Composable
fun ItemTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall
    )
}

/**
 * Build content interface instruction text.
 *
 * @param text text
 */
@Composable
fun ItemText(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = XyTheme.dimens.innerHorizontalPadding),
        style = MaterialTheme.typography.titleSmall
    )
}

/**
 * Build item for the content interface.
 *
 * @param onClick will be called when user clicks on the element
 * @param enabled enabled
 * @param iconPainter icon
 * @param iconPaddingValues iconPaddingValues
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original color
 * @param text main text
 * @param sub sub text
 * @param subColor color of [sub] text
 */
@Composable
fun Item(
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String? = null,
    subColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
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
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    color = subColor,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        Icon(
            modifier = Modifier
                .size(20.dp),
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Build a switcher in the content interface
 *
 * @param state the state of the switcher
 * @param onChange called when state changed
 * @param enabled
 * @param iconPainter icon
 * @param iconPaddingValues iconPaddingValues
 * @param iconColor color of [iconPainter], if this value is null, will use the paint original color
 * @param text main text
 * @param sub sub text
 */
@Composable
fun ItemSwitcher(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
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
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            sub?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        val backgroundColor by animateColorAsState(
            targetValue = if (state) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.1f
            ),
            animationSpec = spring(),
            label = "backgroundColor"
        )
        Box(
            modifier = Modifier
                .size(46.dp, 26.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(color = backgroundColor)
                }
                .padding(5.dp)
        ) {
            val layoutDirection = LocalLayoutDirection.current
            val translationX by animateDpAsState(
                targetValue = if (state) {
                    when (layoutDirection) {
                        LayoutDirection.Ltr -> 20.dp
                        LayoutDirection.Rtl -> (-20).dp
                    }
                } else {
                    0.dp
                },
                animationSpec = spring(),
                label = "startPadding"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = translationX.toPx()
                    }
                    .size(16.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
            )
        }
    }
}

/**
 * Popup Item
 *
 * @param state the state of popup
 */
@Composable
fun ItemPopup(
    state: PopupState,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
    sub: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled) {
                    state.expend()
                }
                .padding(
                    horizontal = XyTheme.dimens.innerHorizontalPadding,
                    vertical = XyTheme.dimens.innerVerticalPadding
                ),
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = text,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            Icon(
                modifier = Modifier
                    .size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PopupMenu(
            expanded = state.expend,
            onDismissRequest = {
                state.dismiss()
            }
        ) {
            content()
        }
    }
}

/**
 * Build a switcher in the content interface
 *
 * @param state the state of the switcher
 * @param onChange called when state changed
 * @param enabled
 * @param text main text
 */
@Composable
fun ItemCheck(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp),
            painter = if (state) painterResource(id = R.drawable.ic_check) else painterResource(id = R.drawable.ic_uncheck),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Value
 */
@Composable
fun ItemValue(
    text: String,
    sub: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 48.dp)
            .padding(vertical = XyTheme.dimens.innerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .sizeIn(
                    maxWidth = 80.dp
                )
                .weight(1f)
                .padding(start = XyTheme.dimens.innerHorizontalPadding),
            text = text
        )

        Row(
            modifier = Modifier
                .weight(3f)
                .padding(
                    start = XyTheme.dimens.contentPadding,
                    end = XyTheme.dimens.innerHorizontalPadding
                ),
            horizontalArrangement = Arrangement.End
        ) {
            SelectionContainer {
                Text(
                    text = sub,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/**
 * Edit
 *
 * @param text text
 * @param onChange called when text changed
 * @param modifier modifier
 * @param hint hint
 * @param hintColor color of [hint] text
 * @param readOnly readOnly
 * @param paddingValues padding in this, beautiful for IME
 * @param keyboardOptions keyboardOptions
 * @param keyboardActions keyboardActions
 * @param visualTransformation visualTransformation
 * @param actionContent actionContent
 */
@Composable
fun ItemEdit(
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
        textStyle = MaterialTheme.typography.titleSmall,
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

/**
 * Password Edit
 *
 * @param text text
 * @param onChange called when text changed
 * @param hint hint
 * @param hintColor color of [hint] text
 * @param readOnly readOnly
 * @param paddingValues padding in this, beautiful for IME
 * @param keyboardOptions keyboardOptions
 * @param keyboardActions keyboardActions
 */
@Composable
fun ItemEditPassword(
    text: String,
    onChange: (String) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
    hint: String? = null,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    readOnly: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.innerHorizontalPadding,
        vertical = XyTheme.dimens.innerVerticalPadding
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var hidden by remember { mutableStateOf(true) }

    ItemEdit(
        text = text,
        onChange = onChange,
        backgroundColor = backgroundColor,
        hint = hint,
        hintColor = hintColor,
        readOnly = readOnly,
        paddingValues = paddingValues,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
        actionContent = {
            Icon(
                painter = painterResource(
                    id = if (hidden) {
                        R.drawable.ic_closed_eye
                    } else {
                        R.drawable.ic_eye
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .noRippleClickable {
                        hidden = !hidden
                    }
                    .padding(XyTheme.dimens.contentPadding)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
fun ItemSlider(
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
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            sub?.let {
                Text(
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

/**
 * Build vertical spacing for the content interface.
 */
@Composable
fun ItemSpacer() {
    Spacer(
        modifier = Modifier
            .height(XyTheme.dimens.contentPadding)
    )
}

/**
 * Build a container with internal margins in the content interface, making it easy to add custom elements such as buttons internally.
 */
@Composable
fun ItemContainer(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            )
    ) {
        content()
    }
}


/**
 * 列表的item
 * 列表项
 * @param [modifier] 修饰语
 * @param [onClick] 点击时
 * @param [enabled] 已启用
 * @param [headlineContent] 标题内容
 * @param [text] 文本
 * @param [sub] 子文本
 * @param [trailingContent] 尾随内容
 */
@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    headlineContent: (@Composable () -> Unit)? = null,
    text: String,
    sub: String,
    trailingContent: (@Composable RowScope.() -> Unit)?
) {

    Box {
        Row(
            modifier = Modifier
                .then(modifier)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .alpha(if (enabled) 1f else 0.5f)
                .debounceClickable(enabled = enabled) {
                    onClick?.invoke()
                }
                .padding(
                    horizontal = XyTheme.dimens.innerHorizontalPadding,
                    vertical = XyTheme.dimens.innerVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            headlineContent?.let {
                headlineContent.invoke()
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = text,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
            trailingContent?.invoke(this)
        }
    }


}