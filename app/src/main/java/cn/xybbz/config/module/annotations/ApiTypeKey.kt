package cn.xybbz.config.module.annotations

import cn.xybbz.localdata.enums.DownloadTypes
import dagger.MapKey

@MapKey
annotation class ApiTypeKey(val value: DownloadTypes)