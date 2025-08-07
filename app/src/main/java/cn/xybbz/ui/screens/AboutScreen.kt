package cn.xybbz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.xybbz.R
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemBig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    XyColumnScreen(
        modifier = Modifier.brashColor()
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "设置"
                )
            })

        Spacer(modifier = Modifier.height(XyTheme.dimens.itemHeight))

        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.3f),
                    RoundedCornerShape(XyTheme.dimens.corner)
                )
                .padding(
                    horizontal = 40.dp,
                    vertical = 40.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.fish),
                contentDescription = "APP图标",
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
        XyItemBig(text = "咸鱼音乐")

        SettingItemComponent(title = "当前版本", info = "0.0.0.1") {

        }

        SettingItemComponent(title = "当前版本", info = "最新版本") {

        }
        SettingItemComponent(title = "问题反馈") {

        }
        SettingItemComponent(title = "官网") {

        }

    }
}