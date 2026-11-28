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

package cn.xybbz.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.ui.components.DesktopThemeModeToggleButton
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopWindowControls
import cn.xybbz.ui.windows.desktopWindowDragArea
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconButton
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ConnectionViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.cancel_24px
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.http_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.logo_new
import xymusic.composeapp.generated.resources.password_24px
import xymusic.composeapp.generated.resources.person_24px
import xymusic.composeapp.generated.resources.visibility_24px
import xymusic.composeapp.generated.resources.visibility_off_24px
import xymusic.composeapp.generated.resources.warning_24px
import xymusic.composeapp.generated.resources.*

/**
 * JVM 连接到媒体服务器页面。
 *
 * 视觉按 Signal Deck 设计稿还原，同时遵循项目约定：
 * 1. 文案统一走 XyText / XyTextSub，样式取自 MaterialTheme.typography；
 * 2. 颜色统一取自 MaterialTheme.colorScheme 与 XyTheme.extendedColors；
 * 3. 间距、圆角、行高取自 XyTheme.dimens，不写死数值；
 * 4. 输入框、按钮、图标按钮复用 XyEdit / XyButton / XyIconButton。
 *
 * @param connectionUiType 页面入口类型，首次打开时页面自行绘制标题栏。
 * @param modifier 外部布局修饰符。
 * @param connectionViewModel 连接业务 ViewModel，默认走 Koin 注入。
 */
