package cn.xybbz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.router.About
import cn.xybbz.router.Album
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.Artist
import cn.xybbz.router.ArtistInfo
import cn.xybbz.router.CacheLimit
import cn.xybbz.router.Connection
import cn.xybbz.router.ConnectionInfo
import cn.xybbz.router.ConnectionManagement
import cn.xybbz.router.CustomApi
import cn.xybbz.router.DailyRecommend
import cn.xybbz.router.Download
import cn.xybbz.router.FavoriteList
import cn.xybbz.router.GenreInfo
import cn.xybbz.router.Genres
import cn.xybbz.router.Home
import cn.xybbz.router.InterfaceSetting
import cn.xybbz.router.LanguageConfig
import cn.xybbz.router.Local
import cn.xybbz.router.MemoryManagement
import cn.xybbz.router.Music
import cn.xybbz.router.Navigator
import cn.xybbz.router.ProxyConfig
import cn.xybbz.router.Search
import cn.xybbz.router.SelectLibrary
import cn.xybbz.router.SetBackgroundImage
import cn.xybbz.router.Setting
import cn.xybbz.router.StreamingQuality
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopInteractiveHitTestOwner
import cn.xybbz.ui.windows.DesktopTooltipBox
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.about
import xymusic.composeapp.generated.resources.all_music
import xymusic.composeapp.generated.resources.album
import xymusic.composeapp.generated.resources.app_icon_info
import xymusic.composeapp.generated.resources.app_name
import xymusic.composeapp.generated.resources.arrow_back_24px
import xymusic.composeapp.generated.resources.artist
import xymusic.composeapp.generated.resources.back
import xymusic.composeapp.generated.resources.background_image_setting
import xymusic.composeapp.generated.resources.cache_limit
import xymusic.composeapp.generated.resources.cancel_24px
import xymusic.composeapp.generated.resources.clear
import xymusic.composeapp.generated.resources.connection_info
import xymusic.composeapp.generated.resources.connection_management
import xymusic.composeapp.generated.resources.customize_lyric_settings
import xymusic.composeapp.generated.resources.daily_recommendations
import xymusic.composeapp.generated.resources.dark
import xymusic.composeapp.generated.resources.download
import xymusic.composeapp.generated.resources.favorite
import xymusic.composeapp.generated.resources.genres
import xymusic.composeapp.generated.resources.home
import xymusic.composeapp.generated.resources.interface_settings
import xymusic.composeapp.generated.resources.language
import xymusic.composeapp.generated.resources.light
import xymusic.composeapp.generated.resources.local_and_download
import xymusic.composeapp.generated.resources.logo_new
import xymusic.composeapp.generated.resources.media_library_selection
import xymusic.composeapp.generated.resources.online_music_quality
import xymusic.composeapp.generated.resources.playlist
import xymusic.composeapp.generated.resources.poxy_config
import xymusic.composeapp.generated.resources.refresh_24px
import xymusic.composeapp.generated.resources.refresh_login
import xymusic.composeapp.generated.resources.search
import xymusic.composeapp.generated.resources.search_24px
import xymusic.composeapp.generated.resources.search_music_album_artist
import xymusic.composeapp.generated.resources.server_connection
import xymusic.composeapp.generated.resources.settings
import xymusic.composeapp.generated.resources.storage_management
import xymusic.composeapp.generated.resources.system
import xymusic.composeapp.generated.resources.theme_mode
import cn.xybbz.ui.windows.DesktopWindowTitleBar as UiDesktopWindowTitleBar

/**
 * 标题栏搜索区是否可见。
 * 需求要求暂时隐藏搜索功能但保留搜索框实现，需要恢复时把这里改回 true 即可。
 */
private val DesktopTitleBarShowSearch: Boolean = false

/**
 * 桌面端主标题栏。
 * 左侧依次为应用图标、应用名称、当前页面面包屑与返回按钮；
 * 右侧依次为刷新登录按钮、主题模式切换按钮与窗口控制按钮。
 */
