package cn.xybbz.config.module.annotations

import cn.xybbz.localdata.enums.DataSourceType
import dagger.MapKey

@MapKey
annotation class DataSourceKey(val value: DataSourceType)