package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemSwitcherNotTextColor
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.InterfaceSettingViewModel
import kotlinx.coroutines.launch

/**
 * 界面设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceSettingScreen(
    interfaceSettingViewModel: InterfaceSettingViewModel = hiltViewModel()
) {

    val navHostController = LocalNavController.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen(modifier = Modifier.statusBarsPadding()) {
        TopAppBarComponent(
            title = {
                Text(
                    text = "界面设置",
                    fontWeight = FontWeight.W900
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
                        contentDescription = "返回设置"
                    )
                }
            }
        )

        LazyColumnNotComponent() {
            item {
                XyItemTextPadding(
                    text = "界面",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                RoundedSurfaceColumnPadding {
                    XyRow {
                        FilterChip(
                            onClick = {
                                coroutineScope.launch {
                                    interfaceSettingViewModel.updateTheme(ThemeTypeEnum.SYSTEM)
                                }
                            },
                            label = {
                                Text(text = "跟随系统")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Android,
                                    contentDescription = "跟随系统"
                                )
                            },
                            selected = interfaceSettingViewModel.settingsConfig.themeType == ThemeTypeEnum.SYSTEM
                        )

                        FilterChip(
                            onClick = {
                                coroutineScope.launch {
                                    interfaceSettingViewModel.updateTheme(ThemeTypeEnum.LIGHT)
                                }
                            },
                            label = {
                                Text(text = "浅色")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.WbSunny,
                                    contentDescription = "浅色"
                                )
                            },
                            selected = interfaceSettingViewModel.settingsConfig.themeType == ThemeTypeEnum.LIGHT
                        )

                        FilterChip(
                            onClick = {
                                coroutineScope.launch {
                                    interfaceSettingViewModel.updateTheme(ThemeTypeEnum.DARK)
                                }
                            },
                            label = {
                                Text(text = "深色")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Bedtime,
                                    contentDescription = "深色"
                                )
                            },
                            selected = interfaceSettingViewModel.settingsConfig.themeType == ThemeTypeEnum.DARK
                        )
                    }

                    XyItemSwitcherNotTextColor(
                        state = interfaceSettingViewModel.settingsConfig.isDynamic,
                        onChange = {
                            coroutineScope.launch {
                                interfaceSettingViewModel.updateDynamicColor(it)
                            }
                        },
                        text = "动态颜色主题"
                    )
                }
            }
            /*item {
                XyItemTextPadding(
                    text = "背景图片选择",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }*/
        }
    }
}