@Composable
fun DesktopWindowTitleBar(navigator: Navigator) {
    UiDesktopWindowTitleBar(
        backgroundColor = DesktopTitleBarColors.current.background,
        front = { hitTestOwner ->
            DesktopTitleBrandAndBreadcrumb(
                navigator = navigator,
                hitTestOwner = hitTestOwner
            )
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
 * 图标与名称沿用项目现有的资源设置，后面追加「应用名 / 当前页面」面包屑和返回按钮。
 */
@Composable
private fun DesktopTitleBrandAndBreadcrumb(
    navigator: Navigator,
    hitTestOwner: DesktopInteractiveHitTestOwner,
) {
    val colors = DesktopTitleBarColors.current
    // 当前展示的路由，优先取当前一级路由栈的栈顶页面
    val currentRoute = navigator.state.backStacks[navigator.state.topLevelRoute]
        ?.lastOrNull() ?: navigator.state.topLevelRoute
    val routeTitle = currentRouteTitleRes(currentRoute)
    val currentStack = navigator.state.backStacks[navigator.state.topLevelRoute]
    // 只有存在可回退页面时才允许点击返回
    val canGoBack = navigator.state.topLevelRoute != navigator.state.startRoute ||
            currentStack?.lastOrNull() != navigator.state.topLevelRoute

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = XyTheme.dimens.outerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_new),
            contentDescription = stringResource(Res.string.app_icon_info),
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
        )
        XyText(
            text = stringResource(Res.string.app_name),
            color = colors.foreground,
            fontWeight = FontWeight.Bold,
        )
        if (routeTitle != null) {
            XyText(
                text = "/",
                color = colors.foregroundVariant,
                fontWeight = FontWeight.Normal,
            )
            XyText(
                text = stringResource(routeTitle),
                color = colors.foregroundVariant,
                fontWeight = FontWeight.Normal,
            )
        }
        // 返回按钮保留在面包屑右侧
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
 * 标题栏中间搜索区。
 * 搜索功能暂时隐藏，但完整实现保留在下方 [DesktopSearchField] 中。
 */
@Composable
private fun DesktopTitleCenter(
    navigator: Navigator,
    hitTestOwner: DesktopInteractiveHitTestOwner,
) {
    // 搜索区已被产品要求暂时隐藏，开关关闭时直接不渲染，避免占用中间拖拽区域
    if (!DesktopTitleBarShowSearch) {
        return
    }

    var keyword by remember { mutableStateOf("") }

    fun submitSearch() {
        val searchQuery = keyword.trim()
        if (searchQuery.isNotEmpty()) {
            keyword = searchQuery
            navigator.navigate(Search(searchQuery = searchQuery))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .widthIn(max = 520.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        DesktopSearchField(
            value = keyword,
            onValueChange = { keyword = it },
            onSearch = ::submitSearch,
            modifier = Modifier
                .weight(1f)
                .desktopTitleBarHitTarget(hitTestOwner, "SearchField")
        )
    }
}

/**
 * 标题栏搜索框。
 * 目前沿用桌面原型交互，只维护本地输入态。
 */
@Composable
private fun DesktopSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DesktopTitleBarColors.current

    XyEdit(
        text = value,
        onChange = onValueChange,
        modifier = modifier
            .height(XyTheme.dimens.itemHeight * 0.8f)
            .border(
                width = 1.dp,
                color = colors.outline,
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .clip(RoundedCornerShape(XyTheme.dimens.corner)),
        backgroundColor = colors.searchBackground,
        hint = stringResource(Res.string.search_music_album_artist),
        hintColor = colors.foregroundVariant,
        showHintWhenFocused = false,
        paddingValues = PaddingValues(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch()
        }),
        singleLine = true,
        leadingContent = {
            Icon(
                painter = painterResource(Res.drawable.search_24px),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        actionContent = if (value.isNotEmpty()) {
            {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(XyTheme.dimens.corner))
                        .jvmHoverDebounceClickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.cancel_24px),
                        contentDescription = stringResource(Res.string.clear),
                        tint = colors.foregroundVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            null
        }
    )
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
    val searchBackground: Color,
    val iconButtonBackground: Color,
    val foreground: Color,
    val foregroundVariant: Color,
    val outline: Color,
) {
    companion object {
        val current: DesktopTitleBarColors
            @Composable
            get() = DesktopTitleBarColors(
                background = MaterialTheme.colorScheme.background,
                searchBackground = MaterialTheme.colorScheme.surfaceContainerLowest,
                iconButtonBackground = MaterialTheme.colorScheme.surfaceContainerLowest,
                foreground = MaterialTheme.colorScheme.onSurface,
                foregroundVariant = MaterialTheme.colorScheme.onSurfaceVariant,
                outline = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
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

/**
 * 当前路由对应的面包屑标题资源。
 * 入参使用 NavKey，未覆盖到的页面返回 null 时不展示面包屑标题。
 */
private fun currentRouteTitleRes(route: Any): StringResource? = when (route) {
    Home -> Res.string.home
    Music -> Res.string.all_music
    Local -> Res.string.local_and_download
    Album -> Res.string.album
    Artist -> Res.string.artist
    FavoriteList -> Res.string.favorite
    Genres -> Res.string.genres
    ConnectionManagement -> Res.string.connection_management
    Setting -> Res.string.settings
    is Search -> Res.string.search
    is AlbumInfo -> if (route.dataType == MusicDataTypeEnum.PLAYLIST) {
        Res.string.playlist
    } else {
        Res.string.album
    }

    is ArtistInfo -> Res.string.artist
    is GenreInfo -> Res.string.genres
    is Connection -> Res.string.server_connection
    is ConnectionInfo -> Res.string.connection_info
    MemoryManagement -> Res.string.storage_management
    InterfaceSetting -> Res.string.interface_settings
    LanguageConfig -> Res.string.language
    About -> Res.string.about
    CacheLimit -> Res.string.cache_limit
    is SelectLibrary -> Res.string.media_library_selection
    DailyRecommend -> Res.string.daily_recommendations
    Download -> Res.string.download
    SetBackgroundImage -> Res.string.background_image_setting
    ProxyConfig -> Res.string.poxy_config
    StreamingQuality -> Res.string.online_music_quality
    CustomApi -> Res.string.customize_lyric_settings
    else -> null
}

private fun Modifier.desktopTitleBarHitTarget(
    owner: DesktopInteractiveHitTestOwner,
    targetId: String,
): Modifier = onGloballyPositioned { coordinates ->
    owner.updateBounds(targetId, coordinates.boundsInWindow())
}
