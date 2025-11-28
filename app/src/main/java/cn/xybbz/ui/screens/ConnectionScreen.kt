package cn.xybbz.ui.screens

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.enums.img
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ItemTrailingArrowRight
import cn.xybbz.ui.xy.LazyColumnComponent
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.LazyColumnNotHorizontalComponent
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnNotHorizontalPadding
import cn.xybbz.ui.xy.XyItemOutSpacer
import cn.xybbz.ui.xy.XyItemTextCheckSelectHeightSmall
import cn.xybbz.ui.xy.XyItemTextLarge
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyLoadingItem
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyRowHeightSmall
import cn.xybbz.ui.xy.XySmallImage
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
    SELECT_ADDRESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    connectionUiType: String?,
    modifier: Modifier = Modifier,
    connectionViewModel: ConnectionViewModel = hiltViewModel<ConnectionViewModel>(),
) {

    val context = LocalContext.current
    val navHostController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    var ifSelectDataSource by remember {
        mutableStateOf(ScreenType.SELECT_DATA_SOURCE)
    }

    var isLoad by remember {
        mutableStateOf(false)
    }

    var showPassword by remember {
        mutableStateOf(false)
    }

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
            Text(text = stringResource(R.string.server_connection))
        }, navigationIcon = {
            if (!connectionUiType.isNullOrBlank() && connectionUiType == "0")
                IconButton(
                    onClick = {
                        navHostController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }

        }, actions = {
            IconButton(onClick = {
                navHostController.navigate(RouterConstants.Setting) {
                    popUpTo(RouterConstants.Home) {
                        saveState = true
                    }
                    restoreState = true
                }
            }) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.open_settings_page_button)
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
                        LazyColumnNotHorizontalComponent(
                            modifier = Modifier,
                            horizontalAlignment = Alignment.Start
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
                        item {
                            if (connectionViewModel.dataSourceType?.ifInputUrl == true)
                                OutlinedTextField(
                                    value = connectionViewModel.address,
                                    modifier = Modifier
                                        .background(Color.Transparent)
                                        .fillMaxWidth(),
                                    onValueChange = {
                                        connectionViewModel.setAddressData(it)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(5.dp),
                                    colors = TextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.LightGray,
                                        focusedIndicatorColor = Color.LightGray,
                                        focusedLabelColor = Color.LightGray,
                                        unfocusedLabelColor = Color.LightGray,
                                        cursorColor = Color.White,
                                        disabledContainerColor = Color.Transparent
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                    enabled = true,
                                    label = { Text(stringResource(R.string.server)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Http,
                                            contentDescription = stringResource(R.string.httpInput)
                                        )
                                    },
                                    placeholder = {
                                        Text(text = "http://")
                                    },
                                    isError = connectionViewModel.address.isBlank() && isLoad,
                                    supportingText = {
                                        if (connectionViewModel.address.isBlank() && isLoad)
                                            Text(text = stringResource(R.string.server_cannot_be_empty))
                                    },
                                )
                        }
                        item {
                            OutlinedTextField(
                                value = connectionViewModel.username,
                                modifier = Modifier
                                    .background(Color.Transparent)
                                    .fillMaxWidth(),
                                onValueChange = {
                                    connectionViewModel.setUserNameData(it)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(5.dp),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.LightGray,
                                    focusedIndicatorColor = Color.LightGray,
                                    focusedLabelColor = Color.LightGray,
                                    unfocusedLabelColor = Color.LightGray,
                                    cursorColor = Color.White,
                                    disabledContainerColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                enabled = true,
                                label = { Text(stringResource(R.string.username)) },
                                placeholder = {
                                    Text(text = stringResource(R.string.username))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = stringResource(R.string.username)
                                    )
                                },
                                isError = connectionViewModel.username.isBlank() && isLoad,
                                supportingText = {
                                    if (connectionViewModel.username.isBlank() && isLoad)
                                        Text(text = stringResource(R.string.username_cannot_be_empty))
                                },
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = connectionViewModel.password,
                                modifier = Modifier
                                    .background(Color.Transparent)
                                    .fillMaxWidth(),
                                onValueChange = {
                                    connectionViewModel.setPasswordData(it)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(5.dp),
                                trailingIcon = {

                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.clickable {
                                            showPassword = !showPassword
                                        }, tint = Color.White
                                    )
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.LightGray,
                                    focusedIndicatorColor = Color.LightGray,
                                    focusedLabelColor = Color.LightGray,
                                    unfocusedLabelColor = Color.LightGray,
                                    cursorColor = Color.White,
                                    disabledContainerColor = Color.Transparent
                                ),
                                enabled = true,
                                label = { Text(stringResource(R.string.password)) },
                                placeholder = {
                                    Text(text = stringResource(R.string.password))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = stringResource(R.string.password)
                                    )
                                }
                            )
                        }

                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    modifier = Modifier.width(width = 150.dp),
                                    onClick = {
                                        isLoad = true
                                        if (connectionViewModel.dataSourceType?.ifInputUrl == false) {
                                            coroutineScope.launch {
                                                ifSelectDataSource = ScreenType.SELECT_ADDRESS
                                                connectionViewModel.updateLoading(true)
                                                connectionViewModel.getResources()
                                            }.invokeOnCompletion {
                                                connectionViewModel.updateLoading(false)
                                            }
                                        } else if (!connectionViewModel.isInputError()) {
                                            if (!connectionViewModel.isHttpStartAndPortEnd()) {
                                                connectionViewModel.createTmpAddress()
                                                connectionViewModel.clearLoginStatus()
                                                ifSelectDataSource = ScreenType.SELECT_ADDRESS
                                            } else {
                                                ifSelectDataSource = ScreenType.SELECT_ADDRESS
                                                coroutineScope.launch {
                                                    connectionViewModel.setTmpAddressData(
                                                        connectionViewModel.address
                                                    )
                                                    connectionViewModel.inputAddress()
                                                }
                                            }
                                        } else {
                                            MessageUtils.sendPopTipError(context.getString(R.string.please_enter_required_content))
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(R.string.connect))
                                }
                                Button(
                                    modifier = Modifier.width(width = 150.dp),
                                    onClick = {
                                        isLoad = false
                                        ifSelectDataSource = ScreenType.SELECT_DATA_SOURCE
//                                        connectionViewModel.setDataSourceTypeData(null)
                                    }
                                ) {
                                    Text(text = stringResource(R.string.reselect))
                                }
                            }
                        }

                    }
                }

                ScreenType.SELECT_ADDRESS -> {

                    if (connectionViewModel.loading) {
                        XyLoadingItem(
                            modifier = Modifier.height(200.dp),
                            text = stringResource(R.string.logging_in)
                        )
                    } else {
                        LazyColumnComponent {
                            if (connectionViewModel.isLoginError) {
                                item {
                                    XyColumnNotHorizontalPadding(modifier = Modifier.height(200.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            XyItemTextLarge(text = stringResource(R.string.login_failed))
                                        }
                                        XyItemTextPadding(text = stringResource(connectionViewModel.errorHint))
                                        XyItemOutSpacer()
                                        if (connectionViewModel.errorMessage.isNotBlank())
                                            XyItemTextPadding(text = connectionViewModel.errorMessage)
                                    }
                                }
                                item {
                                    Button(
                                        modifier = Modifier.width(width = 150.dp),
                                        onClick = {
                                            isLoad = false
                                            connectionViewModel.clearLoginStatus()
                                            connectionViewModel.setSelectUrlIndexData(0)
                                            ifSelectDataSource =
                                                if (connectionViewModel.dataSourceType?.ifInputUrl == false) {
                                                    ScreenType.INPUT_DATA
                                                } else {
                                                    ScreenType.SELECT_ADDRESS
                                                }

                                        }) {
                                        Text(text = stringResource(R.string.reconnect))
                                    }
                                }
                            }

                            if (!connectionViewModel.isLoginError && !connectionViewModel.isLoginSuccess) {
                                item {
                                    XyColumnNotHorizontalPadding(backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                                        connectionViewModel.dataSourceType?.let {
                                            if (connectionViewModel.tmpAddressList.isNotEmpty())
                                                LazyColumnNotComponent(
                                                    modifier = Modifier.height(
                                                        200.dp
                                                    )
                                                ) {
                                                    itemsIndexed(connectionViewModel.tmpAddressList) { index, item ->
                                                        XyItemTextCheckSelectHeightSmall(
                                                            text = item,
                                                            selected = index == connectionViewModel.selectUrlIndex,
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
                                                        200.dp
                                                    )
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
                                item {
                                    Column {
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
                                                    connectionViewModel.inputAddress()
                                                }
                                            }) {
                                            Text(text = stringResource(R.string.connect))
                                        }
                                        Button(
                                            modifier = Modifier.width(width = 150.dp),
                                            onClick = {
                                                isLoad = false
                                                connectionViewModel.setSelectUrlIndexData(0)
                                                ifSelectDataSource = ScreenType.INPUT_DATA
                                            }) {
                                            Text(stringResource(R.string.back_to_input_credentials))
                                        }
                                    }

                                }
                            }


                            if (connectionViewModel.isLoginSuccess) {
                                item {

                                    XyColumn {
                                        XyItemTextLarge(text = stringResource(R.string.connection_successful))
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
                                            if (connectionViewModel.dataSourceManager.dataSourceType == null) {
                                                connectionViewModel.changeDataSource()
                                            } else {
                                                navHostController.popBackStack()
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
    XyRowHeightSmall(
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
            Text(
                text = text,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = serverName,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = address,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }
        if (select)
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.selected_item, text)
            )
    }
}