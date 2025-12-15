package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.viewmodel.ProxyConfigViewModel
import kotlinx.coroutines.launch

/**
 * 代理设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyConfigScreen(proxyConfigViewModel: ProxyConfigViewModel = hiltViewModel()) {


    val navHostController = LocalNavController.current
    val proxyConfig by proxyConfigViewModel.proxyConfig.collectAsState()
    val coroutineScope = rememberCoroutineScope()


    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = proxyConfigViewModel.backgroundConfig.memoryManagementBrash[0],
            bottomVerticalColor = proxyConfigViewModel.backgroundConfig.memoryManagementBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.poxy_config)
                )
            },
            navigationIcon = {
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
            }
        )
        LazyColumnNotComponent(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            contentPadding = PaddingValues()
        ) {
            item {
                SettingRoundedSurfaceColumn {
                    MusicSettingSwitchItemComponent(
                        title = "开启代理",
                        ifChecked = proxyConfig?.enabled == true
                    ) { bol ->
                        coroutineScope.launch {
                            proxyConfigViewModel.poxyConfigServer.updateEnabled(
                                bol
                            )
                        }
                    }

                    SettingParentItemComponent(title = "代理地址", trailingContent = {
                        Row(modifier = Modifier.width(300.dp)) {
                            XyEdit(
                                text = "127.0.0.1:8096",
                                onChange = {},
                                paddingValues = PaddingValues(),
                                backgroundColor = Color.Transparent
                            )
                        }

                    })
                }

            }
        }

    }
}