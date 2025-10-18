package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyButtonNotPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextLarge
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyItemTitlePadding
import cn.xybbz.viewmodel.MemoryManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryManagementScreen(
    memoryManagementViewModel: MemoryManagementViewModel = hiltViewModel<MemoryManagementViewModel>()
) {

    val context = LocalContext.current
    val navHostController = LocalNavController.current


    LaunchedEffect(Unit) {
        memoryManagementViewModel.logStorageInfo(context)
    }
    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = memoryManagementViewModel.backgroundConfig.memoryManagementBrash[0],
            bottomVerticalColor = memoryManagementViewModel.backgroundConfig.memoryManagementBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.storage_management),
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
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }
            }
        )

        LazyColumnNotComponent {
            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.musicCacheSize,
                    onClick = { memoryManagementViewModel.clearMusicCache() },
                    text = stringResource(R.string.audio_cache),
                    describe = stringResource(R.string.audio_cache_description)
                )
            }
            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.cacheSize,
                    onClick = { memoryManagementViewModel.clearAllCache(context) },
                    text = stringResource(R.string.temporary_cache),
                    describe = stringResource(R.string.temporary_cache_description)
                )
            }

            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.databaseSize,
                    onClick = {
                        AlertDialogObject(
                            title = R.string.warning,
                            content = {
                                XyItemTextHorizontal(
                                    text = stringResource(R.string.confirm_delete_database)
                                )
                            },
                            ifWarning = true,
                            onConfirmation = {
                                memoryManagementViewModel.clearDatabaseData()
                            }
                        ).show()
                    },
                    text = stringResource(R.string.database_data),
                    describe = stringResource(R.string.database_data_description)
                )
            }
            item {
                MemoryManagementItem(
                    cacheSize = memoryManagementViewModel.appDataSize,
                    text = stringResource(R.string.essential_data),
                    describe = stringResource(R.string.essential_data_description),
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
                XyItemTitlePadding(
                    modifier = modifier, text = text, paddingValues = PaddingValues(
                        vertical = XyTheme.dimens.innerVerticalPadding
                    )
                )

                XyItemTextLarge(
                    modifier = Modifier,
                    text = cacheSize,
                    fontWeight = FontWeight.Bold
                )
            }
            if (ifShowButton)
                XyButtonNotPadding(
                    modifier = Modifier,
                    enabled = cacheSize != "0B",
                    onClick = { onClick?.invoke() },
                    text = stringResource(R.string.clear)
                )
        }
        XyItemTextPadding(
            text = describe,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Visible
        )
    }
}