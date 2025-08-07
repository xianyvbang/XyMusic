package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheLimitScreen(
    modifier: Modifier = Modifier,
    cacheLimitViewModel: CacheLimitViewModel = hiltViewModel<CacheLimitViewModel>()
) {

    val context = LocalContext.current
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cacheLimitViewModel.getAutomaticCacheSize(context)
    }

    XyColumnScreen(
        modifier = Modifier.brashColor(topVerticalColor = Color(0xFF503803))
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "音乐缓存上限"
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回设置"
                    )
                }
            })

        LazyColumnNotComponent(modifier = Modifier) {
            items(CacheUpperLimitEnum.entries) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .then(modifier)
                            .fillMaxWidth()
                            .debounceClickable {
                                coroutineScope.launch {
                                    cacheLimitViewModel.setCacheUpperLimitData(it)
                                }
                            }
                            .padding(
                                start = XyTheme.dimens.outerHorizontalPadding,
                                end = XyTheme.dimens.outerHorizontalPadding / 2
                            )
                    ) {
                        XyItemText(
                            text = it.message,
                            sub = if (it == CacheUpperLimitEnum.Auto)
                                "当前自动设置缓存上线为${cacheLimitViewModel.cacheSizeInfo}"
                            else null,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = cacheLimitViewModel.cacheUpperLimit == it,
                            onClick = {
                                coroutineScope.launch {
                                    cacheLimitViewModel.setCacheUpperLimitData(it)

                                }
                            },
                            modifier = Modifier
                                .semantics {
                                    contentDescription = it.message
                                }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                }
            }
            item {
                XyItemTextHorizontal(
                    """
                    自动射击缓存上限是根据手机可用内存,自动调节音乐缓存上限.
                    
                    缓存上限自动设置计算规则:
                    1.可用存储空间大于100G,音乐缓存上限为16G.
                    2.可用空间大于50G,小于等于100G,音乐缓存上限为8G.
                    3.可用空间大于10G,小于等于50G,音乐缓存上限为4G.
                    4.可用空间小于等于10G,音乐缓存上限为2G.
                """.trimIndent(),
                    modifier = Modifier.padding(top = XyTheme.dimens.outerVerticalPadding)
                )
            }
        }
    }
}