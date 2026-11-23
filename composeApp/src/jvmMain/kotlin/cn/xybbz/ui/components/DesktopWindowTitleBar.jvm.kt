package cn.xybbz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.router.Navigator
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.screens.jvmRouterMenuWidth
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopInteractiveHitTestOwner
import cn.xybbz.ui.windows.DesktopTooltipBox
import cn.xybbz.ui.xy.XyText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.app_name
import xymusic.composeapp.generated.resources.arrow_back_24px
import xymusic.composeapp.generated.resources.back
import xymusic.composeapp.generated.resources.dark
import xymusic.composeapp.generated.resources.light
import xymusic.composeapp.generated.resources.refresh_24px
import xymusic.composeapp.generated.resources.refresh_login
import xymusic.composeapp.generated.resources.system
import xymusic.composeapp.generated.resources.theme_mode
import cn.xybbz.ui.windows.DesktopWindowTitleBar as UiDesktopWindowTitleBar

/**
 * 桌面端主标题栏。
 * 左侧只展示应用名称，中间是固定在内容区左边缘的返回按钮；
 * 右侧依次为刷新登录按钮、主题模式切换按钮与窗口控制按钮。
 */
@Composable
fun DesktopWindowTitleBar(navigator: Navigator) {
    UiDesktopWindowTitleBar(
        backgroundColor = DesktopTitleBarColors.current.background,
        front = { _ ->
            DesktopTitleBrand()
        },
        middle = { hitTestOwner ->
            DesktopTitleCenter(
                navigator = navigator,
                hitTestOwner = hitTestOwner
            )
        },
        beforeWindowControls = { hitTestOwner ->
            DesktopTitleActions(
                hitTestOwner = hitTestOwner
            )
        }
    )
}

/**
 * 标题栏左侧品牌区。
 * 只展示应用名称，宽度固定为左侧导航栏宽度，
 * 使其右边缘与下方右侧内容区左边缘对齐。
 */
