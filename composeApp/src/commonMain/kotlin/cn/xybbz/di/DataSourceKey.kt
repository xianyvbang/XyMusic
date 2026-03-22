package cn.xybbz.di

import cn.xybbz.localdata.enums.DataSourceType
import org.koin.core.annotation.Named

@Named
annotation class DataSourceKey(val value: DataSourceType)