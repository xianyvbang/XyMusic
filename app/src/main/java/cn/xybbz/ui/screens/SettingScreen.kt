package cn.xybbz.ui.screens


import android.content.ClipData
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.viewmodel.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

/**
 * 设置页面
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun SettingScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>()
) {

    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    val current = LocalContext.current

    val downloadPath by remember {
        mutableStateOf(current.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
    }
    LaunchedEffect(Unit) {
        Log.i("=====", "MusicSettingScreen: ")
    }

    val clipboardManager = LocalClipboard.current

    XyColumnScreen(
        modifier = Modifier.brashColor(topVerticalColor = Color(0xFF503803))
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "设置"
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回首页"
                    )
                }
            })

        LazyColumnNotComponent {
            item {
                XyItemTextPadding(
                    text = "播放",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x4D503803),
                            Color.Gray.copy(alpha = 0.1f)
                        ), tileMode = TileMode.Repeated
                    )
                ) {
                    SettingItemComponent(title = "缓存上限") {
                        navController.navigate(RouterConstants.CacheLimit)
                    }

                    MusicSettingSwitchItemComponent(
                        title = "专辑播放历史",
                        ifChecked = settingsViewModel.settingDataNow.ifEnableAlbumHistory
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsConfig.setIfEnableAlbumHistory(
                                bol
                            )
                        }
                    }

                    MusicSettingSwitchItemComponent(
                        title = "允许与其他应用同时播放",
                        ifChecked = settingsViewModel.settingDataNow.ifHandleAudioFocus
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsConfig.setIfHandleAudioFocus(
                                bol
                            )
                        }
                    }

                    SettingItemComponent(
                        title = "歌曲缓存位置",
                        info = downloadPath?.absolutePath ?: "",
                        maxLines = Int.MAX_VALUE
                    ) {
                        if (!downloadPath?.absolutePath.isNullOrBlank()) {
                            val clipData =
                                ClipData.newPlainText("label", downloadPath?.absolutePath ?: "")
                            coroutineScope.launch {
                                clipboardManager.setClipEntry(ClipEntry(clipData))
                            }.invokeOnCompletion {
                                MessageUtils.sendPopTip("复制成功")
                            }
                        }

                    }

                }
            }
            item {
                XyItemTextPadding(
                    text = "连接管理",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x4D503803),
                            Color.Gray.copy(alpha = 0.1f)
                        ), tileMode = TileMode.Repeated
                    )
                ) {
                    SettingItemComponent(title = "连接管理") {
                        navController.navigate(RouterConstants.ConnectionManagement)
                    }
                }
            }

            item {
                XyItemTextPadding(
                    text = "更多",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x4D503803),
                            Color.Gray.copy(alpha = 0.1f)
                        ), tileMode = TileMode.Repeated
                    )
                ) {

                    SettingItemComponent(title = "存储管理") {
                        navController.navigate(RouterConstants.MemoryManagement)
                    }

                    SettingItemComponent(title = "界面设置") {
//                        navController.navigate(RouterConstants.InterfaceSetting)
                        MessageUtils.sendPopTip("功能未实现")
                    }

                    SettingItemComponent(title = "语言") {
                        navController.navigate(RouterConstants.LanguageConfig)
                    }

                    SettingItemComponent(title = "关于") {
                        //版本信息,检查更新
                        navController.navigate(RouterConstants.About)
                    }

                }
            }
        }
    }

}