@Composable
fun JvmConnectionScreen(
    connectionUiType: ConnectionUiType? = null,
    modifier: Modifier = Modifier,
    connectionViewModel: ConnectionViewModel = koinViewModel<ConnectionViewModel>(),
) {
    val navigator = LocalNavigator.current
    // 成功后的收尾入口复用旧连接页配置：首次配置进入主页，添加连接返回上一页。
    val ifEntryPage by connectionViewModel.settingsManager.ifEntryPage.collectAsState()
    val dataSourceTypes = remember {
        DataSourceType.entries.filter { it.ifShow }
    }
    // 当前协议直接读取 ViewModel；初始化完成前用第一个可展示协议兜底保证 UI 可绘制。
    val selectedDataSource = connectionViewModel.dataSourceType
        ?: dataSourceTypes.firstOrNull()
        ?: DataSourceType.JELLYFIN
    val showResourcePanel = connectionViewModel.showResourcePanel
    val selectedResourceIndex = connectionViewModel.selectUrlIndex
    // 登录结果只有成功与失败两种终态，页面只负责把状态映射成视觉阶段。
    val loginStage = when {
        connectionViewModel.isLoginSuccess -> JvmConnectionLoginStage.Success
        connectionViewModel.loading -> JvmConnectionLoginStage.LoggingIn
        else -> JvmConnectionLoginStage.Idle
    }
    val connectionReady = connectionViewModel.isLoginSuccess

    // 密码显隐只影响本页输入框表现，属于纯 UI 状态。
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(dataSourceTypes) {
        // 首次进入时保证有默认协议，选择规则由 ViewModel 维护。
        connectionViewModel.ensureDefaultDataSource(dataSourceTypes)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val dimens = XyTheme.dimens
        // 协议轨在窄窗口收成图标列，保证主内容仍有足够宽度。
        val compactRail = maxWidth < 900.dp
        val railWidth = if (compactRail) dimens.itemHeight + dimens.contentPadding else 236.dp
        val horizontalPadding = if (maxWidth < 760.dp) dimens.outerHorizontalPadding else dimens.outerHorizontalPadding * 2 + dimens.outerVerticalPadding / 2
        val contentWidth = maxWidth - railWidth - horizontalPadding * 2
        val paneGap = dimens.contentPadding + dimens.outerVerticalPadding
        // 结果卡并排需要表单 600 + 间距 + 结果卡最小 260 的空间，不足时下沉。
        val sideBySide = contentWidth >= 880.dp
        val formWidth = if (sideBySide) {
            minOf(600.dp, contentWidth - paneGap - 260.dp)
        } else {
            minOf(600.dp, contentWidth)
        }
        // 矮窗口压缩头部与间距，保证主操作始终在首屏可见。
        val compactHeight = maxHeight < 640.dp
        // 表单过窄时账号与密码改为上下排列。
        val narrowFields = formWidth < 480.dp
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize()) {
            if (connectionUiType == ConnectionUiType.FIRST_OPEN) {
                JvmConnectionTitleBar()
            }
            Row(modifier = Modifier.fillMaxSize()) {
                JvmConnectionProtocolRail(
                    width = railWidth,
                    compact = compactRail,
                    dataSourceTypes = dataSourceTypes,
                    selectedDataSource = selectedDataSource,
                    busy = connectionViewModel.loading || connectionViewModel.resourceLoading,
                    connected = connectionReady,
                    failed = connectionViewModel.isLoginError || connectionViewModel.isResourceLoginError,
                    onSelectDataSource = { connectionViewModel.selectDataSource(it) }
                )
                // 轨道右侧的细分隔线，避免整列使用四边框。
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(JvmConnectionLineColor())
                )
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(
                                    horizontal = horizontalPadding,
                                    vertical = if (compactHeight) dimens.contentPadding else dimens.outerHorizontalPadding + dimens.outerVerticalPadding
                                )
                        ) {
                            JvmConnectionHero(
                                selectedDataSource = selectedDataSource,
                                compact = compactHeight
                            )
                            Spacer(modifier = Modifier.height(if (compactHeight) dimens.contentPadding else dimens.contentPadding + dimens.outerVerticalPadding))
                            if (sideBySide) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(paneGap),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    JvmConnectionFormPanel(
                                        modifier = Modifier.width(formWidth),
                                        selectedDataSource = selectedDataSource,
                                        address = connectionViewModel.address,
                                        username = connectionViewModel.username,
                                        password = connectionViewModel.password,
                                        showPassword = showPassword,
                                        submitted = showResourcePanel || connectionViewModel.loading || connectionReady,
                                        busy = connectionViewModel.loading || connectionViewModel.resourceLoading || connectionReady,
                                        connected = connectionReady,
                                        narrowFields = narrowFields,
                                        onAddressChange = connectionViewModel::setAddressData,
                                        onUsernameChange = connectionViewModel::setUserNameData,
                                        onPasswordChange = connectionViewModel::setPasswordData,
                                        onTogglePassword = { showPassword = !showPassword },
                                        onConnect = connectionViewModel::connect
                                    )
                                    if (showResourcePanel) {
                                        JvmConnectionResultPanel(
                                            modifier = Modifier.weight(1f),
                                            selectedDataSource = selectedDataSource,
                                            address = connectionViewModel.address,
                                            selectedResourceIndex = selectedResourceIndex,
                                            loginStage = loginStage,
                                            resourceLoading = connectionViewModel.resourceLoading,
                                            resourceError = connectionViewModel.isResourceLoginError,
                                            loginLoading = connectionViewModel.loading,
                                            loginError = connectionViewModel.isLoginError,
                                            errorMessage = connectionViewModel.errorMessage,
                                            errorHint = stringResource(connectionViewModel.errorHint),
                                            tmpAddressList = connectionViewModel.tmpAddressList,
                                            tmpPlexInfo = connectionViewModel.tmpPlexInfo,
                                            connectionReady = connectionReady,
                                            onSelectResource = connectionViewModel::selectResourceAndLogin,
                                            onRetry = connectionViewModel::connect,
                                            onEditConnectionInfo = connectionViewModel::resetConnectionInput,
                                            onEnter = {
                                                if (!ifEntryPage) {
                                                    connectionViewModel.updateIfConnectionConfig()
                                                } else {
                                                    navigator.goBack()
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(paneGap)) {
                                    JvmConnectionFormPanel(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                                        selectedDataSource = selectedDataSource,
                                        address = connectionViewModel.address,
                                        username = connectionViewModel.username,
                                        password = connectionViewModel.password,
                                        showPassword = showPassword,
                                        submitted = showResourcePanel || connectionViewModel.loading || connectionReady,
                                        busy = connectionViewModel.loading || connectionViewModel.resourceLoading || connectionReady,
                                        connected = connectionReady,
                                        narrowFields = narrowFields,
                                        onAddressChange = connectionViewModel::setAddressData,
                                        onUsernameChange = connectionViewModel::setUserNameData,
                                        onPasswordChange = connectionViewModel::setPasswordData,
                                        onTogglePassword = { showPassword = !showPassword },
                                        onConnect = connectionViewModel::connect
                                    )
                                    if (showResourcePanel) {
                                        JvmConnectionResultPanel(
                                            modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                                            selectedDataSource = selectedDataSource,
                                            address = connectionViewModel.address,
                                            selectedResourceIndex = selectedResourceIndex,
                                            loginStage = loginStage,
                                            resourceLoading = connectionViewModel.resourceLoading,
                                            resourceError = connectionViewModel.isResourceLoginError,
                                            loginLoading = connectionViewModel.loading,
                                            loginError = connectionViewModel.isLoginError,
                                            errorMessage = connectionViewModel.errorMessage,
                                            errorHint = stringResource(connectionViewModel.errorHint),
                                            tmpAddressList = connectionViewModel.tmpAddressList,
                                            tmpPlexInfo = connectionViewModel.tmpPlexInfo,
                                            connectionReady = connectionReady,
                                            onSelectResource = connectionViewModel::selectResourceAndLogin,
                                            onRetry = connectionViewModel::connect,
                                            onEditConnectionInfo = connectionViewModel::resetConnectionInput,
                                            onEnter = {
                                                if (!ifEntryPage) {
                                                    connectionViewModel.updateIfConnectionConfig()
                                                } else {
                                                    navigator.goBack()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    JvmConnectionStatusBar(
                        connected = connectionReady,
                        busy = connectionViewModel.loading || connectionViewModel.resourceLoading,
                        failed = connectionViewModel.isLoginError || connectionViewModel.isResourceLoginError,
                        selectedDataSource = selectedDataSource,
                        address = connectionViewModel.address
                    )
                }
            }
        }

        // 窄窗口下结果卡在表单下方，出现时自动滚到可视区，避免“点了没反应”的错觉。
        LaunchedEffect(showResourcePanel, sideBySide) {
            if (showResourcePanel && !sideBySide) {
                delay(80)
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }
}

/**
 * 首次打开时页面自绘的窗口标题栏。
 */
@Composable
private fun JvmConnectionTitleBar() {
    val dimens = XyTheme.dimens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.itemHeight - dimens.contentPadding)
            .desktopWindowDragArea()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.86f))
            .border(
                width = 1.dp,
                color = JvmConnectionLineColor()
            )
            .padding(start = dimens.outerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XySmallImage(
            modifier = Modifier.size(dimens.outerHorizontalPadding + dimens.contentPadding),
            model = painterResource(Res.drawable.logo_new),
            contentDescription = null,
            shape = RoundedCornerShape(dimens.corner / 2)
        )
        Spacer(modifier = Modifier.width(dimens.contentPadding))
        XyText(
            text = "XyMusic",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        // 首次打开时提供主题模式切换入口，与主标题栏保持一致
        DesktopThemeModeToggleButton(
            modifier = Modifier.padding(end = dimens.contentPadding)
        )
        DesktopWindowControls()
    }
}

/**
 * 左侧协议轨。
 *
 * @param width 轨道宽度，窄窗口为图标列。
 * @param compact 是否处于图标模式。
 * @param dataSourceTypes 可展示的协议列表。
 * @param selectedDataSource 当前选中协议。
 * @param busy 是否正在登录。
 * @param connected 是否已连接。
 * @param failed 最近一次连接是否失败。
 * @param onSelectDataSource 协议切换回调。
 */
@Composable
private fun JvmConnectionProtocolRail(
    width: Dp,
    compact: Boolean,
    dataSourceTypes: List<DataSourceType>,
    selectedDataSource: DataSourceType,
    busy: Boolean,
    connected: Boolean,
    failed: Boolean,
    onSelectDataSource: (DataSourceType) -> Unit,
) {
    val dimens = XyTheme.dimens
    val stateColor = when {
        connected -> XyTheme.extendedColors.success
        failed -> XyTheme.extendedColors.danger
        busy -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val stateText = when {
        connected -> stringResource(Res.string.jvm_connection_rail_state_ok)
        failed -> stringResource(Res.string.jvm_connection_rail_state_failed)
        busy -> stringResource(Res.string.jvm_connection_rail_state_busy)
        else -> stringResource(Res.string.jvm_connection_rail_state_idle)
    }

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (!compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimens.outerHorizontalPadding,
                        end = dimens.outerHorizontalPadding,
                        top = dimens.contentPadding + dimens.outerVerticalPadding,
                        bottom = dimens.outerHorizontalPadding
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                XyText(
                    text = stringResource(Res.string.server_connection),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W700,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                XyText(
                    text = stateText,
                    color = stateColor,
                    fontWeight = FontWeight.W600,
                    maxLines = 1,
                    style = JvmConnectionMonoStyle()
                )
            }
        } else {
            Spacer(modifier = Modifier.height(dimens.contentPadding + dimens.outerVerticalPadding))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            dataSourceTypes.forEach { dataSourceType ->
                JvmConnectionProtocolCard(
                    dataSourceType = dataSourceType,
                    selected = dataSourceType == selectedDataSource,
                    compact = compact,
                    onClick = { onSelectDataSource(dataSourceType) }
                )
            }
        }
    }
}

/**
 * 单个协议卡片：字母徽标 + 名称 + 最低支持版本 + 默认端口。
 */
@Composable
private fun JvmConnectionProtocolCard(
    dataSourceType: DataSourceType,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val dimens = XyTheme.dimens
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(dimens.corner / 2)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimens.itemHeight - dimens.contentPadding)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) else Color.Transparent)
            .jvmHoverDebounceClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.outerVerticalPadding, vertical = dimens.outerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            // 图标模式下徽标居中，完整模式下按「徽标 + 名称 + 端口」横向排列。
            horizontalArrangement = if (compact) {
                Arrangement.Center
            } else {
                Arrangement.spacedBy(dimens.outerVerticalPadding + 2.dp)
            }
        ) {
            // 协议图标：选中时使用主色淡底，图标本身沿用各协议官方 logo。
            Box(
                modifier = Modifier
                    .size(dimens.itemHeight - dimens.contentPadding - dimens.outerVerticalPadding * 2)
                    .clip(RoundedCornerShape(dimens.corner / 2))
                    .background(
                        if (selected) primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) primary.copy(alpha = 0.34f) else JvmConnectionLineColor(),
                        shape = RoundedCornerShape(dimens.corner / 2)
                    )
                    .padding(dimens.outerVerticalPadding / 2),
                contentAlignment = Alignment.Center
            ) {
                XySmallImage(
                    modifier = Modifier.fillMaxSize(),
                    model = painterResource(dataSourceType.img),
                    contentDescription = dataSourceType.title,
                    shape = RoundedCornerShape(dimens.corner / 3)
                )
            }
            if (!compact) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    XyText(
                        text = dataSourceType.title,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.W700 else FontWeight.W600,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    XyText(
                        text = stringResource(Res.string.jvm_connection_min_supported, dataSourceType.version),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        style = JvmConnectionMonoStyle()
                    )
                }
                XyText(
                    text = dataSourceType.httpPort.toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    style = JvmConnectionMonoStyle()
                )
            }
        }
        // 选中态左侧强调条。
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .height(dimens.contentPadding * 2)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primary)
            )
        }
    }
}

