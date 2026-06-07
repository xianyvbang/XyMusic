package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.xybbz.common.constants.Constants
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ProxyConfigViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.open_proxy
import xymusic_kmp.composeapp.generated.resources.poxy_config
import xymusic_kmp.composeapp.generated.resources.proxy_address
import xymusic_kmp.composeapp.generated.resources.save
import xymusic_kmp.composeapp.generated.resources.test_connection

/**
 * 代理设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmProxyConfigScreen(proxyConfigViewModel: ProxyConfigViewModel = koinViewModel<ProxyConfigViewModel>()) {


    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.poxy_config)
                )
            },
            actions = {
                TextButton(
                    modifier = Modifier.jvmHoverDebounceClickable(),
                    onClick = {
                        coroutineScope.launch {
                            proxyConfigViewModel.saveConfig()
                        }
                    },
                ) {
                    Text(stringResource(Res.string.save))
                }
            }
        )
        JvmLazyListComponent(
            modifier = Modifier.fillMaxSize(),
            pagingItems = null,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            lazyColumnBottom = null
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(XyTheme.dimens.corner),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        JvmProxyConfigFormRow(label = stringResource(Res.string.open_proxy)) {
                            Switch(
                                checked = proxyConfigViewModel.enabled,
                                onCheckedChange = { bol ->
                                    coroutineScope.launch {
                                        proxyConfigViewModel.updateEnabled(bol)
                                    }
                                }
                            )
                        }

                        JvmProxyConfigComponent(
                            proxyConfigViewModel.addressValue,
                            updateAddress = { proxyConfigViewModel.updateAddress(it) }
                        )

                        JvmProxyConfigFormRow(label = stringResource(Res.string.test_connection)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val connectionAddress = proxyConfigViewModel.getConnectionAddress()
                                if (connectionAddress.isNotBlank()) {
                                    XyTextSub(
                                        modifier = Modifier.weight(1f),
                                        text = connectionAddress,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.End,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                TextButton(
                                    modifier = Modifier.jvmHoverDebounceClickable(),
                                    onClick = {
                                        proxyConfigViewModel.testProxyConfig()
                                    }
                                ) {
                                    Text(stringResource(Res.string.test_connection))
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
private fun JvmProxyConfigInput(
    text: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    actionContent: (@Composable () -> Unit)? = null
) {
    XyEdit(
        modifier = modifier,
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
fun JvmProxyConfigComponent(
    addressValue: TextFieldValue,
    updateAddress: (TextFieldValue) -> Unit,
) {
    JvmProxyConfigFormRow(label = stringResource(Res.string.proxy_address)) {
        JvmProxyConfigInput(
            modifier = Modifier.fillMaxWidth(),
            text = addressValue,
            onChange = { newValue ->
                updateAddress(newValue)
            },
            hint = Constants.DEFAULT_PROXY_ADDRESS
        )
    }
}

@Composable
private fun JvmProxyConfigFormRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyTextSub(
            modifier = Modifier.width(96.dp),
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            content()
        }
    }
}


