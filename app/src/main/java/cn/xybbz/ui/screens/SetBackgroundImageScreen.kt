package cn.xybbz.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xybbz.R
import cn.xybbz.common.constants.UiConstants.MusicCardImageSize
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemBig
import coil.compose.AsyncImage

/**
 * 设置背景图片界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBackgroundImageScreen(modifier: Modifier = Modifier) {

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val navHostController = LocalNavController.current


    // 打开系统相册
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null)
            imageUri = uri
    }

    XyColumnScreen(
        modifier = Modifier.brashColor(
//            topVerticalColor = interfaceSettingViewModel.backgroundConfig.homeBrash[0],
//            bottomVerticalColor = interfaceSettingViewModel.backgroundConfig.homeBrash[1]
        )
    ) {

        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "背景图片设置",
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
                        contentDescription = "返回界面设置"
                    )
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = composeClick() {
                        pickImageLauncher.launch("image/*")
                    }) {
                        Text(text = "选择图片")
                    }

                    TextButton(onClick = composeClick() {
                        imageUri = null
                    }, enabled = imageUri != null) {
                        Text(text = "清除图片")
                    }
                }
            }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (imageUri == null)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MusicCardImageSize + 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    XyItemBig(
                        text = stringResource(R.string.no_data),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {

                Card(
                    modifier = Modifier.size(
                        maxWidth - XyTheme.dimens.outerHorizontalPadding * 2,
                        maxHeight - XyTheme.dimens.outerVerticalPadding * 2 -
                                XyTheme.dimens.snackBarPlayerHeight - WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                        border = BorderStroke()
                    )
                ) {
                    AsyncImage(
                        model = imageUri,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "背景图片",
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(
                modifier = Modifier.height(
                    XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                )
            )
        }
    }
}