/**
 * 页头：小横线 + 英文标签 + 标题 + 说明。
 */
@Composable
private fun JvmConnectionHero(
    selectedDataSource: DataSourceType,
    compact: Boolean,
) {
    val dimens = XyTheme.dimens
    val desc = if (selectedDataSource.ifInputUrl) {
        stringResource(Res.string.jvm_connection_new_screen_text_05, selectedDataSource.title)
    } else {
        stringResource(Res.string.jvm_connection_new_screen_text_06, selectedDataSource.title)
    }
    Column(modifier = Modifier.widthIn(max = 720.dp)) {
        XyText(
            text = stringResource(Res.string.jvm_connection_new_screen_text_07),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W800,
            maxLines = 2,
            style = if (compact) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.headlineMedium
            }
        )
        if (!compact) {
            Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
            XyTextSub(
                text = desc,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
        }
    }
}

/**
 * 凭据表单卡。
 *
 * @param narrowFields 表单宽度不足时把账号与密码改为上下排列。
 * @param connected 是否已连接成功。
 * @param busy 是否正在请求中。
 */
@Composable
private fun JvmConnectionFormPanel(
    modifier: Modifier,
    selectedDataSource: DataSourceType,
    address: String,
    username: String,
    password: String,
    showPassword: Boolean,
    submitted: Boolean,
    busy: Boolean,
    connected: Boolean,
    narrowFields: Boolean,
    onAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onConnect: () -> Unit,
) {
    val dimens = XyTheme.dimens
    // 地址与账号都填写后才允许发起连接，避免无效请求。
    val canConnect = !busy && username.isNotBlank() &&
            (!selectedDataSource.ifInputUrl || address.isNotBlank())
    // 地址为空时不提示；有内容时按“协议 + 主机 + 可选端口/路径”校验，区分可用与不可用。
    val trimmedAddress = address.trim()
    val addressBlank = trimmedAddress.isEmpty()
    val addressValid = !addressBlank && trimmedAddress.isValidServiceAddress()

    JvmConnectionPanelCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                XyText(
                    text = "02 / 04 · " + stringResource(
                        if (submitted) Res.string.jvm_connection_submitted
                        else Res.string.jvm_connection_new_screen_text_02
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    style = JvmConnectionMonoStyle(letterSpacing = 1.6.sp)
                )
                Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
                XyText(
                    text = stringResource(Res.string.jvm_connection_new_screen_text_08, selectedDataSource.title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W800,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            JvmConnectionTag(
                text = stringResource(
                    if (submitted) Res.string.jvm_connection_submitted
                    else Res.string.jvm_connection_required
                )
            )
        }
        Spacer(modifier = Modifier.height(dimens.contentPadding + dimens.outerVerticalPadding))

        if (selectedDataSource.ifInputUrl) {
            JvmConnectionFieldLabel(
                label = stringResource(Res.string.jvm_connection_new_screen_text_09),
                tag = stringResource(Res.string.jvm_connection_uri_tag)
            )
            Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
            JvmConnectionTextField(
                value = address,
                onValueChange = onAddressChange,
                hint = "${ApiConstants.HTTP}192.168.1.12:${selectedDataSource.httpPort}",
                leading = painterResource(Res.drawable.http_24px),
                keyboardType = KeyboardType.Uri,
                trailing = if (address.isNotBlank()) {
                    {
                        JvmConnectionIconAction(
                            painter = painterResource(Res.drawable.cancel_24px),
                            onClick = { onAddressChange("") }
                        )
                    }
                } else null
            )
            Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.outerVerticalPadding)) {
                JvmConnectionTag(
                    text = stringResource(
                        Res.string.jvm_connection_new_screen_text_10,
                        selectedDataSource.httpPort.toString()
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                )
                if (!addressBlank) {
                    if (addressValid) {
                        JvmConnectionTag(
                            text = stringResource(Res.string.jvm_connection_address_valid),
                            color = XyTheme.extendedColors.success,
                            borderColor = XyTheme.extendedColors.success.copy(alpha = 0.34f),
                            backgroundColor = XyTheme.extendedColors.success.copy(alpha = 0.09f)
                        )
                    } else {
                        JvmConnectionTag(
                            text = stringResource(Res.string.jvm_connection_address_invalid),
                            color = XyTheme.extendedColors.danger,
                            borderColor = XyTheme.extendedColors.danger.copy(alpha = 0.34f),
                            backgroundColor = XyTheme.extendedColors.danger.copy(alpha = 0.09f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimens.contentPadding + dimens.outerVerticalPadding))
        }

        // 账号与密码并排，窄表单时自动改为上下排列。
        if (narrowFields) {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.contentPadding + dimens.outerVerticalPadding)) {
                JvmConnectionUserField(username, onUsernameChange)
                JvmConnectionPasswordField(password, showPassword, onPasswordChange, onTogglePassword)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.contentPadding + dimens.outerVerticalPadding)) {
                Box(modifier = Modifier.weight(1f)) {
                    JvmConnectionUserField(username, onUsernameChange)
                }
                Box(modifier = Modifier.weight(1f)) {
                    JvmConnectionPasswordField(password, showPassword, onPasswordChange, onTogglePassword)
                }
            }
        }

        if (!selectedDataSource.ifInputUrl) {
            Spacer(modifier = Modifier.height(dimens.contentPadding + dimens.outerVerticalPadding))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.corner / 2))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(dimens.corner / 2)
                    )
                    .padding(horizontal = dimens.contentPadding, vertical = dimens.outerVerticalPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.outerVerticalPadding),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(Res.drawable.info_24px),
                    contentDescription = null,
                    modifier = Modifier.size(dimens.contentPadding),
                    tint = MaterialTheme.colorScheme.primary
                )
                XyTextSub(
                    text = stringResource(Res.string.jvm_connection_new_screen_text_12),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.contentPadding + dimens.outerVerticalPadding))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(JvmConnectionLineColor())
        )
        Spacer(modifier = Modifier.height(dimens.contentPadding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 重新选择按钮已移除：切换左侧协议即可重置输入，操作行只保留状态提示与连接按钮。
            Spacer(modifier = Modifier.weight(1f))
            XyText(
                text = when {
                    connected -> stringResource(Res.string.jvm_connection_rail_state_ok)
                    busy -> stringResource(Res.string.jvm_connection_rail_state_busy)
                    canConnect -> stringResource(Res.string.jvm_connection_ready)
                    selectedDataSource.ifInputUrl -> stringResource(Res.string.jvm_connection_need_address_user)
                    else -> stringResource(Res.string.jvm_connection_need_user)
                },
                color = if (canConnect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                style = JvmConnectionMonoStyle()
            )
            Spacer(modifier = Modifier.width(dimens.outerVerticalPadding))
            JvmConnectionButton(
                text = stringResource(Res.string.connect),
                primary = true,
                enabled = canConnect,
                onClick = onConnect
            )
        }
    }
}

