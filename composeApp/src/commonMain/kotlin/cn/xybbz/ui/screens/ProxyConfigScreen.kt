package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.viewmodel.ProxyConfigViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.open_proxy
import xymusic_kmp.composeapp.generated.resources.poxy_config
import xymusic_kmp.composeapp.generated.resources.proxy_address
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import xymusic_kmp.composeapp.generated.resources.save
import xymusic_kmp.composeapp.generated.resources.test_connection
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 代理设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyConfigScreen(proxyConfigViewModel: ProxyConfigViewModel = koinViewModel<ProxyConfigViewModel>()) {


    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.poxy_config)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_setting_screen)
                    )
                }
            },
            actions = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        proxyConfigViewModel.saveConfig()
                    }
                }) {
                    Text(stringResource(Res.string.save))
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
                        title = stringResource(Res.string.open_proxy),
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
                        title = stringResource(Res.string.test_connection),
                        info = proxyConfigViewModel.getConnectionAddress(),
                        painter = null,
                        onClick = {
                            proxyConfigViewModel.testProxyConfig()
                        }
                    ) {}
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
        SettingParentItemComponent(
            title = stringResource(Res.string.proxy_address),
            trailingContent = {
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

