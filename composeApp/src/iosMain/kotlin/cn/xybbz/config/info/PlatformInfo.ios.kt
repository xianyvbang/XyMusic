package cn.xybbz.config.info

import cn.xybbz.common.constants.Constants
import cn.xybbz.di.ContextWrapper
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

actual fun getPlatformInfo(contextWrapper: ContextWrapper): PlatformInfo {
    val device = UIDevice.currentDevice
    val bundle = NSBundle.mainBundle

    val appName = bundle.objectForInfoDictionaryKey("CFBundleDisplayName") as? String
        ?: bundle.objectForInfoDictionaryKey("CFBundleName") as? String
        ?: Constants.APP_NAME

    val deviceName = device.name.ifBlank {
        device.model
    }

    val platformName = device.systemName()
    val platformVersion = (bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String)
        ?: (bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)
        ?: device.systemVersion

    return PlatformInfo(
        appName = appName,
        deviceName = deviceName,
        platformName = platformName,
        platformVersion = platformVersion
    )
}