/**
 * 用户名输入区。
 */
@Composable
private fun JvmConnectionUserField(
    username: String,
    onUsernameChange: (String) -> Unit,
) {
    val dimens = XyTheme.dimens
    Column {
        JvmConnectionFieldLabel(label = stringResource(Res.string.username))
        Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
        JvmConnectionTextField(
            value = username,
            onValueChange = onUsernameChange,
            hint = "admin",
            leading = painterResource(Res.drawable.person_24px),
            keyboardType = KeyboardType.Text
        )
    }
}

/**
 * 密码输入区。
 */
@Composable
private fun JvmConnectionPasswordField(
    password: String,
    showPassword: Boolean,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
) {
    val dimens = XyTheme.dimens
    Column {
        JvmConnectionFieldLabel(label = stringResource(Res.string.password))
        Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
        JvmConnectionTextField(
            value = password,
            onValueChange = onPasswordChange,
            hint = "password",
            leading = painterResource(Res.drawable.password_24px),
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = {
                JvmConnectionIconAction(
                    painter = painterResource(
                        if (showPassword) Res.drawable.visibility_24px else Res.drawable.visibility_off_24px
                    ),
                    onClick = onTogglePassword
                )
            }
        )
    }
}

/**
 * 表单字段标签：等宽小字 + 可选标签 + 右侧细线。
 */
