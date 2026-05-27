package cn.xybbz.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.app_name
import xymusic_kmp.composeapp.generated.resources.loading
import xymusic_kmp.composeapp.generated.resources.logo_new

@Composable
fun StartupScreen(
    modifier: Modifier = Modifier,
) {
    // 启动页只使用主题、资源和窗口标题栏，不依赖 MainViewModel 或 loadingObjectList。
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StartupScreenTitleBar()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(XyTheme.dimens.outerHorizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            // 保持首屏内容轻量稳定，避免加载文案或图片尺寸变化导致启动阶段布局跳动。
            Column(
                modifier = Modifier.widthIn(max = 360.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_new),
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier.size(84.dp),
                    contentScale = ContentScale.Fit
                )
                XyText(
                    text = stringResource(Res.string.app_name),
                    color = MaterialTheme.colorScheme.onSurface
                )
                CircularProgressIndicator()
                XyTextSub(
                    text = stringResource(Res.string.loading),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(1.dp))
            }
        }
    }
}
