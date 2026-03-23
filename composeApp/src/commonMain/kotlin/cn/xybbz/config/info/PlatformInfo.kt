package cn.xybbz.config.info

import cn.xybbz.di.ContextWrapper

data class PlatformInfo(
    //应用名称
    val appName:String,
    //设备名称
    val deviceName: String,
    //平台类型名称
    val platformName: String,
    //平台版本号
    val platformVersion: String
)

//获得平台信息
expect fun getPlatformInfo(contextWrapper: ContextWrapper): PlatformInfo