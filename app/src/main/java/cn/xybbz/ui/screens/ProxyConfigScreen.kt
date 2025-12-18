package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingItemComponent
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
            },
            actions = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        proxyConfigViewModel.saveConfig()
                    }
                }) {
                    Text(stringResource(R.string.save))
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
                        title = stringResource(R.string.open_proxy),
                        ifChecked = proxyConfigViewModel.enabled
                    ) { bol ->
                        coroutineScope.launch {
                            proxyConfigViewModel.updateEnabled(
                                bol
                            )
                        }
                    }
                }
            }
            item {
                ProxyConfigComponent(
                    proxyConfigViewModel.addressValue,
                    updateAddress = { proxyConfigViewModel.updateAddress(it) }
                )
            }

            item {
                SettingRoundedSurfaceColumn {
                    SettingItemComponent(
                        title = stringResource(R.string.test_connection),
                        info = proxyConfigViewModel.connectionConfigServer.getAddress(),
                        imageVector = null,
                        onClick = {
                            proxyConfigViewModel.testProxyConfig()
                        }
                    ){}
                }
            }
        }
    }
}

@Composable
private fun ProxyConfigInput(
    text: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null
) {
    XyEdit(
        text = text,
        onChange = onChange,
        hint = hint,
        paddingValues = PaddingValues(),
        textStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End // 右对齐
        ),
        textContentAlignment = Alignment.CenterEnd,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        actionContent = actionContent
    )
}

@Composable
fun ProxyConfigComponent(
    addressValue: TextFieldValue,
    updateAddress: (String) -> Unit,
) {

    SettingRoundedSurfaceColumn {
        SettingParentItemComponent(title = "代理地址", trailingContent = {
            Row(
                modifier = Modifier.width(200.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProxyConfigInput(
                    text = addressValue,
                    onChange = { newValue ->
                        val newText = newValue.text
                        updateAddress(newText)
                    },
                    hint = "127.0.0.1:8080"
                )
            }

        })
    }
}