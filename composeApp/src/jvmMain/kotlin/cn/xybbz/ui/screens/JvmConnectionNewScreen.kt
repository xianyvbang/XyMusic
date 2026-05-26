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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopWindowControls
import cn.xybbz.ui.windows.desktopWindowDragArea
import cn.xybbz.ui.xy.XyEdit
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
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
) {
    val dataSourceTypes = remember {
        DataSourceType.entries.filter { it.ifShow }
    }
    var selectedDataSource by remember {
        mutableStateOf(dataSourceTypes.firstOrNull() ?: DataSourceType.JELLYFIN)
    }
    var address by remember(selectedDataSource) {
        mutableStateOf(selectedDataSource.defaultAddress)
    }
    var username by remember(selectedDataSource) {
        mutableStateOf("admin")
    }
    var password by remember(selectedDataSource) {
        mutableStateOf("password")
    }
    var showPassword by remember {
        mutableStateOf(false)
    }
    var showResourcePanel by remember {
        mutableStateOf(false)
    }
    var selectedResourceIndex by remember {
        mutableIntStateOf(-1)
    }
    var loginStage by remember {
        mutableStateOf(JvmConnectionNewLoginStage.Idle)
    }
    val connectionReady by remember(showResourcePanel, selectedResourceIndex, loginStage) {
        derivedStateOf {
            showResourcePanel &&
                selectedResourceIndex >= 0 &&
                loginStage == JvmConnectionNewLoginStage.Success
        }
    }

    LaunchedEffect(selectedDataSource, address, selectedResourceIndex) {
        if (selectedResourceIndex < 0) {
            loginStage = JvmConnectionNewLoginStage.Idle
            return@LaunchedEffect
        }
        loginStage = JvmConnectionNewLoginStage.LoggingIn
        delay(650)
        loginStage = JvmConnectionNewLoginStage.Syncing
        delay(800)
        loginStage = JvmConnectionNewLoginStage.Success
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
                            selectedResourceIndex >= 0 -> 3
                            showResourcePanel -> 2
                            else -> 1
                        },
                        onSelectDataSource = {
                            selectedDataSource = it
                            showResourcePanel = false
                            selectedResourceIndex = -1
                            loginStage = JvmConnectionNewLoginStage.Idle
                        }
                    )
                    JvmConnectionNewMainContent(
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                        selectedDataSource = selectedDataSource,
                        address = address,
                        username = username,
                        password = password,
                        showPassword = showPassword,
                        showResourcePanel = showResourcePanel,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        connectionReady = connectionReady,
                        onAddressChange = { address = it },
                        onUsernameChange = { username = it },
                        onPasswordChange = { password = it },
                        onTogglePassword = { showPassword = !showPassword },
                        onConnect = {
                            showResourcePanel = true
                            selectedResourceIndex = -1
                            loginStage = JvmConnectionNewLoginStage.Idle
                        },
                        onReset = {
                            showResourcePanel = false
                            selectedResourceIndex = -1
                            loginStage = JvmConnectionNewLoginStage.Idle
                        },
                        onSelectResource = { selectedResourceIndex = it }
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
                            selectedResourceIndex >= 0 -> 3
                            showResourcePanel -> 2
                            else -> 1
                        },
                        onSelectDataSource = {
                            selectedDataSource = it
                            showResourcePanel = false
                            selectedResourceIndex = -1
                            loginStage = JvmConnectionNewLoginStage.Idle
                        }
                    )
                    JvmConnectionNewMainContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        compact = false,
                        selectedDataSource = selectedDataSource,
                        address = address,
                        username = username,
                        password = password,
                        showPassword = showPassword,
                        showResourcePanel = showResourcePanel,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
                        connectionReady = connectionReady,
                        onAddressChange = { address = it },
                        onUsernameChange = { username = it },
                        onPasswordChange = { password = it },
                        onTogglePassword = { showPassword = !showPassword },
                        onConnect = {
                            showResourcePanel = true
                            selectedResourceIndex = -1
                            loginStage = JvmConnectionNewLoginStage.Idle
                        },
                        onReset = {
                            showResourcePanel = false
                            selectedResourceIndex = -1
                            loginStage = JvmConnectionNewLoginStage.Idle
                        },
                        onSelectResource = { selectedResourceIndex = it }
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
        Image(
            modifier = Modifier
                .size(XyTheme.dimens.outerHorizontalPadding + XyTheme.dimens.contentPadding)
                .clip(RoundedCornerShape(XyTheme.dimens.corner / 2)),
            painter = painterResource(Res.drawable.logo_new),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        Text(
            text = "XyMusic",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
            Text(
                text = "服务器连接",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.titleMedium,
            )
            JvmConnectionNewStatusPill(text = "首次打开")
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
            .clickable(onClick = onClick)
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
            Text(
                text = dataSourceType.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = dataSourceType.describe,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
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
        Text(
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
    val steps = listOf("选择服务", "输入凭据", "选择资源", "登录验证")
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
                Text(
                    text = number.toString(),
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.W800,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Text(
            text = text,
            color = textColor,
            fontWeight = if (done || active) FontWeight.W700 else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    onAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onConnect: () -> Unit,
    onReset: () -> Unit,
    onSelectResource: (Int) -> Unit,
) {
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

        AnimatedVisibility(visible = connectionReady) {
            JvmConnectionNewSuccessBanner(selectedDataSource = selectedDataSource)
        }

        if (compact) {
            Column(
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding)
            ) {
                if (showResourcePanel) {
                    JvmConnectionNewResourcePanel(
                        selectedDataSource = selectedDataSource,
                        address = address,
                        selectedResourceIndex = selectedResourceIndex,
                        loginStage = loginStage,
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
                    JvmConnectionNewSummaryPanel(
                        selectedDataSource = selectedDataSource,
                        address = address
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
                Crossfade(
                    targetState = showResourcePanel,
                    modifier = Modifier.width(290.dp),
                    label = "connectionResourcePanel"
                ) { resourcePanelVisible ->
                    if (resourcePanelVisible) {
                        JvmConnectionNewResourcePanel(
                            selectedDataSource = selectedDataSource,
                            address = address,
                            selectedResourceIndex = selectedResourceIndex,
                            loginStage = loginStage,
                            onSelectResource = onSelectResource
                        )
                    } else {
                        JvmConnectionNewSummaryPanel(
                            selectedDataSource = selectedDataSource,
                            address = address
                        )
                    }
                }
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
        "已选择 ${selectedDataSource.title}，输入服务地址和账号后即可读取媒体资源。"
    } else {
        "已选择 ${selectedDataSource.title}，输入账号后会自动读取可用服务器。"
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
        Text(
            text = "连接到媒体服务器",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = titleCopy,
            modifier = Modifier.widthIn(max = 640.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.35,
            style = MaterialTheme.typography.bodyMedium,
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
            Text(
                text = dataSourceType.title.first().toString(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Text(
            text = dataSourceType.title,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
        Text(
            text = "${selectedDataSource.title} 凭据",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = selectedDataSource.describe,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(18.dp))

        Column {
            AnimatedVisibility(visible = selectedDataSource.ifInputUrl) {
                Column {
                    JvmConnectionNewInputField(
                        label = "服务地址",
                        text = address,
                        onChange = onAddressChange,
                        hint = "http://192.168.1.12:${selectedDataSource.httpPort}",
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
                            text = "默认端口 ${selectedDataSource.httpPort}",
                            color = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        )
                        JvmConnectionNewChip(text = "支持 http / https")
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            JvmConnectionNewInputField(
                label = "用户名",
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
                label = "密码",
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
                    IconButton(onClick = onTogglePassword) {
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
                        text = "Plex 将通过账号自动读取可用服务器",
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
                    onClick = onReset,
                    shape = RoundedCornerShape(XyTheme.dimens.corner),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "重新选择")
                }
                Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(XyTheme.dimens.corner)
                ) {
                    Text(text = "连接")
                }
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
        Text(
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
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.cancel_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JvmConnectionNewSummaryPanel(
    selectedDataSource: DataSourceType,
    address: String,
    modifier: Modifier = Modifier,
) {
    val accentBlockSize = XyTheme.dimens.innerHorizontalPadding +
        XyTheme.dimens.contentPadding +
        XyTheme.dimens.innerVerticalPadding +
        XyTheme.dimens.innerVerticalPadding / 2
    val heroLogoSize = accentBlockSize + accentBlockSize

    JvmConnectionNewPanel(
        modifier = modifier,
    ) {
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
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                    painter = painterResource(selectedDataSource.img),
                    contentDescription = selectedDataSource.title,
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        JvmConnectionNewMetaRow(label = "协议", value = selectedDataSource.title)
        JvmConnectionNewMetaRow(
            label = "地址",
            value = if (selectedDataSource.ifInputUrl) {
                address.removePrefix("http://").removePrefix("https://").ifBlank { "未填写" }
            } else {
                "自动发现"
            }
        )
        JvmConnectionNewMetaRow(
            label = "版本",
            value = if (selectedDataSource.ifInputUrl) {
                "${selectedDataSource.version}+"
            } else {
                "账号发现"
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
                Text(
                    text = "准备同步媒体库",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "歌曲、专辑、艺术家将在登录后读取",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun JvmConnectionNewResourcePanel(
    selectedDataSource: DataSourceType,
    address: String,
    selectedResourceIndex: Int,
    loginStage: JvmConnectionNewLoginStage,
    onSelectResource: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onEditConnectionInfo: (() -> Unit)? = null,
) {
    val resources = remember(selectedDataSource, address) {
        selectedDataSource.sampleResources(address)
    }

    JvmConnectionNewPanel(
        modifier = modifier,
    ) {
        Text(
            text = "选择资源地址",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = if (selectedDataSource.ifInputUrl) {
                "已找到可连接的 ${selectedDataSource.title} 地址。"
            } else {
                "已读取账号下的 Plex 服务器。"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        if (onEditConnectionInfo != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditConnectionInfo,
                shape = RoundedCornerShape(XyTheme.dimens.corner),
            ) {
                Text(text = "修改连接信息")
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            resources.forEachIndexed { index, item ->
                JvmConnectionNewResourceItem(
                    title = item.title,
                    address = item.address,
                    tag = item.tag,
                    selected = index == selectedResourceIndex,
                    onClick = { onSelectResource(index) }
                )
            }
        }
        AnimatedVisibility(visible = selectedResourceIndex >= 0) {
            val selectedResource = resources.getOrNull(selectedResourceIndex)
            Column {
                Spacer(modifier = Modifier.height(14.dp))
                JvmConnectionNewLoginStatusPanel(
                    selectedDataSource = selectedDataSource,
                    resourceAddress = selectedResource?.address.orEmpty(),
                    stage = loginStage,
                )
            }
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
            .clickable(onClick = onClick)
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
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.W800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = address,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
        JvmConnectionNewLoginStage.Syncing -> 2
        JvmConnectionNewLoginStage.Success -> 3
    }
    val title = when (activeStage) {
        JvmConnectionNewLoginStage.Idle,
        JvmConnectionNewLoginStage.LoggingIn -> "正在登录"
        JvmConnectionNewLoginStage.Syncing -> "正在同步资料"
        JvmConnectionNewLoginStage.Success -> "登录成功"
    }
    val detail = when (activeStage) {
        JvmConnectionNewLoginStage.Idle,
        JvmConnectionNewLoginStage.LoggingIn -> "正在使用 ${selectedDataSource.title} 账号登录所选资源。"
        JvmConnectionNewLoginStage.Syncing -> "会话已建立，正在读取用户资料和媒体库入口。"
        JvmConnectionNewLoginStage.Success -> "账号和资源地址已确认，可以进入主页。"
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
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = detail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            JvmConnectionNewChip(
                text = if (isSuccess) "完成" else if (activeStage == JvmConnectionNewLoginStage.Syncing) "同步中" else "登录中",
                color = if (isSuccess) successColor else MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            listOf("验证账号", "建立会话", "同步资料").forEachIndexed { index, text ->
                JvmConnectionNewLoginProgressItem(
                    modifier = Modifier.weight(1f),
                    text = text,
                    done = index < activeIndex,
                    active = index == activeIndex,
                    successColor = successColor
                )
            }
        }

        JvmConnectionNewMetaRow(
            label = "资源地址",
            value = resourceAddress.ifBlank { selectedDataSource.defaultAddress.ifBlank { "自动发现" } }
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
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.W800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun JvmConnectionNewSuccessBanner(
    selectedDataSource: DataSourceType,
) {
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
            Text(
                text = "连接成功",
                color = successColor,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${selectedDataSource.title} 登录完成，可以进入主页。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
        Button(
            onClick = {},
            shape = RoundedCornerShape(XyTheme.dimens.corner)
        ) {
            Text(text = "进入主页")
        }
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
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.W700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private val DataSourceType.defaultAddress: String
    get() = when (this) {
        DataSourceType.JELLYFIN -> "http://192.168.1.12:8096"
        DataSourceType.SUBSONIC -> "http://192.168.1.12:4040"
        DataSourceType.NAVIDROME -> "http://192.168.1.12:4533"
        DataSourceType.EMBY -> "http://192.168.1.12:8096"
        DataSourceType.PLEX -> ""
    }

private fun DataSourceType.sampleResources(address: String): List<JvmConnectionNewResource> {
    return if (ifInputUrl) {
        listOf(
            JvmConnectionNewResource(
                title = "主服务器",
                address = address.ifBlank { defaultAddress },
                tag = "推荐"
            ),
            JvmConnectionNewResource(
                title = "备用地址",
                address = "https://music.example.com",
                tag = "可用"
            )
        )
    } else {
        listOf(
            JvmConnectionNewResource(
                title = "Plex Music Library",
                address = "https://app.plex.tv",
                tag = "推荐"
            ),
            JvmConnectionNewResource(
                title = "Home Server",
                address = "http://192.168.1.20:32400",
                tag = "可用"
            )
        )
    }
}

private enum class JvmConnectionNewLoginStage {
    Idle,
    LoggingIn,
    Syncing,
    Success,
}

private data class JvmConnectionNewResource(
    val title: String,
    val address: String,
    val tag: String,
)
