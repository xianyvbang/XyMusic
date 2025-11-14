package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.R
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.OperationTipUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnBottomSheetComponent
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemBig
import cn.xybbz.ui.xy.XyItemTextLarge
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.AboutViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    aboutViewModel: AboutViewModel = hiltViewModel<AboutViewModel>()
) {
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val apkDownloadInfo by aboutViewModel.apkDownloadInfo.collectAsStateWithLifecycle()
    var enabledGetVersion by remember {
        mutableStateOf(true)
    }
    val primary = MaterialTheme.colorScheme.primary


    LaunchedEffect(apkDownloadInfo) {
        Log.i("=====", "数据变化 ${apkDownloadInfo}")
    }

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = aboutViewModel.backgroundConfig.aboutBrash[0],
            bottomVerticalColor = aboutViewModel.backgroundConfig.aboutBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.about)
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

        LazyColumnNotComponent(
            contentPadding = PaddingValues()
        ) {
            item {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.fish),
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(R.string.app_icon_info)
                    )
                }
            }
            item {
                XyRow(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    XyItemBig(text = stringResource(R.string.app_name))
                }

            }
            item {
                SettingItemComponent(
                    title = R.string.current_version,
                    info = "v${aboutViewModel.apkUpdateManager.currentVersion}"
                ) {

                }

            }
            item {
                SettingItemComponent(
                    title = R.string.check_updates,
                    enabled = enabledGetVersion,
                    info = if (aboutViewModel.apkUpdateManager.ifMaxVersion) stringResource(R.string.latest_version) else "${
                        stringResource(
                            R.string.new_version_detected
                        )
                    }:${aboutViewModel.apkUpdateManager.latestVersion}",
                    ifOpenBadge = !aboutViewModel.apkUpdateManager.ifMaxVersion
                ) {
                    coroutineScope.launch {
                        val initLatestVersion = OperationTipUtils.operationTipNotToBlock(
                            loadingMessage = R.string.get_latest_version,
                            successMessage = R.string.get_latest_version_success,
                            errorMessage = R.string.get_latest_version_fail
                        ) {
                            enabledGetVersion = false
                            val initLatestVersion =
                                aboutViewModel.apkUpdateManager.initLatestVersion(true)
                            enabledGetVersion = true
                            initLatestVersion
                        }
                        if (initLatestVersion) {
                            AlertDialogObject(
                                title = R.string.new_version_download,
                                content = {
                                    XyColumn(backgroundColor = Color.Transparent) {
                                        LazyColumnBottomSheetComponent(
                                            modifier = Modifier.height(
                                                300.dp
                                            )
                                        ) {
                                            item {
                                                XyItemTextLarge(text = "${aboutViewModel.apkUpdateManager.releasesInfo?.body}")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                                        XyItemTextLarge(text = "${stringResource(R.string.version_number)}: ${aboutViewModel.apkUpdateManager.latestVersion}")
                                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                                        LinearProgressIndicator(
                                            progress = {
                                                if ((apkDownloadInfo?.totalBytes
                                                        ?: 0) > 0
                                                ) ((apkDownloadInfo?.progress ?: 0f) / 100f) else 0f
                                            },
                                            drawStopIndicator = {
                                                drawStopIndicator(
                                                    drawScope = this,
                                                    stopSize = 0.dp,
                                                    color = primary,
                                                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                                )
                                            },
                                        )
                                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                                        BasicText(text = "${stringResource(R.string.current_download_progress)}: ${apkDownloadInfo?.progress ?: 0f}%")
                                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                                        if (apkDownloadInfo?.status == DownloadStatus.FAILED){
                                            XyItemTextLarge(
                                                text = stringResource(R.string.download_failed),
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                                        }

                                        Row {
                                            XyButton(
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    aboutViewModel.cancelDownload()
                                                },
                                                text = stringResource(R.string.cancel_download),
                                                enabled = apkDownloadInfo?.status == DownloadStatus.DOWNLOADING
                                            )
                                            Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
                                            Button(
                                                onClick = composeClick {
                                                    coroutineScope.launch {
                                                        aboutViewModel.apkUpdateManager.releasesInfo?.assets?.findLast {
                                                            it.name.contains(
                                                                "apk"
                                                            )
                                                        }?.let { asset ->
                                                            aboutViewModel.downloadAndInstall(
                                                                asset.url,
                                                                asset.name,
                                                                asset.size
                                                            )
                                                        }
                                                    }.invokeOnCompletion {

                                                    }
                                                },
                                                enabled = apkDownloadInfo?.status != DownloadStatus.DOWNLOADING,
                                                shape = RoundedCornerShape(XyTheme.dimens.corner),
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                if (apkDownloadInfo?.status == DownloadStatus.DOWNLOADING)
                                                    ContainedLoadingIndicator(
                                                        modifier = Modifier.size(
                                                            30.dp
                                                        )
                                                    )
//                                            CircularWavyProgressIndicator()
                                                Text(
                                                    text = stringResource(R.string.download_install),
                                                    modifier = Modifier
                                                        .fillMaxWidth(),
                                                    color = Color.White,
                                                    textAlign = TextAlign.Center,
                                                    overflow = TextOverflow.Ellipsis,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                },
                                dismissText = R.string.close,
                                onDismissRequest = {}).show()
                        }
                    }
                }
            }
            item {
                SettingItemComponent(title = R.string.problem_feedback) {
                    MessageUtils.sendPopTip(context.getString(R.string.function_not_implemented))
                }
            }

            item {
                SettingItemComponent(title = R.string.official_website) {
                    MessageUtils.sendPopTip(context.getString(R.string.no_official_website_yet))
                }
            }
        }
    }
}