@Composable
private fun JvmConnectionFieldLabel(
    label: String,
    tag: String? = null,
) {
    val dimens = XyTheme.dimens
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyText(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            style = JvmConnectionMonoStyle()
        )
        if (tag != null) {
            Spacer(modifier = Modifier.width(dimens.outerVerticalPadding / 2))
            JvmConnectionTag(text = tag)
        }
        Spacer(modifier = Modifier.width(dimens.outerVerticalPadding))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(JvmConnectionLineColor())
        )
    }
}

/**
 * 统一风格的输入框，复用项目 XyEdit。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun JvmConnectionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    leading: Painter,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val dimens = XyTheme.dimens
    XyEdit(
        text = value,
        onChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.contentPadding * 2 + dimens.innerVerticalPadding * 2),
        // 高度由外层控制，内部不再叠加纵向留白。
        paddingValues = PaddingValues(),
        hint = hint,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        ),
        leadingContent = {
            Icon(
                painter = leading,
                contentDescription = null,
                modifier = Modifier.size(dimens.outerHorizontalPadding),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        actionContent = trailing
    )
}

/**
 * 输入框右侧的小图标操作。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun JvmConnectionIconAction(
    painter: Painter,
    onClick: () -> Unit,
) {
    XyIconButton(
        onClick = onClick,
        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 结果卡：按 ViewModel 状态渲染探测、资源列表、登录与错误。
 */
