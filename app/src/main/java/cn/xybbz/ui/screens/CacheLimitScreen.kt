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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheLimitScreen(
    cacheLimitViewModel: CacheLimitViewModel = hiltViewModel<CacheLimitViewModel>()
) {

    val context = LocalContext.current
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cacheLimitViewModel.getAutomaticCacheSize(context)
    }

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = cacheLimitViewModel.backgroundConfig.cacheLimitBrash[0],
            bottomVerticalColor = cacheLimitViewModel.backgroundConfig.cacheLimitBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.music_cache_limit_title)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
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
                                stringResource(R.string.current_auto_cache_limit,cacheLimitViewModel.cacheSizeInfo)
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
                    text = stringResource(R.string.auto_cache_limit_description).trimIndent(),
                    modifier = Modifier.padding(top = XyTheme.dimens.outerVerticalPadding)
                )
            }
        }
    }
}