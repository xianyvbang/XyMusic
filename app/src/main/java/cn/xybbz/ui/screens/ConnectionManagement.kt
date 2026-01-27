package cn.xybbz.ui.screens


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.Connection
import cn.xybbz.router.ConnectionInfo
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ItemTrailingArrowRight
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.ConnectionManagementViewModel

/**
 * 连接设置列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionManagement(
    connectionManagementViewModel: ConnectionManagementViewModel = hiltViewModel<ConnectionManagementViewModel>()
) {
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()

    XyColumnScreen(
        modifier =
            Modifier.brashColor(
                topVerticalColor = connectionManagementViewModel.backgroundConfig.connectionManagerBrash[0],
                bottomVerticalColor = connectionManagementViewModel.backgroundConfig.connectionManagerBrash[1]
            )
    ) {

        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.connection_settings_list)
                )
            }, actions = {
                IconButton(onClick = {
                    navigator.navigate(Connection(connectionUiType = ConnectionUiType.ADD_CONNECTION))
                }) {
                    Icon(imageVector = Icons.Rounded.AddCard, contentDescription = "")
                }
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }
            })
        ScreenLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                connectionManagementViewModel.connectionList,
                key = { it.id }) { connectionConfig ->
                ItemTrailingArrowRight(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(XyTheme.dimens.corner)
                        ),
                    backgroundColor = Color.Transparent,
                    name = connectionConfig.type.title + "-" + connectionConfig.username,
                    subordination = connectionConfig.address,
                    img = connectionConfig.type.img.let { img -> painterResource(img) },
                    onClick = {
                        navigator.navigate(ConnectionInfo(connectionConfig.id))
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Switch(
                                checked = connectionManagementViewModel.dataSourceManager.getConnectionId() == connectionConfig.id,
                                onCheckedChange = {
                                    if (it)
                                        connectionManagementViewModel.changeDataSource(
                                            connectionConfig
                                        )
                                },
                                enabled = true,
                                colors = SwitchDefaults.colors(
                                    uncheckedBorderColor = Color.Transparent,
                                    checkedThumbColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            IconButton(
                                onClick = composeClick {
                                    navigator.navigate(
                                        ConnectionInfo(
                                            connectionConfig.id
                                        )
                                    )
                                }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription =
                                        stringResource(
                                            R.string.view_connection_info,
                                            connectionConfig.type.title + "-" + connectionConfig.username
                                        )
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