@Composable
private fun JvmConnectionResultPanel(
    modifier: Modifier,
    selectedDataSource: DataSourceType,
    address: String,
    selectedResourceIndex: Int,
    loginStage: JvmConnectionLoginStage,
    resourceLoading: Boolean,
    resourceError: Boolean,
    loginLoading: Boolean,
    loginError: Boolean,
    errorMessage: String,
    errorHint: String,
    tmpAddressList: List<String>,
    tmpPlexInfo: List<ResourceData>,
    connectionReady: Boolean,
    onSelectResource: (Int) -> Unit,
    onRetry: () -> Unit,
    onEditConnectionInfo: () -> Unit,
    onEnter: () -> Unit,
) {
    JvmConnectionPanelCard(modifier = modifier) {
        when {
            resourceLoading -> {
                JvmConnectionProbingContent(address = address)
            }

            loginLoading || connectionReady -> {
                JvmConnectionLoginContent(
                    selectedDataSource = selectedDataSource,
                    stage = loginStage,
                    onEnter = onEnter
                )
            }

            resourceError || loginError -> {
                JvmConnectionErrorContent(
                    loginError = loginError,
                    errorHint = errorHint,
                    errorMessage = errorMessage,
                    onRetry = onRetry
                )
            }

            tmpPlexInfo.isNotEmpty() -> {
                // 字符串在组合上下文内先取好，避免在 map 的普通 lambda 中调用 stringResource。
                val availableTag = stringResource(Res.string.jvm_connection_new_screen_text_25)
                JvmConnectionResourceList(
                    selectedDataSource = selectedDataSource,
                    items = tmpPlexInfo.map { plexItem ->
                        JvmConnectionResourceItem(
                            title = plexItem.name,
                            address = plexItem.addressUrl,
                            tag = plexItem.product.ifBlank { availableTag }
                        )
                    },
                    selectedIndex = selectedResourceIndex,
                    onSelectResource = onSelectResource,
                    onEditConnectionInfo = onEditConnectionInfo
                )
            }

            tmpAddressList.isNotEmpty() -> {
                // 候选地址的标题与标签同样是固定文案，提前取出后在 map 中复用。
                val recommendTitle = stringResource(Res.string.jvm_connection_new_screen_text_26)
                val candidateTitle = stringResource(Res.string.jvm_connection_new_screen_text_27)
                val recommendTag = stringResource(Res.string.recommend)
                val optionalTag = stringResource(Res.string.jvm_connection_new_screen_text_28)
                JvmConnectionResourceList(
                    selectedDataSource = selectedDataSource,
                    items = tmpAddressList.mapIndexed { index, item ->
                        JvmConnectionResourceItem(
                            title = if (index == 0) recommendTitle else candidateTitle,
                            address = item,
                            tag = if (index == 0) recommendTag else optionalTag
                        )
                    },
                    selectedIndex = selectedResourceIndex,
                    onSelectResource = onSelectResource,
                    onEditConnectionInfo = onEditConnectionInfo
                )
            }

            else -> {
                XyTextSub(
                    text = stringResource(Res.string.jvm_connection_new_screen_text_29),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * 资源探测中：细进度条 + 地址 + 骨架占位。
 */
@Composable
private fun JvmConnectionProbingContent(address: String) {
    val dimens = XyTheme.dimens
    JvmConnectionPanelKicker(
        text = "03 / 04 · " + stringResource(Res.string.jvm_connection_new_screen_text_03)
    )
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    JvmConnectionPanelTitle(text = stringResource(Res.string.jvm_connection_new_screen_text_23))
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    XyTextSub(
        text = stringResource(Res.string.jvm_connection_loading_address),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 2
    )
    Spacer(modifier = Modifier.height(dimens.contentPadding))
    JvmConnectionIndeterminateBar()
    Spacer(modifier = Modifier.height(dimens.contentPadding))
    XyTextSub(
        text = address.stripScheme(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = JvmConnectionMonoStyle(),
        maxLines = 1
    )
    Spacer(modifier = Modifier.height(dimens.contentPadding))
    JvmConnectionSkeletonBar(fraction = 0.78f)
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
    JvmConnectionSkeletonBar(fraction = 0.52f)
}

/**
 * 登录状态：单次登录请求，成功与失败两种结果。
 */
@Composable
private fun JvmConnectionLoginContent(
    selectedDataSource: DataSourceType,
    stage: JvmConnectionLoginStage,
    onEnter: () -> Unit,
) {
    val dimens = XyTheme.dimens
    val success = stage == JvmConnectionLoginStage.Success
    JvmConnectionPanelKicker(
        text = if (success) {
            "04 / 04 · " + stringResource(Res.string.jvm_connection_new_screen_text_33)
        } else {
            "04 / 04 · " + stringResource(Res.string.jvm_connection_new_screen_text_04)
        }
    )
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    JvmConnectionPanelTitle(
        text = stringResource(
            if (success) Res.string.login_success else Res.string.jvm_connection_new_screen_text_30
        )
    )
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    XyTextSub(
        text = if (success) {
            stringResource(Res.string.jvm_connection_new_screen_text_32)
        } else {
            stringResource(Res.string.jvm_connection_new_screen_text_31, selectedDataSource.title)
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 2
    )

    if (!success) {
        Spacer(modifier = Modifier.height(dimens.contentPadding))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.outerVerticalPadding)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimens.outerHorizontalPadding),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_connection_verifying),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(dimens.contentPadding))
        JvmConnectionIndeterminateBar()
    }

    if (success) {
        Spacer(modifier = Modifier.height(dimens.contentPadding))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.corner / 2))
                .background(XyTheme.extendedColors.success.copy(alpha = 0.07f))
                .border(
                    width = 1.dp,
                    color = XyTheme.extendedColors.success.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(dimens.corner / 2)
                )
                .padding(dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.contentPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.outerHorizontalPadding + dimens.outerVerticalPadding / 2)
                    .clip(CircleShape)
                    .background(XyTheme.extendedColors.success),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.check_24px),
                    contentDescription = null,
                    modifier = Modifier.size(dimens.contentPadding),
                    tint = MaterialTheme.colorScheme.surface
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                XyText(
                    text = stringResource(Res.string.connection_successful),
                    color = XyTheme.extendedColors.success,
                    fontWeight = FontWeight.W700,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            JvmConnectionButton(
                text = stringResource(Res.string.jvm_connection_new_screen_text_40),
                primary = true,
                onClick = onEnter
            )
        }
    }
}

/**
 * 错误状态：资源不可达或凭据校验失败。
 */
@Composable
private fun JvmConnectionErrorContent(
    loginError: Boolean,
    errorHint: String,
    errorMessage: String,
    onRetry: () -> Unit,
) {
    val dimens = XyTheme.dimens
    val danger = XyTheme.extendedColors.danger
    JvmConnectionPanelKicker(
        text = if (loginError) "LOGIN / FAILED" else "DISCOVERY / FAILED"
    )
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    JvmConnectionPanelTitle(
        text = stringResource(
            if (loginError) Res.string.login_failed else Res.string.jvm_connection_new_screen_text_24
        )
    )
    Spacer(modifier = Modifier.height(dimens.contentPadding))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.outerVerticalPadding)
    ) {
        Icon(
            painter = painterResource(Res.drawable.warning_24px),
            contentDescription = null,
            modifier = Modifier.size(dimens.outerHorizontalPadding),
            tint = danger
        )
        XyText(
            text = stringResource(
                if (loginError) Res.string.jvm_connection_error_auth_title
                else Res.string.jvm_connection_error_probe_title
            ),
            color = danger,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall
        )
        JvmConnectionTag(
            text = if (loginError) "E_AUTH" else "E_PROBE",
            color = danger,
            borderColor = danger.copy(alpha = 0.34f),
            backgroundColor = danger.copy(alpha = 0.09f)
        )
    }
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
    XyTextSub(
        text = stringResource(
            if (loginError) Res.string.jvm_connection_error_auth_hint
            else Res.string.jvm_connection_error_probe_hint
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 3
    )
    val detail = listOf(errorHint, errorMessage).filter { it.isNotBlank() }.joinToString("\n")
    if (detail.isNotBlank()) {
        Spacer(modifier = Modifier.height(dimens.contentPadding))
        XyTextSub(
            text = detail,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.corner / 2))
                .background(MaterialTheme.colorScheme.background)
                .border(
                    width = 1.dp,
                    color = JvmConnectionLineColor(),
                    shape = RoundedCornerShape(dimens.corner / 2)
                )
                .padding(horizontal = dimens.contentPadding, vertical = dimens.outerVerticalPadding),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = JvmConnectionMonoStyle(),
            maxLines = 4
        )
    }
    Spacer(modifier = Modifier.height(dimens.contentPadding))
    Row(horizontalArrangement = Arrangement.spacedBy(dimens.outerVerticalPadding)) {
        JvmConnectionButton(
            text = stringResource(Res.string.jvm_connection_retry),
            primary = true,
            onClick = onRetry
        )
    }
}

