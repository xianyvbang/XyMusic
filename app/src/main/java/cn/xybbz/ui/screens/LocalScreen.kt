package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalScreen() {

    val navController = LocalNavController.current


    XyColumnScreen(
        modifier = Modifier
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "下载列表",
                    fontWeight = FontWeight.W900
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })
    }
}