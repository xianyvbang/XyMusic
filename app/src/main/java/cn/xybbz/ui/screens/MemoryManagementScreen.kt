package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyButtonNotPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemTextBig
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyItemTitleNotHorizontalPadding
import cn.xybbz.viewmodel.MemoryManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryManagementScreen(
    modifier: Modifier = Modifier,
    memoryManagementViewModel: MemoryManagementViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val navHostController = LocalNavController.current


    LaunchedEffect(Unit) {
        memoryManagementViewModel.logStorageInfo(context)
    }
    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = Color(0xFF055934)
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "存储管理",
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

        LazyColumnNotComponent {
            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.musicCacheSize,
                    onClick = { memoryManagementViewModel.clearMusicCache() },
                    text = "音频缓存",
                    describe = "缓存音频数据,清理后无法本地播放"
                )
            }
            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.cacheSize,
                    onClick = { memoryManagementViewModel.clearAllCache(context) },
                    text = "临时缓存",
                    describe = "临时缓存是使用过程中产生的临时缓存，清理临时缓存不会影响应用的使用"
                )
            }

            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.databaseSize,
                    onClick = {
                        AlertDialogObject(
                            title = {
                                XyItemBigTitle(text = "注意", color = Color.Red)
                            },
                            content = {
                                XyItemTextHorizontal(
                                    text = "确定要删除数据库数据吗?"
                                )
                            },
                            onConfirmation = {
                                memoryManagementViewModel.clearDatabaseData()
                            }
                        ).show()
                    },
                    text = "数据库数据",
                    describe = "存储音频、专辑、艺术家、歌单详细信息，清理后会重新从服务端进行获取"
                )
            }
            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.appDataSize,
                    text = "必要数据",
                    describe = "包含应用运行所需的必要文件，这部分数据不能清理",
                    ifShowButton = false
                )
            }
        }
    }
}

/**
 * 存储管理项
 * @param [modifier] 修饰语
 * @param [cacheSize] 缓存大小
 * @param [onClick] 点击时
 * @param [text] 文本
 * @param [describe] 描述
 */
@Composable
fun MemoryManagementItem(
    modifier: Modifier = Modifier,
    cacheSize: String,
    text: String,
    describe: String,
    ifShowButton: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    RoundedSurfaceColumnPadding(
        horizontalAlignment = Alignment.Start,
        color = Color.Black.copy(alpha = 0.3f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .then(modifier)
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.outerHorizontalPadding
                )
        ) {
            Column(modifier = Modifier.weight(1f)) {
                XyItemTitleNotHorizontalPadding(text = text)
                XyItemTextBig(text = cacheSize)
            }
            if (ifShowButton)
                XyButtonNotPadding(
                    modifier = Modifier,
                    enabled = cacheSize != "0B",
                    onClick = { onClick?.invoke() },
                    text = "清除"
                )
        }
        XyItemTextPadding(
            text = describe,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Visible
        )
    }
}