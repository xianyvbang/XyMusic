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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.ui.components.LazyColumnComponent
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.LazyColumnNotHorizontalComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ItemTrailingArrowRight
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnNotHorizontalPadding
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
    SELECT_DATA_SOURCE,
    INPUT_DATA,
    SELECT_ADDRESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    connectionUiType: String?,
    modifier: Modifier = Modifier,
    connectionViewModel: ConnectionViewModel = hiltViewModel<ConnectionViewModel>(),
) {

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
    val imageLoader = ImageLoader.Builder(LocalContext.current).components {
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
            Text(text = "服务器连接")
        }, navigationIcon = {
            if (!connectionUiType.isNullOrBlank() && connectionUiType == "0")
                IconButton(
                    onClick = {
                        navHostController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回设置页面"
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
                subordination = "连接到${connectionViewModel.dataSourceType?.describe}",
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
                                        text = "选择您要使用的协议连接到您的音乐库"
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
                                    subordination = "连接到${it.describe}",
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
                                            Text(text = "${stringResource(R.string.server)}不能为空")
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
                                label = { Text("用户名") },
                                placeholder = {
                                    Text(text = "用户名")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "用户名"
                                    )
                                },
                                isError = connectionViewModel.username.isBlank() && isLoad,
                                supportingText = {
                                    if (connectionViewModel.username.isBlank() && isLoad)
                                        Text(text = "用户名不能为空")
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
                                label = { Text("密码") },
                                placeholder = {
                                    Text(text = "密码")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "密码"
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
                                                connectionViewModel.clearError()
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
                                            //todo 这里需要增加提示
                                        }
                                    }
                                ) {
                                    Text(text = "连接")
                                }
                                Button(
                                    modifier = Modifier.width(width = 150.dp),
                                    onClick = {
                                        isLoad = false
                                        ifSelectDataSource = ScreenType.SELECT_DATA_SOURCE
//                                        connectionViewModel.setDataSourceTypeData(null)
                                    }
                                ) {
                                    Text(text = "重新选择")
                                }
                            }
                        }

                    }
                }

                ScreenType.SELECT_ADDRESS -> {

                    if (connectionViewModel.loading) {
                        XyLoadingItem(modifier = Modifier.height(200.dp), text = "登录中")
                    } else {
                        LazyColumnComponent {
                            if (connectionViewModel.errorMessage.isNotBlank()) {
                                item {
                                    XyColumnNotHorizontalPadding(modifier = Modifier.height(200.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            XyItemTextLarge(text = "登录失败")
                                        }
                                        XyItemTextPadding(text = connectionViewModel.errorMessage)
                                    }
                                }
                                item {
                                    Button(
                                        modifier = Modifier.width(width = 150.dp),
                                        onClick = {
                                            isLoad = false
                                            connectionViewModel.clearError()
                                            connectionViewModel.clearLoginStatus()
                                            connectionViewModel.setSelectUrlIndexData(0)
                                            if (connectionViewModel.dataSourceType?.ifInputUrl == true){
                                                ifSelectDataSource = ScreenType.INPUT_DATA
                                            }

                                        }) {
                                        Text(text = "重新连接")
                                    }
                                }
                            }

                            if (connectionViewModel.errorMessage.isBlank() && connectionViewModel.loginStatus == null) {
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
                                                            select = index == connectionViewModel.selectUrlIndex,
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
                                            Text(text = "连接")
                                        }
                                        Button(
                                            modifier = Modifier.width(width = 150.dp),
                                            onClick = {
                                                isLoad = false
                                                connectionViewModel.setSelectUrlIndexData(0)
                                                ifSelectDataSource = ScreenType.INPUT_DATA
                                            }) {
                                            Text("返回")
                                        }
                                    }

                                }
                            }


                            if (connectionViewModel.loginStatus is ClientLoginInfoState.UserLoginSuccess) {
                                item {

                                    XyColumn {
                                        XyItemTextLarge(text = "连接成功")
                                        XySmallImage(
                                            modifier = Modifier.size(150.dp),
                                            shape = RoundedCornerShape(XyTheme.dimens.corner),
                                            model = //淡出效果
                                                rememberAsyncImagePainter(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .setParameter(REPEAT_COUNT_KEY, 0)
                                                        .data(data = R.drawable.celebrate)
                                                        .apply(block = fun ImageRequest.Builder.() {
                                                            crossfade(true)//淡出效果
                                                        }).build(),
                                                    imageLoader = imageLoader,
                                                ), contentDescription = "连接成功图片"
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
                                        Text("进入页面")
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
fun SelectImage(modifier: Modifier = Modifier, onDataSource: () -> DataSourceType?) {

    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(if (onDataSource() != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
                .drawBehind {
                    // 绘制圆角矩形，可以满足圆角边框需求
                    drawRoundRect(
                        color = onSurface,
                        style = Stroke(
                            width = 1f,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(20f, 20f),
                                phase = 0f
                            )
                        )
                    )

                }
                .padding(20.dp)
        ) {
            if (onDataSource() != null)
                XySmallImage(
                    modifier = Modifier
                        .size(SelectImageSize),
                    model = onDataSource()?.img?.let {
                        painterResource(
                            it
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentDescription = onDataSource()?.title ?: "未选择音乐服务"
                )
            else {
                Spacer(modifier = Modifier.size(SelectImageSize))
            }
        }
        Text(text = onDataSource()?.title ?: "音乐服务")
    }
}

internal val SelectImageSize = 55.dp


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
            Icon(imageVector = Icons.Rounded.Check, contentDescription = "选中${text}")
    }
}