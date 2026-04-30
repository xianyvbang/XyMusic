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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

/**
 * JVM 桌面端右侧弹层。
 *
 * 使用时建议把它放在页面根 Box 的最后一个子项，让遮罩和 Sheet 覆盖在页面内容之上。
 *
 * @param modifier 整个遮罩层和 Sheet 容器的修饰符，默认铺满父布局。
 * @param sheetModifier 右侧 Sheet 本体的修饰符，可用于追加宽高、padding 或测试标记。
 * @param sheetWidth Sheet 的默认宽度。
 * @param sheetMaxWidth Sheet 的最大宽度，避免宽屏下内容面板过宽。
 * @param containerColor Sheet 背景色。
 * @param scrimColor 遮罩颜色，默认是半透明黑色。
 * @param contentPaddingValues Sheet 内容区域的内边距，不包含标题内部自己的 padding。
 * @param shape Sheet 外形，默认只给左上角和左下角圆角。
 * @param tonalElevation Material3 色调阴影高度。
 * @param animationDurationMillis 遮罩淡入淡出和 Sheet 横向滑动动画时长，单位毫秒。
 * @param dismissOnScrimClick 点击遮罩时是否关闭 Sheet。
 * @param onIfDisplay 返回当前是否显示 Sheet。
 * @param onClose 请求关闭 Sheet 时触发；参数保持与底部 Sheet 封装一致，遮罩关闭时传入 false。
 * @param titleText 标题文字，为 null 时不显示标题区域。
 * @param titleSub 副标题文字，只有 titleText 不为 null 时才会显示。
 * @param titleTailContent 标题行右侧自定义内容，例如关闭按钮、确认按钮等。
 * @param content Sheet 主体内容。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModalSideSheetExtendComponent(
    modifier: Modifier = Modifier,
    sheetModifier: Modifier = Modifier,
    sheetWidth: Dp = 360.dp,
    sheetMaxWidth: Dp = 480.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    scrimColor: Color = Color.Black.copy(alpha = 0.32f),
    contentPaddingValues: PaddingValues = PaddingValues(
        vertical = XyTheme.dimens.outerVerticalPadding
    ),
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = XyTheme.dimens.dialogCorner,
        bottomStart = XyTheme.dimens.dialogCorner
    ),
    tonalElevation: Dp = 6.dp,
    animationDurationMillis: Int = 220,
    dismissOnScrimClick: Boolean = true,
    onIfDisplay: () -> Boolean,
    onClose: (Boolean) -> Unit,
    titleText: String? = null,
    titleSub: String? = null,
    titleTailContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // 使用 MutableTransitionState 保留退出动画，避免 visible 变为 false 时组件立刻从组合中移除。
    val visibleState = remember {
        MutableTransitionState(false)
    }
    val visible = onIfDisplay()

    LaunchedEffect(visible) {
        visibleState.targetState = visible
    }

    if (!visibleState.currentState && !visibleState.targetState) {
        return
    }

    val scrimInteractionSource = remember { MutableInteractionSource() }
    val sheetInteractionSource = remember { MutableInteractionSource() }

    // 根容器覆盖整个页面：先绘制遮罩，再绘制右侧 Sheet。
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(animationDurationMillis)),
            exit = fadeOut(animationSpec = tween(animationDurationMillis))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable(
                        interactionSource = scrimInteractionSource,
                        indication = null,
                        enabled = dismissOnScrimClick,
                        onClick = {
                            onClose(false)
                        }
                    )
            )
        }

        // Sheet 从右侧滑入/滑出，适合桌面端详情面板、设置面板等场景。
        AnimatedVisibility(
            visibleState = visibleState,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(
                animationSpec = tween(animationDurationMillis),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(animationSpec = tween(animationDurationMillis)),
            exit = slideOutHorizontally(
                animationSpec = tween(animationDurationMillis),
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(animationSpec = tween(animationDurationMillis))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(sheetWidth)
                    .widthIn(max = sheetMaxWidth)
                    .then(sheetModifier)
                    // 消费 Sheet 内部点击，避免点击内容区域时触发外层遮罩关闭。
                    .clickable(
                        interactionSource = sheetInteractionSource,
                        indication = null,
                        onClick = {}
                    ),
                color = containerColor,
                shape = shape,
                tonalElevation = tonalElevation
            ) {
                Column(
                    modifier = Modifier.padding(contentPaddingValues),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    titleText?.let {
                        XyRow(
                            paddingValues = PaddingValues(
                                top = XyTheme.dimens.innerVerticalPadding,
                                start = XyTheme.dimens.innerHorizontalPadding,
                                end = XyTheme.dimens.innerHorizontalPadding,
                                bottom = XyTheme.dimens.innerVerticalPadding
                            )
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                XyText(
                                    text = titleText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                titleSub?.let {
                                    Spacer(modifier = Modifier.height(XyTheme.dimens.innerVerticalPadding))
                                    XyTextSub(
                                        text = titleSub,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            titleTailContent?.invoke(this)
                        }
                    }
                    content()
                }
            }
        }
    }
}

/**
 * JVM 桌面端右侧全高弹层的简化版本。
 *
 * 这个版本不提供标题区域，也不添加默认内容内边距，适合承载播放器详情、
 * 完整设置页这类需要自行控制布局的内容。
 *
 * @param modifier 整个遮罩层和 Sheet 容器的修饰符，默认铺满父布局。
 * @param sheetModifier 右侧 Sheet 本体的修饰符，可用于追加宽高、padding 或测试标记。
 * @param sheetWidth Sheet 的默认宽度。
 * @param sheetMaxWidth Sheet 的最大宽度，默认不限制。
 * @param containerColor Sheet 背景色。
 * @param scrimColor 遮罩颜色，默认是半透明黑色。
 * @param shape Sheet 外形，默认只给左上角和左下角圆角。
 * @param tonalElevation Material3 色调阴影高度。
 * @param animationDurationMillis 遮罩淡入淡出和 Sheet 横向滑动动画时长，单位毫秒。
 * @param dismissOnScrimClick 点击遮罩时是否关闭 Sheet。
 * @param onIfDisplay 返回当前是否显示 Sheet。
 * @param onClose 请求关闭 Sheet 时触发；参数保持与底部 Sheet 封装一致，遮罩关闭时传入 false。
 * @param content Sheet 主体内容。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModalSideSheetExtendFillMaxSizeComponent(
    modifier: Modifier = Modifier,
    sheetModifier: Modifier = Modifier,
    sheetWidth: Dp = 480.dp,
    sheetMaxWidth: Dp = Dp.Unspecified,
    containerColor: Color = MaterialTheme.colorScheme.background,
    scrimColor: Color = Color.Black.copy(alpha = 0.32f),
    shape: RoundedCornerShape = RoundedCornerShape(
        topStart = XyTheme.dimens.dialogCorner,
        bottomStart = XyTheme.dimens.dialogCorner
    ),
    tonalElevation: Dp = 6.dp,
    animationDurationMillis: Int = 220,
    dismissOnScrimClick: Boolean = true,
    onIfDisplay: () -> Boolean,
    onClose: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalSideSheetExtendComponent(
        modifier = modifier,
        sheetModifier = sheetModifier,
        sheetWidth = sheetWidth,
        sheetMaxWidth = sheetMaxWidth,
        containerColor = containerColor,
        scrimColor = scrimColor,
        contentPaddingValues = PaddingValues(),
        shape = shape,
        tonalElevation = tonalElevation,
        animationDurationMillis = animationDurationMillis,
        dismissOnScrimClick = dismissOnScrimClick,
        onIfDisplay = onIfDisplay,
        onClose = onClose,
        content = content
    )
}
