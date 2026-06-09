package cn.xybbz.config.info

import android.os.Build
import cn.xybbz.platform.ContextWrapper
import kotlin.toString

actual fun getPlatformInfo(contextWrapper: ContextWrapper): PlatformInfo {

    val deviceName = "${Build.BRAND} ${Build.MODEL}"
    val packageManager = contextWrapper.context.packageManager
    val packageName = contextWrapper.context.packageName
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
    val versionName = packageInfo.versionName ?: ""
    val appName = packageManager.getApplicationLabel(applicationInfo).toString()
    return PlatformInfo(appName,deviceName, "Android", versionName)
}
