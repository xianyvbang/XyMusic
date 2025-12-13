package cn.xybbz.ui.screens

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.constants.UiConstants.MusicCardImageSize
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemBig
import cn.xybbz.viewmodel.SetBackgroundImageViewModel

/**
 * 设置背景图片界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBackgroundImageScreen(setBackgroundImageViewModel: SetBackgroundImageViewModel = hiltViewModel()) {

    val ifSelectImage by remember {
        derivedStateOf {
            setBackgroundImageViewModel.backgroundConfig.imageFilePath != null
        }
    }
    val navHostController = LocalNavController.current

    val context = LocalContext.current

    // 打开系统相册
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                setBackgroundImageViewModel.updateBackgroundImageUri(uri)
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    key(XyTheme.brash.backgroundImageUri) {
        XyColumnScreen(
            modifier = Modifier.brashColor(
                Color(0xFF610015),
                Color(0xFF04717D)
            )
        ) {

            TopAppBarComponent(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    TopAppBarTitle(
                        title = stringResource(R.string.background_image_setting)
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
                            contentDescription = stringResource(R.string.return_interface_settings)
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = composeClick() {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text(text = stringResource(R.string.select_image))
                        }

                        TextButton(onClick = composeClick() {
                            setBackgroundImageViewModel.updateBackgroundImageUri(null)
                        }, enabled = ifSelectImage) {
                            Text(text = stringResource(R.string.clear_image))
                        }
                    }
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!ifSelectImage)
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
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    }


}