/**
 * 资源候选列表。
 */
@Composable
private fun JvmConnectionResourceList(
    selectedDataSource: DataSourceType,
    items: List<JvmConnectionResourceItem>,
    selectedIndex: Int,
    onSelectResource: (Int) -> Unit,
    onEditConnectionInfo: () -> Unit,
) {
    val dimens = XyTheme.dimens
    JvmConnectionPanelKicker(
        text = "03 / 04 · " + stringResource(Res.string.jvm_connection_new_screen_text_03)
    )
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    JvmConnectionPanelTitle(text = stringResource(Res.string.jvm_connection_new_screen_text_19))
    Spacer(modifier = Modifier.height(dimens.outerVerticalPadding / 2))
    val baseDesc = if (selectedDataSource.ifInputUrl) {
        stringResource(Res.string.jvm_connection_new_screen_text_20, selectedDataSource.title)
    } else {
        stringResource(Res.string.jvm_connection_new_screen_text_21)
    }
    XyTextSub(
        text = baseDesc + " " + stringResource(Res.string.jvm_connection_click_to_login),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 2
    )
    Spacer(modifier = Modifier.height(dimens.contentPadding))

    items.forEachIndexed { index, item ->
        JvmConnectionResourceRow(
            item = item,
            selected = index == selectedIndex,
            recommended = index == 0,
            onClick = { onSelectResource(index) }
        )
        if (index != items.lastIndex) {
            Spacer(modifier = Modifier.height(dimens.outerVerticalPadding))
        }
    }

    Spacer(modifier = Modifier.height(dimens.contentPadding))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(JvmConnectionLineColor())
    )
    Spacer(modifier = Modifier.height(dimens.contentPadding))
    JvmConnectionButton(
        text = stringResource(Res.string.jvm_connection_new_screen_text_22),
        primary = false,
        modifier = Modifier.fillMaxWidth(),
        onClick = onEditConnectionInfo
    )
}

/**
 * 单条资源候选。
 */
@Composable
private fun JvmConnectionResourceRow(
    item: JvmConnectionResourceItem,
    selected: Boolean,
    recommended: Boolean,
    onClick: () -> Unit,
) {
    val dimens = XyTheme.dimens
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(dimens.corner / 2)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimens.itemHeight - dimens.contentPadding)
            .clip(shape)
            .background(if (selected) primary.copy(alpha = 0.06f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) primary.copy(alpha = 0.52f) else JvmConnectionLineColor(),
                shape = shape
            )
            .jvmHoverDebounceClickable(onClick = onClick)
            .padding(horizontal = dimens.contentPadding, vertical = dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.contentPadding)
    ) {
        // 单选框：选中时填充主色圆点。
        Box(
            modifier = Modifier
                .size(dimens.contentPadding + dimens.outerVerticalPadding / 2)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = if (selected) primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(dimens.outerVerticalPadding - 2.dp)
                        .clip(CircleShape)
                        .background(primary)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            XyText(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W700,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
            XyTextSub(
                text = item.address,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = JvmConnectionMonoStyle(),
                maxLines = 1
            )
        }
        JvmConnectionTag(
            text = item.tag,
            color = if (recommended) XyTheme.extendedColors.success else MaterialTheme.colorScheme.onSurfaceVariant,
            borderColor = if (recommended) {
                XyTheme.extendedColors.success.copy(alpha = 0.34f)
            } else {
                JvmConnectionLineColor()
            },
            backgroundColor = if (recommended) {
                XyTheme.extendedColors.success.copy(alpha = 0.09f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            }
        )
    }
}

/**
 * 底部状态条：连接状态与地址。
 */
@Composable
private fun JvmConnectionStatusBar(
    connected: Boolean,
    busy: Boolean,
    failed: Boolean,
    selectedDataSource: DataSourceType,
    address: String,
) {
    val dimens = XyTheme.dimens
    val dotColor = when {
        connected -> XyTheme.extendedColors.success
        failed -> XyTheme.extendedColors.danger
        busy -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val stateText = when {
        connected -> stringResource(Res.string.jvm_connection_rail_state_ok)
        failed -> stringResource(Res.string.jvm_connection_rail_state_failed)
        busy -> stringResource(Res.string.jvm_connection_rail_state_busy)
        else -> stringResource(Res.string.jvm_connection_rail_state_idle)
    }
    val addressText = if (selectedDataSource.ifInputUrl) {
        address.stripScheme().ifBlank { stringResource(Res.string.jvm_connection_new_screen_text_15) }
    } else {
        stringResource(Res.string.jvm_connection_new_screen_text_15)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(JvmConnectionLineColor())
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.outerHorizontalPadding * 2)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = dimens.outerHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.contentPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.outerVerticalPadding / 2)
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.outerVerticalPadding - 3.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                XyText(
                    text = stateText,
                    color = dotColor,
                    fontWeight = FontWeight.W600,
                    maxLines = 1,
                    style = JvmConnectionMonoStyle()
                )
            }
            XyTextSub(
                text = stringResource(Res.string.jvm_connection_status_address) + " " + addressText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = JvmConnectionMonoStyle(),
                maxLines = 1
            )
        }
    }
}

