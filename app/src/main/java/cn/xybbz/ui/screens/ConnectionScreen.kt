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

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Http
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ItemTrailingArrowRight
import cn.xybbz.ui.xy.LazyColumnComponent
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemOutSpacer
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.ui.xy.XyLoadingItem
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionViewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder.Companion.REPEAT_COUNT_KEY
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.launch

private enum class ScreenType {
    /**
     * 选择数据源
     */
    SELECT_DATA_SOURCE,

    /**
     * 输入地址
     */
    INPUT_DATA,

    /**
     * 选择地址
     */
    SELECT_ADDRESS,

    /**
     * 登陆
     */
    LOGIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    connectionUiType: ConnectionUiType?,
    modifier: Modifier = Modifier,
    connectionViewModel: ConnectionViewModel = hiltViewModel<ConnectionViewModel>(),
) {

    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    var ifSelectDataSource by remember {
        mutableStateOf(ScreenType.SELECT_DATA_SOURCE)
    }

    var showPassword by remember {
        mutableStateOf(false)
    }

    val pleaseEnterRequiredContent = stringResource(R.string.please_enter_required_content)

    //自己构建图片加载器
    val imageLoader = ImageLoader.Builder(context).components {
        add(ImageDecoderDecoder.Factory())
    }.build()

    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopAppBarComponent(title = {
            TopAppBarTitle(title = stringResource(R.string.server_connection))
        }, navigationIcon = {
            if (connectionUiType != null && connectionUiType == ConnectionUiType.ADD_CONNECTION)
                IconButton(
                    onClick = composeClick {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }

        })

        AnimatedVisibility(visible = ifSelectDataSource != ScreenType.SELECT_DATA_SOURCE) {
            ItemTrailingArrowRight(
                modifier = Modifier
                    .padding(horizontal = XyTheme.dimens.innerHorizontalPadding)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(XyTheme.dimens.corner)
                    ),
                name = connectionViewModel.dataSourceType?.title ?: "",
                subordination = stringResource(
                    R.string.connect_to_service,
                    connectionViewModel.dataSourceType?.describe ?: ""
                ),
                img = connectionViewModel.dataSourceType?.img?.let { img -> painterResource(img) },
                onClick = {
                    connectionViewModel.setDataSourceTypeData(connectionViewModel.dataSourceType)
                    ifSelectDataSource = ScreenType.INPUT_DATA
                }
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding / 2))
        }


        AnimatedContent(
            targetState = ifSelectDataSource, label = "切换",
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(

                    SizeTransform(clip = false)
                )
            }
        ) { screen ->
            when (screen) {
                ScreenType.SELECT_DATA_SOURCE -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumnNotComponent(
                            modifier = Modifier,
                            horizontalAlignment = Alignment.Start,
                            contentPadding = PaddingValues(
                                vertical = XyTheme.dimens.outerVerticalPadding
                            )
                        ) {

                            item {
                                XyRow {
                                    Text(
                                        text = stringResource(R.string.select_protocol)
                                    )
                                }
                            }
                            items(DataSourceType.entries.filter { it.ifShow }) {
                                ItemTrailingArrowRight(
                                    modifier = Modifier
                                        .padding(horizontal = XyTheme.dimens.innerHorizontalPadding)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                            shape = RoundedCornerShape(XyTheme.dimens.corner)
                                        ),
                                    name = it.title,
                                    subordination = stringResource(
                                        R.string.connect_to_service,
                                        it.describe
                                    ),
                                    img = painterResource(it.img),
                                    onClick = {
                                        connectionViewModel.setDataSourceTypeData(it)
                                        ifSelectDataSource = ScreenType.INPUT_DATA
                                    }
                                )
                            }
                        }
                    }
                }

                ScreenType.INPUT_DATA -> {
                    LazyColumnComponent {
                        if (connectionViewModel.dataSourceType?.ifInputUrl == true)
                            item {
                                AddressInputEdit(
                                    address = connectionViewModel.address,
                                    updateAddress = {
                                        connectionViewModel.setAddressData(it)
                                    }
                                )
                            }
                        item {
                            UsernameInputEdit(
                                username = connectionViewModel.username,
                                updateUsername = {
                                    connectionViewModel.setUserNameData(it)
                                }
                            )
                        }

                        item {
                            PasswordInputEdit(
                                password = connectionViewModel.password,
                                updatePassword = {
                                    connectionViewModel.setPasswordData(it)
                                }
                            )
                        }

                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    modifier = Modifier.width(width = 150.dp),
                                    onClick = {
                                        if (connectionViewModel.dataSourceType?.ifInputUrl == false) {
                                            if (connectionViewModel.ifInputAccount()) {
                                                MessageUtils.sendPopTipError("用户名不能为空")
                                                return@Button
                                            }
                                            Log.i("ConnectionScreen", "noifInputUrl")
                                            coroutineScope.launch {
                                                ifSelectDataSource = ScreenType.SELECT_ADDRESS
                                                connectionViewModel.getResources()
                                            }.invokeOnCompletion {
                                                connectionViewModel.updateResourceLoading(false)
                                            }
                                        } else if (!connectionViewModel.isInputError()) {
                                            Log.i("ConnectionScreen", "ifInputUrl")
                                            if (!connectionViewModel.isHttpStartAndPortEnd()) {
                                                connectionViewModel.createTmpAddress()
                                                ifSelectDataSource = ScreenType.SELECT_ADDRESS
                                            } else {
                                                ifSelectDataSource = ScreenType.LOGIN
                                                coroutineScope.launch {
                                                    connectionViewModel.setTmpAddressData(
                                                        connectionViewModel.address
                                                    )
                                                    connectionViewModel.inputAddress(context)
                                                }
                                            }
                                        } else {
                                            MessageUtils.sendPopTipError("连接地址或用户名不能为空")
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(R.string.connect))
                                }
                                Button(
                                    modifier = Modifier.width(width = 150.dp),
                                    onClick = {
                                        ifSelectDataSource = ScreenType.SELECT_DATA_SOURCE
                                    }
                                ) {
                                    Text(text = stringResource(R.string.reselect))
                                }
                            }
                        }

                    }
                }

                ScreenType.SELECT_ADDRESS -> {
                    LazyColumnComponent {
                        if (connectionViewModel.resourceLoading) {
                            item {
                                XyLoadingItem(
                                    modifier = Modifier.height(200.dp),
                                    text = "正在获取资源"
                                )
                            }

                        } else if (connectionViewModel.isResourceLoginError) {
                            item {
                                LoginError(
                                    modifier = Modifier.height(200.dp),
                                    errorMessage = connectionViewModel.errorMessage,
                                    errorHint = stringResource(connectionViewModel.errorHint)
                                )
                            }

                        } else {
                            item {
                                XyColumn(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    paddingValues = PaddingValues()
                                ) {
                                    connectionViewModel.dataSourceType?.let {
                                        if (connectionViewModel.tmpAddressList.isNotEmpty())
                                            LazyColumnNotComponent(
                                                modifier = Modifier.height(
                                                    400.dp
                                                )
                                            ) {
                                                itemsIndexed(connectionViewModel.tmpAddressList) { index, item ->
                                                    XyItemRadioButton(
                                                        text = item,
                                                        selected = index == connectionViewModel.selectUrlIndex,
                                                        fontWeight = null,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        onClick = {
                                                            connectionViewModel.setSelectUrlIndexData(
                                                                index
                                                            )
                                                        })
                                                }
                                            }

                                        if (connectionViewModel.tmpPlexInfo.isNotEmpty())
                                            LazyColumnNotComponent(
                                                modifier = Modifier.height(
                                                    300.dp
                                                ),
                                                bottomItem = null
                                            ) {
                                                itemsIndexed(connectionViewModel.tmpPlexInfo) { index, item ->
                                                    PlexResourceItem(
                                                        text = item.name,
                                                        serverName = item.product,
                                                        address = item.addressUrl,
                                                        select = index == connectionViewModel.selectUrlIndex,
                                                        onClick = {
                                                            connectionViewModel.setSelectInfoIndexData(
                                                                index
                                                            )
                                                        })
                                                }
                                            }
                                    }
                                }
                            }
                        }
                        item {
                            Column {
                                if (!connectionViewModel.isResourceLoginError && !connectionViewModel.resourceLoading) {
                                    Button(
                                        modifier = Modifier.width(width = 150.dp),
                                        onClick = {
                                            coroutineScope.launch {

                                                connectionViewModel.setSelectInfoIndexData(
                                                    connectionViewModel.selectUrlIndex
                                                )
                                                connectionViewModel.setSelectUrlIndexData(
                                                    connectionViewModel.selectUrlIndex
                                                )
                                                ifSelectDataSource = ScreenType.LOGIN
                                                connectionViewModel.inputAddress(context)
                                            }
                                        }) {
                                        Text(text = stringResource(R.string.connect))
                                    }
                                }

                                Button(
                                    modifier = Modifier.width(width = 150.dp),
                                    onClick = {
                                        ifSelectDataSource = ScreenType.INPUT_DATA
                                    }) {
                                    Text(stringResource(R.string.back_to_input_credentials))
                                }
                            }

                        }
                    }
                }

                ScreenType.LOGIN -> {
                    if (connectionViewModel.loading) {
                        XyLoadingItem(
                            modifier = Modifier.height(200.dp),
                            text = stringResource(R.string.logging_in)
                        )
                    } else {
                        LazyColumnComponent {
                            if (connectionViewModel.isLoginError) {
                                item {
                                    LoginError(
                                        modifier = Modifier.height(200.dp),
                                        errorMessage = connectionViewModel.errorMessage,
                                        errorHint = stringResource(connectionViewModel.errorHint)
                                    )
                                }
                                item {
                                    Button(
                                        modifier = Modifier.width(width = 150.dp),
                                        onClick = {
                                            ifSelectDataSource =
                                                if (connectionViewModel.isHttpStartAndPortEnd()) {
                                                    ScreenType.INPUT_DATA
                                                } else
                                                    ScreenType.SELECT_ADDRESS
                                        }) {
                                        Text(text = stringResource(R.string.reconnect))
                                    }
                                }
                            }

                            if (connectionViewModel.isLoginSuccess) {
                                item {
                                    XyColumn {
                                        XyText(
                                            text = stringResource(R.string.connection_successful),
                                            style = MaterialTheme.typography.titleLarge,
                                        )
                                        XySmallImage(
                                            modifier = Modifier.size(150.dp),
                                            shape = RoundedCornerShape(XyTheme.dimens.corner),
                                            model = //淡出效果
                                                rememberAsyncImagePainter(
                                                    model = ImageRequest.Builder(context)
                                                        .setParameter(REPEAT_COUNT_KEY, 0)
                                                        .data(data = R.drawable.celebrate)
                                                        .apply(block = fun ImageRequest.Builder.() {
                                                            crossfade(true)//淡出效果
                                                        }).build(),
                                                    imageLoader = imageLoader,
                                                ),
                                            contentDescription = stringResource(R.string.connection_success_image)
                                        )
                                    }
                                }
                                item {
                                    Button(
                                        modifier = Modifier.width(width = 150.dp),
                                        onClick = {
                                            if (!connectionViewModel.settingsManager.ifConnectionConfig) {
                                                connectionViewModel.updateIfConnectionConfig()
                                            } else {
                                                navigator.goBack()
                                            }

                                        }) {
                                        Text(stringResource(R.string.enter_page))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlexResourceItem(
    modifier: Modifier = Modifier,
    text: String,
    serverName: String,
    address: String,
    select: Boolean,
    onClick: (() -> Unit)? = null
) {
    XyRow(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .debounceClickable { onClick?.invoke() },
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            XyText(
                text = text,
                maxLines = 1,
            )
            XyTextSub(
                text = serverName,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            XyTextSubSmall(
                text = address,
                maxLines = 3,
            )
        }
        if (select)
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.selected_item, text)
            )
    }
}

@Composable
private fun LoginError(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.login_failed),
    errorMessage: String,
    errorHint: String
) {
    XyColumn(
        modifier = modifier.height(200.dp),
        paddingValues = PaddingValues()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            XyText(text = title, style = MaterialTheme.typography.titleLarge)
        }
        XyText(text = errorHint)
        XyItemOutSpacer()
        if (errorMessage.isNotBlank())
            XyTextSub(text = errorMessage)
    }
}

/**
 * 地址输入框
 */
@Composable
fun AddressInputEdit(
    address: String,
    updateAddress: (String) -> Unit
) {
    ConnectionDataInfoInputEdit(
        text = address,
        onChange = {
            updateAddress(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        hint = "连接地址:http://192.168.3.12:8096",
        icon = Icons.Rounded.Http,
        iconContentDescription = stringResource(R.string.httpInput),
        actionContent = if (address.isNotBlank()) {
            {
                IconButton(onClick = composeClick {
                    updateAddress("")
                }) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = "清空"
                    )
                }
            }
        } else null
    )
}

@Composable
fun UsernameInputEdit(
    modifier: Modifier = Modifier,
    username: String,
    updateUsername: (String) -> Unit
) {
    ConnectionDataInfoInputEdit(
        text = username,
        modifier = modifier,
        onChange = {
            updateUsername(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        hint = stringResource(R.string.username),
        icon = Icons.Rounded.Person,
        iconContentDescription = stringResource(R.string.username),
        actionContent = if (username.isNotBlank()) {
            {
                IconButton(onClick = composeClick {
                    updateUsername("")
                }) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = "清空"
                    )
                }
            }
        } else null
    )
}

@Composable
fun PasswordInputEdit(
    modifier: Modifier = Modifier,
    password: String,
    updatePassword: (String) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    ConnectionDataInfoInputEdit(
        text = password,
        modifier = modifier,
        onChange = {
            updatePassword(it)
        },
        actionContent = {
            IconButton(onClick = composeClick() {
                showPassword = !showPassword
            }) {
                Icon(
                    imageVector = if (showPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        hint = stringResource(R.string.password),
        icon = Icons.Rounded.Password,
        iconContentDescription = stringResource(R.string.password)
    )
}

@Composable
fun ConnectionNameInputEdit(
    modifier: Modifier = Modifier,
    connectionName: String,
    updateConnectionName: (String) -> Unit
) {
    ConnectionDataInfoInputEdit(
        text = connectionName,
        modifier = modifier,
        onChange = {
            updateConnectionName(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        hint = stringResource(R.string.set_alias),
        icon = Icons.Rounded.DriveFileRenameOutline,
        iconContentDescription = stringResource(R.string.set_alias),
        actionContent = if (connectionName.isNotBlank()) {
            {
                IconButton(onClick = composeClick {
                    updateConnectionName("")
                }) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = "清空"
                    )
                }
            }
        } else null
    )
}


@Composable
private fun ConnectionDataInfoInputEdit(
    modifier: Modifier = Modifier,
    text: String,
    onChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    hint: String? = null,
    icon: ImageVector,
    iconContentDescription: String,
    actionContent: (@Composable () -> Unit)? = null,
) {
    XyEdit(
        text = text,
        modifier = modifier
            .fillMaxWidth(),
        paddingValues = PaddingValues(
            vertical = XyTheme.dimens.outerVerticalPadding
        ),
        onChange = onChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        hint = hint,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription
            )
        },
        actionContent = actionContent
    )
}