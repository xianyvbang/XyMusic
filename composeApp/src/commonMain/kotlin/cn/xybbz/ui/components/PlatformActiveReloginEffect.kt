package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import cn.xybbz.api.client.DataSourceManager

/**
 * 平台应用重新活跃后的自动重登录监听。
 *
 * 平台层只上报活跃/失活，具体重登录门禁由 DataSourceManager 统一处理。
 */
@Composable
expect fun PlatformActiveReloginEffect(dataSourceManager: DataSourceManager)