@Composable
private fun DesktopTitleBrand() {
    val colors = DesktopTitleBarColors.current

    Row(
        modifier = Modifier
            // 宽度与左侧导航栏保持一致，保证中间区域从内容区左边缘开始
            .width(jvmRouterMenuWidth)
            .fillMaxHeight()
            .padding(start = XyTheme.dimens.outerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    ) {
        XyText(
            text = stringResource(Res.string.app_name),
            color = colors.foreground,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 标题栏中间操作区。
 * 搜索框已移动到左侧边栏顶部，这里只保留固定在内容区左边缘的返回按钮。
 */
@Composable
private fun DesktopTitleCenter(
    navigator: Navigator,
    hitTestOwner: DesktopInteractiveHitTestOwner,
) {
    // 当前一级路由栈，用于判断是否存在可回退页面
    val currentStack = navigator.state.backStacks[navigator.state.topLevelRoute]
    // 只有存在可回退页面时才允许点击返回
    val canGoBack = navigator.state.topLevelRoute != navigator.state.startRoute ||
            currentStack?.lastOrNull() != navigator.state.topLevelRoute

    Row(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    ) {
        // 返回按钮：品牌区宽度已固定为左侧导航栏宽度，这里天然位于内容区左边缘
        DesktopTitleBarPlainIconButton(
            resource = Res.drawable.arrow_back_24px,
            enabled = canGoBack,
            tooltip = stringResource(Res.string.back),
            contentDescription = stringResource(Res.string.back),
            onClick = navigator::goBack,
            modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "BackButton"),
        )
    }
}

/**
 * 标题栏右侧操作区。
 * 只保留刷新登录按钮和主题模式切换按钮，数据源切换入口已收敛到左侧栏的连接管理。
 */
@Composable
private fun DesktopTitleActions(
    hitTestOwner: DesktopInteractiveHitTestOwner,
) {
    val koin = getKoin()
    val sessionCoordinator: SessionCoordinator = remember { koin.get() }
    val settingsManager: SettingsManager = remember { koin.get() }
    val coroutineScope = rememberCoroutineScope()
    // 当前主题模式，用于决定月牙按钮展示的图标与循环切换的下一档
    val themeType by settingsManager.themeType.collectAsState()
    val colors = DesktopTitleBarColors.current

    Row(
        modifier = Modifier.padding(end = XyTheme.dimens.outerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        // 刷新登录按钮：沿用原有按钮样式，位置保持在主题切换按钮左侧
        DesktopToolbarIconButton(
            resource = Res.drawable.refresh_24px,
            enabled = true,
            tooltip = stringResource(Res.string.refresh_login),
            onClick = {
                coroutineScope.launch {
                    sessionCoordinator.retry()
                }
            },
            modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "RefreshButton"),
            contentDescription = stringResource(Res.string.refresh_login)
        )

        // 月牙形主题模式切换按钮：按 跟随系统 -> 深色 -> 浅色 顺序循环
        DesktopThemeModeButton(
            themeType = themeType,
            tint = colors.foreground,
            onCycle = { nextThemeType ->
                coroutineScope.launch {
                    settingsManager.setThemeTypeData(nextThemeType)
                }
            },
            modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "ThemeModeButton"),
        )
    }
}

/**
 * 主题模式切换按钮。
 * 按钮为圆形，图标随当前模式在月牙、太阳与跟随系统之间变化。
 */
@Composable
private fun DesktopThemeModeButton(
    themeType: ThemeTypeEnum,
    tint: Color,
    onCycle: (ThemeTypeEnum) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DesktopTitleBarColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val tooltip = "${stringResource(Res.string.theme_mode)}：${themeType.modeLabel()}"

    DesktopTooltipBox(tooltip = tooltip) {
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (hovered) colors.iconButtonBackground else Color.Transparent)
                .jvmHoverDebounceClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onCycle(themeType.nextThemeMode()) },
                ),
            contentAlignment = Alignment.Center
        ) {
            DesktopThemeModeIcon(
                themeType = themeType,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * 主题模式图标。
 * 使用 Canvas 直接绘制，避免为该按钮额外新增图片资源。
 */
@Composable
private fun DesktopThemeModeIcon(
    themeType: ThemeTypeEnum,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        // 统一描边宽度，保证三种图标观感一致
        val strokeWidth = size.minDimension * 0.09f
        val baseRadius = size.minDimension * 0.36f

        when (themeType) {
            ThemeTypeEnum.DARK -> {
                // 月牙：整圆减去一个向右上偏移的圆
                val outer = Path().apply {
                    addOval(Rect(center = center, radius = baseRadius))
                }
                val cutCenter = Offset(
                    x = center.x + baseRadius * 0.55f,
                    y = center.y - baseRadius * 0.35f
                )
                val cut = Path().apply {
                    addOval(Rect(center = cutCenter, radius = baseRadius * 0.9f))
                }
                val crescent = Path().apply {
                    // 用整圆减去偏移圆得到月牙轮廓
                    op(outer, cut, PathOperation.Difference)
                }
                drawPath(crescent, tint)
            }

            ThemeTypeEnum.LIGHT -> {
                // 太阳：中心实心圆加八条射线
                drawCircle(color = tint, radius = baseRadius * 0.62f)
                val rayStart = baseRadius * 0.88f
                val rayEnd = baseRadius * 1.22f
                repeat(8) { index ->
                    // 每 45 度画一条射线
                    val angle = index * (Math.PI / 4).toFloat()
                    val directionX = kotlin.math.cos(angle)
                    val directionY = kotlin.math.sin(angle)
                    drawLine(
                        color = tint,
                        start = Offset(
                            x = center.x + directionX * rayStart,
                            y = center.y + directionY * rayStart,
                        ),
                        end = Offset(
                            x = center.x + directionX * rayEnd,
                            y = center.y + directionY * rayEnd,
                        ),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }

            ThemeTypeEnum.SYSTEM -> {
                // 跟随系统：圆环加右半圆实心
                drawCircle(
                    color = tint,
                    radius = baseRadius,
                    style = Stroke(width = strokeWidth)
                )
                val arcRect = Rect(center = center, radius = baseRadius)
                drawArc(
                    color = tint,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size,
                )
            }
        }
    }
}

/**
 * 标题栏中的通用小图标按钮，用于刷新登录等轻量操作。
 * 默认无背景与边框，仅在鼠标悬停时显示浅色背景。
 */
@Composable
private fun DesktopToolbarIconButton(
    resource: DrawableResource,
    enabled: Boolean,
    tooltip: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = DesktopTitleBarColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val iconTint = if (enabled) {
        colors.foreground
    } else {
        colors.foreground.copy(alpha = 0.45f)
    }
    DesktopTooltipBox(tooltip = tooltip) {
        Box(
            modifier = modifier
                .size(36.dp)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(
                    if (hovered && enabled) {
                        colors.iconButtonBackground
                    } else {
                        Color.Transparent
                    }
                )
                .jvmHoverDebounceClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource),
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = iconTint
            )
        }
    }

}

/**
 * 标题栏中的无底色图标按钮，用于返回等紧贴面包屑的轻量操作。
 */
@Composable
private fun DesktopTitleBarPlainIconButton(
    resource: DrawableResource,
    enabled: Boolean,
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = DesktopTitleBarColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val iconTint = if (enabled) {
        colors.foreground
    } else {
        colors.foreground.copy(alpha = 0.35f)
    }

    DesktopTooltipBox(tooltip = tooltip) {
        Box(
            modifier = modifier
                .size(30.dp)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(
                    if (hovered && enabled) {
                        colors.iconButtonBackground
                    } else {
                        Color.Transparent
                    }
                )
                .jvmHoverDebounceClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource),
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = iconTint
            )
        }
    }
}

/**
 * 标题栏使用的颜色集合，统一从当前 MaterialTheme 派生。
 */
private data class DesktopTitleBarColors(
    val background: Color,
    val iconButtonBackground: Color,
    val foreground: Color,
) {
    companion object {
        val current: DesktopTitleBarColors
            @Composable
            get() = DesktopTitleBarColors(
                background = MaterialTheme.colorScheme.background,
                iconButtonBackground = MaterialTheme.colorScheme.surfaceContainerLowest,
                foreground = MaterialTheme.colorScheme.onSurface,
            )
    }
}

/**
 * 主题模式循环顺序：跟随系统 -> 深色 -> 浅色 -> 跟随系统。
 */
private fun ThemeTypeEnum.nextThemeMode(): ThemeTypeEnum = when (this) {
    ThemeTypeEnum.SYSTEM -> ThemeTypeEnum.DARK
    ThemeTypeEnum.DARK -> ThemeTypeEnum.LIGHT
    ThemeTypeEnum.LIGHT -> ThemeTypeEnum.SYSTEM
}

/**
 * 主题模式的悬浮提示文案。
 */
@Composable
private fun ThemeTypeEnum.modeLabel(): String = when (this) {
    ThemeTypeEnum.SYSTEM -> stringResource(Res.string.system)
    ThemeTypeEnum.DARK -> stringResource(Res.string.dark)
    ThemeTypeEnum.LIGHT -> stringResource(Res.string.light)
}

private fun Modifier.desktopTitleBarHitTarget(
    owner: DesktopInteractiveHitTestOwner,
    targetId: String,
): Modifier = onGloballyPositioned { coordinates ->
    owner.updateBounds(targetId, coordinates.boundsInWindow())
}