/**
 * 面板容器：细边框 + 低对比底色。
 */
@Composable
private fun JvmConnectionPanelCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = XyTheme.dimens
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.corner))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f))
            .border(
                width = 1.dp,
                color = JvmConnectionLineColor(),
                shape = RoundedCornerShape(dimens.corner)
            )
            .padding(dimens.contentPadding + dimens.outerVerticalPadding),
        content = content
    )
}

/**
 * 面板顶部等宽小标题。
 */
@Composable
private fun JvmConnectionPanelKicker(text: String) {
    XyText(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Normal,
        maxLines = 1,
        style = JvmConnectionMonoStyle(letterSpacing = 1.6.sp)
    )
}

/**
 * 面板标题。
 */
@Composable
private fun JvmConnectionPanelTitle(text: String) {
    XyText(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.W800,
        maxLines = 1,
        style = MaterialTheme.typography.titleMedium
    )
}

/**
 * 小标签：可选自定义前景、描边与底色。
 */
@Composable
private fun JvmConnectionTag(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color = JvmConnectionLineColor(),
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
) {
    val dimens = XyTheme.dimens
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimens.corner / 3))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(dimens.corner / 3))
            .padding(horizontal = dimens.outerVerticalPadding - 2.dp, vertical = 2.dp)
    ) {
        XyText(
            text = text,
            color = color,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            style = JvmConnectionMonoStyle()
        )
    }
}

/**
 * 通用按钮：复用 XyButton，主按钮使用主色填充，次按钮使用描边。
 */
@Composable
private fun JvmConnectionButton(
    text: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val line = JvmConnectionLineColor()
    XyButton(
        modifier = modifier,
        onClick = onClick,
        text = text,
        enabled = enabled,
        color = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        backgroundColor = if (primary) MaterialTheme.colorScheme.primary else Color.Transparent,
        hoverBackgroundColor = if (primary) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        },
        border = if (primary) null else BorderStroke(1.dp, line),
        textStyle = MaterialTheme.typography.labelMedium
    )
}

/**
 * 不确定进度条：用于资源探测与登录请求等待。
 */
@Composable
private fun JvmConnectionIndeterminateBar() {
    val transition = rememberInfiniteTransition(label = "connectionProgress")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "connectionProgressOffset"
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(JvmConnectionLineColor())
    ) {
        val barWidth = maxWidth
        Box(
            modifier = Modifier
                .offset(x = barWidth * progress - barWidth * 0.3f)
                .width(barWidth * 0.3f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

/**
 * 骨架占位条。
 */
@Composable
private fun JvmConnectionSkeletonBar(fraction: Float) {
    val dimens = XyTheme.dimens
    Box(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(dimens.outerVerticalPadding)
            .clip(RoundedCornerShape(dimens.corner / 3))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    )
}

/**
 * 统一的细线颜色，跟随主题明暗。
 */
@Composable
private fun JvmConnectionLineColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)

/**
 * 等宽小字样式：字号与行高沿用主题，仅替换字体族与字距。
 */
@Composable
private fun JvmConnectionMonoStyle(letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp) =
    MaterialTheme.typography.labelSmall.copy(
        fontFamily = FontFamily.Monospace,
        letterSpacing = letterSpacing
    )

/**
 * 去掉地址里的协议前缀，便于等宽展示。
 */
private fun String.stripScheme(): String =
    removePrefix(ApiConstants.HTTP).removePrefix(ApiConstants.HTTPS)

/**
 * 地址格式规则：协议可省略（应用会自动补全 http/https），主机必填，端口与路径可选。
 * 主机支持域名、IPv4 以及方括号形式的 IPv6。
 */
private val JvmConnectionAddressPattern = Regex(
    "^(?:https?://)?(?:\\[[0-9a-fA-F:]+\\]|[A-Za-z0-9._~%-]+)(?::\\d{1,5})?(?:[/?#]\\S*)?$",
    RegexOption.IGNORE_CASE
)

/** 端口提取：正则只约束位数，这里再确认端口落在 1..65535。 */
private val JvmConnectionPortPattern = Regex(":(\\d{1,5})(?:/|$)")

/**
 * 判断输入内容是否为可用的服务地址。
 */
private fun String.isValidServiceAddress(): Boolean {
    val value = trim()
    if (value.isEmpty() || !JvmConnectionAddressPattern.matches(value)) {
        return false
    }
    val port = JvmConnectionPortPattern.find(value)?.groupValues?.getOrNull(1)?.toIntOrNull()
    return port == null || port in 1..65535
}

/**
 * 资源候选的数据载体。
 */
private data class JvmConnectionResourceItem(
    val title: String,
    val address: String,
    val tag: String,
)

/**
 * 登录阶段：登录只有一次请求，成功与失败是终态。
 */
private enum class JvmConnectionLoginStage {
    Idle,
    LoggingIn,
    Success,
}
