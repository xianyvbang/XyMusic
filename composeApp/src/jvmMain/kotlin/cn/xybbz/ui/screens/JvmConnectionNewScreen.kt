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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopWindowControls
import cn.xybbz.ui.windows.desktopWindowDragArea
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconButton
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ConnectionViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.*
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.cancel_24px
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.logo_new
import xymusic_kmp.composeapp.generated.resources.password_24px
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.visibility_24px
import xymusic_kmp.composeapp.generated.resources.visibility_off_24px

@Composable
fun JvmConnectionNewScreen(
    connectionUiType: ConnectionUiType? = null,
    modifier: Modifier = Modifier,
    // 连接业务统一由 ViewModel 承担，默认走 Koin 注入，测试时可传入替身实例。
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
    // 资源面板显隐由 ViewModel 统一计算，页面只负责按状态渲染。
    val showResourcePanel = connectionViewModel.showResourcePanel
    // 当前资源选择下标直接来自 ViewModel，候选地址和 Plex 资源共用这一份选择状态。
    val selectedResourceIndex = connectionViewModel.selectUrlIndex
    // 新页面只需要把 ViewModel 的加载/成功状态映射成视觉阶段，不参与真实登录流程。
    val loginStage = when {
        connectionViewModel.isLoginSuccess -> JvmConnectionNewLoginStage.Success
        connectionViewModel.loading -> JvmConnectionNewLoginStage.LoggingIn
        else -> JvmConnectionNewLoginStage.Idle
    }
    val connectionReady = connectionViewModel.isLoginSuccess

    // 密码显隐只影响本页输入框表现，属于纯 UI 状态，不进入连接业务层。
    var showPassword by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(dataSourceTypes) {
        // 首次进入时保证有默认协议，选择规则由 ViewModel 维护。
        connectionViewModel.ensureDefaultDataSource(dataSourceTypes)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val compact = maxWidth < 920.dp
        val contentModifier = if (compact) {
            Modifier.verticalScroll(rememberScrollState())
        } else {
            Modifier
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (connectionUiType == ConnectionUiType.FIRST_OPEN) {
                JvmConnectionNewTitleBar()
            }

            if (compact) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(contentModifier)
                ) {
                    JvmConnectionNewSidebar(
                        modifier = Modifier.fillMaxWidth(),
                        dataSourceTypes = dataSourceTypes,
                        selectedDataSource = selectedDataSource,
                        activeStep = when {
                            connectionViewModel.loading || connectionViewModel.isLoginSuccess -> 3
                            showResourcePanel -> 2
                            else -> 1
                        },
                        statusText = connectionUiType.connectionStatusText(),
                        onSelectDataSource = {
                            connectionViewModel.selectDataSource(it)
                        }
                    )
                    JvmConnectionNewMainContent(
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                        selectedDataSource = selectedDataSource,
                        address = connectionViewModel.address,
                        username = connectionViewModel.username,
                        password = connectionViewModel.password,
                        showPassword = showPassword,
                        showResourcePanel = showResourcePanel,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        connectionReady = connectionReady,
                        resourceLoading = connectionViewModel.resourceLoading,
                        resourceError = connectionViewModel.isResourceLoginError,
                        loginLoading = connectionViewModel.loading,
                        loginError = connectionViewModel.isLoginError,
                        errorMessage = connectionViewModel.errorMessage,
                        errorHint = stringResource(connectionViewModel.errorHint),
                        tmpAddressList = connectionViewModel.tmpAddressList,
                        tmpPlexInfo = connectionViewModel.tmpPlexInfo,
                        selectedResourceAddress = connectionViewModel.tmpAddress,
                        onAddressChange = connectionViewModel::setAddressData,
                        onUsernameChange = connectionViewModel::setUserNameData,
                        onPasswordChange = connectionViewModel::setPasswordData,
                        onTogglePassword = { showPassword = !showPassword },
                        // 连接、重置和资源选择都委托给 ViewModel，页面不承载业务分支。
                        onConnect = connectionViewModel::connect,
                        onReset = connectionViewModel::resetConnectionInput,
                        onSelectResource = connectionViewModel::selectResourceAndLogin,
                        onEnter = {
                            if (!ifEntryPage) {
                                connectionViewModel.updateIfConnectionConfig()
                            } else {
                                navigator.goBack()
                            }
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    JvmConnectionNewSidebar(
                        modifier = Modifier
                            .width(324.dp)
                            .fillMaxHeight(),
                        dataSourceTypes = dataSourceTypes,
                        selectedDataSource = selectedDataSource,
                        activeStep = when {
                            connectionViewModel.loading || connectionViewModel.isLoginSuccess -> 3
                            showResourcePanel -> 2
                            else -> 1
                        },
                        statusText = connectionUiType.connectionStatusText(),
                        onSelectDataSource = {
                            connectionViewModel.selectDataSource(it)
                        }
                    )
                    JvmConnectionNewMainContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        compact = false,
                        selectedDataSource = selectedDataSource,
                        address = connectionViewModel.address,
                        username = connectionViewModel.username,
                        password = connectionViewModel.password,
                        showPassword = showPassword,
                        showResourcePanel = showResourcePanel,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        connectionReady = connectionReady,
                        resourceLoading = connectionViewModel.resourceLoading,
                        resourceError = connectionViewModel.isResourceLoginError,
                        loginLoading = connectionViewModel.loading,
                        loginError = connectionViewModel.isLoginError,
                        errorMessage = connectionViewModel.errorMessage,
                        errorHint = stringResource(connectionViewModel.errorHint),
                        tmpAddressList = connectionViewModel.tmpAddressList,
                        tmpPlexInfo = connectionViewModel.tmpPlexInfo,
                        selectedResourceAddress = connectionViewModel.tmpAddress,
                        onAddressChange = connectionViewModel::setAddressData,
                        onUsernameChange = connectionViewModel::setUserNameData,
                        onPasswordChange = connectionViewModel::setPasswordData,
                        onTogglePassword = { showPassword = !showPassword },
                        // 连接、重置和资源选择都委托给 ViewModel，页面不承载业务分支。
                        onConnect = connectionViewModel::connect,
                        onReset = connectionViewModel::resetConnectionInput,
                        onSelectResource = connectionViewModel::selectResourceAndLogin,
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

@Composable
private fun JvmConnectionNewTitleBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .desktopWindowDragArea()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.86f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
            .padding(start = XyTheme.dimens.outerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XySmallImage(
            modifier = Modifier
                .size(XyTheme.dimens.outerHorizontalPadding + XyTheme.dimens.contentPadding),
            model = painterResource(Res.drawable.logo_new),
            contentDescription = null,
            shape = RoundedCornerShape(XyTheme.dimens.corner / 2)
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        XyText(
            text = "XyMusic",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        DesktopWindowControls()
    }
}

@Composable
private fun JvmConnectionNewSidebar(
    modifier: Modifier,
    dataSourceTypes: List<DataSourceType>,
    selectedDataSource: DataSourceType,
    activeStep: Int,
    statusText: String,
    onSelectDataSource: (DataSourceType) -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding + XyTheme.dimens.outerVerticalPadding
            ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding + XyTheme.dimens.outerVerticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            XyText(
                text = stringResource(Res.string.server_connection),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.titleMedium,
            )
            JvmConnectionNewStatusPill(text = statusText)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            dataSourceTypes.forEach { dataSourceType ->
                JvmConnectionNewProtocolItem(
                    dataSourceType = dataSourceType,
                    selected = dataSourceType == selectedDataSource,
                    onClick = { onSelectDataSource(dataSourceType) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))

        JvmConnectionNewStepPanel(activeStep = activeStep)
    }
}

@Composable
private fun JvmConnectionNewProtocolItem(
    dataSourceType: DataSourceType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.56f)
    } else {
        Color.Transparent
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .jvmHoverDebounceClickable(onClick = onClick)
            .padding(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        JvmConnectionNewProtocolLogo(
            painter = painterResource(dataSourceType.img),
            contentDescription = dataSourceType.title
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            XyText(
                text = dataSourceType.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W800,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
            )
            XyTextSub(
                text = dataSourceType.describe,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        JvmConnectionNewChip(text = dataSourceType.httpPort.toString())
    }
}

@Composable
private fun JvmConnectionNewProtocolLogo(
    painter: Painter,
    contentDescription: String?,
    size: Dp? = null,
) {
    val logoSize = size
        ?: XyTheme.dimens.innerHorizontalPadding +
        XyTheme.dimens.innerHorizontalPadding +
        XyTheme.dimens.contentPadding

    Surface(
        modifier = Modifier.size(logoSize),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
    ) {
        XySmallImage(
            modifier = Modifier.fillMaxSize(),
            model = painter,
            contentDescription = contentDescription,
            shape = RoundedCornerShape(XyTheme.dimens.corner)
        )
    }
}

@Composable
private fun JvmConnectionNewStatusPill(
    text: String,
) {
    val successColor = Color(0xFF16824A)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(successColor.copy(alpha = 0.1f))
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding / 2,
                vertical = XyTheme.dimens.innerVerticalPadding / 2
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding / 2)
    ) {
        Box(
            modifier = Modifier
                .size(XyTheme.dimens.innerVerticalPadding / 2)
                .clip(CircleShape)
                .background(successColor)
        )
        XyText(
            text = text,
            color = successColor,
            fontWeight = FontWeight.W700,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun JvmConnectionNewStepPanel(
    activeStep: Int,
) {
    val steps = listOf(stringResource(Res.string.jvm_connection_new_screen_text_01), stringResource(Res.string.jvm_connection_new_screen_text_02), stringResource(Res.string.jvm_connection_new_screen_text_03), stringResource(Res.string.jvm_connection_new_screen_text_04))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(XyTheme.dimens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        steps.forEachIndexed { index, text ->
            JvmConnectionNewStepItem(
                number = index + 1,
                text = text,
                done = index < activeStep,
                active = index == activeStep,
            )
        }
    }
}

@Composable
private fun JvmConnectionNewStepItem(
    number: Int,
    text: String,
    done: Boolean,
    active: Boolean,
) {
    val successColor = Color(0xFF16824A)
    val circleColor = when {
        done -> successColor
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f)
    }
    val textColor = if (done || active) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
    ) {
        Box(
            modifier = Modifier
                .size(XyTheme.dimens.contentPadding + XyTheme.dimens.innerVerticalPadding)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    modifier = Modifier.size(XyTheme.dimens.outerHorizontalPadding),
                    painter = painterResource(Res.drawable.check_24px),
                    contentDescription = null,
                    tint = Color.White
                )
            } else {
                XyText(
                    text = number.toString(),
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.W800,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        XyText(
            text = text,
            color = textColor,
            fontWeight = if (done || active) FontWeight.W700 else FontWeight.Normal,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun JvmConnectionNewMainContent(
    modifier: Modifier,
    compact: Boolean,
    selectedDataSource: DataSourceType,
    address: String,
    username: String,
    password: String,
    showPassword: Boolean,
    showResourcePanel: Boolean,
    selectedResourceIndex: Int,
    loginStage: JvmConnectionNewLoginStage,
    connectionReady: Boolean,
    resourceLoading: Boolean,
    resourceError: Boolean,
    loginLoading: Boolean,
    loginError: Boolean,
    errorMessage: String,
    errorHint: String,
    tmpAddressList: List<String>,
    tmpPlexInfo: List<ResourceData>,
    selectedResourceAddress: String,
    onAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onConnect: () -> Unit,
    onReset: () -> Unit,
    onSelectResource: (Int) -> Unit,
    onEnter: () -> Unit,
) {
    // MainContent 只接收 ViewModel 已经整理好的状态，用来分发给左右面板和成功横幅。
    Column(
        modifier = modifier
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding + XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.innerVerticalPadding + XyTheme.dimens.contentPadding
            ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding + XyTheme.dimens.outerVerticalPadding)
    ) {
        JvmConnectionNewHeader(
            selectedDataSource = selectedDataSource,
            compact = compact
        )

        // 登录成功后展示真实收尾按钮，按钮动作由上层根据入口类型处理。
        AnimatedVisibility(visible = connectionReady) {
            JvmConnectionNewSuccessBanner(
                selectedDataSource = selectedDataSource,
                onEnter = onEnter
            )
        }

        if (compact) {
            Column(
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding)
            ) {
                if (showResourcePanel) {
                    JvmConnectionNewSidePanel(
                        showResourcePanel = true,
                        selectedDataSource = selectedDataSource,
                        address = address,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        resourceLoading = resourceLoading,
                        resourceError = resourceError,
                        loginLoading = loginLoading,
                        loginError = loginError,
                        errorMessage = errorMessage,
                        errorHint = errorHint,
                        tmpAddressList = tmpAddressList,
                        tmpPlexInfo = tmpPlexInfo,
                        selectedResourceAddress = selectedResourceAddress,
                        connectionReady = connectionReady,
                        onSelectResource = onSelectResource,
                        onEditConnectionInfo = onReset
                    )
                } else {
                    JvmConnectionNewFormPanel(
                        selectedDataSource = selectedDataSource,
                        address = address,
                        username = username,
                        password = password,
                        showPassword = showPassword,
                        onAddressChange = onAddressChange,
                        onUsernameChange = onUsernameChange,
                        onPasswordChange = onPasswordChange,
                        onTogglePassword = onTogglePassword,
                        onConnect = onConnect,
                        onReset = onReset
                    )
                    JvmConnectionNewSidePanel(
                        showResourcePanel = false,
                        selectedDataSource = selectedDataSource,
                        address = address,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        resourceLoading = resourceLoading,
                        resourceError = resourceError,
                        loginLoading = loginLoading,
                        loginError = loginError,
                        errorMessage = errorMessage,
                        errorHint = errorHint,
                        tmpAddressList = tmpAddressList,
                        tmpPlexInfo = tmpPlexInfo,
                        selectedResourceAddress = selectedResourceAddress,
                        connectionReady = connectionReady,
                        onSelectResource = onSelectResource
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding),
                verticalAlignment = Alignment.Top
            ) {
                JvmConnectionNewFormPanel(
                    modifier = Modifier.weight(1f),
                    selectedDataSource = selectedDataSource,
                    address = address,
                    username = username,
                    password = password,
                    showPassword = showPassword,
                    onAddressChange = onAddressChange,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onTogglePassword = onTogglePassword,
                    onConnect = onConnect,
                    onReset = onReset
                )
                JvmConnectionNewSidePanel(
                    modifier = Modifier.weight(1f),
                    showResourcePanel = showResourcePanel,
                    selectedDataSource = selectedDataSource,
                    address = address,
                    selectedResourceIndex = selectedResourceIndex,
                    loginStage = loginStage,
                    resourceLoading = resourceLoading,
                    resourceError = resourceError,
                    loginLoading = loginLoading,
                    loginError = loginError,
                    errorMessage = errorMessage,
                    errorHint = errorHint,
                    tmpAddressList = tmpAddressList,
                    tmpPlexInfo = tmpPlexInfo,
                    selectedResourceAddress = selectedResourceAddress,
                    connectionReady = connectionReady,
                    onSelectResource = onSelectResource
                )
            }
        }
    }
}

@Composable
private fun JvmConnectionNewHeader(
    selectedDataSource: DataSourceType,
    compact: Boolean,
) {
    val titleCopy = if (selectedDataSource.ifInputUrl) {
        stringResource(Res.string.jvm_connection_new_screen_text_05, selectedDataSource.title)
    } else {
        stringResource(Res.string.jvm_connection_new_screen_text_06, selectedDataSource.title)
    }

    if (compact) {
        Column(
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            JvmConnectionNewHeaderText(titleCopy = titleCopy)
            JvmConnectionNewSelectedService(dataSourceType = selectedDataSource)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding),
            verticalAlignment = Alignment.Top
        ) {
            JvmConnectionNewHeaderText(
                modifier = Modifier.weight(1f),
                titleCopy = titleCopy
            )
            JvmConnectionNewSelectedService(dataSourceType = selectedDataSource)
        }
    }
}

@Composable
private fun JvmConnectionNewHeaderText(
    titleCopy: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        XyTextLarge(
            text = stringResource(Res.string.jvm_connection_new_screen_text_07),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(9.dp))
        XyTextSub(
            text = titleCopy,
            modifier = Modifier.widthIn(max = 640.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.35
            ),
        )
    }
}

@Composable
private fun JvmConnectionNewSelectedService(
    dataSourceType: DataSourceType,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .padding(
                start = XyTheme.dimens.outerHorizontalPadding / 2,
                end = XyTheme.dimens.contentPadding,
                top = XyTheme.dimens.innerVerticalPadding / 2,
                bottom = XyTheme.dimens.innerVerticalPadding / 2
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2)
    ) {
        Box(
            modifier = Modifier
                .size(XyTheme.dimens.dialogCorner)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            XyText(
                text = dataSourceType.title.first().toString(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.labelSmall
            )
        }
        XyText(
            text = dataSourceType.title,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun JvmConnectionNewFormPanel(
    selectedDataSource: DataSourceType,
    address: String,
    username: String,
    password: String,
    showPassword: Boolean,
    onAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onConnect: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JvmConnectionNewPanel(
        modifier = modifier,
    ) {
        XyText(
            text = stringResource(Res.string.jvm_connection_new_screen_text_08, selectedDataSource.title),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(5.dp))
        XyTextSub(
            text = selectedDataSource.describe,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(18.dp))

        Column {
            AnimatedVisibility(visible = selectedDataSource.ifInputUrl) {
                Column {
                    JvmConnectionNewInputField(
                        label = stringResource(Res.string.jvm_connection_new_screen_text_09),
                        text = address,
                        onChange = onAddressChange,
                        hint = "${ApiConstants.HTTP}192.168.1.12:${selectedDataSource.httpPort}",
                        keyboardType = KeyboardType.Uri,
                        leadingIcon = painterResource(Res.drawable.http_24px),
                        actionContent = if (address.isNotBlank()) {
                            {
                                JvmConnectionNewClearButton {
                                    onAddressChange("")
                                }
                            }
                        } else null
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
                    ) {
                        JvmConnectionNewChip(
                            text = stringResource(Res.string.jvm_connection_new_screen_text_10, selectedDataSource.httpPort),
                            color = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        )
                        JvmConnectionNewChip(text = stringResource(Res.string.jvm_connection_new_screen_text_11))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            JvmConnectionNewInputField(
                label = stringResource(Res.string.username),
                text = username,
                onChange = onUsernameChange,
                hint = "admin",
                keyboardType = KeyboardType.Text,
                leadingIcon = painterResource(Res.drawable.person_24px),
                actionContent = if (username.isNotBlank()) {
                    {
                        JvmConnectionNewClearButton {
                            onUsernameChange("")
                        }
                    }
                } else null
            )
            Spacer(modifier = Modifier.height(14.dp))

            JvmConnectionNewInputField(
                label = stringResource(Res.string.password),
                text = password,
                onChange = onPasswordChange,
                hint = "password",
                keyboardType = KeyboardType.Password,
                leadingIcon = painterResource(Res.drawable.password_24px),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                actionContent = {
                    XyIconButton(
                        onClick = onTogglePassword,
                    ) {
                        Icon(
                            painter = painterResource(
                                if (showPassword) {
                                    Res.drawable.visibility_24px
                                } else {
                                    Res.drawable.visibility_off_24px
                                }
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = !selectedDataSource.ifInputUrl) {
                Column {
                    JvmConnectionNewChip(
                        text = stringResource(Res.string.jvm_connection_new_screen_text_12),
                        color = Color(0xFFA86200),
                        backgroundColor = Color(0xFFFFF4DF),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    modifier = Modifier.jvmHoverDebounceClickable(),
                    onClick = onReset,
                    shape = RoundedCornerShape(XyTheme.dimens.corner),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    XyText(
                        text = stringResource(Res.string.reselect),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                XyButton(
                    onClick = onConnect,
                    text = stringResource(Res.string.connect)
                )
            }
        }
    }
}

@Composable
private fun JvmConnectionNewInputField(
    label: String,
    text: String,
    onChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType,
    leadingIcon: Painter,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        XyText(
            text = label,
//            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W800,
            style = MaterialTheme.typography.bodySmall,
        )
        XyEdit(
            text = text,
            onChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
//            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            paddingValues = PaddingValues(),
            hint = hint,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            leadingContent = {
                Icon(
                    painter = leadingIcon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            actionContent = actionContent
        )
    }
}

@Composable
private fun JvmConnectionNewClearButton(
    onClick: () -> Unit,
) {
    XyIconButton(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(Res.drawable.cancel_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JvmConnectionNewSidePanel(
    showResourcePanel: Boolean,
    selectedDataSource: DataSourceType,
    address: String,
    selectedResourceIndex: Int,
    loginStage: JvmConnectionNewLoginStage,
    resourceLoading: Boolean,
    resourceError: Boolean,
    loginLoading: Boolean,
    loginError: Boolean,
    errorMessage: String,
    errorHint: String,
    tmpAddressList: List<String>,
    tmpPlexInfo: List<ResourceData>,
    selectedResourceAddress: String,
    connectionReady: Boolean,
    onSelectResource: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onEditConnectionInfo: (() -> Unit)? = null,
) {
    // 侧栏卡片根据 ViewModel 的 showResourcePanel 切换预览或真实资源/登录状态。
    JvmConnectionNewPanel(
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = showResourcePanel,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 180,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    initialContentExit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 140,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    sizeTransform = SizeTransform(clip = true) { _, _ ->
                        tween(
                            durationMillis = 280,
                            easing = FastOutSlowInEasing
                        )
                    }
                )
            },
            label = "connectionSidePanel"
        ) { resourcePanelVisible ->
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (resourcePanelVisible) {
                    JvmConnectionNewResourceContent(
                        selectedDataSource = selectedDataSource,
                        address = address,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        resourceLoading = resourceLoading,
                        resourceError = resourceError,
                        loginLoading = loginLoading,
                        loginError = loginError,
                        errorMessage = errorMessage,
                        errorHint = errorHint,
                        tmpAddressList = tmpAddressList,
                        tmpPlexInfo = tmpPlexInfo,
                        selectedResourceAddress = selectedResourceAddress,
                        connectionReady = connectionReady,
                        onSelectResource = onSelectResource,
                        onEditConnectionInfo = onEditConnectionInfo,
                    )
                } else {
                    JvmConnectionNewSummaryContent(
                        selectedDataSource = selectedDataSource,
                        address = address,
                    )
                }
            }
        }
    }
}

@Composable
private fun JvmConnectionNewSummaryContent(
    selectedDataSource: DataSourceType,
    address: String,
) {
    val accentBlockSize = XyTheme.dimens.innerHorizontalPadding +
        XyTheme.dimens.contentPadding +
        XyTheme.dimens.innerVerticalPadding +
        XyTheme.dimens.innerVerticalPadding / 2
    val heroLogoSize = accentBlockSize + accentBlockSize

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        Color(0xFF16A06B).copy(alpha = 0.12f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(heroLogoSize)
                .clip(RoundedCornerShape(XyTheme.dimens.dialogCorner))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.78f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(XyTheme.dimens.dialogCorner)
                )
                .padding(XyTheme.dimens.outerHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            XySmallImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = painterResource(selectedDataSource.img),
                contentDescription = selectedDataSource.title,
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    JvmConnectionNewMetaRow(label = stringResource(Res.string.jvm_connection_new_screen_text_13), value = selectedDataSource.title)
    JvmConnectionNewMetaRow(
        label = stringResource(Res.string.jvm_connection_new_screen_text_14),
        value = if (selectedDataSource.ifInputUrl) {
            address.removePrefix(ApiConstants.HTTP).removePrefix(ApiConstants.HTTPS).ifBlank { stringResource(Res.string.jvm_connection_config_info_screen_text_37) }
        } else {
            stringResource(Res.string.jvm_connection_new_screen_text_15)
        }
    )
    JvmConnectionNewMetaRow(
        label = stringResource(Res.string.jvm_about_screen_text_09),
        value = if (selectedDataSource.ifInputUrl) {
            "${selectedDataSource.version}+"
        } else {
            stringResource(Res.string.jvm_connection_new_screen_text_16)
        }
    )
    Spacer(modifier = Modifier.height(14.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        Box(
            modifier = Modifier
                .size(accentBlockSize)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFF16A06B)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            XyText(
                text = stringResource(Res.string.jvm_connection_new_screen_text_17),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W800,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_connection_new_screen_text_18),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun JvmConnectionNewMetaRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = XyTheme.dimens.outerVerticalPadding / 2),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyTextSub(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        XyText(
            text = value,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun JvmConnectionNewResourceContent(
    selectedDataSource: DataSourceType,
    address: String,
    selectedResourceIndex: Int,
    loginStage: JvmConnectionNewLoginStage,
    resourceLoading: Boolean,
    resourceError: Boolean,
    loginLoading: Boolean,
    loginError: Boolean,
    errorMessage: String,
    errorHint: String,
    tmpAddressList: List<String>,
    tmpPlexInfo: List<ResourceData>,
    selectedResourceAddress: String,
    connectionReady: Boolean,
    onSelectResource: (Int) -> Unit,
    onEditConnectionInfo: (() -> Unit)? = null,
) {
    // 资源内容只渲染 ViewModel 暴露的真实候选地址、Plex 资源和错误状态。
    XyText(
        text = stringResource(Res.string.jvm_connection_new_screen_text_19),
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.W900,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(5.dp))
    XyTextSub(
        text = if (selectedDataSource.ifInputUrl) {
            stringResource(Res.string.jvm_connection_new_screen_text_20, selectedDataSource.title)
        } else {
            stringResource(Res.string.jvm_connection_new_screen_text_21)
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
    if (onEditConnectionInfo != null) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .jvmHoverDebounceClickable(),
            onClick = onEditConnectionInfo,
            shape = RoundedCornerShape(XyTheme.dimens.corner),
        ) {
            XyText(
                text = stringResource(Res.string.jvm_connection_new_screen_text_22),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
    Spacer(modifier = Modifier.height(14.dp))

    // 状态优先级由业务流程决定：加载和登录结果优先，其次才展示可选择资源。
    when {
        resourceLoading -> {
            JvmConnectionNewLoadingPanel(text = stringResource(Res.string.jvm_connection_new_screen_text_23))
        }

        loginLoading || connectionReady -> {
            // 点击资源后 ViewModel 会立即开始登录，因此这里展示登录进度或成功状态。
            JvmConnectionNewLoginStatusPanel(
                selectedDataSource = selectedDataSource,
                resourceAddress = selectedResourceAddress.ifBlank { address },
                stage = loginStage,
            )
        }

        resourceError -> {
            JvmConnectionNewErrorPanel(
                title = stringResource(Res.string.jvm_connection_new_screen_text_24),
                errorHint = errorHint,
                errorMessage = errorMessage
            )
        }

        loginError -> {
            JvmConnectionNewErrorPanel(
                title = stringResource(Res.string.login_failed),
                errorHint = errorHint,
                errorMessage = errorMessage
            )
        }

        tmpPlexInfo.isNotEmpty() -> {
            // Plex 等账号发现型协议会展示真实资源列表。
            Column(
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
            ) {
                tmpPlexInfo.forEachIndexed { index, item ->
                    JvmConnectionNewResourceItem(
                        title = item.name,
                        address = item.addressUrl,
                        tag = item.product.ifBlank { stringResource(Res.string.jvm_connection_new_screen_text_25) },
                        selected = index == selectedResourceIndex,
                        onClick = { onSelectResource(index) }
                    )
                }
            }
        }

        tmpAddressList.isNotEmpty() -> {
            // URL 输入型协议在地址不完整时会由 ViewModel 生成候选连接地址。
            Column(
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
            ) {
                tmpAddressList.forEachIndexed { index, item ->
                    JvmConnectionNewResourceItem(
                        title = if (index == 0) stringResource(Res.string.jvm_connection_new_screen_text_26) else stringResource(Res.string.jvm_connection_new_screen_text_27),
                        address = item,
                        tag = if (index == 0) stringResource(Res.string.recommend) else stringResource(Res.string.jvm_connection_new_screen_text_28),
                        selected = index == selectedResourceIndex,
                        onClick = { onSelectResource(index) }
                    )
                }
            }
        }

        else -> {
            // 资源请求已触发但没有可展示数据时，保留面板并给出空态。
            JvmConnectionNewEmptyResourcePanel()
        }
    }
}

@Composable
private fun JvmConnectionNewResourceItem(
    title: String,
    address: String,
    tag: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(shape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
                },
                shape = shape
            )
            .jvmHoverDebounceClickable(onClick = onClick)
            .padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        Box(
            modifier = Modifier
                .size(XyTheme.dimens.contentPadding + XyTheme.dimens.innerVerticalPadding / 2)
                .clip(CircleShape)
                .border(
                    width = if (selected) 5.dp else 2.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f)
                    },
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            XyText(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W800,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
            XyTextSub(
                text = address,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        }
        JvmConnectionNewChip(
            text = tag,
            color = Color(0xFF16824A),
            backgroundColor = Color(0xFFE8F6EE)
        )
    }
}

@Composable
private fun JvmConnectionNewLoadingPanel(
    text: String,
) {
    // 资源加载和登录加载共用轻量状态面板，文案由调用处按业务阶段传入。
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        XyText(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W800,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun JvmConnectionNewErrorPanel(
    title: String,
    errorHint: String,
    errorMessage: String,
) {
    // 错误内容完全来自 ViewModel，页面只负责组合标题、提示和后端返回的详细信息。
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(Color(0xFFB3261E).copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = Color(0xFFB3261E).copy(alpha = 0.24f),
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(XyTheme.dimens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        XyText(
            text = title,
            color = Color(0xFFB3261E),
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.bodyMedium
        )
        XyTextSub(
            text = errorHint,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
        if (errorMessage.isNotBlank()) {
            XyTextSub(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun JvmConnectionNewEmptyResourcePanel() {
    // ViewModel 已请求资源面板但暂无候选数据时，保留面板避免视觉状态跳回表单。
    XyTextSub(
        text = stringResource(Res.string.jvm_connection_new_screen_text_29),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun JvmConnectionNewLoginStatusPanel(
    selectedDataSource: DataSourceType,
    resourceAddress: String,
    stage: JvmConnectionNewLoginStage,
    modifier: Modifier = Modifier,
) {
    val successColor = Color(0xFF16824A)
    val activeStage = if (stage == JvmConnectionNewLoginStage.Idle) {
        JvmConnectionNewLoginStage.LoggingIn
    } else {
        stage
    }
    val isSuccess = activeStage == JvmConnectionNewLoginStage.Success
    val activeIndex = when (activeStage) {
        JvmConnectionNewLoginStage.Idle,
        JvmConnectionNewLoginStage.LoggingIn -> 1
        JvmConnectionNewLoginStage.Success -> 3
    }
    val title = when (activeStage) {
        JvmConnectionNewLoginStage.Idle,
        JvmConnectionNewLoginStage.LoggingIn -> stringResource(Res.string.jvm_connection_new_screen_text_30)
        JvmConnectionNewLoginStage.Success -> stringResource(Res.string.login_success)
    }
    val detail = when (activeStage) {
        JvmConnectionNewLoginStage.Idle,
        JvmConnectionNewLoginStage.LoggingIn -> stringResource(Res.string.jvm_connection_new_screen_text_31, selectedDataSource.title)
        JvmConnectionNewLoginStage.Success -> stringResource(Res.string.jvm_connection_new_screen_text_32)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(
                if (isSuccess) {
                    successColor.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                }
            )
            .border(
                width = 1.dp,
                color = if (isSuccess) {
                    successColor.copy(alpha = 0.24f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                },
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(XyTheme.dimens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmConnectionNewLoginIndicator(
                success = isSuccess,
                successColor = successColor
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                XyText(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W900,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
                XyTextSub(
                    text = detail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            JvmConnectionNewChip(
                text = if (isSuccess) stringResource(Res.string.jvm_connection_new_screen_text_33) else stringResource(Res.string.jvm_connection_new_screen_text_34),
                color = if (isSuccess) successColor else MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            listOf(stringResource(Res.string.jvm_connection_new_screen_text_35), stringResource(Res.string.jvm_connection_new_screen_text_36), stringResource(Res.string.jvm_connection_new_screen_text_37)).forEachIndexed { index, text ->
                JvmConnectionNewLoginProgressItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    done = index < activeIndex,
                    active = index == activeIndex,
                    successColor = successColor
                )
            }
        }

        JvmConnectionNewMetaRow(
            label = stringResource(Res.string.jvm_connection_new_screen_text_38),
            value = resourceAddress.ifBlank { stringResource(Res.string.jvm_connection_new_screen_text_15) }
        )
    }
}

@Composable
private fun JvmConnectionNewLoginIndicator(
    success: Boolean,
    successColor: Color,
) {
    Box(
        modifier = Modifier.size(30.dp),
        contentAlignment = Alignment.Center
    ) {
        if (success) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(successColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(Res.drawable.check_24px),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun JvmConnectionNewLoginProgressItem(
    text: String,
    done: Boolean,
    active: Boolean,
    successColor: Color,
    modifier: Modifier = Modifier,
) {
    val color = when {
        done -> successColor
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f))
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding / 2,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        Box(
            modifier = Modifier
                .size(XyTheme.dimens.outerVerticalPadding)
                .clip(CircleShape)
                .background(color.copy(alpha = if (done || active) 1f else 0.55f))
        )
        XyText(
            text = text,
            color = color,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun JvmConnectionNewSuccessBanner(
    selectedDataSource: DataSourceType,
    onEnter: () -> Unit,
) {
    // 成功横幅只出现于 ViewModel 登录成功后，进入主页/返回上一页由 onEnter 统一收尾。
    val successColor = Color(0xFF16824A)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(successColor.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = successColor.copy(alpha = 0.24f),
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            XyText(
                text = stringResource(Res.string.connection_successful),
                color = successColor,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.bodyMedium
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_connection_new_screen_text_39, selectedDataSource.title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
        XyButton(
            onClick = onEnter,
            text = stringResource(Res.string.jvm_connection_new_screen_text_40),
        )
    }
}

@Composable
private fun JvmConnectionNewPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            content = content
        )
    }
}

@Composable
private fun JvmConnectionNewChip(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding / 2,
                vertical = XyTheme.dimens.outerVerticalPadding / 2
            ),
        contentAlignment = Alignment.Center
    ) {
        XyText(
            text = text,
            color = color,
            fontWeight = FontWeight.W700,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private enum class JvmConnectionNewLoginStage {
    Idle,
    LoggingIn,
    Success,
}

@Composable
private fun ConnectionUiType?.connectionStatusText(): String = when (this) {
    // 侧栏状态文案跟随入口类型，避免添加连接时仍显示“首次打开”。
    ConnectionUiType.FIRST_OPEN -> stringResource(Res.string.jvm_connection_new_screen_text_41)
    ConnectionUiType.ADD_CONNECTION -> stringResource(Res.string.add_connection)
    null -> stringResource(Res.string.server_connection)
}
